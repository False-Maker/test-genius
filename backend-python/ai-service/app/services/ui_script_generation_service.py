"""
UI脚本生成服务
根据自然语言描述和页面元素信息，生成可执行的UI自动化脚本
"""
import re
import json
import logging
from typing import Dict, List, Optional, Any
from sqlalchemy.orm import Session
from app.services.page_parser_service import PageParserService
from app.services.nlp_service import NLPService
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


class UIScriptGenerationService:
    """UI脚本生成服务"""
    
    # Selenium Python模板
    SELENIUM_PYTHON_TEMPLATE = """from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
import time

# 初始化浏览器
driver = webdriver.Chrome()
driver.maximize_window()

try:
    # 打开页面
    driver.get("{url}")
    
    # 等待页面加载
    time.sleep(2)
    
{actions}
    
    print("测试执行成功")
    
finally:
    # 关闭浏览器
    driver.quit()
"""
    
    # Playwright Python模板
    PLAYWRIGHT_PYTHON_TEMPLATE = """from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    page = browser.new_page()
    
    # 打开页面
    page.goto("{url}")
    
{actions}
    
    print("测试执行成功")
    
    browser.close()
"""
    
    def __init__(self, db: Session):
        """
        初始化UI脚本生成服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.page_parser = PageParserService()
        self.nlp_service = NLPService(db)
        self.llm_service = LLMService(db)
    
    def generate_script(
        self,
        natural_language_desc: str,
        page_code_url: Optional[str] = None,
        page_elements: Optional[List[Dict]] = None,
        script_type: str = "SELENIUM",
        script_language: str = "PYTHON",
        page_url: Optional[str] = None,
        use_llm: bool = True
    ) -> Dict[str, Any]:
        """
        生成UI自动化脚本
        
        Args:
            natural_language_desc: 自然语言描述
            page_code_url: 页面代码URL或文件路径（可选）
            page_elements: 页面元素信息列表（可选，如果提供则跳过解析）
            script_type: 脚本类型（SELENIUM/PLAYWRIGHT）
            script_language: 脚本语言（PYTHON/JAVA/JAVASCRIPT）
            page_url: 页面URL（用于脚本中的页面打开）
            use_llm: 是否使用大语言模型优化脚本生成
            
        Returns:
            生成的脚本信息：
            - script_content: 脚本内容
            - script_type: 脚本类型
            - script_language: 脚本语言
            - elements_used: 使用的元素列表
        """
        try:
            # 1. 解析自然语言描述
            logger.info("开始解析自然语言描述")
            nlp_result = self.nlp_service.parse_natural_language(natural_language_desc, use_llm)
            steps = nlp_result.get("steps", [])
            
            if not steps:
                raise ValueError("未能从自然语言描述中提取操作步骤")
            
            logger.info(f"解析出 {len(steps)} 个操作步骤")
            
            # 2. 解析页面代码（如果需要）
            elements = page_elements
            if elements is None and page_code_url:
                logger.info("开始解析页面代码")
                page_parse_result = self.page_parser.parse_page_code(page_code_url)
                elements = page_parse_result.get("elements", [])
                logger.info(f"解析出 {len(elements)} 个页面元素")
            elif elements is None:
                elements = []
            
            # 3. 匹配元素和操作
            logger.info("开始匹配元素和操作")
            matched_steps = self._match_elements_to_steps(steps, elements)
            
            # 4. 生成脚本代码
            logger.info("开始生成脚本代码")
            if use_llm and script_type in ["SELENIUM", "PLAYWRIGHT"]:
                # 使用大语言模型生成脚本
                script_content = self._generate_script_with_llm(
                    matched_steps, script_type, script_language, page_url
                )
            else:
                # 使用模板生成脚本
                script_content = self._generate_script_with_template(
                    matched_steps, script_type, script_language, page_url
                )
            
            # 5. 提取使用的元素
            elements_used = [step.get("matched_element") for step in matched_steps if step.get("matched_element")]
            
            logger.info("脚本生成完成")
            
            return {
                "script_content": script_content,
                "script_type": script_type,
                "script_language": script_language,
                "elements_used": elements_used,
                "steps": matched_steps,
                "page_url": page_url
            }
            
        except Exception as e:
            logger.error(f"脚本生成失败: {str(e)}")
            raise ValueError(f"脚本生成失败: {str(e)}")
    
    def _match_elements_to_steps(self, steps: List[Dict], elements: List[Dict]) -> List[Dict]:
        """匹配元素和操作步骤"""
        matched_steps = []
        
        for step in steps:
            matched_element = None
            locator = None
            
            # 尝试匹配元素
            if elements:
                matched_element = self._find_matching_element(step, elements)
                if matched_element:
                    # 选择最佳定位方式
                    locator = self._select_best_locator(matched_element)
            
            step_with_match = {
                **step,
                "matched_element": matched_element,
                "locator": locator
            }
            
            matched_steps.append(step_with_match)
        
        return matched_steps
    
    def _find_matching_element(self, step: Dict, elements: List[Dict]) -> Optional[Dict]:
        """查找匹配的元素"""
        element_text = step.get("element_text", "").lower()
        element_identifier = step.get("element_identifier", "").lower()
        element_description = step.get("element_description", "").lower()
        
        best_match = None
        best_score = 0
        
        for element in elements:
            score = 0
            
            # 文本匹配
            elem_text = element.get("element_text", "").lower()
            if element_text and element_text in elem_text:
                score += 3
            if elem_text and elem_text in element_description:
                score += 2
            
            # ID匹配
            elem_id = element.get("id", "").lower()
            if element_identifier and element_identifier == elem_id:
                score += 5
            
            # Name匹配
            elem_name = element.get("name", "").lower()
            if element_identifier and element_identifier == elem_name:
                score += 4
            
            # 类型匹配
            elem_type = element.get("element_type", "").lower()
            step_type = step.get("element_type", "").lower()
            if step_type and step_type == elem_type:
                score += 2
            
            if score > best_score:
                best_score = score
                best_match = element
        
        return best_match if best_score > 0 else None
    
    def _select_best_locator(self, element: Dict) -> Dict[str, str]:
        """选择最佳定位方式"""
        locators = element.get("locators", {})
        
        # 优先级：ID > Name > CSS Selector > XPath > Text
        if "id" in locators:
            return {"type": "id", "value": locators["id"]}
        elif "name" in locators:
            return {"type": "name", "value": locators["name"]}
        elif "css_selector" in locators:
            return {"type": "css_selector", "value": locators["css_selector"]}
        elif "xpath" in locators:
            return {"type": "xpath", "value": locators["xpath"]}
        elif "text" in locators:
            return {"type": "text", "value": locators["text"]}
        else:
            return {"type": "css_selector", "value": element.get("tag_name", "div")}
    
    def _generate_script_with_template(
        self,
        steps: List[Dict],
        script_type: str,
        script_language: str,
        page_url: Optional[str]
    ) -> str:
        """使用模板生成脚本"""
        if script_language != "PYTHON":
            raise ValueError(f"当前仅支持PYTHON语言，不支持{script_language}")
        
        actions = []
        for step in steps:
            action_code = self._generate_action_code(step, script_type)
            if action_code:
                actions.append(action_code)
        
        actions_str = "\n    ".join(actions)
        
        if script_type == "SELENIUM":
            template = self.SELENIUM_PYTHON_TEMPLATE
        elif script_type == "PLAYWRIGHT":
            template = self.PLAYWRIGHT_PYTHON_TEMPLATE
        else:
            raise ValueError(f"不支持的脚本类型: {script_type}")
        
        return template.format(
            url=page_url or "https://example.com",
            actions=actions_str
        )
    
    def _generate_action_code(self, step: Dict, script_type: str) -> Optional[str]:
        """生成单个操作的代码"""
        action_type = step.get("action_type", "")
        locator = step.get("locator")
        action_value = step.get("action_value")
        
        if not locator:
            # 如果没有定位器，使用描述性定位
            element_desc = step.get("element_description", "")
            logger.warning(f"未找到元素定位器，操作类型: {action_type}, 元素描述: {element_desc}")
            return f"    # 警告：未找到元素定位器，操作类型: {action_type}, 元素描述: {element_desc}\n    # 请手动添加元素定位代码"
        
        locator_type = locator.get("type")
        locator_value = locator.get("value")
        
        if script_type == "SELENIUM":
            return self._generate_selenium_action(action_type, locator_type, locator_value, action_value)
        elif script_type == "PLAYWRIGHT":
            return self._generate_playwright_action(action_type, locator_type, locator_value, action_value)
        else:
            return None
    
    def _generate_selenium_action(self, action_type: str, locator_type: str, locator_value: str, action_value: Optional[str]) -> str:
        """生成Selenium操作代码"""
        # 转换定位方式
        by_map = {
            "id": "By.ID",
            "name": "By.NAME",
            "css_selector": "By.CSS_SELECTOR",
            "xpath": "By.XPATH",
            "text": "By.XPATH"  # 文本定位使用XPath
        }
        
        by = by_map.get(locator_type, "By.CSS_SELECTOR")
        
        if locator_type == "text":
            locator_value = f"//*[text()='{locator_value}']"
        
        element_var = f"element_{action_type}"
        
        if action_type == "click":
            return f"""    # 点击元素
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    {element_var}.click()"""
        
        elif action_type == "input":
            value = action_value or ""
            return f"""    # 输入内容
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    {element_var}.clear()
    {element_var}.send_keys("{value}")"""
        
        elif action_type == "select":
            value = action_value or ""
            return f"""    # 选择选项
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    from selenium.webdriver.support.ui import Select
    select = Select({element_var})
    select.select_by_visible_text("{value}")"""
        
        elif action_type == "wait":
            wait_time = action_value or "2"
            return f"""    # 等待
    time.sleep({wait_time})"""
        
        elif action_type == "verify":
            return f"""    # 验证元素存在
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    assert {element_var}.is_displayed(), "元素未显示" """
        
        else:
            # 对于未实现的操作类型，返回基础代码框架
            logger.warning(f"未实现的操作类型: {action_type}")
            return f"""    # {action_type}操作（未实现，需要手动补充）
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    # 请根据实际需求实现{action_type}操作的具体逻辑"""
    
    def _generate_playwright_action(self, action_type: str, locator_type: str, locator_value: str, action_value: Optional[str]) -> str:
        """生成Playwright操作代码"""
        # Playwright使用CSS选择器或文本定位
        if locator_type == "id":
            selector = f"#{locator_value}"
        elif locator_type == "name":
            selector = f"[name='{locator_value}']"
        elif locator_type == "css_selector":
            selector = locator_value
        elif locator_type == "xpath":
            selector = locator_value
        elif locator_type == "text":
            selector = f"text={locator_value}"
        else:
            selector = locator_value
        
        if action_type == "click":
            return f"""    # 点击元素
    page.click("{selector}")"""
        
        elif action_type == "input":
            value = action_value or ""
            return f"""    # 输入内容
    page.fill("{selector}", "{value}")"""
        
        elif action_type == "select":
            value = action_value or ""
            return f"""    # 选择选项
    page.select_option("{selector}", label="{value}")"""
        
        elif action_type == "wait":
            wait_time = action_value or "2000"
            return f"""    # 等待
    page.wait_for_timeout({wait_time})"""
        
        elif action_type == "verify":
            return f"""    # 验证元素存在
    assert page.locator("{selector}").is_visible(), "元素未显示" """
        
        else:
            # 对于未实现的操作类型，返回基础代码框架
            logger.warning(f"未实现的操作类型: {action_type}")
            return f"""    # {action_type}操作（未实现，需要手动补充）
    # 请根据实际需求实现{action_type}操作的具体逻辑
    page.locator("{selector}")"""
    
    def _generate_script_with_llm(
        self,
        steps: List[Dict],
        script_type: str,
        script_language: str,
        page_url: Optional[str]
    ) -> str:
        """使用大语言模型生成脚本"""
        try:
            # 构建提示词
            steps_json = json.dumps(steps, ensure_ascii=False, indent=2)
            
            prompt = f"""请根据以下操作步骤，生成{script_type}的{script_language}自动化测试脚本。

操作步骤（JSON格式）：
{steps_json}

页面URL：{page_url or "待指定"}

要求：
1. 生成完整可执行的脚本代码
2. 使用{script_type}框架
3. 使用{script_language}语言
4. 包含必要的导入和初始化代码
5. 每个操作步骤都要有注释说明
6. 使用合适的等待策略（WebDriverWait或page.wait_for）
7. 包含错误处理和浏览器关闭代码

只返回脚本代码，不要其他说明。"""
            
            # 调用大语言模型
            response = self.llm_service.call_model(
                model_code=None,
                prompt=prompt,
                temperature=0.2
            )
            
            script_content = response.get("content", "")
            
            # 清理响应（移除可能的markdown代码块标记）
            script_content = re.sub(r'```python\n?', '', script_content)
            script_content = re.sub(r'```\n?', '', script_content)
            script_content = script_content.strip()
            
            return script_content
            
        except Exception as e:
            logger.warning(f"LLM脚本生成失败，降级到模板生成: {str(e)}")
            return self._generate_script_with_template(steps, script_type, script_language, page_url)

