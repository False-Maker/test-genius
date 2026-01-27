"""
转换节点实现
"""
from typing import Dict, Any, List
from .base_node import BaseNode


class FormatTransformNode(BaseNode):
    """格式转换节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行格式转换节点
        
        Args:
            input_data: 输入数据
            config: 节点配置（包含from_format、to_format）
            context: 执行上下文
            
        Returns:
            转换后的数据
        """
        from_format = config.get("from_format", "json")
        to_format = config.get("to_format", "json")
        
        # 这里可以实现各种格式转换逻辑
        # 暂时直接返回原数据
        return input_data


class DataCleanNode(BaseNode):
    """数据清洗节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行数据清洗节点
        
        Args:
            input_data: 输入数据
            config: 节点配置（包含清洗规则）
            context: 执行上下文
            
        Returns:
            清洗后的数据
        """
        # 这里可以实现数据清洗逻辑
        # 暂时直接返回原数据
        return input_data


class DataMergeNode(BaseNode):
    """数据合并节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行数据合并节点
        
        Args:
            input_data: 输入数据（可能是列表，包含多个上游节点的输出）
            config: 节点配置
            context: 执行上下文
            
        Returns:
            合并后的数据
        """
        # 如果input_data是列表，合并所有数据
        if isinstance(input_data, list):
            merged = []
            for item in input_data:
                if isinstance(item, dict):
                    merged.append(item)
                elif isinstance(item, list):
                    merged.extend(item)
            return merged
        
        return input_data
