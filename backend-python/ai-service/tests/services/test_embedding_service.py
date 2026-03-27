"""
嵌入服务测试
"""

from unittest.mock import Mock, patch

import pytest

from app.services.embedding_service import EmbeddingService


@pytest.fixture
def db_session():
    return Mock()


@pytest.fixture
def embedding_service(db_session):
    return EmbeddingService(db_session)


class TestEmbeddingService:
    def test_get_embedding_local_success(self, embedding_service):
        embedding_service.provider = "local"

        with patch("app.services.embedding_service._get_local_model") as mock_get_local_model:
            mock_model = Mock()
            mock_embedding = Mock()
            mock_embedding.tolist.return_value = [0.1, 0.2, 0.3]
            mock_model.encode.return_value = mock_embedding
            mock_get_local_model.return_value = (mock_model, "sentence-transformers")

            result = embedding_service.get_embedding("测试文本")

        assert result == [0.1, 0.2, 0.3]

    def test_batch_get_embeddings_local_success(self, embedding_service):
        embedding_service.provider = "local"

        with patch("app.services.embedding_service._get_local_model") as mock_get_local_model:
            mock_model = Mock()
            emb1 = Mock()
            emb1.tolist.return_value = [0.1, 0.2]
            emb2 = Mock()
            emb2.tolist.return_value = [0.3, 0.4]
            mock_model.encode.return_value = [emb1, emb2]
            mock_get_local_model.return_value = (mock_model, "sentence-transformers")

            results = embedding_service.batch_get_embeddings(["文本1", "文本2"])

        assert results == [[0.1, 0.2], [0.3, 0.4]]

    def test_cosine_similarity(self, embedding_service):
        similarity = embedding_service.cosine_similarity(
            [1.0, 0.0],
            [1.0, 0.0]
        )

        assert similarity == pytest.approx(1.0)
