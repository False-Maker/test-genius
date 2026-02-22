"""
嵌入服务测试
"""

import pytest
from unittest.mock import Mock, patch
from app.services.embedding_service import EmbeddingService


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    return Mock()


@pytest.fixture
def embedding_service(db_session):
    """嵌入服务fixture"""
    return EmbeddingService(db_session)


class TestEmbeddingService:
    """测试EmbeddingService"""

    @patch("app.services.embedding_service SentenceTransformer")
    def test_generate_embedding(self, mock_transformer, embedding_service):
        """测试生成嵌入向量"""
        mock_model = Mock()
        mock_model.encode.return_value = [[0.1, 0.2, 0.3]]
        mock_transformer.return_value = mock_model

        result = embedding_service.generate_embedding("测试文本")

        assert len(result) > 0
        assert result[0] == 0.1

    @patch("app.services.embedding_service SentenceTransformer")
    def test_batch_generate_embeddings(self, mock_transformer, embedding_service):
        """测试批量生成嵌入向量"""
        mock_model = Mock()
        mock_model.encode.return_value = [[0.1, 0.2, 0.3], [0.4, 0.5, 0.6]]
        mock_transformer.return_value = mock_model

        texts = ["文本1", "文本2"]
        results = embedding_service.batch_generate_embeddings(texts)

        assert len(results) == 2

    @patch("app.services.embedding_service SentenceTransformer")
    def test_compute_similarity(self, mock_transformer, embedding_service):
        """测试计算相似度"""
        mock_model = Mock()
        mock_transformer.return_value = mock_model

        # 模拟余弦相似度计算
        with patch("app.services.embedding_service cosine_similarity") as mock_cosine:
            mock_cosine.return_value = [[0.85]]

            similarity = embedding_service.compute_similarity(
                [0.1, 0.2, 0.3], [0.2, 0.3, 0.4]
            )

            assert similarity > 0.8
