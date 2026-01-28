"""
测试相关工具
"""
import logging
import requests
from typing import Dict, Any, List
from app.services.agent_engine import BaseTool

logger = logging.getLogger(__name__)


class SearchTestCasesTool(BaseTool):
    """搜索历史测试用例工具"""
    
    def __init__(self, java_api_base_url: str = "http://localhost:8080"):
        """
        初始化工具
        
        Args:
            java_api_base_url: Java后端API基础URL
        """
        schema = {
            "name": "search_test_cases",
            "description": "搜索历史测试用例，可以根据用例名称、需求ID、状态等条件搜索",
            "parameters": {
                "type": "object",
                "properties": {
                    "case_name": {
                        "type": "string",
                        "description": "用例名称（支持模糊搜索）"
                    },
                    "requirement_id": {
                        "type": "integer",
                        "description": "需求ID"
                    },
                    "case_status": {
                        "type": "string",
                        "description": "用例状态（DRAFT, PENDING_REVIEW, REVIEWED, DEPRECATED）"
                    },
                    "limit": {
                        "type": "integer",
                        "description": "返回结果数量限制（默认10）",
                        "default": 10
                    }
                }
            }
        }
        super().__init__("search_test_cases", "搜索历史测试用例", schema)
        self.java_api_base_url = java_api_base_url
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行搜索
        
        Args:
            arguments: 搜索参数
            context: 执行上下文
            
        Returns:
            搜索结果
        """
        try:
            case_name = arguments.get("case_name")
            requirement_id = arguments.get("requirement_id")
            case_status = arguments.get("case_status")
            limit = arguments.get("limit", 10)
            
            # 调用Java后端API
            url = f"{self.java_api_base_url}/api/v1/test-cases"
            params = {
                "page": 1,
                "size": limit
            }
            if case_name:
                params["caseName"] = case_name
            if requirement_id:
                params["requirementId"] = requirement_id
            if case_status:
                params["caseStatus"] = case_status
            
            response = requests.get(url, params=params, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            if data.get("code") == 200:
                cases = data.get("data", {}).get("content", [])
                return {
                    "success": True,
                    "count": len(cases),
                    "cases": cases[:limit]
                }
            else:
                return {
                    "success": False,
                    "error": data.get("message", "搜索失败")
                }
        except Exception as e:
            logger.error(f"搜索测试用例失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }


class GetRequirementDetailsTool(BaseTool):
    """获取需求详情工具"""
    
    def __init__(self, java_api_base_url: str = "http://localhost:8080"):
        schema = {
            "name": "get_requirement_details",
            "description": "根据需求ID获取需求的详细信息，包括需求描述、状态、关联的测试用例等",
            "parameters": {
                "type": "object",
                "properties": {
                    "requirement_id": {
                        "type": "integer",
                        "description": "需求ID"
                    },
                    "requirement_code": {
                        "type": "string",
                        "description": "需求编码"
                    }
                },
                "required": ["requirement_id"]
            }
        }
        super().__init__("get_requirement_details", "获取需求详情", schema)
        self.java_api_base_url = java_api_base_url
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        try:
            requirement_id = arguments.get("requirement_id")
            requirement_code = arguments.get("requirement_code")
            
            if requirement_id:
                url = f"{self.java_api_base_url}/api/v1/requirements/{requirement_id}"
            elif requirement_code:
                url = f"{self.java_api_base_url}/api/v1/requirements/code/{requirement_code}"
            else:
                return {
                    "success": False,
                    "error": "必须提供requirement_id或requirement_code"
                }
            
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            if data.get("code") == 200:
                requirement = data.get("data", {})
                return {
                    "success": True,
                    "requirement": requirement
                }
            else:
                return {
                    "success": False,
                    "error": data.get("message", "获取需求详情失败")
                }
        except Exception as e:
            logger.error(f"获取需求详情失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }


class ValidateTestCaseTool(BaseTool):
    """验证测试用例质量工具"""
    
    def __init__(self, java_api_base_url: str = "http://localhost:8080"):
        schema = {
            "name": "validate_test_case",
            "description": "验证测试用例的质量，检查用例是否完整、是否符合规范等",
            "parameters": {
                "type": "object",
                "properties": {
                    "case_id": {
                        "type": "integer",
                        "description": "用例ID"
                    },
                    "case_code": {
                        "type": "string",
                        "description": "用例编码"
                    },
                    "case_data": {
                        "type": "object",
                        "description": "用例数据（如果提供，则直接验证，不查询数据库）"
                    }
                }
            }
        }
        super().__init__("validate_test_case", "验证测试用例质量", schema)
        self.java_api_base_url = java_api_base_url
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        try:
            case_id = arguments.get("case_id")
            case_code = arguments.get("case_code")
            case_data = arguments.get("case_data")
            
            # 如果没有提供用例数据，从API获取
            if not case_data:
                if case_id:
                    url = f"{self.java_api_base_url}/api/v1/test-cases/{case_id}"
                elif case_code:
                    url = f"{self.java_api_base_url}/api/v1/test-cases/code/{case_code}"
                else:
                    return {
                        "success": False,
                        "error": "必须提供case_id、case_code或case_data"
                    }
                
                response = requests.get(url, timeout=10)
                response.raise_for_status()
                data = response.json()
                
                if data.get("code") != 200:
                    return {
                        "success": False,
                        "error": data.get("message", "获取用例失败")
                    }
                case_data = data.get("data", {})
            
            # 验证用例质量
            issues = []
            suggestions = []
            
            # 检查必填字段
            if not case_data.get("caseName"):
                issues.append("用例名称不能为空")
            if not case_data.get("testStep"):
                issues.append("测试步骤不能为空")
            if not case_data.get("expectedResult"):
                issues.append("预期结果不能为空")
            
            # 检查用例完整性
            if not case_data.get("preCondition"):
                suggestions.append("建议添加前置条件")
            if not case_data.get("casePriority"):
                suggestions.append("建议设置用例优先级")
            
            # 检查测试步骤格式
            test_step = case_data.get("testStep", "")
            if len(test_step) < 10:
                issues.append("测试步骤过于简单，建议详细描述")
            
            return {
                "success": True,
                "is_valid": len(issues) == 0,
                "issues": issues,
                "suggestions": suggestions,
                "case_data": case_data
            }
        except Exception as e:
            logger.error(f"验证测试用例失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }


class GenerateTestDataTool(BaseTool):
    """生成测试数据工具"""
    
    def __init__(self):
        schema = {
            "name": "generate_test_data",
            "description": "根据参数类型和约束生成测试数据，支持等价类、边界值等测试数据生成",
            "parameters": {
                "type": "object",
                "properties": {
                    "data_type": {
                        "type": "string",
                        "description": "数据类型（string, integer, decimal, date, email, phone等）"
                    },
                    "constraints": {
                        "type": "object",
                        "description": "数据约束（min, max, length, pattern等）"
                    },
                    "count": {
                        "type": "integer",
                        "description": "生成数量（默认5）",
                        "default": 5
                    },
                    "test_type": {
                        "type": "string",
                        "description": "测试类型（valid, invalid, boundary）",
                        "enum": ["valid", "invalid", "boundary"]
                    }
                },
                "required": ["data_type"]
            }
        }
        super().__init__("generate_test_data", "生成测试数据", schema)
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        try:
            data_type = arguments.get("data_type")
            constraints = arguments.get("constraints", {})
            count = arguments.get("count", 5)
            test_type = arguments.get("test_type", "valid")
            
            # 简单的测试数据生成逻辑
            test_data = []
            
            if data_type == "string":
                min_length = constraints.get("min_length", 1)
                max_length = constraints.get("max_length", 100)
                
                if test_type == "valid":
                    for i in range(count):
                        length = min_length + (max_length - min_length) // count * i
                        test_data.append("a" * length)
                elif test_type == "boundary":
                    test_data.append("a" * min_length)
                    test_data.append("a" * max_length)
                elif test_type == "invalid":
                    test_data.append("")
                    test_data.append("a" * (max_length + 1))
            
            elif data_type == "integer":
                min_val = constraints.get("min", 0)
                max_val = constraints.get("max", 100)
                
                if test_type == "valid":
                    step = (max_val - min_val) // count if count > 1 else 1
                    for i in range(count):
                        test_data.append(min_val + step * i)
                elif test_type == "boundary":
                    test_data.append(min_val)
                    test_data.append(max_val)
                elif test_type == "invalid":
                    test_data.append(min_val - 1)
                    test_data.append(max_val + 1)
            
            elif data_type == "email":
                if test_type == "valid":
                    test_data = ["test@example.com", "user@domain.cn", "name.surname@company.com"]
                elif test_type == "invalid":
                    test_data = ["invalid", "@domain.com", "user@", "user@domain"]
            
            return {
                "success": True,
                "data_type": data_type,
                "test_type": test_type,
                "test_data": test_data[:count]
            }
        except Exception as e:
            logger.error(f"生成测试数据失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }

