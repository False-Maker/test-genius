"""
知识库服务测试
"""

import pytest
from unittest.mock import Mock, patch
from sqlalchemy import text
from app.services.knowledge_base_service import KnowledgeBaseService


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    return Mock()


@pytest.fixture
def kb_service(db_session):
    """知识库服务fixture"""
    return KnowledgeBaseService(db_session)


class TestKnowledgeBaseService:
    """测试KnowledgeBaseService"""

    def test_create_knowledge_base(self, kb_service, db_session):
        """测试创建知识库"""
        db_session.execute.return_value.inserted_primary_key = [1]

        result = kb_service.create_knowledge_base(
            name="测试知识库", description="测试描述", user_id=1
        )

        assert result > 0
        assert db_session.commit.called

    def test_get_knowledge_base(self, kb_service, db_session):
        """测试获取知识库"""
        mock_result = Mock()
        mock_result.fetchone.return_value = (1, "测试知识库", "描述", 1)
        db_session.execute.return_value = mock_result

        result = kb_service.get_knowledge_base(1)

        assert result is not None
        assert result["name"] == "测试知识库"

    def test_delete_knowledge_base(self, kb_service, db_session):
        """测试删除知识库"""
        kb_service.delete_knowledge_base(1)

        assert db_session.execute.called
        assert db_session.commit.called

    def test_add_documents(self, kb_service, db_session):
        """测试添加文档"""
        db_session.execute.return_value.inserted_primary_key = [1]

        result = kb_service.add_documents(
            kb_id=1,
            documents=[
                {"content": "文档1内容", "metadata": {"title": "文档1"}},
                {"content": "文档2内容", "metadata": {"title": "文档2"}},
            ],
        )

        assert len(result) > 0

    def test_search_knowledge_base(self, kb_service, db_session):
        """测试搜索知识库"""
        mock_result = Mock()
        mock_result.fetchall.return_value = [
            (1, "匹配的内容1", 0.95),
            (2, "匹配的内容2", 0.85),
        ]
        db_session.execute.return_value = mock_result

        results = kb_service.search_knowledge_base(kb_id=1, query="测试查询", limit=5)

        assert len(results) > 0

    def test_update_knowledge_base(self, kb_service, db_session):
        """测试更新知识库"""
        kb_service.update_knowledge_base(kb_id=1, name="新名称", description="新描述")

        assert db_session.execute.called
        assert db_session.commit.called

    def test_get_statistics(self, kb_service, db_session):
        """测试获取知识库统计"""
        mock_result = Mock()
        mock_result.fetchone.return_value = (100,)  # 100个文档
        db_session.execute.return_value = mock_result

        stats = kb_service.get_statistics(1)

        assert stats["document_count"] == 100
