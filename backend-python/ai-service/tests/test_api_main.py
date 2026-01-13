"""
主应用API测试
"""
import pytest
from fastapi.testclient import TestClient


class TestMainApp:
    """主应用测试类"""
    
    def test_root(self, client: TestClient):
        """测试根路径"""
        response = client.get("/")
        
        assert response.status_code == 200
        data = response.json()
        assert data["message"] == "测试设计助手系统 - AI服务"
        assert data["version"] == "1.0.0"
    
    def test_health(self, client: TestClient):
        """测试健康检查"""
        response = client.get("/health")
        
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "healthy"

