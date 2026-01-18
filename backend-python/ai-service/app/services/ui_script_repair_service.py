"""
UI脚本修复服务
根据错误日志和页面代码信息，修复执行失败的UI自动化脚本
"""
import re
import json
import logging
from typing import Dict, List, Optional, Any
from sqlalchemy.orm import Session
from app.services.page_parser_service import PageParserService
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


class UIScriptRepairService:
    """UI脚本修复服务"""
    
    # 错误类型匹配规则
    ERROR_PATTERNS = {
        "ELEMENT_NOT_FOUND": [
            r"NoSuchElementException",
            r"Element not found",
            r"Unable to locate element",
            r"element not found",
            r"locator not found"
        ],
        "ELEMENT_NOT_VISIBLE": [
            r"ElementNotVisibleException",
            r"Element is not visible",
            r"element is not displayed",
            r"element not visible"
        ],
        "ELEMENT_NOT_CLICKABLE": [
            r"ElementNotInteractableException",
            r"Element is not clickable",
            r"element not clickable",
            r"element is not interactable"
        ],
        "TIMEOUT": [
            r"TimeoutException",
            r"timeout",
            r"time out",
            r"wait timeout"
        ],
        "STALE_ELEMENT": [
            r"StaleElementReferenceException",
            r"stale element",
            r"element is stale"
        ],
        "INVALID_SELECTOR": [
            r"InvalidSelectorException",
            r"invalid selector",
            r"invalid xpath",
            r"invalid css selector"
        ]
    }
    
    def __init__(self, db: Session):
        """
        初始化UI脚本修复服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.page_parser = PageParserService()
        self.llm_service = LLMService(db)
    
    def analyze_error(
        self,
        error_log: str,
        script_content: Optional[str] = None,
        use_llm: bool = True
    ) -> Dict[str, Any]:
        """
        分析错误日志
        
        Args:
            error_log: 错误日志
            script_content: 脚本内容（可选）
            use_llm: 是否使用大语言模型分析
            
        Returns:
            错误分析结果：
            - error_type: 错误类型
            - error_message: 错误消息
            - error_context: 错误上下文
            - affected_element: 受影响的元素信息
            - suggestions: 修复建议
        """
        try:
            logger.info("开始分析错误日志")
            
            if use_llm:
                # 使用大语言模型分析
                error_analysis = self._analyze_error_with_llm(error_log, script_content)
            else:
                # 使用规则分析
                error_analysis = self._analyze_error_with_rules(error_log, script_content)
            
            logger.info(f"错误分析完成: {error_analysis.get('error_type')}")
            
            return error_analysis
            
        except Exception as e:
            logger.error(f"错误分析失败: {str(e)}")
            raise ValueError(f"错误分析失败: {str(e)}")
    
    def detect_page_changes(
        self,
        old_page_code_url: Optional[str] = None,
        old_page_elements: Optional[List[Dict]] = None,
        new_page_code_url: Optional[str] = None,
        new_page_elements: Optional[List[Dict]] = None,
        script_locators: Optional[List[Dict]] = None
    ) -> Dict[str, Any]:
        """
        检测页面变化
        
        Args:
            old_page_code_url: 旧页面代码URL或文件路径（可选）
            old_page_elements: 旧页面元素列表（可选）
            new_page_code_url: 新页面代码URL或文件路径（可选）
            new_page_elements: 新页面元素列表（可选）
            script_locators: 脚本中使用的定位器列表（可选）
            
        Returns:
            页面变化检测结果：
            - has_changes: 是否有变化
            - changed_elements: 变化的元素列表
            - missing_elements: 缺失的元素列表
            - new_elements: 新增的元素列表
            - locator_changes: 定位器变化列表
        """
        try:
            logger.info("开始检测页面变化")
            
            # 获取旧页面元素
            if old_page_elements is None and old_page_code_url:
                old_parse_result = self.page_parser.parse_page_code(old_page_code_url)
                old_page_elements = old_parse_result.get("elements", [])
            
            # 获取新页面元素
            if new_page_elements is None and new_page_code_url:
                new_parse_result = self.page_parser.parse_page_code(new_page_code_url)
                new_page_elements = new_parse_result.get("elements", [])
            
            if old_page_elements is None:
                old_page_elements = []
            if new_page_elements is None:
                new_page_elements = []
            
            # 检测元素变化
            changes = self._compare_elements(old_page_elements, new_page_elements, script_locators)
            
            logger.info(f"页面变化检测完成: {len(changes.get('changed_elements', []))} 个变化")
            
            return changes
            
        except Exception as e:
            logger.error(f"页面变化检测失败: {str(e)}")
            raise ValueError(f"页面变化检测失败: {str(e)}")
    
    def repair_script(
        self,
        script_content: str,
        error_log: str,
        error_analysis: Optional[Dict] = None,
        page_changes: Optional[Dict] = None,
        new_page_code_url: Optional[str] = None,
        new_page_elements: Optional[List[Dict]] = None,
        script_type: str = "SELENIUM",
        script_language: str = "PYTHON",
        use_llm: bool = True
    ) -> Dict[str, Any]:
        """
        修复UI脚本
        
        Args:
            script_content: 原始脚本内容
            error_log: 错误日志
            error_analysis: 错误分析结果（可选，如果不提供则自动分析）
            page_changes: 页面变化检测结果（可选，如果不提供则自动检测）
            new_page_code_url: 新页面代码URL或文件路径（可选）
            new_page_elements: 新页面元素列表（可选）
            script_type: 脚本类型（SELENIUM/PLAYWRIGHT）
            script_language: 脚本语言（PYTHON/JAVA/JAVASCRIPT）
            use_llm: 是否使用大语言模型修复
            
        Returns:
            修复结果：
            - repaired_script: 修复后的脚本内容
            - repair_changes: 修复变更列表
            - repair_summary: 修复摘要
        """
        try:
            logger.info("开始修复UI脚本")
            
            # 1. 分析错误（如果需要）
            if error_analysis is None:
                error_analysis = self.analyze_error(error_log, script_content, use_llm)
            
            # 2. 检测页面变化（如果需要）
            if page_changes is None and new_page_code_url:
                # 从脚本中提取定位器
                script_locators = self._extract_locators_from_script(script_content)
                page_changes = self.detect_page_changes(
                    new_page_code_url=new_page_code_url,
                    new_page_elements=new_page_elements,
                    script_locators=script_locators
                )
            
            # 3. 修复脚本
            if use_llm:
                # 使用大语言模型修复
                repair_result = self._repair_script_with_llm(
                    script_content, error_analysis, page_changes, script_type, script_language
                )
            else:
                # 使用规则修复
                repair_result = self._repair_script_with_rules(
                    script_content, error_analysis, page_changes, script_type, script_language
                )
            
            logger.info("脚本修复完成")
            
            return repair_result
            
        except Exception as e:
            logger.error(f"脚本修复失败: {str(e)}")
            raise ValueError(f"脚本修复失败: {str(e)}")
    
    def _analyze_error_with_rules(self, error_log: str, script_content: Optional[str] = None) -> Dict[str, Any]:
        """使用规则分析错误"""
        error_log_lower = error_log.lower()
        
        # 识别错误类型
        error_type = "UNKNOWN"
        for err_type, patterns in self.ERROR_PATTERNS.items():
            for pattern in patterns:
                if re.search(pattern.lower(), error_log_lower):
                    error_type = err_type
                    break
            if error_type != "UNKNOWN":
                break
        
        # 提取错误消息
        error_message = ""
        lines = error_log.split("\n")
        for line in lines:
            if any(keyword in line.lower() for keyword in ["exception", "error", "failed", "timeout"]):
                error_message = line.strip()
                break
        
        # 提取定位器信息
        affected_element = None
        locator_patterns = [
            r"locator[:\s]+['\"]([^'\"]+)['\"]",
            r"selector[:\s]+['\"]([^'\"]+)['\"]",
            r"xpath[:\s]+['\"]([^'\"]+)['\"]",
            r"id[:\s]+['\"]([^'\"]+)['\"]",
            r"name[:\s]+['\"]([^'\"]+)['\"]",
        ]
        
        for pattern in locator_patterns:
            match = re.search(pattern, error_log, re.IGNORECASE)
            if match:
                affected_element = {
                    "locator": match.group(1),
                    "locator_type": "unknown"
                }
                break
        
        # 生成修复建议
        suggestions = self._generate_repair_suggestions(error_type, affected_element)
        
        return {
            "error_type": error_type,
            "error_message": error_message or error_log[:200],
            "error_context": {
                "error_log_snippet": error_log[:500]
            },
            "affected_element": affected_element,
            "suggestions": suggestions
        }
    
    def _analyze_error_with_llm(self, error_log: str, script_content: Optional[str] = None) -> Dict[str, Any]:
        """使用大语言模型分析错误"""
        try:
            # 构建提示词
            prompt = f"""请分析以下UI自动化测试脚本的错误日志，识别错误类型、提取错误信息和上下文。

