"""
自然语言理解服务
解析自然语言描述，提取操作类型和目标元素信息
"""
import re
import logging
from typing import Dict, List, Optional, Any
from app.services.llm_service import LLMService
from sqlalchemy.orm import Session

logger = logging.getLogger(__name__)


class NLPService:
    """自然语言理解服务"""
    
    # 操作类型关键词映射
    ACTION_KEYWORDS = {
        'click': ['点击', '单击', '双击', '选择', '打开', '进入', '跳转', 'click', 'select', 'open'],
        'input': ['输入', '填写', '填入', '输入内容', 'input', 'enter', 'fill'],
        'select': ['选择', '下拉', '下拉选择', 'select', 'choose', 'pick'],
        'hover': ['悬停', '鼠标悬停', 'hover', 'mouseover'],
        'scroll': ['滚动', '滑动', 'scroll', 'swipe'],
        'wait': ['等待', '等待加载', 'wait', 'sleep'],
        'verify': ['验证', '检查', '确认', 'verify', 'check', 'assert'],
        'upload': ['上传', 'upload'],
        'download': ['下载', 'download']
    }
    
    # 元素类型关键词映射
    ELEMENT_KEYWORDS = {
        'button': ['按钮', 'button', 'btn'],
        'input': ['输入框', '文本框', 'input', 'textbox', 'field'],
        'link': ['链接', '超链接', 'link', 'a标签'],
        'select': ['下拉框', '选择框', 'select', 'dropdown'],
        'checkbox': ['复选框', 'checkbox'],
        'radio': ['单选框', 'radio'],
        'image': ['图片', '图像', 'image', 'img'],
        'table': ['表格', 'table'],
        'form': ['表单', 'form']
    }
    
    def __init__(self, db: Session):
        """
        初始化自然语言理解服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.llm_service = LLMService(db)
    
    def parse_natural_language(self, description: str, use_llm: bool = True) -> Dict[str, Any]:
        """
        解析自然语言描述
        
        Args:
            description: 自然语言描述
            use_llm: 是否使用大语言模型（默认True）
            
        Returns:
            解析结果：
            - actions: 操作列表
            - elements: 目标元素信息
            - steps: 操作步骤
        """
        try:
            if use_llm:
                # 使用大语言模型解析
                return self._parse_with_llm(description)
            else:
                # 使用规则解析
                return self._parse_with_rules(description)
                
        except Exception as e:
            logger.error(f"自然语言解析失败: {str(e)}")
            # 降级到规则解析
            return self._parse_with_rules(description)
    
    def _parse_with_llm(self, description: str) -> Dict[str, Any]:
        """使用大语言模型解析自然语言"""
        try:
            prompt = f"""请解析以下测试操作的自然语言描述，提取操作步骤、操作类型和目标元素信息。

自然语言描述：
{description}

请以JSON格式返回解析结果，格式如下：
{{
    "steps": [
        {{
            "step_number": 1,
            "action_type": "click|input|select|hover|scroll|wait|verify|upload|download",
            "element_description": "目标元素的描述",
            "element_type": "button|input|link|select|checkbox|radio|image|table|form",
            "element_text": "元素文本（如果有）",
            "element_identifier": "元素标识（ID、名称等）",
            "action_value": "操作值（如输入的内容、选择的值等）",
            "description": "操作描述"
        }}
    ]
}}

