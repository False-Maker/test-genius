"""
输入节点实现
"""
from typing import Dict, Any
from .base_node import BaseNode


class RequirementInputNode(BaseNode):
    """需求文本输入节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行需求输入节点
        
        Args:
            input_data: 输入数据（包含requirement_text）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            需求文本
        """
        if isinstance(input_data, dict):
            requirement_text = input_data.get("requirement_text") or input_data.get("requirementText")
            if requirement_text:
                return {
                    "requirement_text": requirement_text,
                    "requirement_id": input_data.get("requirement_id") or input_data.get("requirementId")
                }
        
        # 如果input_data是字符串，直接返回
        if isinstance(input_data, str):
            return {"requirement_text": input_data}
        
        return input_data


class TestCaseInputNode(BaseNode):
    """测试用例输入节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行测试用例输入节点
        
        Args:
            input_data: 输入数据（包含test_case或testCase）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            测试用例数据
        """
        if isinstance(input_data, dict):
            test_case = input_data.get("test_case") or input_data.get("testCase")
            if test_case:
                return {"test_case": test_case}
        
        return input_data


class FileUploadNode(BaseNode):
    """文件上传节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行文件上传节点
        
        Args:
            input_data: 输入数据（包含file_path或filePath）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            文件内容或路径
        """
        if isinstance(input_data, dict):
            file_path = input_data.get("file_path") or input_data.get("filePath")
            if file_path:
                # 这里可以读取文件内容
                # 暂时返回文件路径
                return {"file_path": file_path}
        
        return input_data
