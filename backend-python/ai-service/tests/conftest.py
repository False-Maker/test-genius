"""
pytest配置文件和fixtures
"""
import pytest
from unittest.mock import Mock, MagicMock
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import StaticPool
from fastapi.testclient import TestClient
from typing import Generator

from app.database import Base, get_db
from app.main import app


# 创建内存数据库用于测试
TEST_DATABASE_URL = "sqlite:///:memory:"
test_engine = create_engine(
    TEST_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


@pytest.fixture(scope="function")
def test_db() -> Generator[Session, None, None]:
    """
    创建测试数据库会话
    """
    # 创建表
    Base.metadata.create_all(bind=test_engine)
    
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()
        # 清理表
        Base.metadata.drop_all(bind=test_engine)


@pytest.fixture
def client(test_db: Session) -> TestClient:
    """
    创建测试客户端
    """
    def override_get_db():
        try:
            yield test_db
        finally:
            pass
    
    app.dependency_overrides[get_db] = override_get_db
    test_client = TestClient(app)
    
    yield test_client
    
    app.dependency_overrides.clear()


@pytest.fixture
def mock_model_config():
    """
    模拟模型配置对象
    """
    config = Mock()
    config.id = 1
    config.model_code = "TEST_MODEL"
    config.model_name = "测试模型"
    config.model_type = "DEEPSEEK"
    config.api_endpoint = "https://api.deepseek.com"
    config.api_key = "test-api-key"
    config.model_version = "v1"
    config.max_tokens = 2000
    config.temperature = 0.7
    config.is_active = "1"
    config.priority = 1
    config.daily_limit = 1000
    return config


@pytest.fixture
def mock_llm_response():
    """
    模拟LLM响应对象
    """
    response = Mock()
    response.content = "测试响应内容"
    response.text = "测试响应内容"
    return response


@pytest.fixture
def mock_llm_instance(mock_llm_response):
    """
    模拟LLM实例
    """
    llm = Mock()
    llm.invoke = Mock(return_value=mock_llm_response)
    llm.get_num_tokens = Mock(return_value=100)
    return llm

