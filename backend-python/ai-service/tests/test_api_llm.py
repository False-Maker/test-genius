"""
LLM API路由测试
"""
import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, Mock

from app.services.llm_service import LLMService


class TestLLMRouter:
    """LLM路由测试类"""
    
    def test_call_llm_success(self, client: TestClient):
        """测试调用LLM API成功"""
        mock_result = {
            "content": "测试响应内容",
            "model_code": "TEST_MODEL",
            "tokens_used": 100,
            "response_time": 500
        }
        
        with patch('app.api.llm_router.LLMService') as mock_service_class:
            mock_service = Mock()
            mock_service.call_model.return_value = mock_result
            mock_service_class.return_value = mock_service
            
            response = client.post(
                "/api/v1/llm/call",
                json={
                    "model_code": "TEST_MODEL",
                    "prompt": "测试提示词"
                }
            )
            
            assert response.status_code == 200
            data = response.json()
            assert data["content"] == "测试响应内容"
            assert data["model_code"] == "TEST_MODEL"
            assert data["tokens_used"] == 100
    
    def test_call_llm_missing_model_code(self, client: TestClient):
        """测试调用LLM API - 缺少模型代码"""
        response = client.post(
            "/api/v1/llm/call",
            json={
                "prompt": "测试提示词"
            }
        )
        
        assert response.status_code == 422  # 验证错误
    
    def test_call_llm_empty_prompt(self, client: TestClient):
        """测试调用LLM API - 提示词为空"""
        with patch('app.api.llm_router.LLMService') as mock_service_class:
            response = client.post(
                "/api/v1/llm/call",
                json={
                    "model_code": "TEST_MODEL",
                    "prompt": ""
                }
            )
            
            assert response.status_code == 400
            assert "提示词不能为空" in response.json()["detail"]
    
    def test_call_llm_service_error(self, client: TestClient):
        """测试调用LLM API - 服务错误"""
        with patch('app.api.llm_router.LLMService') as mock_service_class:
            mock_service = Mock()
            mock_service.call_model.side_effect = ValueError("模型配置不存在")
            mock_service_class.return_value = mock_service
            
            response = client.post(
                "/api/v1/llm/call",
                json={
                    "model_code": "NOT_EXIST",
                    "prompt": "测试提示词"
                }
            )
            
            assert response.status_code == 400
    
    def test_batch_call_llm_success(self, client: TestClient):
        """测试批量调用LLM API成功"""
        mock_results = [
            {
                "content": "响应1",
                "model_code": "TEST_MODEL",
                "tokens_used": 50,
                "response_time": 300
            },
            {
                "content": "响应2",
                "model_code": "TEST_MODEL",
                "tokens_used": 60,
                "response_time": 350
            }
        ]
        
        with patch('app.api.llm_router.LLMService') as mock_service_class:
            mock_service = Mock()
            mock_service.batch_call.return_value = mock_results
            mock_service_class.return_value = mock_service
            
            response = client.post(
                "/api/v1/llm/batch-call",
                json={
                    "requests": [
                        {"model_code": "TEST_MODEL", "prompt": "提示词1"},
                        {"model_code": "TEST_MODEL", "prompt": "提示词2"}
                    ]
                }
            )
            
            assert response.status_code == 200
            data = response.json()
            assert len(data["results"]) == 2
    
    def test_batch_call_llm_empty_list(self, client: TestClient):
        """测试批量调用LLM API - 空列表"""
        response = client.post(
            "/api/v1/llm/batch-call",
            json={
                "requests": []
            }
        )
        
        assert response.status_code == 400
        assert "批量请求列表不能为空" in response.json()["detail"]
    
    def test_batch_call_llm_too_many(self, client: TestClient):
        """测试批量调用LLM API - 请求数量过多"""
        requests = [{"model_code": "TEST_MODEL", "prompt": f"提示词{i}"} for i in range(11)]
        
        response = client.post(
            "/api/v1/llm/batch-call",
            json={"requests": requests}
        )
        
        assert response.status_code == 400
        assert "批量请求数量不能超过10个" in response.json()["detail"]