错误日志：
{error_log[:2000]}

"""
            
            if script_content:
                prompt += f"""
相关脚本代码（片段）：
{script_content[:1000]}

"""
            
            prompt += """
请分析并返回JSON格式的结果：
{
    "error_type": "错误类型（ELEMENT_NOT_FOUND/ELEMENT_NOT_VISIBLE/TIMEOUT等）",
    "error_message": "错误消息摘要",
    "error_context": {{
        "error_line": "错误发生的行号或位置",
        "locator": "受影响的定位器",
        "action": "失败的操作类型"
    }},
    "affected_element": {{
        "locator": "元素定位器",
        "locator_type": "定位器类型（id/name/xpath/css_selector）",
        "element_type": "元素类型"
    }},
    "suggestions": ["修复建议1", "修复建议2"]
}

只返回JSON，不要其他说明。"""
            
            # 调用大语言模型
            response = self.llm_service.call_model(
                model_code=None,
                prompt=prompt,
                temperature=0.2
            )
            
            content = response.get("content", "")
            
            # 解析JSON响应
            try:
                # 尝试提取JSON
                json_match = re.search(r'\{[\s\S]*\}', content)
                if json_match:
                    error_analysis = json.loads(json_match.group())
                else:
                    raise ValueError("未找到JSON格式的响应")
            except json.JSONDecodeError:
                # 如果解析失败，使用规则分析
                logger.warning("LLM响应解析失败，降级到规则分析")
                return self._analyze_error_with_rules(error_log, script_content)
            
            return error_analysis
            
        except Exception as e:
            logger.warning(f"LLM错误分析失败，降级到规则分析: {str(e)}")
            return self._analyze_error_with_rules(error_log, script_content)
    
    def _compare_elements(
        self,
        old_elements: List[Dict],
        new_elements: List[Dict],
        script_locators: Optional[List[Dict]] = None
    ) -> Dict[str, Any]:
        """比较页面元素"""
        changed_elements = []
        missing_elements = []
        new_elements_list = []
        locator_changes = []
        
        # 构建旧元素索引（按定位器）
        old_elements_map = {}
        for elem in old_elements:
            locators = elem.get("locators", {})
            for loc_type, loc_value in locators.items():
                key = f"{loc_type}:{loc_value}"
                old_elements_map[key] = elem
        
        # 构建新元素索引
        new_elements_map = {}
        for elem in new_elements:
            locators = elem.get("locators", {})
            for loc_type, loc_value in locators.items():
                key = f"{loc_type}:{loc_value}"
                new_elements_map[key] = elem
        
        # 检测缺失的元素（旧有但新无）
        for key, elem in old_elements_map.items():
            if key not in new_elements_map:
                missing_elements.append(elem)
                if script_locators:
                    # 检查是否在脚本中使用
                    for script_loc in script_locators:
                        if self._locator_matches(key, script_loc):
                            locator_changes.append({
                                "old_locator": script_loc,
                                "status": "missing",
                                "element": elem
                            })
        
        # 检测新增的元素
        for key, elem in new_elements_map.items():
            if key not in old_elements_map:
                new_elements_list.append(elem)
        
        # 检测变化的元素（定位器相同但属性不同）
        for key in old_elements_map:
            if key in new_elements_map:
                old_elem = old_elements_map[key]
                new_elem = new_elements_map[key]
                if self._element_changed(old_elem, new_elem):
                    changed_elements.append({
                        "old_element": old_elem,
                        "new_element": new_elem
                    })
        
        has_changes = len(changed_elements) > 0 or len(missing_elements) > 0 or len(new_elements_list) > 0
        
        return {
            "has_changes": has_changes,
            "changed_elements": changed_elements,
            "missing_elements": missing_elements,
            "new_elements": new_elements_list,
            "locator_changes": locator_changes
        }
    
    def _element_changed(self, old_elem: Dict, new_elem: Dict) -> bool:
        """检查元素是否变化"""
        # 比较关键属性
        old_text = old_elem.get("element_text", "")
        new_text = new_elem.get("element_text", "")
        if old_text != new_text:
            return True
        
        old_id = old_elem.get("id", "")
        new_id = new_elem.get("id", "")
        if old_id != new_id:
            return True
        
        old_name = old_elem.get("name", "")
        new_name = new_elem.get("name", "")
        if old_name != new_name:
            return True
        
        return False
    
    def _locator_matches(self, element_key: str, script_locator: Dict) -> bool:
        """检查定位器是否匹配"""
        loc_type = script_locator.get("type", "")
        loc_value = script_locator.get("value", "")
        script_key = f"{loc_type}:{loc_value}"
        return script_key == element_key
    
    def _extract_locators_from_script(self, script_content: str) -> List[Dict]:
        """从脚本中提取定位器"""
        locators = []
        
        # Selenium定位器模式
        selenium_patterns = [
            (r'By\.ID\(["\']([^"\']+)["\']\)', "id"),
            (r'By\.NAME\(["\']([^"\']+)["\']\)', "name"),
            (r'By\.CSS_SELECTOR\(["\']([^"\']+)["\']\)', "css_selector"),
            (r'By\.XPATH\(["\']([^"\']+)["\']\)', "xpath"),
        ]
        
        for pattern, loc_type in selenium_patterns:
            matches = re.findall(pattern, script_content)
            for match in matches:
                locators.append({
                    "type": loc_type,
                    "value": match
                })
        
        return locators
    
    def _generate_repair_suggestions(self, error_type: str, affected_element: Optional[Dict] = None) -> List[str]:
        """生成修复建议"""
        suggestions = []
        
        if error_type == "ELEMENT_NOT_FOUND":
            suggestions.append("元素定位失败，检查定位器是否正确")
            suggestions.append("尝试使用其他定位方式（ID、Name、XPath、CSS选择器）")
            suggestions.append("检查元素是否在iframe中")
            suggestions.append("增加等待时间，等待元素加载")
        
        elif error_type == "ELEMENT_NOT_VISIBLE":
            suggestions.append("元素不可见，等待元素可见后再操作")
            suggestions.append("检查元素是否被其他元素遮挡")
            suggestions.append("使用JavaScript滚动到元素位置")
        
        elif error_type == "ELEMENT_NOT_CLICKABLE":
            suggestions.append("元素不可点击，等待元素可交互")
            suggestions.append("检查元素是否被禁用或只读")
            suggestions.append("使用JavaScript执行点击操作")
        
        elif error_type == "TIMEOUT":
            suggestions.append("操作超时，增加等待时间")
            suggestions.append("检查元素定位器是否正确")
            suggestions.append("使用显式等待替代隐式等待")
        
        elif error_type == "STALE_ELEMENT":
            suggestions.append("元素已过期，重新定位元素")
            suggestions.append("避免在循环中使用已定位的元素")
        
        elif error_type == "INVALID_SELECTOR":
            suggestions.append("定位器格式错误，检查XPath或CSS选择器语法")
            suggestions.append("转义特殊字符")
        
        else:
            suggestions.append("检查错误日志，定位具体问题")
            suggestions.append("尝试重新运行脚本")
        
        return suggestions
    
    def _repair_script_with_rules(
        self,
        script_content: str,
        error_analysis: Dict,
        page_changes: Optional[Dict],
        script_type: str,
        script_language: str
    ) -> Dict[str, Any]:
        """使用规则修复脚本"""
        repair_changes = []
        repaired_script = script_content
        
        error_type = error_analysis.get("error_type")
        affected_element = error_analysis.get("affected_element")
        
        # 根据错误类型修复
        if error_type == "TIMEOUT":
            # 增加等待时间
            repaired_script = re.sub(
                r'(WebDriverWait\(driver,\s*)(\d+)(\))',
                lambda m: f"{m.group(1)}{max(int(m.group(2)), 10)}{m.group(3)}",
                repaired_script
            )
            repair_changes.append("增加了等待时间")
        
        if affected_element:
            locator = affected_element.get("locator")
            if locator and error_type in ["ELEMENT_NOT_FOUND", "ELEMENT_NOT_VISIBLE"]:
                # 尝试修复定位器
                # 这里可以添加更复杂的修复逻辑
                repair_changes.append(f"尝试修复定位器: {locator}")
        
        return {
            "repaired_script": repaired_script,
            "repair_changes": repair_changes,
            "repair_summary": f"修复了{len(repair_changes)}处问题"
        }
    
    def _repair_script_with_llm(
        self,
        script_content: str,
        error_analysis: Dict,
        page_changes: Optional[Dict],
        script_type: str,
        script_language: str
    ) -> Dict[str, Any]:
        """使用大语言模型修复脚本"""
        try:
            # 构建提示词
            prompt = f"""请修复以下UI自动化测试脚本。脚本执行失败，错误分析结果如下。

