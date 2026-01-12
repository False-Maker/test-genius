"""
数据库配置和会话管理
使用SQLAlchemy 2.0
"""
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from pydantic_settings import BaseSettings
import os

# 数据库配置
class DatabaseSettings(BaseSettings):
    """数据库配置"""
    database_url: str = os.getenv(
        "DATABASE_URL",
        "postgresql://postgres:postgres@localhost:5432/test_design_assistant"
    )
    
    class Config:
        env_file = ".env"
        case_sensitive = False

# 创建数据库引擎
db_settings = DatabaseSettings()
engine = create_engine(
    db_settings.database_url,
    pool_pre_ping=True,  # 连接前检查连接是否有效
    pool_size=10,  # 连接池大小
    max_overflow=20,  # 连接池溢出大小
    echo=False  # 是否打印SQL语句
)

# 创建会话工厂
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 创建基础模型类
Base = declarative_base()


def get_db():
    """
    获取数据库会话
    用于依赖注入
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

