"""
文本分块服务测试
"""

import pytest
from app.services.text_chunking_service import TextChunkingService, ChunkingStrategy


@pytest.fixture
def chunking_service():
    """文本分块服务fixture"""
    return TextChunkingService()


class TestTextChunkingService:
    """测试TextChunkingService"""

    def test_chunk_by_fixed_size(self):
        """测试按固定长度分块"""
        text = "这是一段很长的文本内容。" * 50
        chunking_service = TextChunkingService(strategy=ChunkingStrategy.FIXED_SIZE, chunk_size=100, chunk_overlap=20)
        chunks = chunking_service.chunk_by_fixed_size(text)

        assert len(chunks) > 1
        assert all("content" in chunk for chunk in chunks)

    def test_chunk_by_paragraphs(self, chunking_service):
        """测试按段落分块"""
        text = """第一段内容。

第二段内容。

第三段内容。"""

        chunks = chunking_service.chunk_by_paragraph(text)

        assert len(chunks) == 3
        assert "第一段" in chunks[0]["content"]

    def test_chunk_by_sentences(self):
        """测试按句子分块"""
        text = "这是第一句。这是第二句！这是第三句？"
        chunking_service = TextChunkingService(
            strategy=ChunkingStrategy.SENTENCE,
            chunk_size=10,
            chunk_overlap=0,
            min_chunk_size=1
        )

        chunks = chunking_service.chunk_by_sentence(text)

        assert len(chunks) >= 1
        assert all("content" in chunk for chunk in chunks)

    def test_chunk_empty_text(self):
        """测试空文本分块"""
        chunking_service = TextChunkingService(strategy=ChunkingStrategy.FIXED_SIZE, chunk_size=100)
        chunks = chunking_service.chunk_text("")

        assert len(chunks) == 0

    def test_chunk_short_text(self):
        """测试短文本分块"""
        text = "短文本"
        chunking_service = TextChunkingService(
            strategy=ChunkingStrategy.FIXED_SIZE,
            chunk_size=100,
            min_chunk_size=1
        )
        chunks = chunking_service.chunk_by_fixed_size(text)

        assert len(chunks) == 1
        assert chunks[0]["content"] == text

    def test_semantic_chunking(self):
        """测试语义分块"""
        text = """这是关于Python的主题。Python是一门编程语言。
        
现在切换到JavaScript的主题。JavaScript也是一门编程语言。
        
最后讨论Rust。Rust是系统编程语言。"""
        chunking_service = TextChunkingService(strategy=ChunkingStrategy.SEMANTIC)

        chunks = chunking_service.chunk_by_semantic(text)

        assert len(chunks) >= 1
        assert all("content" in chunk for chunk in chunks)
