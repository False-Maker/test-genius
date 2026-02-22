"""
Agent执行引擎测试
"""

import pytest
from unittest.mock import Mock, patch, MagicMock
from app.services.agent_engine import AgentEngine, BaseTool


class MockTool(BaseTool):
    """模拟工具类"""

    def __init__(self):
        super().__init__(
            name="test_tool",
            description="测试工具",
            schema={
                "name": "test_tool",
                "description": "测试工具",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "arg1": {"type": "string"},
                        "arg2": {"type": "integer"},
                    },
                },
            },
        )

    def execute(self, arguments, context=None):
        return {"result": f"执行成功，参数: {arguments}"}


class FailingTool(BaseTool):
    """模拟失败工具类"""

    def __init__(self):
        super().__init__(
            name="failing_tool",
            description="失败工具",
            schema={
                "name": "failing_tool",
                "description": "失败工具",
                "parameters": {},
            },
        )

    def execute(self, arguments, context=None):
        raise Exception("工具执行失败")


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    return Mock()


@pytest.fixture
def agent_config():
    """Agent配置fixture"""
    return {
        "max_iterations": 5,
        "max_tokens": 2000,
        "temperature": 0.7,
        "model_code": "TEST_MODEL",
        "system_prompt": "你是一个测试助手",
        "mode": "function_calling",
    }


@pytest.fixture
def agent_engine(db_session, agent_config):
    """Agent引擎fixture"""
    return AgentEngine(db_session, agent_config)


@pytest.fixture
def mock_tool():
    """模拟工具fixture"""
    return MockTool()


class TestBaseTool:
    """测试BaseTool基类"""

    def test_base_tool_initialization(self):
        """测试BaseTool初始化"""
        tool = MockTool()
        assert tool.name == "test_tool"
        assert tool.description == "测试工具"
        assert "name" in tool.schema

    def test_base_tool_get_schema(self):
        """测试get_schema方法"""
        tool = MockTool()
        schema = tool.get_schema()
        assert schema["name"] == "test_tool"
        assert "parameters" in schema


