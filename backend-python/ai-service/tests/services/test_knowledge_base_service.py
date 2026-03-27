"""
知识库服务测试
"""

from unittest.mock import Mock, call, patch

import pytest

from app.services.knowledge_base_service import KnowledgeBaseService


@pytest.fixture
def db_session():
    """数据库会话 fixture"""
    return Mock()


@pytest.fixture
def kb_service(db_session):
    """知识库服务 fixture"""
    with patch("app.services.knowledge_base_service.EmbeddingService") as mock_embedding_service_cls:
        mock_embedding_service = Mock()
        mock_embedding_service_cls.return_value = mock_embedding_service
        service = KnowledgeBaseService(db_session)
        service.embedding_service = mock_embedding_service
        return service


class TestKnowledgeBaseService:
    """测试当前实际存在的 KnowledgeBaseService 能力。"""

    def test_add_document_persists_kb_id_and_embedding(self, kb_service, db_session):
        kb_service.embedding_service.get_embedding.return_value = [0.1, 0.2]

        mock_result = Mock()
        mock_result.fetchone.return_value = [12]
        db_session.execute.return_value = mock_result

        result = kb_service.add_document(
            kb_id=2,
            doc_code="DOC-001",
            doc_name="测试文档",
            doc_type="测试规范",
            doc_content="文档内容",
            doc_category="分类",
            doc_url="http://example.com/doc",
            creator_id=1
        )

        assert result == 12
        assert db_session.commit.called

        args, _ = db_session.execute.call_args
        params = args[1]
        assert params["kb_id"] == 2
        assert params["doc_code"] == "DOC-001"
        assert params["embedding"] == str([0.1, 0.2])

    def test_count_documents_missing_embeddings_supports_kb_filter(self, kb_service, db_session):
        mock_result = Mock()
        mock_result.scalar.return_value = 3
        db_session.execute.return_value = mock_result

        result = kb_service.count_documents_missing_embeddings(kb_id=2)

        assert result == 3
        args, _ = db_session.execute.call_args
        params = args[1]
        assert params["kb_id"] == 2

    def test_backfill_missing_embeddings_batches_until_remaining_zero(self, kb_service):
        kb_service.count_documents_missing_embeddings = Mock(side_effect=[5, 0])
        kb_service._backfill_missing_embeddings = Mock(side_effect=[2, 2, 1])

        result = kb_service.backfill_missing_embeddings(batch_size=2)

        assert result == {
            "before_count": 5,
            "updated_count": 5,
            "remaining_count": 0,
            "batches": 3
        }
        kb_service._backfill_missing_embeddings.assert_has_calls([
            call(limit=2, kb_id=None),
            call(limit=2, kb_id=None),
            call(limit=2, kb_id=None)
        ])

    def test_backfill_missing_embeddings_stops_when_batch_limit_reached(self, kb_service):
        kb_service.count_documents_missing_embeddings = Mock(side_effect=[9, 5])
        kb_service._backfill_missing_embeddings = Mock(side_effect=[2, 2])

        result = kb_service.backfill_missing_embeddings(batch_size=2, max_batches=2, kb_id=7)

        assert result == {
            "before_count": 9,
            "updated_count": 4,
            "remaining_count": 5,
            "batches": 2
        }
        kb_service._backfill_missing_embeddings.assert_has_calls([
            call(limit=2, kb_id=7),
            call(limit=2, kb_id=7)
        ])

    def test_search_by_semantic_triggers_limited_backfill(self, kb_service, db_session):
        kb_service.embedding_service.get_embedding.return_value = [0.1, 0.2]
        kb_service._backfill_missing_embeddings = Mock(return_value=1)

        mock_result = Mock()
        mock_result.fetchall.return_value = [
            (1, "DOC-001", "测试文档", "测试规范", "分类", "文档内容", None, "2026-03-26T00:00:00", 0.91)
        ]
        db_session.execute.return_value = mock_result

        result = kb_service.search_by_semantic("测试查询", top_k=5, similarity_threshold=0.7)

        assert len(result) == 1
        assert result[0]["doc_code"] == "DOC-001"
        assert result[0]["similarity"] == 0.91
        kb_service._backfill_missing_embeddings.assert_called_once_with(limit=20)

    def test_search_by_kb_id_triggers_scoped_backfill(self, kb_service, db_session):
        kb_service.embedding_service.get_embedding.return_value = [0.1, 0.2]
        kb_service._backfill_missing_embeddings = Mock(return_value=1)

        mock_result = Mock()
        mock_result.fetchall.return_value = [
            (1, "DOC-001", "测试文档", "测试规范", "分类", "文档内容", None, "2026-03-26T00:00:00", 0.88)
        ]
        db_session.execute.return_value = mock_result

        result = kb_service._search_by_kb_id("测试查询", kb_id=2, top_k=3, similarity_threshold=0.7)

        assert len(result) == 1
        assert result[0]["kb_id"] == 2
        kb_service._backfill_missing_embeddings.assert_called_once_with(limit=20, kb_id=2)
