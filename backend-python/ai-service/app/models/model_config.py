"""
模型配置数据库模型
"""
from sqlalchemy import Column, BigInteger, String, Integer, Numeric, DateTime
from sqlalchemy.sql import func
from app.database import Base


class ModelConfig(Base):
    """模型配置表"""
    __tablename__ = "model_config"
    
    id = Column(BigInteger, primary_key=True, index=True)
    model_code = Column(String(100), unique=True, nullable=False, index=True)
    model_name = Column(String(200), nullable=False)
    model_type = Column(String(50))
    api_endpoint = Column(String(500))
    api_key = Column(String(500))
    model_version = Column(String(50))
    max_tokens = Column(Integer)
    temperature = Column(Numeric(3, 2))
    is_active = Column(String(1), default="1")
    priority = Column(Integer)
    daily_limit = Column(Integer)
    create_time = Column(DateTime(timezone=True), server_default=func.now())
    update_time = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    def to_dict(self):
        """转换为字典"""
        return {
            "id": self.id,
            "model_code": self.model_code,
            "model_name": self.model_name,
            "model_type": self.model_type,
            "api_endpoint": self.api_endpoint,
            "api_key": self.api_key,  # 注意：实际使用时可能需要脱敏
            "model_version": self.model_version,
            "max_tokens": self.max_tokens,
            "temperature": float(self.temperature) if self.temperature else None,
            "is_active": self.is_active,
            "priority": self.priority,
            "daily_limit": self.daily_limit,
            "create_time": self.create_time.isoformat() if self.create_time else None,
            "update_time": self.update_time.isoformat() if self.update_time else None,
        }

