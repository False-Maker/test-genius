"""
EchoTool 单测（第四阶段 4.4）
演示工具测试框架用法与结果契约断言。
"""
import pytest
from app.services.agent_tools.demo_tools import EchoTool
from tests.tools.conftest import run_tool, tool_context


@pytest.fixture
def echo_tool():
    return EchoTool()


def test_echo_missing_message(echo_tool):
    """必填参数 message 缺失时，应返回 success=False 且带 error。"""
    result = run_tool(echo_tool, {})
    assert result.get("success") is False
    assert "error" in result
    assert "message" in result["error"].lower() or "空" in result["error"]


def test_echo_empty_message(echo_tool):
    """message 为空字符串时，应返回 success=False。"""
    result = run_tool(echo_tool, {"message": "   "})
    assert result.get("success") is False
    assert "error" in result


def test_echo_success(echo_tool, tool_context):
    """正常调用应返回 success=True 且 output 为输入内容。"""
    result = run_tool(echo_tool, {"message": "hello"}, tool_context)
    assert result.get("success") is True
    assert result.get("output") == "hello"
    assert result.get("uppercase") is False


def test_echo_uppercase(echo_tool):
    """uppercase=True 时，output 为大写。"""
    result = run_tool(echo_tool, {"message": "hello", "uppercase": True})
    assert result.get("success") is True
    assert result.get("output") == "HELLO"
    assert result.get("uppercase") is True


def test_echo_schema(echo_tool):
    """Schema 应包含 name、description、parameters。"""
    schema = echo_tool.get_schema()
    assert schema.get("name") == "echo"
    assert "description" in schema
    params = schema.get("parameters", {})
    assert params.get("type") == "object"
    assert "message" in params.get("properties", {})
    assert "message" in params.get("required", [])