class TestAgentEngine:
    """测试AgentEngine"""

    def test_agent_engine_initialization(self, agent_engine, agent_config):
        """测试AgentEngine初始化"""
        assert agent_engine.max_iterations == 5
        assert agent_engine.max_tokens == 2000
        assert agent_engine.temperature == 0.7
        assert agent_engine.model_code == "TEST_MODEL"
        assert agent_engine.system_prompt == "你是一个测试助手"
        assert agent_engine.mode == "function_calling"
        assert len(agent_engine.tools) == 0

    def test_register_tool(self, agent_engine, mock_tool):
        """测试注册单个工具"""
        agent_engine.register_tool(mock_tool)
        assert "test_tool" in agent_engine.tools
        assert agent_engine.tools["test_tool"] == mock_tool

    def test_register_tools(self, agent_engine):
        """测试批量注册工具"""
        tool1 = MockTool()
        tool1.name = "tool1"
        tool2 = MockTool()
        tool2.name = "tool2"

        agent_engine.register_tools([tool1, tool2])
        assert "tool1" in agent_engine.tools
        assert "tool2" in agent_engine.tools

    def test_build_tools_schema(self, agent_engine, mock_tool):
        """测试构建工具schema"""
        agent_engine.register_tool(mock_tool)
        schemas = agent_engine._build_tools_schema()
        assert len(schemas) == 1
        assert schemas[0]["name"] == "test_tool"

    def test_build_system_prompt(self, agent_engine, mock_tool):
        """测试构建系统提示词"""
        agent_engine.register_tool(mock_tool)
        prompt = agent_engine._build_system_prompt()
        assert "你是一个测试助手" in prompt
        assert "test_tool" in prompt

    def test_build_system_prompt_react_mode(self, agent_engine):
        """测试ReAct模式下的系统提示词"""
        agent_engine.mode = "react"
        prompt = agent_engine._build_system_prompt()
        assert "ReAct模式" in prompt
        assert "Thought" in prompt

    def test_parse_function_call_with_json_code_block(self, agent_engine):
        """测试解析JSON代码块格式的函数调用"""
        response = """
这是我的思考过程。
```json
{"name": "test_tool", "arguments": {"arg1": "value1"}}
```
"""
        result = agent_engine._parse_function_call(response)
        assert result is not None
        assert result["name"] == "test_tool"
        assert result["arguments"]["arg1"] == "value1"

    def test_parse_function_call_with_plain_json(self, agent_engine):
        """测试解析纯JSON格式的函数调用"""
        response = '{"name": "test_tool", "arguments": {"arg1": "value1", "arg2": 123}}'
        result = agent_engine._parse_function_call(response)
        assert result is not None
        assert result["name"] == "test_tool"
        assert result["arguments"]["arg2"] == 123

    def test_parse_function_call_invalid(self, agent_engine):
        """测试解析无效的函数调用"""
        response = "这是一个普通的回复，没有函数调用"
        result = agent_engine._parse_function_call(response)
        assert result is None

    def test_execute_tool_success(self, agent_engine, mock_tool):
        """测试成功执行工具"""
        agent_engine.register_tool(mock_tool)
        result = agent_engine._execute_tool("test_tool", {"arg1": "value1"})
        assert result["success"] is True
        assert "result" in result

    def test_execute_tool_not_found(self, agent_engine):
        """测试执行不存在的工具"""
        result = agent_engine._execute_tool("nonexistent_tool", {})
        assert result["success"] is False
        assert "不存在" in result["error"]

    def test_execute_tool_exception(self, agent_engine):
        """测试工具执行异常"""
        failing_tool = FailingTool()
        agent_engine.register_tool(failing_tool)
        result = agent_engine._execute_tool("failing_tool", {})
        assert result["success"] is False
        assert "工具执行失败" in result["error"]

    @patch("app.services.agent_engine.LLMService")
    def test_execute_function_calling_with_tool_call(
        self, mock_llm_service_class, agent_engine, mock_tool
    ):
        """测试Function Calling模式（带工具调用）"""
        agent_engine.register_tool(mock_tool)

        # 模拟LLM响应（第一轮返回函数调用，第二轮返回最终答案）
        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.side_effect = [
            {
                "content": '{"name": "test_tool", "arguments": {"arg1": "value1"}}',
                "model_code": "TEST_MODEL",
            },
            {"content": "最终答案：处理完成", "model_code": "TEST_MODEL"},
        ]

        result = agent_engine.execute_function_calling("请执行测试工具")

        assert result["content"] == "最终答案：处理完成"
        assert len(result["tool_calls"]) == 1
        assert result["tool_calls"][0]["tool"] == "test_tool"

    @patch("app.services.agent_engine.LLMService")
    def test_execute_function_calling_without_tool(
        self, mock_llm_service_class, agent_engine
    ):
        """测试Function Calling模式（无需工具调用）"""
        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.return_value = {
            "content": "这是一个简单的回答，不需要工具",
            "model_code": "TEST_MODEL",
        }

        result = agent_engine.execute_function_calling("你好")

        assert "简单的回答" in result["content"]
        assert len(result["tool_calls"]) == 0

    @patch("app.services.agent_engine.LLMService")
    def test_execute_function_calling_max_iterations(
        self, mock_llm_service_class, agent_engine, mock_tool
    ):
        """测试Function Calling模式（达到最大迭代次数）"""
        agent_engine.register_tool(mock_tool)
        agent_engine.max_iterations = 2

        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        # 一直要求调用工具，不返回最终答案
        mock_llm_service.call_model.return_value = {
            "content": '{"name": "test_tool", "arguments": {}}',
            "model_code": "TEST_MODEL",
        }

        result = agent_engine.execute_function_calling("测试")

        assert "最大迭代次数" in result["content"]
        assert result["error"] == "max_iterations_exceeded"

    @patch("app.services.agent_engine.LLMService")
    def test_execute_react_mode(self, mock_llm_service_class, agent_engine, mock_tool):
        """测试ReAct模式"""
        agent_engine.mode = "react"
        agent_engine.register_tool(mock_tool)

        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        # 模拟ReAct响应
        mock_llm_service.call_model.side_effect = [
            {
                "content": 'Thought: 我需要使用工具\nAction: test_tool\nAction Input: {"arg1": "value1"}',
                "model_code": "TEST_MODEL",
            },
            {"content": "Final Answer: 任务完成", "model_code": "TEST_MODEL"},
        ]

        result = agent_engine.execute_react("测试ReAct")

        assert "任务完成" in result["content"]
        assert len(result["tool_calls"]) == 1

    @patch("app.services.agent_engine.LLMService")
    def test_execute_react_max_iterations(self, mock_llm_service_class, agent_engine):
        """测试ReAct模式（达到最大迭代次数）"""
        agent_engine.mode = "react"
        agent_engine.max_iterations = 2

        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        # 一直思考，不返回Final Answer
        mock_llm_service.call_model.return_value = {
            "content": "Thought: 还在思考...",
            "model_code": "TEST_MODEL",
        }

        result = agent_engine.execute_react("测试")

        assert "最大迭代次数" in result["content"]
        assert result["error"] == "max_iterations_exceeded"

    @patch("app.services.agent_engine.LLMService")
    def test_execute_with_function_calling_mode(
        self, mock_llm_service_class, agent_engine
    ):
        """测试execute方法（function_calling模式）"""
        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.return_value = {
            "content": "直接回答",
            "model_code": "TEST_MODEL",
        }

        result = agent_engine.execute("测试消息")
        assert "直接回答" in result["content"]

    @patch("app.services.agent_engine.LLMService")
    def test_execute_with_react_mode(self, mock_llm_service_class, agent_engine):
        """测试execute方法（react模式）"""
        agent_engine.mode = "react"

        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.return_value = {
            "content": "Final Answer: 完成",
            "model_code": "TEST_MODEL",
        }

        result = agent_engine.execute("测试消息")
        assert "完成" in result["content"]

    @patch("app.services.agent_engine.LLMService")
    def test_execute_with_conversation_history(
        self, mock_llm_service_class, agent_engine
    ):
        """测试带对话历史的执行"""
        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.return_value = {
            "content": "回复",
            "model_code": "TEST_MODEL",
        }

        context = {
            "conversation_history": [
                {"role": "user", "content": "第一条消息"},
                {"role": "assistant", "content": "第一条回复"},
            ]
        }

        result = agent_engine.execute("新消息", context)
        assert "回复" in result["content"]

    @patch("app.services.agent_engine.LLMService")
    def test_execute_with_exception(self, mock_llm_service_class, agent_engine):
        """测试执行时的异常处理"""
        mock_llm_service = Mock()
        mock_llm_service_class.return_value = mock_llm_service

        mock_llm_service.call_model.side_effect = Exception("LLM调用失败")

        result = agent_engine.execute("测试消息")
        assert "error" in result
        assert "LLM调用失败" in result["error"]
