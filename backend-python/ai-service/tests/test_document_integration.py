"""
文档处理集成测试
测试DocumentParserService的Word、PDF、TXT等文档解析功能
"""

import pytest
import os
import tempfile
from pathlib import Path
from sqlalchemy.orm import Session

from app.services.document_parser_service import DocumentParserService


# ============================================================================
# 文档处理集成测试
# ============================================================================


class TestDocumentParserIntegration:
    """文档解析服务集成测试类"""

    def test_document_parser_initialization(self, test_db: Session):
        """测试DocumentParserService初始化"""
        service = DocumentParserService()

        assert service is not None
        assert isinstance(service.supported_formats, list)
        assert len(service.supported_formats) > 0

    def test_supported_formats(self, test_db: Session):
        """测试支持的格式列表"""
        service = DocumentParserService()

        # 检查基本格式总是支持
        assert "txt" in service.supported_formats
        assert "csv" in service.supported_formats
        assert "html" in service.supported_formats
        assert "htm" in service.supported_formats

    def test_parse_text_file(self, test_db: Session):
        """测试解析纯文本文件"""
        service = DocumentParserService()

        # 创建临时文本文件
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="utf-8"
        ) as f:
            f.write("这是测试文本\n第二行内容\n第三行内容")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert result["content"] is not None
            assert "这是测试文本" in result["content"]
            assert result["char_count"] > 0
            # encoding字段在metadata中
            assert result.get("metadata", {}).get("encoding") == "utf-8"
        finally:
            os.unlink(temp_file)

    def test_parse_text_file_different_encodings(self, test_db: Session):
        """测试解析不同编码的文本文件"""
        service = DocumentParserService()

        # 测试GBK编码
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="gbk"
        ) as f:
            f.write("测试GBK编码")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)
            assert result["content"] is not None
            assert "测试GBK编码" in result["content"]
        finally:
            os.unlink(temp_file)

    def test_parse_csv_file(self, test_db: Session):
        """测试解析CSV文件"""
        service = DocumentParserService()

        # 创建临时CSV文件
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".csv", delete=False, encoding="utf-8"
        ) as f:
            f.write("name,age,city\n")
            f.write("张三,25,北京\n")
            f.write("李四,30,上海\n")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert result["content"] is not None
            assert "张三" in result["content"]
            assert result["row_count"] == 3  # 包括标题行
            assert "headers" in result["structure"]
            assert result["structure"]["column_count"] == 3
        finally:
            os.unlink(temp_file)

    def test_parse_html_file(self, test_db: Session):
        """测试解析HTML文件"""
        service = DocumentParserService()

        # 创建临时HTML文件
        html_content = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>测试页面</title>
        </head>
        <body>
            <h1>主标题</h1>
            <p>这是段落内容</p>
            <a href="https://example.com">链接</a>
            <table>
                <tr>
                    <th>列1</th>
                    <th>列2</th>
                </tr>
                <tr>
                    <td>数据1</td>
                    <td>数据2</td>
                </tr>
            </table>
        </body>
        </html>
        """

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".html", delete=False, encoding="utf-8"
        ) as f:
            f.write(html_content)
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert result["content"] is not None
            assert "主标题" in result["content"]
            assert "段落内容" in result["content"]
            assert result["structure"]["title"] == "测试页面"
            assert len(result["structure"]["headings"]) > 0
        finally:
            os.unlink(temp_file)

    def test_extract_structure_from_text(self, test_db: Session):
        """测试从文本中提取结构"""
        service = DocumentParserService()

        paragraphs = [
            "文档标题",
            "第一章：介绍",
            "这是第一章的内容",
            "第二章：正文",
            "这是第二章的内容",
        ]

        structure = service._extract_structure(paragraphs)

        assert structure is not None
        assert structure["title"] == "文档标题"
        assert "headings" in structure
        assert isinstance(structure["headings"], list)

    def test_extract_key_info(self, test_db: Session):
        """测试提取关键信息"""
        service = DocumentParserService()

        content = "这是一个测试文档。测试文档用于验证文档解析功能。关键词包括测试、文档、解析。"

        key_info = service.extract_key_info(content)

        assert key_info is not None
        assert "keywords" in key_info
        assert "content_length" in key_info
        assert key_info["content_length"] > 0
        assert "sentence_count" in key_info

    def test_parse_nonexistent_file(self, test_db: Session):
        """测试解析不存在的文件"""
        service = DocumentParserService()

        with pytest.raises(FileNotFoundError):
            service.parse_document("/nonexistent/file.txt")

    def test_parse_unsupported_format(self, test_db: Session):
        """测试解析不支持的格式"""
        service = DocumentParserService()

        # 创建一个不支持的扩展名文件
        with tempfile.NamedTemporaryFile(mode="w", suffix=".xyz", delete=False) as f:
            f.write("test content")
            temp_file = f.name

        try:
            with pytest.raises(ValueError):
                service.parse_document(temp_file)
        finally:
            os.unlink(temp_file)

    def test_extract_markdown_structure(self, test_db: Session):
        """测试提取Markdown结构"""
        service = DocumentParserService()

        md_content = """
