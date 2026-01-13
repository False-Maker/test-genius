"""
LLM服务单元测试
"""
import pytest
from unittest.mock import Mock, patch, MagicMock
from sqlalchemy.orm import Session

from app.services.llm_service import LLMService
from app.services.model_config_service import ModelConfigService


class TestLLMService:
    """LLM服务测试类"""
    
    def test_init(self, test_db: Session):
        """测试初始化"""
        service = LLMService(test_db)
        assert service.db == test_db
        assert service.model_config_service is not None
        assert isinstance(service.model_config_service, ModelConfigService)
        assert service._llm_cache == {}
    
    @patch('app.services.llm_service.ModelAdapterFactory.create_llm')
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_get_llm_instance_success(
        self, 
        mock_get_by_code, 
        mock_create_llm,
        test_db: Session,
        mock_model_config,
        mock_llm_instance
    ):
        """测试获取LLM实例成功"""
        # 设置mock
        mock_get_by_code.return_value = mock_model_config
        mock_create_llm.return_value = mock_llm_instance
        
        service = LLMService(test_db)
        llm = service._get_llm_instance("TEST_MODEL")
        
        assert llm == mock_llm_instance
        mock_get_by_code.assert_called_once_with("TEST_MODEL")
        mock_create_llm.assert_called_once()
        assert "TEST_MODEL_2000_0.7" in service._llm_cache
    
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_get_llm_instance_not_found(self, mock_get_by_code, test_db: Session):
        """测试获取LLM实例 - 模型配置不存在"""
        mock_get_by_code.return_value = None
        
        service = LLMService(test_db)
        with pytest.raises(ValueError, match="模型配置不存在"):
            service._get_llm_instance("NOT_EXIST")
    
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_get_llm_instance_inactive(self, mock_get_by_code, test_db: Session, mock_model_config):
        """测试获取LLM实例 - 模型配置未启用"""
        mock_model_config.is_active = "0"
        mock_get_by_code.return_value = mock_model_config
        
        service = LLMService(test_db)
        with pytest.raises(ValueError, match="模型配置未启用"):
            service._get_llm_instance("TEST_MODEL")
    
    @patch('app.services.llm_service.ModelAdapterFactory.create_llm')
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_call_with_retry_success(
        self,
        mock_get_by_code,
        mock_create_llm,
        test_db: Session,
        mock_model_config,
        mock_llm_response,
        mock_llm_instance
    ):
        """测试带重试机制的模型调用成功"""
        mock_get_by_code.return_value = mock_model_config
        mock_create_llm.return_value = mock_llm_instance
        
        service = LLMService(test_db)
        result = service._call_with_retry(mock_llm_instance, "测试提示词")
        
        assert result == "测试响应内容"
        mock_llm_instance.invoke.assert_called_once()
    
    @patch('app.services.llm_service.ModelAdapterFactory.create_llm')
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_call_model_success(
        self,
        mock_get_by_code,
        mock_create_llm,
        test_db: Session,
        mock_model_config,
        mock_llm_instance
    ):
        """测试调用模型成功"""
        mock_get_by_code.return_value = mock_model_config
        mock_create_llm.return_value = mock_llm_instance
        
        service = LLMService(test_db)
        result = service.call_model(
            model_code="TEST_MODEL",
            prompt="测试提示词"
        )
        
        assert result["content"] == "测试响应内容"
        assert result["model_code"] == "TEST_MODEL"
        assert result["response_time"] is not None
        assert result["tokens_used"] is not None
    
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_call_model_not_found(self, mock_get_by_code, test_db: Session):
        """测试调用模型 - 模型配置不存在"""
        mock_get_by_code.return_value = None
        
        service = LLMService(test_db)
        with pytest.raises(ValueError):
            service.call_model(
                model_code="NOT_EXIST",
                prompt="测试提示词"
            )
    
    @patch('app.services.llm_service.ModelAdapterFactory.create_llm')
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_batch_call_success(
        self,
        mock_get_by_code,
        mock_create_llm,
        test_db: Session,
        mock_model_config,
        mock_llm_instance
    ):
        """测试批量调用成功"""
        mock_get_by_code.return_value = mock_model_config
        mock_create_llm.return_value = mock_llm_instance
        
        service = LLMService(test_db)
        requests = [
            {
                "model_code": "TEST_MODEL",
                "prompt": "测试提示词1"
            },
            {
                "model_code": "TEST_MODEL",
                "prompt": "测试提示词2"
            }
        ]
        
        results = service.batch_call(requests)
        
        assert len(results) == 2
        assert all("content" in r for r in results)
        assert all("model_code" in r for r in results)
    
    @patch('app.services.llm_service.ModelConfigService.get_by_code')
    def test_batch_call_partial_failure(
        self,
        mock_get_by_code,
        test_db: Session,
        mock_model_config
    ):
        """测试批量调用部分失败"""
        # 第一个请求成功，第二个请求失败
        mock_get_by_code.side_effect = [mock_model_config, None]
        
        service = LLMService(test_db)
        requests = [
            {
                "model_code": "TEST_MODEL",
                "prompt": "测试提示词1"
            },
            {
                "model_code": "NOT_EXIST",
                "prompt": "测试提示词2"
            }
        ]
        
        with patch.object(service, 'call_model') as mock_call:
            mock_call.side_effect = [
                {"content": "成功", "model_code": "TEST_MODEL"},
                ValueError("模型配置不存在")
            ]
            results = service.batch_call(requests)
        
        assert len(results) == 2
        assert "error" in results[1]

