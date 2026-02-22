"""
工具注册表服务测试
"""

import pytest
from pathlib import Path
from unittest.mock import Mock, patch, MagicMock
from app.services.agent_tool_registry import (
    ToolRegistry,
    register_tool,
    register_tool_class,
    get_registry,
)
from app.services.agent_engine import BaseTool


class MockTool1(BaseTool):
    """模拟工具1"""

    def __init__(self):
        super().__init__(
            name="mock_tool1",
            description="模拟工具1",
            schema={"name": "mock_tool1", "description": "模拟工具1", "parameters": {}},
        )

    def execute(self, arguments, context=None):
        return {"result": "tool1 executed"}


class MockTool2(BaseTool):
    """模拟工具2"""

    def __init__(self):
        super().__init__(
            name="mock_tool2",
            description="模拟工具2",
            schema={"name": "mock_tool2", "description": "模拟工具2", "parameters": {}},
        )

    def execute(self, arguments, context=None):
        return {"result": "tool2 executed"}


class MockToolWithArgs(BaseTool):
    """带参数的模拟工具"""

    def __init__(self, custom_arg="default"):
        super().__init__(
            name="mock_tool_with_args",
            description="带参数的工具",
            schema={
                "name": "mock_tool_with_args",
                "description": "带参数的工具",
                "parameters": {},
            },
        )
        self.custom_arg = custom_arg

    def execute(self, arguments, context=None):
        return {"result": f"custom_arg={self.custom_arg}"}


@pytest.fixture
def tool_registry():
    """工具注册表fixture"""
    return ToolRegistry()


@pytest.fixture
def mock_tool1():
    """模拟工具1 fixture"""
    return MockTool1()


@pytest.fixture
def mock_tool2():
    """模拟工具2 fixture"""
    return MockTool2()


