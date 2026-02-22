"""
Agent系统集成测试
测试AgentEngine的多轮对话、工具调用和ReAct模式功能
"""

import pytest
from unittest.mock import Mock, patch, MagicMock
from sqlalchemy.orm import Session
from typing import Dict, Any

from app.services.agent_engine import AgentEngine, BaseTool


# ============================================================================
# 测试工具实现
# ============================================================================


class MockCalculatorTool(BaseTool):
    """模拟计算器工具（用于测试）"""

    def __init__(self):
        schema = {
            "name": "calculator",
            "description": "执行基本数学运算（加、减、乘、除）",
            "parameters": {
                "type": "object",
                "properties": {
                    "operation": {
                        "type": "string",
                        "enum": ["add", "subtract", "multiply", "divide"],
                        "description": "运算类型",
                    },
                    "a": {"type": "number", "description": "第一个操作数"},
                    "b": {"type": "number", "description": "第二个操作数"},
                },
                "required": ["operation", "a", "b"],
            },
        }
        super().__init__("calculator", "执行基本数学运算", schema)

    def execute(
        self, arguments: Dict[str, Any], context: Dict[str, Any] = None
    ) -> Dict[str, Any]:
        """执行计算"""
        operation = arguments.get("operation")
        a = arguments.get("a", 0)
        b = arguments.get("b", 0)

        if operation == "add":
            result = a + b
        elif operation == "subtract":
            result = a - b
        elif operation == "multiply":
            result = a * b
        elif operation == "divide":
            if b == 0:
                return {"error": "除数不能为零"}
            result = a / b
        else:
            return {"error": f"未知运算: {operation}"}

        return {"result": result, "operation": operation}


class MockSearchTool(BaseTool):
    """模拟搜索工具（用于测试）"""

    def __init__(self):
        schema = {
            "name": "search",
            "description": "搜索测试用例和历史数据",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {"type": "string", "description": "搜索关键词"},
                    "limit": {
                        "type": "integer",
                        "description": "返回结果数量限制",
                        "default": 10,
                    },
                },
                "required": ["query"],
            },
        }
        super().__init__("search", "搜索测试用例和历史数据", schema)

    def execute(
        self, arguments: Dict[str, Any], context: Dict[str, Any] = None
    ) -> Dict[str, Any]:
        """执行搜索"""
        query = arguments.get("query", "")
        limit = arguments.get("limit", 10)

        # 模拟搜索结果
        results = [
            {"id": 1, "title": f"测试用例 - {query}", "description": "相关测试用例"},
            {"id": 2, "title": f"历史数据 - {query}", "description": "历史测试数据"},
        ]

        return {"query": query, "total": len(results), "results": results[:limit]}


class MockWeatherTool(BaseTool):
    """模拟天气查询工具（用于测试ReAct模式）"""

    def __init__(self):
        schema = {
            "name": "get_weather",
            "description": "查询指定城市的天气信息",
            "parameters": {
                "type": "object",
                "properties": {"city": {"type": "string", "description": "城市名称"}},
                "required": ["city"],
            },
        }
        super().__init__("get_weather", "查询天气信息", schema)

    def execute(
        self, arguments: Dict[str, Any], context: Dict[str, Any] = None
    ) -> Dict[str, Any]:
        """查询天气"""
        city = arguments.get("city", "未知城市")

        # 模拟天气数据
        weather_data = {
            "北京": {"temperature": 25, "condition": "晴", "humidity": 45},
            "上海": {"temperature": 28, "condition": "多云", "humidity": 65},
            "深圳": {"temperature": 32, "condition": "阵雨", "humidity": 80},
        }

        return weather_data.get(
            city, {"temperature": 20, "condition": "未知", "humidity": 50}
        )


# ============================================================================
# AgentEngine集成测试
# ============================================================================


