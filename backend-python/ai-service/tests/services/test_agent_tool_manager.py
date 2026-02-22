"""
工具权限管理服务测试
"""

import pytest
from unittest.mock import Mock, MagicMock
from sqlalchemy import text
from app.services.agent_tool_manager import (
    ToolPermissionManager,
    check_tool_permission,
    ToolPermissionDeniedException,
)


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    return Mock()


@pytest.fixture
def permission_manager(db_session):
    """权限管理器fixture"""
    return ToolPermissionManager(db_session)


class TestToolPermissionManager:
    """测试ToolPermissionManager"""

    def test_initialization(self, permission_manager):
        """测试初始化"""
        assert permission_manager.db is not None

    def test_check_permission_normal_tool_active(self, permission_manager, db_session):
        """测试检查普通工具权限（已启用）"""
        # 模拟数据库查询返回
        mock_result = Mock()
        mock_result.fetchone.return_value = (
            "NORMAL",
            "1",
        )  # permission_level, is_active
        db_session.execute.return_value = mock_result

        result = permission_manager.check_permission("normal_tool")
        assert result is True

    def test_check_permission_normal_tool_inactive(
        self, permission_manager, db_session
    ):
        """测试检查普通工具权限（未启用）"""
        mock_result = Mock()
        mock_result.fetchone.return_value = ("NORMAL", "0")  # 未启用
        db_session.execute.return_value = mock_result

        result = permission_manager.check_permission("normal_tool")
        assert result is False

    def test_check_permission_admin_tool_with_admin_user(
        self, permission_manager, db_session
    ):
        """测试检查管理员工具权限（有管理员权限的用户）"""
        # 工具查询返回
        mock_tool_result = Mock()
        mock_tool_result.fetchone.return_value = ("ADMIN", "1")

        # 用户查询返回
        mock_user_result = Mock()
        mock_user_result.fetchone.return_value = [1]  # COUNT(*) = 1

        db_session.execute.side_effect = [mock_tool_result, mock_user_result]

        result = permission_manager.check_permission("admin_tool", user_id=1)
        assert result is True

    def test_check_permission_admin_tool_without_admin_user(
        self, permission_manager, db_session
    ):
        """测试检查管理员工具权限（无管理员权限的用户）"""
        # 工具查询返回
        mock_tool_result = Mock()
        mock_tool_result.fetchone.return_value = ("ADMIN", "1")

        # 用户查询返回
        mock_user_result = Mock()
        mock_user_result.fetchone.return_value = [0]  # COUNT(*) = 0

        db_session.execute.side_effect = [mock_tool_result, mock_user_result]

        result = permission_manager.check_permission("admin_tool", user_id=2)
        assert result is False

    def test_check_permission_admin_tool_no_user_id(
        self, permission_manager, db_session
    ):
        """测试检查管理员工具权限（未提供用户ID）"""
        mock_result = Mock()
        mock_result.fetchone.return_value = ("ADMIN", "1")
        db_session.execute.return_value = mock_result

        result = permission_manager.check_permission("admin_tool")
        assert result is False

    def test_check_permission_tool_not_found(self, permission_manager, db_session):
        """测试检查不存在的工具权限"""
        mock_result = Mock()
        mock_result.fetchone.return_value = None
        db_session.execute.return_value = mock_result

        result = permission_manager.check_permission("nonexistent_tool")
        assert result is False

    def test_check_permission_database_error(self, permission_manager, db_session):
        """测试数据库错误处理"""
        db_session.execute.side_effect = Exception("数据库连接失败")

        result = permission_manager.check_permission("test_tool")
        assert result is False

    def test_check_permissions_batch(self, permission_manager, db_session):
        """测试批量检查权限"""
        # 模拟每次查询返回不同结果
        results = [("NORMAL", "1"), ("ADMIN", "1"), ("NORMAL", "0"), None]

        mock_result = Mock()
        mock_result.fetchone.side_effect = results
        db_session.execute.return_value = mock_result

        permissions = permission_manager.check_permissions(
            ["tool1", "tool2", "tool3", "tool4"], user_id=1
        )

        assert len(permissions) == 4
        # tool1: NORMAL, active
        assert permissions["tool1"] is True
        # tool2: ADMIN (需要检查用户权限，这里简化为True)
        # tool3: inactive
        # tool4: not found

    def test_get_user_tools_all(self, permission_manager, db_session):
        """测试获取用户所有工具"""
        mock_result = Mock()
        mock_result.__iter__ = Mock(
            return_value=iter(
                [
                    ("tool1", "工具1", "http", "描述1", "NORMAL"),
                    ("tool2", "工具2", "script", "描述2", "ADMIN"),
                ]
            )
        )
        db_session.execute.return_value = mock_result

        # 模拟用户是管理员
        mock_user_result = Mock()
        mock_user_result.fetchone.return_value = ("1",)
        db_session.execute.side_effect = [mock_result, mock_user_result]

        tools = permission_manager.get_user_tools(1)

        assert len(tools) == 2
        assert tools[0]["tool_code"] == "tool1"
        assert tools[1]["tool_code"] == "tool2"

    def test_get_user_tools_normal_user(self, permission_manager, db_session):
        """测试获取普通用户工具（过滤ADMIN级别）"""
        mock_result = Mock()
        mock_result.__iter__ = Mock(
            return_value=iter(
                [
                    ("tool1", "工具1", "http", "描述1", "NORMAL"),
                    ("tool2", "工具2", "script", "描述2", "ADMIN"),
                ]
            )
        )
        db_session.execute.return_value = mock_result

        # 模拟用户不是管理员
        mock_user_result = Mock()
        mock_user_result.fetchone.return_value = ("0",)
        db_session.execute.side_effect = [mock_result, mock_user_result]

        tools = permission_manager.get_user_tools(1)

        # 应该只返回NORMAL级别的工具
        assert len(tools) == 1
        assert tools[0]["tool_code"] == "tool1"

    def test_get_user_tools_with_permission_filter(
        self, permission_manager, db_session
    ):
        """测试按权限级别过滤工具"""
        mock_result = Mock()
        mock_result.__iter__ = Mock(
            return_value=iter(
                [
                    ("tool1", "工具1", "http", "描述1", "NORMAL"),
                ]
            )
        )
        db_session.execute.return_value = mock_result

        mock_user_result = Mock()
        mock_user_result.fetchone.return_value = ("1",)
        db_session.execute.side_effect = [mock_result, mock_user_result]

        tools = permission_manager.get_user_tools(1, permission_level="NORMAL")

        assert len(tools) == 1
        assert tools[0]["permission_level"] == "NORMAL"

    def test_log_tool_usage(self, permission_manager):
        """测试记录工具使用"""
        # 这个方法主要是记录日志，不抛出异常即成功
        permission_manager.log_tool_usage(
            session_id=1,
            tool_code="test_tool",
            user_id=1,
            result_success=True,
            execution_time=100,
        )
        # 如果没有异常则通过


class TestCheckToolPermissionDecorator:
    """测试权限检查装饰器"""

    def test_decorator_with_valid_permission(self, db_session):
        """测试装饰器（有权限）"""
        mock_result = Mock()
        mock_result.fetchone.return_value = ("NORMAL", "1")
        db_session.execute.return_value = mock_result

        @check_tool_permission("test_tool")
        def test_function(arguments, context):
            return "执行成功"

        result = test_function({}, db=db_session)
        assert result == "执行成功"

    def test_decorator_without_db_session(self):
        """测试装饰器（无db会话，直接执行）"""

        @check_tool_permission("test_tool")
        def test_function(arguments, context):
            return "执行成功"

        result = test_function({})
        assert result == "执行成功"

    def test_decorator_without_permission(self, db_session):
        """测试装饰器（无权限）"""
        mock_result = Mock()
        mock_result.fetchone.return_value = ("NORMAL", "0")  # 未启用
        db_session.execute.return_value = mock_result

        @check_tool_permission("test_tool")
        def test_function(arguments, context):
            return "执行成功"

        with pytest.raises(ToolPermissionDeniedException):
            test_function({}, db=db_session)
