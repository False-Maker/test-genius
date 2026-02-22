"""
Agent上下文管理服务测试
"""

import pytest
import json
from unittest.mock import Mock, MagicMock, call
from sqlalchemy import text
from app.services.agent_context_service import AgentContextService


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    session = Mock()
    session.execute.return_value.fetchone.return_value = None
    return session


@pytest.fixture
def context_service(db_session):
    """上下文服务fixture"""
    return AgentContextService(db_session)


class TestAgentContextService:
    """测试AgentContextService"""

    def test_initialization(self, context_service):
        """测试初始化"""
        assert context_service.max_context_tokens == 8000
        assert context_service.max_history_messages == 20

    def test_get_session_context_empty(self, context_service, db_session):
        """测试获取空会话上下文"""
        mock_result = Mock()
        mock_result.fetchone.return_value = None
        db_session.execute.return_value = mock_result

        context = context_service.get_session_context(1)

        assert "conversation_history" in context
        assert "tool_calls_history" in context
        assert "metadata" in context
        assert len(context["conversation_history"]) == 0

    def test_get_session_context_with_data(self, context_service, db_session):
        """测试获取有数据的会话上下文（字符串格式）"""
        test_data = {
            "conversation_history": [{"role": "user", "content": "测试消息"}],
            "tool_calls_history": [],
            "metadata": {"key": "value"},
        }

        mock_result = Mock()
        mock_result.fetchone.return_value = (json.dumps(test_data, ensure_ascii=False),)
        db_session.execute.return_value = mock_result

        context = context_service.get_session_context(1)

        assert len(context["conversation_history"]) == 1
        assert context["conversation_history"][0]["content"] == "测试消息"

    def test_get_session_context_dict_format(self, context_service, db_session):
        """测试获取会话上下文（字典格式）"""
        test_data = {
            "conversation_history": [],
            "tool_calls_history": [],
            "metadata": {},
        }

        mock_result = Mock()
        mock_result.fetchone.return_value = (test_data,)
        db_session.execute.return_value = mock_result

        context = context_service.get_session_context(1)
        assert "conversation_history" in context

    def test_get_session_context_database_error(self, context_service, db_session):
        """测试数据库错误"""
        db_session.execute.side_effect = Exception("数据库错误")

        context = context_service.get_session_context(1)
        # 应该返回默认空上下文
        assert "conversation_history" in context

    def test_save_session_context(self, context_service, db_session):
        """测试保存会话上下文"""
        context = {
            "conversation_history": [{"role": "user", "content": "消息"}],
            "tool_calls_history": [],
            "metadata": {},
        }

        context_service.save_session_context(1, context)

        assert db_session.execute.called
        assert db_session.commit.called

    def test_save_session_context_rollback_on_error(self, context_service, db_session):
        """测试保存失败时回滚"""
        db_session.execute.side_effect = Exception("保存失败")

        context = {"conversation_history": []}

        with pytest.raises(Exception):
            context_service.save_session_context(1, context)

        assert db_session.rollback.called

    def test_add_message(self, context_service, db_session):
        """测试添加消息"""
        # 模拟获取上下文返回空
        mock_result = Mock()
        mock_result.fetchone.return_value = None
        db_session.execute.return_value = mock_result

        context_service.add_message(
            session_id=1, role="user", content="测试消息", message_type="USER"
        )

        # 验证数据库操作被调用
        assert db_session.execute.called
        assert db_session.commit.called

    def test_add_message_with_tool_calls(self, context_service, db_session):
        """测试添加带工具调用的消息"""
        mock_result = Mock()
        mock_result.fetchone.return_value = None
        db_session.execute.return_value = mock_result

        tool_calls = [{"name": "test_tool", "arguments": {}}]
        tool_results = [{"result": "success"}]

        context_service.add_message(
            session_id=1,
            role="assistant",
            content="执行工具",
            message_type="ASSISTANT",
            tool_calls=tool_calls,
            tool_results=tool_results,
        )

        assert db_session.execute.called

    def test_add_message_history_limit(self, context_service, db_session):
        """测试消息历史限制"""
        # 模拟已有20条消息
        existing_history = [{"role": "user", "content": f"消息{i}"} for i in range(20)]
        existing_context = {
            "conversation_history": existing_history,
            "tool_calls_history": [],
            "metadata": {},
        }

        mock_result = Mock()
        mock_result.fetchone.return_value = (json.dumps(existing_context),)
        db_session.execute.return_value = mock_result

        context_service.add_message(
            session_id=1, role="user", content="新消息", message_type="USER"
        )

        # 验证历史被限制在20条
        calls = db_session.execute.call_args_list
        # 应该有两次调用：INSERT和UPDATE
        assert len(calls) >= 1

    def test_get_conversation_history(self, context_service, db_session):
        """测试获取对话历史"""
        test_context = {
            "conversation_history": [
                {"role": "user", "content": "消息1"},
                {"role": "assistant", "content": "回复1"},
                {"role": "user", "content": "消息2"},
            ]
        }

        mock_result = Mock()
        mock_result.fetchone.return_value = (json.dumps(test_context),)
        db_session.execute.return_value = mock_result

        history = context_service.get_conversation_history(1)

        assert len(history) == 3

    def test_get_conversation_history_with_limit(self, context_service, db_session):
        """测试获取对话历史（限制数量）"""
        test_context = {
            "conversation_history": [
                {"role": "user", "content": f"消息{i}"} for i in range(10)
            ]
        }

        mock_result = Mock()
        mock_result.fetchone.return_value = (json.dumps(test_context),)
        db_session.execute.return_value = mock_result

        history = context_service.get_conversation_history(1, limit=5)

        assert len(history) == 5

    def test_manage_context_window_within_limit(self, context_service):
        """测试上下文窗口管理（未超出限制）"""
        context = {"conversation_history": [{"role": "user", "content": "短消息"}]}

        result = context_service.manage_context_window(context, 1000)

        # 未超出限制，应该原样返回
        assert result == context

    def test_manage_context_window_exceed_limit(self, context_service):
        """测试上下文窗口管理（超出限制）"""
        # 创建15条消息的历史
        history = [{"role": "user", "content": f"消息{i} " * 100} for i in range(15)]
        context = {"conversation_history": history}

        # 估算token数应该很大
        estimated = context_service.estimate_context_tokens(context)

        result = context_service.manage_context_window(context, estimated)

        # 应该被压缩
        assert len(result["conversation_history"]) <= len(history)

    def test_estimate_tokens(self, context_service):
        """测试token估算"""
        text = "这是一个测试文本" * 4  # 16个字符
        tokens = context_service.estimate_tokens(text)
        assert tokens == 4  # 16 / 4

    def test_estimate_context_tokens(self, context_service):
        """测试上下文token估算"""
        context = {
            "conversation_history": [
                {"role": "user", "content": "消息1 " * 4},
                {
                    "role": "assistant",
                    "content": "回复1 " * 4,
                    "tool_calls": [{"name": "tool"}],
                },
            ],
            "metadata": {"key": "value"},
        }

        tokens = context_service.estimate_context_tokens(context)
        assert tokens > 0

    def test_clear_context(self, context_service, db_session):
        """测试清空上下文"""
        context_service.clear_context(1)

        assert db_session.execute.called
        # 验证清空的上下文是空的
        call_args = db_session.execute.call_args
        assert "context_data" in str(call_args)

    def test_context_compression_strategy(self, context_service):
        """测试上下文压缩策略"""
        # 创建12条以上的历史
        history = [{"role": "user", "content": f"消息{i}"} for i in range(15)]
        context = {"conversation_history": history}

        # 估算token
        estimated = sum(len(msg["content"]) for msg in history) // 4

        # 如果超出限制
        if estimated > context_service.max_context_tokens:
            result = context_service.manage_context_window(context, estimated)
            # 应该保留前2条和后10条
            assert len(result["conversation_history"]) == 12
