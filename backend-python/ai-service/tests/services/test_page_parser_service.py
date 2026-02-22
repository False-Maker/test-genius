"""
页面解析服务测试
"""

import pytest
from unittest.mock import Mock, patch
from app.services.page_parser_service import PageParserService


@pytest.fixture
def page_parser():
    """页面解析器fixture"""
    return PageParserService()


class TestPageParserService:
    """测试PageParserService"""

    def test_initialization(self, page_parser):
        """测试初始化"""
        assert "BUTTON" in page_parser.supported_element_types
        assert "INPUT" in page_parser.supported_element_types
        assert "TEXTAREA" in page_parser.supported_element_types

    def test_parse_html_success(self, page_parser):
        """测试成功解析HTML"""
        html_content = """
        <html>
        <head><title>测试页面</title></head>
        <body>
            <button id="btn1">点击我</button>
            <input type="text" name="username" placeholder="用户名">
            <a href="/home">首页</a>
        </body>
        </html>
        """

        result = page_parser.parse_html(html_content)

        assert "elements" in result
        assert "structure" in result
        assert "metadata" in result
        assert result["metadata"]["title"] == "测试页面"
        assert result["element_count"] > 0

    def test_parse_html_with_page_url(self, page_parser):
        """测试带URL的HTML解析"""
        html_content = "<html><head><title>测试</title></head></html>"
        page_url = "https://example.com/test"

        result = page_parser.parse_html(html_content, page_url)

        assert result["metadata"]["url"] == page_url

    def test_parse_html_empty(self, page_parser):
        """测试解析空HTML"""
        result = page_parser.parse_html("")

        assert "elements" in result
        assert result["element_count"] == 0

    def test_parse_html_invalid(self, page_parser):
        """测试解析无效HTML"""
        with pytest.raises(ValueError, match="HTML解析失败"):
            page_parser.parse_html(None)

    def test_extract_metadata(self, page_parser):
        """测试提取元数据"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <head>
            <title>页面标题</title>
            <meta name="description" content="页面描述">
            <meta name="keywords" content="关键词1, 关键词2, 关键词3">
        </head>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        metadata = page_parser._extract_metadata(soup, "https://test.com")

        assert metadata["title"] == "页面标题"
        assert metadata["description"] == "页面描述"
        assert "关键词1" in metadata["keywords"]
        assert metadata["url"] == "https://test.com"

    def test_extract_elements_buttons(self, page_parser):
        """测试提取按钮元素"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <button id="btn1" class="btn-primary">提交</button>
            <input type="submit" value="保存">
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        elements = page_parser._extract_elements(soup)

        button_elements = [e for e in elements if e.get("type") == "BUTTON"]
        assert len(button_elements) > 0

    def test_extract_elements_inputs(self, page_parser):
        """测试提取输入框元素"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <input type="text" name="username" placeholder="用户名">
            <input type="password" name="password">
            <input type="email" name="email">
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        elements = page_parser._extract_elements(soup)

        input_elements = [e for e in elements if e.get("type") == "INPUT"]
        assert len(input_elements) > 0

    def test_extract_elements_links(self, page_parser):
        """测试提取链接元素"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <a href="/home">首页</a>
            <a href="https://example.com">外部链接</a>
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        elements = page_parser._extract_elements(soup)

        link_elements = [e for e in elements if e.get("type") == "LINK"]
        assert len(link_elements) > 0

    def test_extract_elements_selects(self, page_parser):
        """测试提取下拉框元素"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <select name="country">
                <option value="cn">中国</option>
                <option value="us">美国</option>
            </select>
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        elements = page_parser._extract_elements(soup)

        select_elements = [e for e in elements if e.get("type") == "SELECT"]
        assert len(select_elements) > 0

    def test_extract_elements_checkboxes_and_radios(self, page_parser):
        """测试提取复选框和单选框"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <input type="checkbox" name="agree" value="1">
            <input type="radio" name="gender" value="male">
            <input type="radio" name="gender" value="female">
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        elements = page_parser._extract_elements(soup)

        checkbox_elements = [e for e in elements if e.get("type") == "CHECKBOX"]
        radio_elements = [e for e in elements if e.get("type") == "RADIO"]
        assert len(checkbox_elements) > 0
        assert len(radio_elements) > 0

    def test_extract_structure(self, page_parser):
        """测试提取页面结构"""
        from bs4 import BeautifulSoup

        html = """
        <html>
        <body>
            <div class="container">
                <div class="header">头部</div>
                <div class="content">内容</div>
            </div>
        </body>
        </html>
        """

        soup = BeautifulSoup(html, "html.parser")
        structure = page_parser._extract_structure(soup)

        assert isinstance(structure, dict)

    @patch(
        "builtins.open", new_callable=Mock, read_data="<html><body>测试</body></html>"
    )
    def test_parse_page_code_from_file(self, mock_open, page_parser, tmp_path):
        """测试从文件路径解析页面代码"""
        test_file = tmp_path / "test.html"
        test_file.write_text("<html><body>测试</body></html>", encoding="utf-8")

        result = page_parser.parse_page_code(str(test_file))

        assert "elements" in result

    @patch("requests.get")
    def test_parse_page_code_from_url(self, mock_get, page_parser):
        """测试从URL解析页面代码"""
        mock_response = Mock()
        mock_response.text = "<html><body>远程页面</body></html>"
        mock_response.raise_for_status = Mock()
        mock_get.return_value = mock_response

        result = page_parser.parse_page_code("https://example.com")

        assert "elements" in result
        mock_get.assert_called_once()

    def test_parse_page_code_file_not_found(self, page_parser):
        """测试文件不存在"""
        with pytest.raises(ValueError, match="文件不存在"):
            page_parser.parse_page_code("/nonexistent/file.html")

    @patch("requests.get")
    def test_parse_page_code_http_error(self, mock_get, page_parser):
        """测试HTTP请求失败"""
        mock_get.side_effect = Exception("网络错误")

        with pytest.raises(ValueError, match="页面代码解析失败"):
            page_parser.parse_page_code("https://example.com")
