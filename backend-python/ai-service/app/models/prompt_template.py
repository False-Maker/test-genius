"""
提示词模板数据库模型
"""
from sqlalchemy import Column, BigInteger, String, Integer, Text, DateTime
from sqlalchemy.sql import func
from app.database import Base


class PromptTemplate(Base):
    """提示词模板表"""
    __tablename__ = "prompt_template"
    
    id = Column(BigInteger, primary_key=True, index=True)
    template_code = Column(String(100), unique=True, nullable=False, index=True)
    template_name = Column(String(500), nullable=False)
    template_category = Column(String(100))
    template_type = Column(String(50))
    template_content = Column(Text, nullable=False)
    template_variables = Column(Text)
    applicable_layers = Column(String(500))
    applicable_methods = Column(String(500))
    applicable_modules = Column(String(500))
    template_description = Column(Text)
    version = Column(Integer, default=1)
    is_active = Column(String(1), default="1")
    creator_id = Column(BigInteger)
    create_time = Column(DateTime(timezone=True), server_default=func.now())
    update_time = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    def to_dict(self):
        """转换为字典"""
        return {
            "id": self.id,
            "template_code": self.template_code,
            "template_name": self.template_name,
            "template_category": self.template_category,
            "template_type": self.template_type,
            "template_content": self.template_content,
            "template_variables": self.template_variables,
            "applicable_layers": self.applicable_layers,
            "applicable_methods": self.applicable_methods,
            "applicable_modules": self.applicable_modules,
            "template_description": self.template_description,
            "version": self.version,
            "is_active": self.is_active,
            "creator_id": self.creator_id,
            "create_time": self.create_time.isoformat() if self.create_time else None,
            "update_time": self.update_time.isoformat() if self.update_time else None,
        }