class TestAgentEngineIntegration:
    """Agent引擎集成测试类"""

    def test_agent_engine_initialization(self, test_db: Session):
        """测试AgentEngine初始化"""
        agent_config = {
            "model_code": "TEST_MODEL",
            "max_iterations": 5,
            "max_tokens": 2000,
            "temperature": 0.7,
            "system_prompt": "你是一个测试助手",
            "mode": "function_calling",
        }

        engine = AgentEngine(test_db, agent_config)

        assert engine.db == test_db
        assert engine.model_code == "TEST_MODEL"
        assert engine.max_iterations == 5
        assert engine.max_tokens == 2000
        assert engine.temperature == 0.7
        assert engine.system_prompt == "你是一个测试助手"
        assert engine.mode == "function_calling"
        assert engine.tools == {}

    def test_tool_registration_single(self, test_db: Session):
        """测试单个工具注册"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        calculator = MockCalculatorTool()
        engine.register_tool(calculator)

        assert "calculator" in engine.tools
        assert engine.tools["calculator"] == calculator

    def test_tool_registration_multiple(self, test_db: Session):
        """测试批量工具注册"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        tools = [MockCalculatorTool(), MockSearchTool(), MockWeatherTool()]
        engine.register_tools(tools)

        assert len(engine.tools) == 3
        assert "calculator" in engine.tools
        assert "search" in engine.tools
        assert "get_weather" in engine.tools

    def test_build_tools_schema(self, test_db: Session):
        """测试工具schema构建"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        calculator = MockCalculatorTool()
        engine.register_tool(calculator)

        schemas = engine._build_tools_schema()

        assert len(schemas) == 1
        assert schemas[0]["name"] == "calculator"
        assert "description" in schemas[0]
        assert "parameters" in schemas[0]

    def test_build_system_prompt(self, test_db: Session):
        """测试系统提示词构建"""
        agent_config = {
            "model_code": "TEST_MODEL",
            "system_prompt": "你是一个智能助手",
            "mode": "function_calling",
        }
        engine = AgentEngine(test_db, agent_config)

        tools = [MockCalculatorTool(), MockSearchTool()]
        engine.register_tools(tools)

        prompt = engine._build_system_prompt()

        assert "你是一个智能助手" in prompt
        assert "calculator" in prompt
        assert "search" in prompt

    def test_build_system_prompt_react_mode(self, test_db: Session):
        """测试ReAct模式的系统提示词构建"""
        agent_config = {
            "model_code": "TEST_MODEL",
            "system_prompt": "你是一个智能助手",
            "mode": "react",
        }
        engine = AgentEngine(test_db, agent_config)

        prompt = engine._build_system_prompt()

        assert "ReAct模式" in prompt
        assert "思考(Thought) -> 行动(Action) -> 观察(Observation)" in prompt


# ============================================================================
# 工具执行测试
# ============================================================================


class TestToolExecution:
    """工具执行测试类"""

    def test_tool_execute_calculator_add(self, test_db: Session):
        """测试计算器工具 - 加法"""
        calculator = MockCalculatorTool()
        result = calculator.execute({"operation": "add", "a": 5, "b": 3})

        assert result["result"] == 8
        assert result["operation"] == "add"

    def test_tool_execute_calculator_divide_by_zero(self, test_db: Session):
        """测试计算器工具 - 除零错误"""
        calculator = MockCalculatorTool()
        result = calculator.execute({"operation": "divide", "a": 10, "b": 0})

        assert "error" in result
        assert "除数不能为零" in result["error"]

    def test_tool_execute_search(self, test_db: Session):
        """测试搜索工具"""
        search = MockSearchTool()
        result = search.execute({"query": "登录测试", "limit": 5})

        assert result["query"] == "登录测试"
        assert result["total"] == 2
        assert len(result["results"]) == 2
        assert "title" in result["results"][0]

    def test_tool_execute_weather(self, test_db: Session):
        """测试天气查询工具"""
        weather = MockWeatherTool()
        result = weather.execute({"city": "北京"})

        assert result["temperature"] == 25
        assert result["condition"] == "晴"

    @patch("app.services.agent_engine.AgentEngine._execute_tool")
    def test_agent_execute_tool_success(self, mock_execute, test_db: Session):
        """测试Agent执行工具成功"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        calculator = MockCalculatorTool()
        engine.register_tool(calculator)

        mock_execute.return_value = {
            "success": True,
            "result": {"result": 8, "operation": "add"},
        }

        result = engine._execute_tool(
            "calculator", {"operation": "add", "a": 5, "b": 3}
        )

        assert result["success"] is True
        assert result["result"]["result"] == 8

    def test_agent_execute_tool_not_found(self, test_db: Session):
        """测试Agent执行不存在的工具"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        result = engine._execute_tool("unknown_tool", {})

        assert result["success"] is False
        assert "不存在" in result["error"]


# ============================================================================
# Function Calling模式测试
# ============================================================================


class TestFunctionCallingMode:
    """Function Calling模式测试类"""

    @patch("app.services.agent_engine.LLMService")
    def test_function_calling_no_tool_needed(
        self, mock_llm_service_class, test_db: Session, mock_llm_response
    ):
        """测试Function Calling模式 - 无需调用工具"""
        # 设置mock
        mock_llm_response.content = "根据我的知识，答案是42"
        mock_llm_instance = Mock()
        mock_llm_instance.invoke.return_value = mock_llm_response
        mock_llm_instance.get_num_tokens.return_value = 100

        mock_llm_service = Mock()
        mock_llm_service.call_model.return_value = {
            "content": "根据我的知识，答案是42",
            "model_code": "TEST_MODEL",
            "tokens_used": 100,
            "response_time": 1.5,
        }

        mock_llm_service_class.return_value = mock_llm_service

        # 创建Agent
        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "function_calling",
            "system_prompt": "你是一个数学助手",
        }
        engine = AgentEngine(test_db, agent_config)

        # 执行
        result = engine.execute_function_calling("什么是2+2？")

        assert result["content"] == "根据我的知识，答案是42"
        assert result["iterations"] == 1
        assert result["tool_calls"] == []

    @patch("app.services.agent_engine.LLMService")
    def test_function_calling_with_single_tool_call(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试Function Calling模式 - 单次工具调用"""
        # 设置mock - 第一次返回工具调用，第二次返回最终答案
        mock_llm_service = Mock()

        # 第一次调用：返回工具调用指令
        mock_llm_service.call_model.side_effect = [
            {
                "content": '{"name": "calculator", "arguments": {"operation": "add", "a": 5, "b": 3}}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            # 第二次调用：返回最终答案
            {
                "content": "计算结果是8",
                "model_code": "TEST_MODEL",
                "tokens_used": 50,
                "response_time": 1.0,
            },
        ]

        mock_llm_service_class.return_value = mock_llm_service

        # 创建Agent并注册工具
        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "function_calling",
            "system_prompt": "你是一个数学助手",
        }
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockCalculatorTool())

        # 执行
        result = engine.execute_function_calling("5加3等于多少？")

        assert result["content"] == "计算结果是8"
        assert result["iterations"] == 2
        assert len(result["tool_calls"]) == 1
        assert result["tool_calls"][0]["tool"] == "calculator"

    @patch("app.services.agent_engine.LLMService")
    def test_function_calling_multi_turn(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试Function Calling模式 - 多轮对话"""
        mock_llm_service = Mock()

        # 模拟多轮对话：工具调用 -> 工具调用 -> 最终答案
        mock_llm_service.call_model.side_effect = [
            {
                "content": '{"name": "calculator", "arguments": {"operation": "multiply", "a": 5, "b": 4}}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": '{"name": "calculator", "arguments": {"operation": "add", "a": 20, "b": 10}}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": "最终答案是30",
                "model_code": "TEST_MODEL",
                "tokens_used": 50,
                "response_time": 1.0,
            },
        ]

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "function_calling",
            "system_prompt": "你是一个数学助手",
        }
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockCalculatorTool())

        result = engine.execute_function_calling("先计算5乘4，再加10")

        assert result["content"] == "最终答案是30"
        assert result["iterations"] == 3
        assert len(result["tool_calls"]) == 2

    @patch("app.services.agent_engine.LLMService")
    def test_function_calling_with_conversation_history(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试Function Calling模式 - 带对话历史"""
        mock_llm_service = Mock()
        mock_llm_service.call_model.return_value = {
            "content": "之前我们计算了2+2=4，现在答案是8",
            "model_code": "TEST_MODEL",
            "tokens_used": 80,
            "response_time": 1.2,
        }

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        context = {
            "conversation_history": [
                {"role": "user", "content": "2+2等于多少？"},
                {"role": "assistant", "content": "2+2等于4"},
            ]
        }

        result = engine.execute_function_calling("那4+4呢？", context)

        assert "之前" in result["content"]
        assert result["iterations"] == 1


# ============================================================================
# ReAct模式测试
# ============================================================================


class TestReActMode:
    """ReAct模式测试类"""

    @patch("app.services.agent_engine.LLMService")
    def test_react_mode_simple_workflow(self, mock_llm_service_class, test_db: Session):
        """测试ReAct模式 - 简单工作流"""
        mock_llm_service = Mock()

        # ReAct循环：Thought -> Action -> Observation -> Final Answer
        mock_llm_service.call_model.side_effect = [
            {
                "content": 'Thought: 需要查询天气信息\nAction: get_weather\nAction Input: {"city": "北京"}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": "Final Answer: 北京今天温度25度，天气晴",
                "model_code": "TEST_MODEL",
                "tokens_used": 80,
                "response_time": 1.2,
            },
        ]

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "react",
            "system_prompt": "你是一个天气助手",
        }
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockWeatherTool())

        result = engine.execute_react("北京今天天气怎么样？")

        assert result["content"] == "北京今天温度25度，天气晴"
        assert result["iterations"] == 2
        assert len(result["tool_calls"]) == 1
        assert result["tool_calls"][0]["tool"] == "get_weather"

    @patch("app.services.agent_engine.LLMService")
    def test_react_mode_multi_step(self, mock_llm_service_class, test_db: Session):
        """测试ReAct模式 - 多步推理"""
        mock_llm_service = Mock()

        # 多步ReAct循环
        mock_llm_service.call_model.side_effect = [
            {
                "content": 'Thought: 先查询北京天气\nAction: get_weather\nAction Input: {"city": "北京"}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": 'Thought: 再查询上海天气\nAction: get_weather\nAction Input: {"city": "上海"}',
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": "Final Answer: 北京25度晴，上海28度多云",
                "model_code": "TEST_MODEL",
                "tokens_used": 80,
                "response_time": 1.2,
            },
        ]

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {"model_code": "TEST_MODEL", "mode": "react"}
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockWeatherTool())

        result = engine.execute_react("北京和上海今天天气对比")

        assert result["iterations"] == 3
        assert len(result["tool_calls"]) == 2
        assert "北京" in result["content"] and "上海" in result["content"]

    @patch("app.services.agent_engine.LLMService")
    def test_react_mode_unknown_action(self, mock_llm_service_class, test_db: Session):
        """测试ReAct模式 - 未知动作处理"""
        mock_llm_service = Mock()

        mock_llm_service.call_model.side_effect = [
            {
                "content": "Thought: 我不需要工具\nAction: unknown_action\nAction Input: {}",
                "model_code": "TEST_MODEL",
                "tokens_used": 100,
                "response_time": 1.5,
            },
            {
                "content": "Final Answer: 我直接回答问题",
                "model_code": "TEST_MODEL",
                "tokens_used": 80,
                "response_time": 1.2,
            },
        ]

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {"model_code": "TEST_MODEL", "mode": "react"}
        engine = AgentEngine(test_db, agent_config)

        result = engine.execute_react("直接回答这个问题")

        assert result["content"] == "我直接回答问题"
        assert result["iterations"] == 2