# 主标题

## 子标题1

内容1

## 子标题2

内容2

```python
def hello():
    print("Hello, World!")
```

[链接文本](https://example.com)
        """

        structure = service._extract_markdown_structure(md_content)

        assert structure is not None
        assert structure["title"] == "主标题"
        assert len(structure["headings"]) > 0
        assert structure["code_blocks"] >= 1
        assert structure["links"] >= 1

    def test_extract_tables_from_html(self, test_db: Session):
        """测试从HTML中提取表格"""
        service = DocumentParserService()

        from bs4 import BeautifulSoup

        html = """
        <table>
            <tr>
                <th>姓名</th>
                <th>年龄</th>
            </tr>
            <tr>
                <td>张三</td>
                <td>25</td>
            </tr>
            <tr>
                <td>李四</td>
                <td>30</td>
            </tr>
        </table>
        """

        soup = BeautifulSoup(html, "html.parser")
        tables = service._extract_tables_from_html(soup)

        assert len(tables) == 1
        assert tables[0]["headers"] == ["姓名", "年龄"]
        assert tables[0]["row_count"] == 3
        assert tables[0]["column_count"] == 2

    def test_detect_language_chinese(self, test_db: Session):
        """测试检测中文语言"""
        service = DocumentParserService()

        chinese_text = "这是中文文本"
        language = service._detect_language(chinese_text)

        assert language == "zh"

    def test_detect_language_english(self, test_db: Session):
        """测试检测英文语言"""
        service = DocumentParserService()

        english_text = "This is English text"
        language = service._detect_language(english_text)

        assert language == "en"

    def test_detect_language_mixed(self, test_db: Session):
        """测试检测混合语言"""
        service = DocumentParserService()

        mixed_text = "这是中文 and this is English"
        language = service._detect_language(mixed_text)

        # 混合文本应该返回mixed或zh/en
        assert language in ["zh", "en", "mixed"]

    def test_parse_text_with_empty_file(self, test_db: Session):
        """测试解析空文本文件"""
        service = DocumentParserService()

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="utf-8"
        ) as f:
            f.write("")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert result["content"] == ""
            assert result["char_count"] == 0
        finally:
            os.unlink(temp_file)

    def test_get_file_metadata(self, test_db: Session):
        """测试获取文件元数据"""
        service = DocumentParserService()

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="utf-8"
        ) as f:
            f.write("测试内容")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert "metadata" in result
            assert result["metadata"]["title"] is not None
            assert result["metadata"]["file_size"] > 0
            assert "created_time" in result["metadata"]
            assert "modified_time" in result["metadata"]
        finally:
            os.unlink(temp_file)


# ============================================================================
# 边界情况和错误处理测试
# ============================================================================


class TestDocumentParserEdgeCases:
    """文档解析边界情况测试"""

    def test_parse_text_with_special_characters(self, test_db: Session):
        """测试解析包含特殊字符的文本"""
        service = DocumentParserService()

        special_content = (
            "测试特殊字符：!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`\n换行符\t制表符"
        )

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="utf-8"
        ) as f:
            f.write(special_content)
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            assert result["content"] is not None
            assert "!@#$%" in result["content"]
        finally:
            os.unlink(temp_file)

    def test_parse_text_multiline(self, test_db: Session):
        """测试解析多行文本"""
        service = DocumentParserService()

        lines = [f"这是第{i}行" for i in range(100)]
        content = "\n".join(lines)

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".txt", delete=False, encoding="utf-8"
        ) as f:
            f.write(content)
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            # line_count可能在metadata中或通过structure计算
            assert result["char_count"] > 0
            assert len(result["content"].split("\n")) == 100
        finally:
            os.unlink(temp_file)

    def test_parse_csv_empty_file(self, test_db: Session):
        """测试解析空CSV文件"""
        service = DocumentParserService()

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".csv", delete=False, encoding="utf-8"
        ) as f:
            f.write("")
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            # 空CSV应该有结果，但可能没有行
            assert "content" in result
        finally:
            os.unlink(temp_file)

    def test_parse_html_with_nested_tables(self, test_db: Session):
        """测试解析包含嵌套表格的HTML"""
        service = DocumentParserService()

        html = """
        <table>
            <tr>
                <td>
                    <table>
                        <tr>
                            <th>嵌套表头</th>
                        </tr>
                        <tr>
                            <td>嵌套数据</td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        """

        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".html", delete=False, encoding="utf-8"
        ) as f:
            f.write(html)
            temp_file = f.name

        try:
            result = service.parse_document(temp_file)

            # 应该能够解析，即使有嵌套表格
            assert result["content"] is not None
        finally:
            os.unlink(temp_file)
