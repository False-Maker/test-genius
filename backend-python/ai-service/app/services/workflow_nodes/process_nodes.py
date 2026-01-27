"""
处理节点实现
"""
from typing import Dict, Any, Optional
from sqlalchemy.orm import Session
from .base_node import BaseNode
from app.services.prompt_service import PromptService
from app.services.llm_service import LLMService
from app.services.case_generation_service import CaseGenerationService


class RequirementAnalysisNode(BaseNode):
    """需求分析节点"""
    
    def __init__(self, db: Optional[Session] = None):
        super().__init__(db)
        if db:
            self.llm_service = LLMService(db)
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行需求分析节点
        
        Args:
            input_data: 输入数据（包含requirement_text）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            需求分析结果
        """
        requirement_text = None
        if isinstance(input_data, dict):
            requirement_text = input_data.get("requirement_text")
        elif isinstance(input_data, str):
            requirement_text = input_data
        
        if not requirement_text:
            raise ValueError("缺少需求文本")
        
        # 使用LLM进行需求分析
        # 这里简化实现，实际应该调用专门的需求分析服务
        analysis_prompt = f"请分析以下需求，提取关键信息：\n\n{requirement_text}"
        
        if self.db and hasattr(self, 'llm_service'):
            model_code = config.get("model_code", None)
            response = self.llm_service.call_model(
                model_code=model_code,
                prompt=analysis_prompt
            )
            return {
                "requirement_text": requirement_text,
                "analysis_result": response.get("content", "")
            }
        
        # 如果没有LLM服务，返回简单分析
        return {
            "requirement_text": requirement_text,
            "analysis_result": "需求分析完成"
        }


class TemplateSelectNode(BaseNode):
    """模板选择节点"""
    
    def __init__(self, db: Optional[Session] = None):
        super().__init__(db)
        if db:
            self.prompt_service = PromptService(db)
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行模板选择节点
        
        Args:
            input_data: 输入数据（包含layer_code、method_code等）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            选中的模板信息
        """
        if not isinstance(input_data, dict):
            raise ValueError("输入数据格式错误")
        
        layer_code = input_data.get("layer_code") or config.get("layer_code")
        method_code = input_data.get("method_code") or config.get("method_code")
        
        if not layer_code or not method_code:
            raise ValueError("缺少layer_code或method_code")
        
        if self.db and hasattr(self, 'prompt_service'):
            # 选择模板
            template = self.prompt_service.select_template(layer_code, method_code)
            if template:
                return {
                    **input_data,
                    "template_id": template.get("id"),
                    "template": template
                }
        
        # 如果没有找到模板，返回原数据
        return input_data


class PromptGenerateNode(BaseNode):
    """提示词生成节点"""
    
    def __init__(self, db: Optional[Session] = None):
        super().__init__(db)
        if db:
            self.prompt_service = PromptService(db)
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行提示词生成节点
        
        Args:
            input_data: 输入数据（包含template_id、variables等）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            生成的提示词
        """
        if not isinstance(input_data, dict):
            raise ValueError("输入数据格式错误")
        
        template_id = input_data.get("template_id") or config.get("template_id")
        if not template_id:
            raise ValueError("缺少template_id")
        
        # 构建变量
        variables = input_data.get("variables", {})
        if not variables:
            # 从input_data中提取变量
            variables = {
                "requirement_text": input_data.get("requirement_text"),
                "layer_code": input_data.get("layer_code"),
                "method_code": input_data.get("method_code")
            }
        
        if self.db and hasattr(self, 'prompt_service'):
            prompt = self.prompt_service.generate_prompt(
                template_id=template_id,
                variables=variables
            )
            return {
                **input_data,
                "prompt": prompt
            }
        
        return input_data


class LLMCallNode(BaseNode):
    """模型调用节点"""
    
    def __init__(self, db: Optional[Session] = None):
        super().__init__(db)
        if db:
            self.llm_service = LLMService(db)
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行模型调用节点
        
        Args:
            input_data: 输入数据（包含prompt）
            config: 节点配置（包含model_code等）
            context: 执行上下文
            
        Returns:
            模型响应结果
        """
        if not isinstance(input_data, dict):
            raise ValueError("输入数据格式错误")
        
        prompt = input_data.get("prompt")
        if not prompt:
            raise ValueError("缺少prompt")
        
        model_code = config.get("model_code") or input_data.get("model_code")
        if not model_code:
            raise ValueError("缺少model_code")
        
        if self.db and hasattr(self, 'llm_service'):
            response = self.llm_service.call_model(
                model_code=model_code,
                prompt=prompt,
                max_tokens=config.get("max_tokens"),
                temperature=config.get("temperature")
            )
            return {
                **input_data,
                "model_response": response,
                "content": response.get("content", "")
            }
        
        raise ValueError("LLM服务不可用")


class ResultParseNode(BaseNode):
    """结果解析节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行结果解析节点
        
        Args:
            input_data: 输入数据（包含model_response或content）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            解析后的结构化数据
        """
        if not isinstance(input_data, dict):
            raise ValueError("输入数据格式错误")
        
        content = input_data.get("content") or input_data.get("model_response", {}).get("content")
        if not content:
            raise ValueError("缺少content")
        
        # 这里应该调用专门的解析服务
        # 暂时简化处理
        parse_type = config.get("parse_type", "case")
        
        if parse_type == "case":
            # 解析用例（调用CaseGenerationService的解析方法）
            # 这里简化实现
            return {
                **input_data,
                "parsed_data": [{"case_name": "解析的用例", "content": content}]
            }
        
        return {
            **input_data,
            "parsed_data": content
        }
