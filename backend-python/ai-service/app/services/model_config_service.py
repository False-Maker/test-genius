"""
模型配置服务
从数据库查询模型配置
"""
from typing import Optional, List
from sqlalchemy.orm import Session
from sqlalchemy import and_
from app.models.model_config import ModelConfig


class ModelConfigService:
    """模型配置服务"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def get_by_code(self, model_code: str) -> Optional[ModelConfig]:
        """
        根据模型编码查询模型配置
        
        Args:
            model_code: 模型编码
            
        Returns:
            模型配置对象，如果不存在返回None
        """
        return self.db.query(ModelConfig).filter(
            ModelConfig.model_code == model_code
        ).first()
    
    def get_by_id(self, model_id: int) -> Optional[ModelConfig]:
        """
        根据ID查询模型配置
        
        Args:
            model_id: 模型配置ID
            
        Returns:
            模型配置对象，如果不存在返回None
        """
        return self.db.query(ModelConfig).filter(
            ModelConfig.id == model_id
        ).first()
    
    def get_active_configs(self) -> List[ModelConfig]:
        """
        获取所有启用的模型配置，按优先级排序
        
        Returns:
            启用的模型配置列表
        """
        return self.db.query(ModelConfig).filter(
            ModelConfig.is_active == "1"
        ).order_by(ModelConfig.priority.asc()).all()
    
    def get_active_configs_by_type(self, model_type: str) -> List[ModelConfig]:
        """
        根据模型类型获取启用的模型配置
        
        Args:
            model_type: 模型类型
            
        Returns:
            启用的模型配置列表
        """
        return self.db.query(ModelConfig).filter(
            and_(
                ModelConfig.model_type == model_type,
                ModelConfig.is_active == "1"
            )
        ).order_by(ModelConfig.priority.asc()).all()
    
    def get_default_config(self) -> Optional[ModelConfig]:
        """
        获取默认模型配置（优先级最高的启用配置）
        
        Returns:
            默认模型配置，如果不存在返回None
        """
        return self.db.query(ModelConfig).filter(
            ModelConfig.is_active == "1"
        ).order_by(ModelConfig.priority.asc()).first()

