"""
参数提取服务
从测试用例中智能提取输入参数和等价类
"""
import logging
import json
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


class ParameterExtractionService:
    """参数提取服务"""
    
    def __init__(self, db: Session):
        """
        初始化参数提取服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.llm_service = LLMService(db)
    
    def extract_parameters_and_equivalence_classes(
        self,
        test_cases: List[Dict[str, Any]],
        model_code: Optional[str] = None,
        use_llm: bool = True
    ) -> Dict[str, Any]:
        """
        从测试用例中提取参数和等价类
        
        Args:
            test_cases: 测试用例列表，每个用例包含case_name、test_step、expected_result等字段
            model_code: 模型代码（可选，默认使用配置的模型）
            use_llm: 是否使用LLM（如果False，使用规则提取）
        
        Returns:
            包含参数和等价类的字典
        """
        logger.info(f"开始提取参数和等价类，用例数量: {len(test_cases)}, 使用LLM: {use_llm}")
        
        if not test_cases:
            return {
                "parameters": [],
                "equivalence_classes": {}
            }
        
        if use_llm:
            return self._extract_with_llm(test_cases, model_code)
        else:
            return self._extract_with_rules(test_cases)
    
    def _extract_with_llm(
        self,
        test_cases: List[Dict[str, Any]],
        model_code: Optional[str] = None
    ) -> Dict[str, Any]:
        """使用LLM提取参数和等价类"""
        try:
            # 构建提示词
            prompt = self._build_extraction_prompt(test_cases)
            
            # 获取默认模型代码（如果未指定）
            if not model_code:
                # 从配置中获取默认模型
                from app.services.model_config_service import ModelConfigService
                model_config_service = ModelConfigService(self.db)
                default_model = model_config_service.get_default_config()
                if default_model:
                    model_code = default_model.model_code
                else:
                    model_code = "DEEPSEEK_CHAT"  # 默认模型
            
            logger.info(f"使用模型提取参数: {model_code}")
            
            # 调用LLM
            llm = self.llm_service._get_llm_instance(model_code, max_tokens=4000, temperature=0.3)
            response = self.llm_service._call_with_retry(llm, prompt, max_retries=2)
            
            # 解析响应
            result = self._parse_llm_response(response)
            
            logger.info(f"参数提取完成，提取到 {len(result.get('parameters', []))} 个参数")
            return result
            
        except Exception as e:
            logger.error(f"使用LLM提取参数失败: {str(e)}", exc_info=True)
            # 降级到规则提取
            logger.warning("降级到规则提取")
            return self._extract_with_rules(test_cases)
    
    def _build_extraction_prompt(self, test_cases: List[Dict[str, Any]]) -> str:
        """构建提取提示词"""
        # 构建用例文本
        cases_text = ""
        for i, case in enumerate(test_cases, 1):
            case_name = case.get("case_name", "")
            test_step = case.get("test_step", "")
            expected_result = case.get("expected_result", "")
            pre_condition = case.get("pre_condition", "")
            
            cases_text += f"""
用例{i}：
- 用例名称：{case_name}
- 前置条件：{pre_condition}
- 测试步骤：{test_step}
- 预期结果：{expected_result}
"""
        
        prompt = f"""你是一个测试设计专家。请从以下测试用例中提取输入参数，并为每个参数划分有效等价类和无效等价类。

测试用例：
{cases_text}

要求：
1. 识别所有输入参数（如：金额、数量、日期、用户名、密码等）
2. 为每个参数划分有效等价类和无效等价类
3. 有效等价类应包含正常值、边界值等
4. 无效等价类应包含空值、非法值、超出范围值等
5. 返回JSON格式，格式如下：
{{
    "parameters": [
        {{
            "name": "参数名称",
            "description": "参数描述",
            "type": "参数类型（如：string、number、date等）",
            "valid_equivalence_classes": [
                "有效等价类1",
                "有效等价类2"
            ],
            "invalid_equivalence_classes": [
                "无效等价类1",
                "无效等价类2"
            ]
        }}
    ]
}}

