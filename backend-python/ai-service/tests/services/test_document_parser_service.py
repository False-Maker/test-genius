"""
文档解析服务测试
"""

import pytest
from pathlib import Path
from unittest.mock import Mock, patch, MagicMock
from app.services.document_parser_service import DocumentParserService


@pytest.fixture
def document_parser():
    """文档解析器fixture"""
    return DocumentParserService()


class TestDocumentParserService:
    """测试DocumentParserService"""

    def test_initialization(self, document_parser):
        """测试初始化"""
        assert "txt" in document_parser.supported_formats
        assert "csv" in document_parser.supported_formats

    def test_parse_document_file_not_found(self, document_parser):
        """测试解析不存在的文件"""
        with pytest.raises(FileNotFoundError):
            document_parser.parse_document("/nonexistent/file.txt")

    def test_parse_document_unsupported_format(self, document_parser, tmp_path):
        """测试不支持的文件格式"""
        test_file = tmp_path / "test.xyz"
        test_file.write_text("content")

        with pytest.raises(ValueError, match="不支持的文件格式"):
            document_parser.parse_document(str(test_file))

    @patch("app.services.document_parser_service.DOCX_AVAILABLE", False)
    @patch("app.services.document_parser_service.PDF_AVAILABLE", False)
    @patch("app.services.document_parser_service.PPTX_AVAILABLE", False)
    @patch("app.services.document_parser_service.MARKDOWN_AVAILABLE", False)
    @patch("app.services.document_parser_service.HTML_AVAILABLE", False)
    def test_supported_formats_without_dependencies(self):
        """测试无依赖时的支持格式"""
        parser = DocumentParserService()
        # 只有txt和csv应该可用
        assert "txt" in parser.supported_formats
        assert "csv" in parser.supported_formats
        # 其他格式不应该可用
        assert "docx" not in parser.supported_formats

    def test_parse_txt(self, document_parser, tmp_path):
        """测试解析TXT文件"""
        test_file = tmp_path / "test.txt"
        test_content = "这是测试文本\n第二行"
        test_file.write_text(test_content, encoding="utf-8")

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        assert "这是测试文本" in result["content"]

    def test_parse_csv(self, document_parser, tmp_path):
        """测试解析CSV文件"""
        test_file = tmp_path / "test.csv"
        test_content = "Name,Age\nAlice,30\nBob,25"
        test_file.write_text(test_content, encoding="utf-8")

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        assert "Alice" in result["content"]

    @patch("app.services.document_parser_service.DOCX_AVAILABLE", True)
    @patch("app.services.document_parser_service.Document")
    def test_parse_word_success(self, mock_document_class, document_parser, tmp_path):
        """测试解析Word文档（成功）"""
        test_file = tmp_path / "test.docx"
        test_file.write_bytes(b"fake docx")

        # 模拟docx.Document
        mock_doc = Mock()
        mock_para1 = Mock()
        mock_para1.text = "段落1"
        mock_para2 = Mock()
        mock_para2.text = "段落2"
        mock_doc.paragraphs = [mock_para1, mock_para2]
        mock_doc.core_properties.title = "测试标题"
        mock_doc.core_properties.author = "测试作者"
        mock_doc.core_properties.created = None
        mock_doc.core_properties.modified = None

        mock_document_class.return_value = mock_doc

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        assert "段落1" in result["content"]
        assert result["metadata"]["author"] == "测试作者"

    @patch("app.services.document_parser_service.DOCX_AVAILABLE", False)
    def test_parse_word_not_available(self, document_parser, tmp_path):
        """测试解析Word文档（库不可用）"""
        test_file = tmp_path / "test.docx"
        test_file.write_bytes(b"fake docx")

        with pytest.raises(ImportError, match="python-docx未安装"):
            document_parser.parse_document(str(test_file))

    @patch("app.services.document_parser_service.PDF_AVAILABLE", True)
    @patch("app.services.document_parser_service.PyPDF2.PdfReader")
    def test_parse_pdf_success(self, mock_pdf_reader_class, document_parser, tmp_path):
        """测试解析PDF文档（成功）"""
        test_file = tmp_path / "test.pdf"
        test_file.write_bytes(b"fake pdf")

        # 模拟PDF解析
        mock_pdf_reader = Mock()
        mock_page1 = Mock()
        mock_page1.extract_text.return_value = "PDF页面1内容"
        mock_page2 = Mock()
        mock_page2.extract_text.return_value = "PDF页面2内容"
        mock_pdf_reader.pages = [mock_page1, mock_page2]
        mock_pdf_reader.metadata = {"/Title": "PDF标题", "/Author": "PDF作者"}

        mock_pdf_reader_class.return_value = mock_pdf_reader

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        assert "PDF页面1" in result["content"]
        assert result["metadata"]["title"] == "PDF标题"

    @patch("app.services.document_parser_service.PDF_AVAILABLE", False)
    def test_parse_pdf_not_available(self, document_parser, tmp_path):
        """测试解析PDF文档（库不可用）"""
        test_file = tmp_path / "test.pdf"
        test_file.write_bytes(b"fake pdf")

        with pytest.raises(ImportError, match="PyPDF2未安装"):
            document_parser.parse_document(str(test_file))

    @patch("app.services.document_parser_service.MARKDOWN_AVAILABLE", True)
    def test_parse_markdown(self, document_parser, tmp_path):
        """测试解析Markdown文档"""
        test_file = tmp_path / "test.md"
        test_content = "# 标题\n\n内容"
        test_file.write_text(test_content, encoding="utf-8")

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        assert result["content"] == test_content

    @patch("app.services.document_parser_service.HTML_AVAILABLE", True)
    def test_parse_html(self, document_parser, tmp_path):
        """测试解析HTML文档"""
        test_file = tmp_path / "test.html"
        test_content = "<html><body><h1>标题</h1><p>内容</p></body></html>"
        test_file.write_text(test_content, encoding="utf-8")

        result = document_parser.parse_document(str(test_file))

        assert "content" in result
        # HTML应该被转换为纯文本
        assert "标题" in result["content"] or "h1" in result["content"]

    def test_extract_structure(self, document_parser):
        """测试提取文档结构"""
        paragraphs = ["标题1", "子标题", "普通段落"]

        structure = document_parser._extract_structure(paragraphs)

        assert isinstance(structure, dict)

    def test_parse_error_handling(self, document_parser, tmp_path):
        """测试解析错误处理"""
        test_file = tmp_path / "test.txt"
        test_file.write_text("内容")

        # 模拟读取错误
        with patch("builtins.open", side_effect=PermissionError("权限拒绝")):
            with pytest.raises(Exception):
                document_parser.parse_document(str(test_file))
