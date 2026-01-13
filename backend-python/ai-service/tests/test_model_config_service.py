"""
模型配置服务单元测试
"""
import pytest
from sqlalchemy.orm import Session
from unittest.mock import Mock, patch

from app.services.model_config_service import ModelConfigService
from app.models.model_config import ModelConfig


class TestModelConfigService:
    """模型配置服务测试类"""
    
    def test_init(self, test_db: Session):
        """测试初始化"""
        service = ModelConfigService(test_db)
        assert service.db == test_db
    
    def test_get_by_code(self, test_db: Session, mock_model_config):
        """测试根据代码查询模型配置"""
        # 注意：由于使用内存数据库，需要实际创建数据或mock
        service = ModelConfigService(test_db)
        
        # 使用mock进行测试
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.first.return_value = mock_model_config
            
            result = service.get_by_code("TEST_MODEL")
            
            assert result == mock_model_config
            mock_query.assert_called_once_with(ModelConfig)
    
    def test_get_by_code_not_found(self, test_db: Session):
        """测试根据代码查询模型配置 - 不存在"""
        service = ModelConfigService(test_db)
        
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.first.return_value = None
            
            result = service.get_by_code("NOT_EXIST")
            
            assert result is None
    
    def test_get_by_id(self, test_db: Session, mock_model_config):
        """测试根据ID查询模型配置"""
        service = ModelConfigService(test_db)
        
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.first.return_value = mock_model_config
            
            result = service.get_by_id(1)
            
            assert result == mock_model_config
    
    def test_get_active_configs(self, test_db: Session, mock_model_config):
        """测试获取所有启用的模型配置"""
        service = ModelConfigService(test_db)
        
        config_list = [mock_model_config]
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.order_by.return_value.all.return_value = config_list
            
            results = service.get_active_configs()
            
            assert len(results) == 1
            assert results[0] == mock_model_config