请直接返回JSON，不要包含其他说明文字。"""
        
        return prompt
    
    def _parse_llm_response(self, response: str) -> Dict[str, Any]:
        """解析LLM响应"""
        try:
            # 尝试提取JSON部分
            response = response.strip()
            
            # 如果响应包含```json代码块，提取其中的内容
            if "```json" in response:
                start = response.find("```json") + 7
                end = response.find("```", start)
                if end > start:
                    response = response[start:end].strip()
            elif "```" in response:
                # 处理其他代码块格式
                start = response.find("```") + 3
                end = response.find("```", start)
                if end > start:
                    response = response[start:end].strip()
            
            # 尝试解析JSON
            result = json.loads(response)
            
            # 验证和规范化结果
            if not isinstance(result, dict):
                raise ValueError("响应不是字典格式")
            
            if "parameters" not in result:
                result["parameters"] = []
            
            # 构建等价类映射
            equivalence_classes = {}
            for param in result.get("parameters", []):
                param_name = param.get("name", "")
                if param_name:
                    equivalence_classes[param_name] = {
                        "有效等价类": param.get("valid_equivalence_classes", []),
                        "无效等价类": param.get("invalid_equivalence_classes", [])
                    }
            
            return {
                "parameters": result.get("parameters", []),
                "equivalence_classes": equivalence_classes
            }
            
        except json.JSONDecodeError as e:
            logger.error(f"解析LLM响应JSON失败: {str(e)}, 响应内容: {response[:200]}")
            # 尝试使用规则提取作为降级
            return {
                "parameters": [],
                "equivalence_classes": {}
            }
        except Exception as e:
            logger.error(f"解析LLM响应失败: {str(e)}", exc_info=True)
            return {
                "parameters": [],
                "equivalence_classes": {}
            }
    
    def _extract_with_rules(self, test_cases: List[Dict[str, Any]]) -> Dict[str, Any]:
        """使用规则提取参数和等价类"""
        logger.info("使用规则提取参数和等价类")
        
        parameters = []
        equivalence_classes = {}
        
        # 常见参数关键词
        param_keywords = {
            "金额": ["金额", "保费", "保额", "费用", "价格", "元"],
            "数量": ["数量", "件数", "个数", "份数"],
            "日期": ["日期", "时间", "年月日", "生日"],
            "用户名": ["用户名", "账号", "登录名"],
            "密码": ["密码", "口令"],
            "手机号": ["手机", "电话", "手机号", "电话号码"],
            "邮箱": ["邮箱", "邮件", "email"],
            "身份证": ["身份证", "证件号", "身份证号"],
            "姓名": ["姓名", "名字", "名称"]
        }
        
        # 从测试用例中提取
        for case in test_cases:
            test_step = case.get("test_step", "")
            case_name = case.get("case_name", "")
            
            # 合并文本进行匹配
            text = f"{case_name} {test_step}"
            
            for param_name, keywords in param_keywords.items():
                for keyword in keywords:
                    if keyword in text:
                        # 检查是否已存在该参数
                        if not any(p["name"] == param_name for p in parameters):
                            parameters.append({
                                "name": param_name,
                                "description": f"从用例中提取的{param_name}参数",
                                "type": self._infer_parameter_type(param_name),
                                "valid_equivalence_classes": self._get_default_valid_classes(param_name),
                                "invalid_equivalence_classes": self._get_default_invalid_classes(param_name)
                            })
                            
                            equivalence_classes[param_name] = {
                                "有效等价类": self._get_default_valid_classes(param_name),
                                "无效等价类": self._get_default_invalid_classes(param_name)
                            }
                        break
        
        # 如果没有提取到参数，使用默认示例
        if not parameters:
            parameters.append({
                "name": "输入参数",
                "description": "从用例中提取的输入参数",
                "type": "string",
                "valid_equivalence_classes": ["正常值1", "正常值2", "正常值3"],
                "invalid_equivalence_classes": ["空值", "非法值", "超出范围值"]
            })
            
            equivalence_classes["输入参数"] = {
                "有效等价类": ["正常值1", "正常值2", "正常值3"],
                "无效等价类": ["空值", "非法值", "超出范围值"]
            }
        
        return {
            "parameters": parameters,
            "equivalence_classes": equivalence_classes
        }
    
    def _infer_parameter_type(self, param_name: str) -> str:
        """推断参数类型"""
        type_map = {
            "金额": "number",
            "数量": "number",
            "日期": "date",
            "手机号": "string",
            "邮箱": "string",
            "身份证": "string"
        }
        return type_map.get(param_name, "string")
    
    def _get_default_valid_classes(self, param_name: str) -> List[str]:
        """获取默认有效等价类"""
        defaults = {
            "金额": ["100", "1000", "10000"],
            "数量": ["1", "10", "100"],
            "日期": ["2024-01-01", "2024-12-31"],
            "手机号": ["13800138000", "13900139000"],
            "邮箱": ["test@example.com", "user@test.com"]
        }
        return defaults.get(param_name, ["正常值1", "正常值2", "正常值3"])
    
    def _get_default_invalid_classes(self, param_name: str) -> List[str]:
        """获取默认无效等价类"""
        defaults = {
            "金额": ["空值", "负数", "超出范围值"],
            "数量": ["空值", "负数", "0", "超出范围值"],
            "日期": ["空值", "非法格式", "超出范围值"],
            "手机号": ["空值", "格式错误", "位数不对"],
            "邮箱": ["空值", "格式错误", "缺少@符号"]
        }
        return defaults.get(param_name, ["空值", "非法值", "超出范围值"])
