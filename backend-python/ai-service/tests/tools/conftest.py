"""
工具测试公共 fixture 与辅助函数（第四阶段 4.4）
"""
import pytest
from typing import Dict, Any, Optional

from app.services.agent_engine import BaseTool


@pytest.fixture
def tool_context() -> Dict[str, Any]:
    """默认工具执行上下文（如 user_id、session_id 等）。"""
    return {"user_id": 1, "session_id": "test-session"}


def run_tool(
    tool: BaseTool,
    arguments: Dict[str, Any],
    context: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    """
    执行工具并返回结果，用于单测。
    
    Args:
        tool: BaseTool 实例
        arguments: 工具参数
        context: 执行上下文，可选
        
    Returns:
        execute 的返回值
    """
    return tool.execute(arguments, context or {})
