"""
演示 / 测试用工具（第四阶段 4.4）
用于 SDK 示例与单测，不依赖外部 API。
"""
from typing import Dict, Any
from app.services.agent_engine import BaseTool


class EchoTool(BaseTool):
    """回显工具：原样返回输入，用于规范示例与单测。"""

    def __init__(self):
        schema = {
            "name": "echo",
            "description": "回显输入内容，用于演示与测试",
            "parameters": {
                "type": "object",
                "properties": {
                    "message": {"type": "string", "description": "要回显的内容"},
                    "uppercase": {"type": "boolean", "description": "是否转为大写", "default": False},
                },
                "required": ["message"],
            },
        }
        super().__init__("echo", "回显输入", schema)

    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        msg = arguments.get("message")
        if msg is None or (isinstance(msg, str) and not msg.strip()):
            return {"success": False, "error": "message 不能为空"}
        uppercase = arguments.get("uppercase", False)
        out = str(msg).upper() if uppercase else str(msg)
        return {"success": True, "output": out, "uppercase": bool(uppercase)}