错误分析：
{json.dumps(error_analysis, ensure_ascii=False, indent=2)}

"""
            
            if page_changes:
                prompt += f"""
页面变化检测：
{json.dumps(page_changes, ensure_ascii=False, indent=2)}

"""
            
            prompt += f"""
原始脚本（{script_type} {script_language}）：
{script_content[:3000]}

修复要求：
1. 根据错误分析结果修复脚本
2. 如果页面元素发生变化，更新相应的定位器
3. 优化等待策略，避免超时错误
4. 保持代码风格和结构不变
5. 添加必要的错误处理
6. 确保修复后的脚本可以正常执行

请返回修复后的完整脚本代码。只返回代码，不要其他说明。"""
            
            # 调用大语言模型
            response = self.llm_service.call_model(
                model_code=None,
                prompt=prompt,
                temperature=0.2
            )
            
            repaired_script = response.get("content", "")
            
            # 清理响应（移除可能的markdown代码块标记）
            repaired_script = re.sub(r'```python\n?', '', repaired_script)
            repaired_script = re.sub(r'```\n?', '', repaired_script)
            repaired_script = repaired_script.strip()
            
            # 生成修复变更列表
            repair_changes = self._generate_repair_changes(script_content, repaired_script, error_analysis, page_changes)
            
            return {
                "repaired_script": repaired_script,
                "repair_changes": repair_changes,
                "repair_summary": f"修复了{len(repair_changes)}处问题"
            }
            
        except Exception as e:
            logger.warning(f"LLM脚本修复失败，降级到规则修复: {str(e)}")
            return self._repair_script_with_rules(script_content, error_analysis, page_changes, script_type, script_language)
    
    def _generate_repair_changes(
        self,
        old_script: str,
        new_script: str,
        error_analysis: Dict,
        page_changes: Optional[Dict]
    ) -> List[str]:
        """生成修复变更列表"""
        changes = []
        
        error_type = error_analysis.get("error_type")
        if error_type:
            changes.append(f"修复了{error_type}错误")
        
        if page_changes and page_changes.get("locator_changes"):
            changes.append(f"更新了{len(page_changes['locator_changes'])}个定位器")
        
        # 检查等待时间变化
        old_waits = len(re.findall(r'WebDriverWait\(driver,\s*\d+\)', old_script))
        new_waits = len(re.findall(r'WebDriverWait\(driver,\s*\d+\)', new_script))
        if new_waits > old_waits:
            changes.append("增加了显式等待")
        
        return changes