只返回JSON，不要其他内容。"""
            
            # 调用大语言模型
            response = self.llm_service.call_model(
                model_code=None,  # 使用默认模型
                prompt=prompt,
                temperature=0.3  # 降低温度，提高准确性
            )
            
            content = response.get("content", "")
            
            # 解析JSON响应
            import json
            # 尝试提取JSON部分
            json_match = re.search(r'\{.*\}', content, re.DOTALL)
            if json_match:
                result = json.loads(json_match.group())
            else:
                # 如果提取失败，尝试直接解析
                result = json.loads(content)
            
            # 转换为标准格式
            return self._convert_llm_result(result)
            
        except Exception as e:
            logger.warning(f"LLM解析失败，降级到规则解析: {str(e)}")
            return self._parse_with_rules(description)
    
    def _parse_with_rules(self, description: str) -> Dict[str, Any]:
        """使用规则解析自然语言"""
        steps = []
        
        # 按句号、分号、换行符分割描述
        sentences = re.split(r'[。；\n]', description)
        
        step_number = 1
        for sentence in sentences:
            sentence = sentence.strip()
            if not sentence:
                continue
            
            # 提取操作类型
            action_type = self._extract_action_type(sentence)
            
            # 提取元素信息
            element_info = self._extract_element_info(sentence)
            
            # 提取操作值
            action_value = self._extract_action_value(sentence, action_type)
            
            step = {
                "step_number": step_number,
                "action_type": action_type,
                "element_description": sentence,
                "element_type": element_info.get("type", ""),
                "element_text": element_info.get("text", ""),
                "element_identifier": element_info.get("identifier", ""),
                "action_value": action_value,
                "description": sentence
            }
            
            steps.append(step)
            step_number += 1
        
        return {
            "steps": steps,
            "actions": [step["action_type"] for step in steps],
            "elements": [step["element_description"] for step in steps]
        }
    
    def _extract_action_type(self, sentence: str) -> str:
        """提取操作类型"""
        sentence_lower = sentence.lower()
        
        for action_type, keywords in self.ACTION_KEYWORDS.items():
            for keyword in keywords:
                if keyword.lower() in sentence_lower:
                    return action_type
        
        # 默认返回click
        return 'click'
    
    def _extract_element_info(self, sentence: str) -> Dict[str, str]:
        """提取元素信息"""
        element_info = {
            "type": "",
            "text": "",
            "identifier": ""
        }
        
        sentence_lower = sentence.lower()
        
        # 提取元素类型
        for element_type, keywords in self.ELEMENT_KEYWORDS.items():
            for keyword in keywords:
                if keyword.lower() in sentence_lower:
                    element_info["type"] = element_type
                    break
            if element_info["type"]:
                break
        
        # 提取元素标识（ID、名称等）
        # 匹配"ID为xxx"、"名称为xxx"等
        id_match = re.search(r'[iI][dD][为是：:]\s*([a-zA-Z0-9_-]+)', sentence)
        if id_match:
            element_info["identifier"] = id_match.group(1)
        
        name_match = re.search(r'名称[为是：:]\s*([^\s，,。]+)', sentence)
        if name_match:
            element_info["identifier"] = name_match.group(1)
        
        # 提取元素文本（引号内的内容）
        text_match = re.search(r'["""]([^"""]+)["""]', sentence)
        if text_match:
            element_info["text"] = text_match.group(1)
        
        return element_info
    
    def _extract_action_value(self, sentence: str, action_type: str) -> Optional[str]:
        """提取操作值"""
        if action_type == 'input':
            # 提取输入内容（引号内的内容）
            value_match = re.search(r'输入[为是：:]\s*["""]([^"""]+)["""]', sentence)
            if value_match:
                return value_match.group(1)
            
            # 提取"输入xxx"
            value_match = re.search(r'输入\s*([^\s，,。]+)', sentence)
            if value_match:
                return value_match.group(1)
        
        elif action_type == 'select':
            # 提取选择的值
            value_match = re.search(r'选择[为是：:]\s*["""]([^"""]+)["""]', sentence)
            if value_match:
                return value_match.group(1)
            
            value_match = re.search(r'选择\s*([^\s，,。]+)', sentence)
            if value_match:
                return value_match.group(1)
        
        return None
    
    def _convert_llm_result(self, llm_result: Dict[str, Any]) -> Dict[str, Any]:
        """转换LLM结果为标准格式"""
        steps = llm_result.get("steps", [])
        
        actions = []
        elements = []
        
        for step in steps:
            actions.append(step.get("action_type", ""))
            elements.append(step.get("element_description", ""))
        
        return {
            "steps": steps,
            "actions": actions,
            "elements": elements
        }

