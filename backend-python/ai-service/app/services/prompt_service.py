"""
提示词服务
处理提示词模板和变量替换
"""
import re
import json
import logging
from typing import Dict, Optional, Any
from sqlalchemy.orm import Session
from app.models.prompt_template import PromptTemplate

logger = logging.getLogger(__name__)

# 变量占位符正则表达式：匹配 {变量名} 格式
VARIABLE_PATTERN = re.compile(r'\{([^}]+)\}')


class PromptService:
    """提示词服务"""
    
    def __init__(self, db: Session):
        """
        初始化提示词服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
    
    def load_template(self, template_id: int) -> Optional[Dict]:
        """
        加载提示词模板
        
        Args:
            template_id: 模板ID
            
        Returns:
            模板信息字典，如果不存在返回None
        """
        template = self.db.query(PromptTemplate).filter(
            PromptTemplate.id == template_id
        ).first()
        
        if not template:
            logger.warning(f"模板不存在: {template_id}")
            return None
        
        if template.is_active != "1":
            logger.warning(f"模板未启用: {template_id}")
            return None
        
        return template.to_dict()
    
    def generate_prompt(
        self,
        template_id: int,
        variables: Dict[str, Any]
    ) -> str:
        """
        根据模板和变量生成提示词
        
        Args:
            template_id: 模板ID
            variables: 变量字典
            
        Returns:
            生成的提示词
            
        Raises:
            ValueError: 模板不存在或未启用
        """
        # 加载模板
        template = self.load_template(template_id)
        if not template:
            raise ValueError(f"模板不存在或未启用: {template_id}")
        
        # 获取模板内容
        template_content = template.get("template_content", "")
        if not template_content:
            raise ValueError(f"模板内容为空: {template_id}")
        
        # 生成提示词（变量替换）
        return self.replace_variables(template_content, variables)
    
    def replace_variables(
        self,
        template_content: str,
        variables: Dict[str, Any]
    ) -> str:
        """
        替换模板中的变量占位符
        
        Args:
            template_content: 模板内容
            variables: 变量字典
            
        Returns:
            替换后的提示词
        """
        if not variables:
            variables = {}
        
        # 查找所有变量占位符
        required_variables = set()
        for match in VARIABLE_PATTERN.finditer(template_content):
            variable_name = match.group(1).strip()
            required_variables.add(variable_name)
        
        # 定义输出格式变量（这些变量是生成用例后才有的，不应该在生成提示词时提供）
        # 这些变量在模板中作为输出格式的占位符，应该提供说明性文本
        output_format_variables = {
            "caseName": "[请生成用例名称]",
            "preCondition": "[请生成前置条件]",
            "testStep": "[请生成测试步骤]",
            "expectedResult": "[请生成预期结果]"
        }
        
        # 检查必需变量是否都提供了值（仅警告，不阻止）
        for var_name in required_variables:
            if var_name not in variables or variables[var_name] is None:
                # 如果是输出格式变量，提供说明性文本，不警告
                if var_name in output_format_variables:
                    logger.debug(f"模板变量 {var_name} 是输出格式变量，将使用说明性文本")
                else:
                    logger.warning(f"模板变量 {var_name} 未提供值，将使用空字符串替换")
        
        # 替换所有变量
        def replace_func(match):
            variable_name = match.group(1).strip()
            value = variables.get(variable_name)
            
            # 如果变量未提供值
            if value is None:
                # 如果是输出格式变量，提供说明性文本
                if variable_name in output_format_variables:
                    replacement = output_format_variables[variable_name]
                else:
                    # 其他变量使用空字符串
                    replacement = ""
            else:
                replacement = str(value)
            
            return replacement
        
        result = VARIABLE_PATTERN.sub(replace_func, template_content)
        
        return result
    
    def get_template_variables(self, template_id: int) -> Optional[Dict]:
        """
        获取模板变量定义
        
        Args:
            template_id: 模板ID
            
        Returns:
            变量定义字典（从JSON解析），如果不存在返回None
        """
        template = self.load_template(template_id)
        if not template:
            return None
        
        template_variables = template.get("template_variables")
        if not template_variables:
            return {}
        
        try:
            # 解析JSON格式的变量定义
            return json.loads(template_variables)
        except json.JSONDecodeError as e:
            logger.error(f"解析模板变量定义失败: {template_id}, 错误: {str(e)}")
            return {}
    
    def find_applicable_templates(
        self,
        layer_code: Optional[str] = None,
        method_code: Optional[str] = None,
        module_code: Optional[str] = None
    ) -> list[Dict]:
        """
        查找适用的模板
        
        Args:
            layer_code: 测试分层代码
            method_code: 测试方法代码
            module_code: 业务模块代码
            
        Returns:
            适用的模板列表
        """
        query = self.db.query(PromptTemplate).filter(
            PromptTemplate.is_active == "1"
        )
        
        # 如果提供了分层代码，筛选适用的模板
        if layer_code:
            query = query.filter(
                (PromptTemplate.applicable_layers.is_(None)) |
                (PromptTemplate.applicable_layers == "") |
                (PromptTemplate.applicable_layers.contains(layer_code))
            )
        
        # 如果提供了方法代码，筛选适用的模板
        if method_code:
            query = query.filter(
                (PromptTemplate.applicable_methods.is_(None)) |
                (PromptTemplate.applicable_methods == "") |
                (PromptTemplate.applicable_methods.contains(method_code))
            )
        
        # 如果提供了模块代码，筛选适用的模板
        if module_code:
            query = query.filter(
                (PromptTemplate.applicable_modules.is_(None)) |
                (PromptTemplate.applicable_modules == "") |
                (PromptTemplate.applicable_modules.contains(module_code))
            )
        
        templates = query.all()
        return [t.to_dict() for t in templates]
