"""
页面代码解析服务
解析HTML/CSS/JavaScript代码，提取页面元素信息
"""
import re
import json
import logging
from typing import List, Dict, Optional, Any
from bs4 import BeautifulSoup
from urllib.parse import urlparse

logger = logging.getLogger(__name__)


class PageParserService:
    """页面代码解析服务"""
    
    def __init__(self):
        """初始化页面解析服务"""
        pass
    
    def parse_html(self, html_content: str, page_url: Optional[str] = None) -> Dict[str, Any]:
        """
        解析HTML内容，提取页面元素信息
        
        Args:
            html_content: HTML内容
            page_url: 页面URL（可选）
            
        Returns:
            包含页面元素信息的字典：
            - elements: 元素列表
            - structure: 页面结构
            - metadata: 页面元数据
        """
        try:
            soup = BeautifulSoup(html_content, 'html.parser')
            
            # 提取页面元数据
            metadata = self._extract_metadata(soup, page_url)
            
            # 提取所有交互元素
            elements = self._extract_elements(soup)
            
            # 提取页面结构
            structure = self._extract_structure(soup)
            
            logger.info(f"页面解析完成: 提取了 {len(elements)} 个元素")
            
            return {
                "elements": elements,
                "structure": structure,
                "metadata": metadata,
                "element_count": len(elements)
            }
            
        except Exception as e:
            logger.error(f"HTML解析失败: {str(e)}")
            raise ValueError(f"HTML解析失败: {str(e)}")
    
    def parse_page_code(self, page_code_url: str) -> Dict[str, Any]:
        """
        解析页面代码（从URL或文件路径）
        
        Args:
            page_code_url: 页面代码URL或文件路径
            
        Returns:
            包含页面元素信息的字典
        """
        try:
            # 判断是URL还是文件路径
            parsed_url = urlparse(page_code_url)
            
            if parsed_url.scheme in ['http', 'https']:
                # 从URL读取
                import requests
                response = requests.get(page_code_url, timeout=10)
                response.raise_for_status()
                html_content = response.text
            else:
                # 从文件路径读取
                with open(page_code_url, 'r', encoding='utf-8') as f:
                    html_content = f.read()
            
            return self.parse_html(html_content, page_code_url)
            
        except FileNotFoundError:
            logger.error(f"文件不存在: {page_code_url}")
            raise ValueError(f"文件不存在: {page_code_url}")
        except Exception as e:
            logger.error(f"页面代码解析失败: {str(e)}")
            raise ValueError(f"页面代码解析失败: {str(e)}")
    
    def _extract_metadata(self, soup: BeautifulSoup, page_url: Optional[str] = None) -> Dict[str, Any]:
        """提取页面元数据"""
        metadata = {
            "title": "",
            "url": page_url or "",
            "description": "",
            "keywords": []
        }
        
        # 提取标题
        title_tag = soup.find('title')
        if title_tag:
            metadata["title"] = title_tag.get_text(strip=True)
        
        # 提取meta描述
        meta_desc = soup.find('meta', attrs={'name': 'description'})
        if meta_desc:
            metadata["description"] = meta_desc.get('content', '')
        
        # 提取meta关键词
        meta_keywords = soup.find('meta', attrs={'name': 'keywords'})
        if meta_keywords:
            keywords_str = meta_keywords.get('content', '')
            metadata["keywords"] = [k.strip() for k in keywords_str.split(',') if k.strip()]
        
        return metadata
    
    def _extract_elements(self, soup: BeautifulSoup) -> List[Dict[str, Any]]:
        """提取所有交互元素"""
        elements = []
        
        # 提取按钮
        buttons = soup.find_all(['button', 'input'], type=['button', 'submit', 'reset'])
        for btn in buttons:
            element = self._extract_element_info(btn, 'BUTTON')
            if element:
                elements.append(element)
        
        # 提取输入框
        inputs = soup.find_all('input', type=['text', 'password', 'email', 'number', 'tel', 'url', 'search'])
        for inp in inputs:
            element = self._extract_element_info(inp, 'INPUT')
            if element:
                elements.append(element)
        
        # 提取文本域
        textareas = soup.find_all('textarea')
        for ta in textareas:
            element = self._extract_element_info(ta, 'TEXTAREA')
            if element:
                elements.append(element)
        
        # 提取链接
        links = soup.find_all('a', href=True)
        for link in links:
            element = self._extract_element_info(link, 'LINK')
            if element:
                elements.append(element)
        
        # 提取下拉框
        selects = soup.find_all('select')
        for sel in selects:
            element = self._extract_element_info(sel, 'SELECT')
            if element:
                elements.append(element)
        
        # 提取复选框和单选框
        checkboxes = soup.find_all('input', type=['checkbox', 'radio'])
        for cb in checkboxes:
            element = self._extract_element_info(cb, 'CHECKBOX' if cb.get('type') == 'checkbox' else 'RADIO')
            if element:
                elements.append(element)
        
        return elements
    
    def _extract_element_info(self, element, element_type: str) -> Optional[Dict[str, Any]]:
        """提取单个元素信息"""
        try:
            # 获取元素文本
            element_text = element.get_text(strip=True)
            if not element_text and element.name == 'input':
                element_text = element.get('value', '') or element.get('placeholder', '')
            
            # 生成定位策略
            locators = self._generate_locators(element)
            
            # 提取元素属性
            attributes = {}
            for attr in element.attrs:
                if attr not in ['id', 'class', 'name', 'type', 'value', 'href']:
                    attributes[attr] = element.get(attr)
            
            element_info = {
                "element_type": element_type,
                "element_text": element_text,
                "locators": locators,
                "attributes": attributes,
                "tag_name": element.name
            }
            
            # 添加特定属性
            if element.get('id'):
                element_info["id"] = element.get('id')
            if element.get('name'):
                element_info["name"] = element.get('name')
            if element.get('class'):
                element_info["class"] = element.get('class')
            if element.get('href'):
                element_info["href"] = element.get('href')
            if element.get('type'):
                element_info["type"] = element.get('type')
            
            return element_info
            
        except Exception as e:
            logger.warning(f"提取元素信息失败: {str(e)}")
            return None
    
    def _generate_locators(self, element) -> Dict[str, str]:
        """生成元素定位策略"""
        locators = {}
        
        # ID定位（优先级最高）
        if element.get('id'):
            locators['id'] = element.get('id')
            locators['css_selector'] = f"#{element.get('id')}"
        
        # Name定位
        if element.get('name'):
            locators['name'] = element.get('name')
            if 'css_selector' not in locators:
                locators['css_selector'] = f"[name='{element.get('name')}']"
        
        # Class定位
        if element.get('class'):
            classes = element.get('class')
            if isinstance(classes, list):
                class_str = '.'.join(classes)
            else:
                class_str = classes
            if 'css_selector' not in locators:
                locators['css_selector'] = f".{class_str}"
            locators['class'] = class_str
        
        # XPath定位（作为备选）
        xpath = self._generate_xpath(element)
        if xpath:
            locators['xpath'] = xpath
        
        # 文本定位（如果元素有文本）
        element_text = element.get_text(strip=True)
        if element_text and len(element_text) < 50:  # 文本不太长
            locators['text'] = element_text
        
        return locators
    
    def _generate_xpath(self, element) -> Optional[str]:
        """生成XPath定位表达式"""
        try:
            # 简单的XPath生成
            parts = []
            current = element
            
            while current and current.name:
                tag = current.name
                index = 1
                sibling = current.previous_sibling
                while sibling:
                    if sibling.name == tag:
                        index += 1
                    sibling = sibling.previous_sibling
                
                if index == 1:
                    parts.append(tag)
                else:
                    parts.append(f"{tag}[{index}]")
                
                current = current.parent
                if current and current.name == 'html':
                    break
            
            if parts:
                return '/' + '/'.join(reversed(parts))
            return None
            
        except Exception:
            return None
    
    def _extract_structure(self, soup: BeautifulSoup) -> Dict[str, Any]:
        """提取页面结构"""
        structure = {
            "sections": [],
            "forms": [],
            "navigation": []
        }
        
        # 提取表单
        forms = soup.find_all('form')
        for form in forms:
            form_info = {
                "id": form.get('id', ''),
                "name": form.get('name', ''),
                "action": form.get('action', ''),
                "method": form.get('method', 'GET'),
                "fields": []
            }
            
            # 提取表单字段
            inputs = form.find_all(['input', 'select', 'textarea'])
            for inp in inputs:
                field_info = {
                    "name": inp.get('name', ''),
                    "type": inp.get('type', inp.name),
                    "required": inp.has_attr('required')
                }
                form_info["fields"].append(field_info)
            
            structure["forms"].append(form_info)
        
        # 提取导航结构
        navs = soup.find_all(['nav', 'ul', 'ol'], class_=re.compile(r'nav|menu', re.I))
        for nav in navs[:5]:  # 最多提取5个导航
            nav_info = {
                "type": nav.name,
                "items": []
            }
            links = nav.find_all('a', href=True)
            for link in links:
                nav_info["items"].append({
                    "text": link.get_text(strip=True),
                    "href": link.get('href', '')
                })
            structure["navigation"].append(nav_info)
        
        return structure

