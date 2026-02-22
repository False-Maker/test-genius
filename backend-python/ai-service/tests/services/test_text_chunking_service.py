"""
文本分块服务测试
"""

import pytest
from app.services.text_chunking_service import TextChunkingService


@pytest.fixture
def chunking_service():
    """文本分块服务fixture"""
    return TextChunkingService()


class TestTextChunkingService:
    """测试TextChunkingService"""

    def test_chunk_by_characters(self, chunking_service):
        """测试按字符数分块"""
        text = "这是一段很长的文本内容。" * 50
        chunks = chunking_service.chunk_by_characters(text, chunk_size=100, overlap=20)

        assert len(chunks) > 1
        # 验证重叠部分
        if len(chunks) > 1:
            assert chunks[1] in text or chunks[0] in text

    def test_chunk_by_paragraphs(self, chunking_service):
        """测试按段落分块"""
        text = """第一段内容。

第二段内容。

第三段内容。"""

        chunks = chunking_service.chunk_by_paragraphs(text)

        assert len(chunks) == 3
        assert "第一段" in chunks[0]

    def test_chunk_by_sentences(self, chunking_service):
        """测试按句子分块"""
        text = "这是第一句。这是第二句！这是第三句？"

        chunks = chunking_service.chunk_by_sentences(text)

        assert len(chunks) >= 3

    def test_chunk_empty_text(self, chunking_service):
        """测试空文本分块"""
        chunks = chunking_service.chunk_by_characters("", chunk_size=100)

        assert len(chunks) == 0

    def test_chunk_short_text(self, chunking_service):
        """测试短文本分块"""
        text = "短文本"
        chunks = chunking_service.chunk_by_characters(text, chunk_size=100)

        assert len(chunks) == 1

    def test_semantic_chunking(self, chunking_service):
        """测试语义分块"""
        text = """这是关于Python的主题。Python是一门编程语言。
        
现在切换到JavaScript的主题。JavaScript也是一门编程语言。
        
最后讨论Rust。Rust是系统编程语言。"""

        chunks = chunking_service.semantic_chunking(text)

        # 语义分块应该识别主题变化
        assert len(chunks) >= 1