# ============================================================================
# 解析和错误处理测试
# ============================================================================


class TestParsingAndErrorHandling:
    """解析和错误处理测试类"""

    def test_parse_function_call_json_format(self, test_db: Session):
        """测试解析JSON格式的函数调用"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        response = (
            '{"name": "calculator", "arguments": {"operation": "add", "a": 1, "b": 2}}'
        )
        result = engine._parse_function_call(response)

        assert result is not None
        assert result["name"] == "calculator"
        assert result["arguments"]["operation"] == "add"

    def test_parse_function_call_markdown_code_block(self, test_db: Session):
        """测试解析Markdown代码块中的函数调用"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        response = """这是一个计算任务
```json
{"name": "calculator", "arguments": {"operation": "multiply", "a": 3, "b": 4}}
```
"""
        result = engine._parse_function_call(response)

        assert result is not None
        assert result["name"] == "calculator"
        assert result["arguments"]["a"] == 3

    def test_parse_function_call_invalid_json(self, test_db: Session):
        """测试解析无效的JSON"""
        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        response = "这不是一个有效的JSON格式"
        result = engine._parse_function_call(response)

        assert result is None

    @patch("app.services.agent_engine.LLMService")
    def test_function_calling_max_iterations_exceeded(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试达到最大迭代次数"""
        mock_llm_service = Mock()

        # 持续返回工具调用，超过最大迭代次数
        mock_llm_service.call_model.return_value = {
            "content": '{"name": "calculator", "arguments": {"operation": "add", "a": 1, "b": 1}}',
            "model_code": "TEST_MODEL",
            "tokens_used": 100,
            "response_time": 1.5,
        }

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "function_calling",
            "max_iterations": 3,
        }
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockCalculatorTool())

        result = engine.execute_function_calling("持续计算")

        assert result["iterations"] == 3
        assert "max_iterations_exceeded" in result.get("error", "")

    @patch("app.services.agent_engine.LLMService")
    def test_react_max_iterations_exceeded(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试ReAct模式达到最大迭代次数"""
        mock_llm_service = Mock()

        # 持续返回Thought，不返回Final Answer
        mock_llm_service.call_model.return_value = {
            "content": 'Thought: 我还在思考...\nAction: get_weather\nAction Input: {"city": "北京"}',
            "model_code": "TEST_MODEL",
            "tokens_used": 100,
            "response_time": 1.5,
        }

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {
            "model_code": "TEST_MODEL",
            "mode": "react",
            "max_iterations": 2,
        }
        engine = AgentEngine(test_db, agent_config)
        engine.register_tool(MockWeatherTool())

        result = engine.execute_react("复杂问题")

        assert result["iterations"] == 2
        assert "max_iterations_exceeded" in result.get("error", "")


# ============================================================================
# 执行入口测试
# ============================================================================


class TestExecuteEntryPoint:
    """测试execute入口方法"""

    @patch("app.services.agent_engine.LLMService")
    def test_execute_uses_function_calling_mode(
        self, mock_llm_service_class, test_db: Session
    ):
        """测试execute方法使用Function Calling模式"""
        mock_llm_service = Mock()
        mock_llm_service.call_model.return_value = {
            "content": "直接回答",
            "model_code": "TEST_MODEL",
            "tokens_used": 50,
            "response_time": 1.0,
        }

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {"model_code": "TEST_MODEL", "mode": "function_calling"}
        engine = AgentEngine(test_db, agent_config)

        result = engine.execute("测试消息")

        assert result["content"] == "直接回答"
        assert result["iterations"] == 1

    @patch("app.services.agent_engine.LLMService")
    def test_execute_uses_react_mode(self, mock_llm_service_class, test_db: Session):
        """测试execute方法使用ReAct模式"""
        mock_llm_service = Mock()
        mock_llm_service.call_model.return_value = {
            "content": "Final Answer: 这是ReAct模式的回答",
            "model_code": "TEST_MODEL",
            "tokens_used": 80,
            "response_time": 1.2,
        }

        mock_llm_service_class.return_value = mock_llm_service

        agent_config = {"model_code": "TEST_MODEL", "mode": "react"}
        engine = AgentEngine(test_db, agent_config)

        result = engine.execute("测试消息")

        assert result["content"] == "这是ReAct模式的回答"
        assert "full_response" in result