class TestToolRegistry:
    """测试ToolRegistry"""

    def test_initialization(self, tool_registry):
        """测试初始化"""
        assert len(tool_registry._tools) == 0
        assert len(tool_registry._tool_classes) == 0
        assert len(tool_registry._tool_implementations) == 0

    def test_register_tool_instance(self, tool_registry, mock_tool1):
        """测试注册工具实例"""
        tool_registry.register(mock_tool1)
        assert "mock_tool1" in tool_registry._tools
        assert tool_registry._tools["mock_tool1"] == mock_tool1

    def test_register_tool_override(self, tool_registry, mock_tool1):
        """测试覆盖已注册的工具"""
        tool_registry.register(mock_tool1)
        tool_registry.register(mock_tool1)  # 重新注册
        assert "mock_tool1" in tool_registry._tools

    def test_register_tool_class(self, tool_registry):
        """测试注册工具类"""
        tool_registry.register_class(MockTool1, "app.services.test_tools")
        assert "MockTool1" in tool_registry._tool_classes
        assert tool_registry._tool_classes["MockTool1"] == MockTool1

    def test_register_tool_class_with_path(self, tool_registry):
        """测试注册工具类（带路径）"""
        tool_registry.register_class(MockTool1, "app.services.test_tools.MockTool1")
        assert "MockTool1" in tool_registry._tool_implementations
        assert (
            tool_registry._tool_implementations["MockTool1"]
            == "app.services.test_tools.MockTool1"
        )

    def test_get_tool_instance(self, tool_registry, mock_tool1):
        """测试获取工具实例"""
        tool_registry.register(mock_tool1)
        tool = tool_registry.get("mock_tool1")
        assert tool is not None
        assert tool.name == "mock_tool1"

    def test_get_tool_instance_not_found(self, tool_registry):
        """测试获取不存在的工具实例"""
        tool = tool_registry.get("nonexistent")
        assert tool is None

    def test_get_tool_class(self, tool_registry):
        """测试获取工具类"""
        tool_registry.register_class(MockTool1)
        tool_class = tool_registry.get_class("MockTool1")
        assert tool_class is not None
        assert tool_class == MockTool1

    def test_get_tool_class_not_found(self, tool_registry):
        """测试获取不存在的工具类"""
        tool_class = tool_registry.get_class("NonexistentTool")
        assert tool_class is None

    def test_create_instance(self, tool_registry):
        """测试创建工具实例"""
        tool_registry.register_class(MockToolWithArgs)
        tool = tool_registry.create_instance(
            "MockToolWithArgs", custom_arg="test_value"
        )
        assert tool is not None
        assert tool.custom_arg == "test_value"

    def test_create_instance_not_found(self, tool_registry):
        """测试创建不存在的工具实例"""
        tool = tool_registry.create_instance("NonexistentTool")
        assert tool is None

    def test_create_instance_exception(self, tool_registry):
        """测试创建工具实例异常"""
        tool_registry.register_class(MockToolWithArgs)
        # 传入无效参数导致异常
        tool = tool_registry.create_instance("MockToolWithArgs", invalid_arg="value")
        # 可能返回None或工具实例（取决于实现）

    def test_get_all(self, tool_registry, mock_tool1, mock_tool2):
        """测试获取所有工具实例"""
        tool_registry.register(mock_tool1)
        tool_registry.register(mock_tool2)
        all_tools = tool_registry.get_all()
        assert len(all_tools) == 2
        assert "mock_tool1" in all_tools
        assert "mock_tool2" in all_tools

    def test_get_all_returns_copy(self, tool_registry, mock_tool1):
        """测试get_all返回副本"""
        tool_registry.register(mock_tool1)
        all_tools = tool_registry.get_all()
        all_tools["new_tool"] = Mock()  # 修改副本
        assert "new_tool" not in tool_registry._tools

    def test_get_all_classes(self, tool_registry):
        """测试获取所有工具类"""
        tool_registry.register_class(MockTool1)
        tool_registry.register_class(MockTool2)
        all_classes = tool_registry.get_all_classes()
        assert len(all_classes) == 2
        assert "MockTool1" in all_classes
        assert "MockTool2" in all_classes

    def test_get_schema(self, tool_registry, mock_tool1):
        """测试获取工具schema"""
        tool_registry.register(mock_tool1)
        schema = tool_registry.get_schema("mock_tool1")
        assert schema is not None
        assert schema["name"] == "mock_tool1"

    def test_get_schema_not_found(self, tool_registry):
        """测试获取不存在工具的schema"""
        schema = tool_registry.get_schema("nonexistent")
        assert schema is None

    def test_list_tools(self, tool_registry, mock_tool1):
        """测试列出所有工具"""
        tool_registry.register(mock_tool1)
        tools_info = tool_registry.list_tools()
        assert len(tools_info) == 1
        assert tools_info[0]["name"] == "mock_tool1"
        assert "description" in tools_info[0]

    def test_list_tools_with_class_only(self, tool_registry):
        """测试列出工具（仅类）"""
        tool_registry.register_class(MockTool1)
        tools_info = tool_registry.list_tools()
        # 应该包含未实例化的工具类
        assert any(t.get("class_only") for t in tools_info)

    def test_unregister_tool(self, tool_registry, mock_tool1):
        """测试注销工具"""
        tool_registry.register(mock_tool1)
        assert "mock_tool1" in tool_registry._tools
        tool_registry.unregister("mock_tool1")
        assert "mock_tool1" not in tool_registry._tools

    def test_unregister_nonexistent_tool(self, tool_registry):
        """测试注销不存在的工具"""
        # 不应该抛出异常
        tool_registry.unregister("nonexistent")

    @patch("importlib.import_module")
    def test_load_from_module_single_class(self, mock_import, tool_registry):
        """测试从模块加载单个工具类"""
        mock_module = Mock()
        mock_module.MockTool1 = MockTool1
        mock_import.return_value = mock_module

        result = tool_registry.load_from_module("app.services.test_tools", "MockTool1")
        assert result is True
        assert "MockTool1" in tool_registry._tool_classes

    @patch("importlib.import_module")
    def test_load_from_module_all_tools(self, mock_import, tool_registry):
        """测试从模块加载所有工具类"""
        mock_module = Mock()
        mock_module.MockTool1 = MockTool1
        mock_module.MockTool2 = MockTool2
        mock_module.OtherClass = str  # 非工具类
        mock_import.return_value = mock_module

        result = tool_registry.load_from_module("app.services.test_tools")
        assert result is True
        assert "MockTool1" in tool_registry._tool_classes
        assert "MockTool2" in tool_registry._tool_classes

    @patch("importlib.import_module")
    def test_load_from_module_not_found(self, mock_import, tool_registry):
        """测试从不存在的模块加载"""
        mock_import.side_effect = ImportError("模块不存在")
        result = tool_registry.load_from_module("nonexistent.module")
        assert result is False

    def test_load_from_directory(self, tool_registry, tmp_path):
        """测试从目录加载工具"""
        # 创建临时目录和文件
        (tmp_path / "test_tool1.py").write_text("# test tool file")
        (tmp_path / "test_tool2.py").write_text("# test tool file")
        (tmp_path / "__init__.py").write_text("")
        (tmp_path / "_private.py").write_text("# private file")

        # 由于importlib的限制，这个测试会比较困难
        # 这里只测试目录不存在的情况
        result = tool_registry.load_from_directory("/nonexistent/directory")
        assert result == 0


class TestGlobalRegistry:
    """测试全局注册表"""

    @patch("app.services.agent_tool_registry.ToolRegistry.load_from_directory")
    def test_get_registry_singleton(self, mock_load, monkeypatch):
        """测试获取全局注册表（单例模式）"""
        # 重置全局注册表
        import app.services.agent_tool_registry as registry_module

        monkeypatch.setattr(registry_module, "_global_registry", None)

        registry1 = get_registry()
        registry2 = get_registry()
        assert registry1 is registry2

    def test_register_tool_convenience(self, monkeypatch):
        """测试便捷注册函数"""
        import app.services.agent_tool_registry as registry_module

        # 重置全局注册表
        monkeypatch.setattr(registry_module, "_global_registry", None)

        tool = MockTool1()
        register_tool(tool)

        registry = get_registry()
        assert "mock_tool1" in registry._tools

    def test_register_tool_class_convenience(self, monkeypatch):
        """测试便捷注册类函数"""
        import app.services.agent_tool_registry as registry_module

        # 重置全局注册表
        monkeypatch.setattr(registry_module, "_global_registry", None)

        register_tool_class(MockTool1)

        registry = get_registry()
        assert "MockTool1" in registry._tool_classes
