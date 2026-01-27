"""
工作流节点基类
"""
from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
from sqlalchemy.orm import Session


class BaseNode(ABC):
    """工作流节点基类"""
    
    def __init__(self, db: Optional[Session] = None):
        """
        初始化节点
        
        Args:
            db: 数据库会话（可选）
        """
        self.db = db
    
    @abstractmethod
    def execute(
        self,
        input_data: Any,
        config: Dict[str, Any],
        context: Dict[str, Any]
    ) -> Any:
        """
        执行节点
        
        Args:
            input_data: 输入数据
            config: 节点配置
            context: 执行上下文
            
        Returns:
            节点输出数据
        """
        pass
    
    def validate_config(self, config: Dict[str, Any]) -> bool:
        """
        验证节点配置
        
        Args:
            config: 节点配置
            
        Returns:
            配置是否有效
        """
        return True
