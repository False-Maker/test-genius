"""
提示词服务单元测试
"""
import pytest
from unittest.mock import Mock, patch
from sqlalchemy.orm import Session

from app.services.prompt_service import PromptService, VARIABLE_PATTERN
from app.models.prompt_template import PromptTemplate


class TestPromptService:
    """提示词服务测试类"""
    
    def test_init(self, test_db: Session):
        """测试初始化"""
        service = PromptService(test_db)
        assert service.db == test_db
    
    def test_replace_variables_simple(self, test_db: Session):
        """测试简单变量替换"""
        service = PromptService(test_db)
        template = "这是{name}的测试"
        variables = {"name": "测试用户"}
        
        result = service.replace_variables(template, variables)
        
        assert result == "这是测试用户的测试"
    
    def test_replace_variables_multiple(self, test_db: Session):
        """测试多个变量替换"""
        service = PromptService(test_db)
        template = "{layer}层的{method}方法测试"
        variables = {"layer": "单元", "method": "等价类"}
        
        result = service.replace_variables(template, variables)
        
        assert result == "单元层的等价类方法测试"
    
    def test_replace_variables_missing(self, test_db: Session):
        """测试缺失变量的替换（应使用空字符串）"""
        service = PromptService(test_db)
        template = "测试{missing}变量"
        variables = {}
        
        result = service.replace_variables(template, variables)
        
        assert result == "测试变量"
    
    def test_replace_variables_none_value(self, test_db: Session):
        """测试None值的替换"""
        service = PromptService(test_db)
        template = "测试{value}值"
        variables = {"value": None}
        
        result = service.replace_variables(template, variables)
        
        assert result == "测试值"
    
    def test_load_template_not_found(self, test_db: Session):
        """测试加载模板 - 不存在"""
        service = PromptService(test_db)
        
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.first.return_value = None
            
            result = service.load_template(999)
            
            assert result is None
    
    def test_load_template_inactive(self, test_db: Session):
        """测试加载模板 - 未启用"""
        mock_template = Mock()
        mock_template.is_active = "0"
        mock_template.to_dict.return_value = {"id": 1}
        
        service = PromptService(test_db)
        
        with patch.object(test_db, 'query') as mock_query:
            mock_query.return_value.filter.return_value.first.return_value = mock_template
            
            result = service.load_template(1)
            
            assert result is None
    
    def test_generate_prompt_success(self, test_db: Session):
        """测试生成提示词成功"""
        mock_template_dict = {
            "id": 1,
            "template_content": "生成{layer}层用例，使用{method}方法",
            "is_active": "1"
        }
        
        service = PromptService(test_db)
        
        with patch.object(service, 'load_template') as mock_load:
            mock_load.return_value = mock_template_dict
            
            result = service.generate_prompt(
                template_id=1,
                variables={"layer": "单元", "method": "等价类"}
            )
            
            assert result == "生成单元层用例，使用等价类方法"
    
    def test_generate_prompt_template_not_found(self, test_db: Session):
        """测试生成提示词 - 模板不存在"""
        service = PromptService(test_db)
        
        with patch.object(service, 'load_template') as mock_load:
            mock_load.return_value = None
            
            with pytest.raises(ValueError, match="模板不存在或未启用"):
                service.generate_prompt(template_id=999, variables={})
    
    def test_get_template_variables(self, test_db: Session):
        """测试获取模板变量定义"""
        import json
        variables_def = {"layer": "测试分层", "method": "测试方法"}
        
        mock_template_dict = {
            "id": 1,
            "template_variables": json.dumps(variables_def),
            "is_active": "1"
        }
        
        service = PromptService(test_db)
        
        with patch.object(service, 'load_template') as mock_load:
            mock_load.return_value = mock_template_dict
            
            result = service.get_template_variables(1)
            
            assert result == variables_def
    
    def test_get_template_variables_invalid_json(self, test_db: Session):
        """测试获取模板变量定义 - JSON格式错误"""
        mock_template_dict = {
            "id": 1,
            "template_variables": "invalid json{",
            "is_active": "1"
        }
        
        service = PromptService(test_db)
        
        with patch.object(service, 'load_template') as mock_load:
            mock_load.return_value = mock_template_dict
            
            result = service.get_template_variables(1)
            
            assert result == {}

