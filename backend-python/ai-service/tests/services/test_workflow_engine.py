"""
工作流引擎测试
"""

import pytest
import json
from unittest.mock import Mock, patch, MagicMock
from sqlalchemy import text
from app.services.workflow_engine import WorkflowEngine


@pytest.fixture
def db_session():
    """数据库会话fixture"""
    return Mock()


@pytest.fixture
def workflow_engine(db_session):
    """工作流引擎fixture"""
    return WorkflowEngine(db_session)


class TestWorkflowEngine:
    """测试WorkflowEngine"""

    def test_initialization(self, workflow_engine):
        """测试初始化"""
        assert workflow_engine.db is not None
        assert len(workflow_engine.node_executors) > 0

    def test_register_node_executor(self, workflow_engine):
        """测试注册节点执行器"""

        def custom_executor(input_data, config, context):
            return {"result": "custom"}

        workflow_engine.register_node_executor("custom_node", custom_executor)

        assert "custom_node" in workflow_engine.node_executors

    def test_execute_workflow_simple(self, workflow_engine):
        """测试执行简单工作流"""
        workflow_config = json.dumps(
            {
                "nodes": [
                    {"id": "node1", "type": "input", "config": {"input_key": "value"}}
                ],
                "edges": [],
            }
        )

        # 模拟节点执行器
        with patch.object(workflow_engine, "_execute_nodes") as mock_execute:
            mock_execute.return_value = {"output": "result"}

            result = workflow_engine.execute_workflow(
                workflow_config, {"test": "input"}
            )

            assert result["status"] == "success"

    def test_execute_workflow_invalid_config(self, workflow_engine):
        """测试无效的工作流配置"""
        with pytest.raises(Exception):
            workflow_engine.execute_workflow("invalid json", {})

    def test_execute_workflow_no_nodes(self, workflow_engine):
        """测试没有节点的工作流"""
        workflow_config = json.dumps({"nodes": [], "edges": []})

        result = workflow_engine.execute_workflow(workflow_config, {})

        assert result["status"] == "failed"

    def test_find_start_nodes(self, workflow_engine):
        """测试查找起始节点"""
        nodes = [{"id": "node1"}, {"id": "node2"}, {"id": "node3"}]
        edges = [{"source": "node1", "target": "node2"}]

        start_nodes = workflow_engine._find_start_nodes(nodes, edges)

        # node1和node3都是起始节点（没有入边）
        assert "node1" in start_nodes
        assert "node3" in start_nodes
        assert "node2" not in start_nodes

    def test_find_start_nodes_all_connected(self, workflow_engine):
        """测试所有节点都连接的情况"""
        nodes = [{"id": "node1"}, {"id": "node2"}]
        edges = [{"source": "node1", "target": "node2"}]

        start_nodes = workflow_engine._find_start_nodes(nodes, edges)

        # 只有node1是起始节点
        assert len(start_nodes) == 1
        assert "node1" in start_nodes

    def test_execute_nodes_success(self, workflow_engine):
        """测试成功执行节点"""
        node_map = {"node1": {"id": "node1", "type": "input", "config": {}}}
        edge_map = {}
        context = {"execution_id": "test_exec", "input_data": {}, "node_outputs": {}}

        # 模拟执行器
        with patch.object(workflow_engine, "node_executors") as mock_executors:
            mock_executors.get.return_value = lambda input_data, config, ctx: {
                "result": "success"
            }

            result = workflow_engine._execute_nodes(
                ["node1"], node_map, edge_map, context
            )

            # 应该有执行结果
            assert context["node_outputs"].get("node1") is not None

    def test_execute_nodes_max_depth(self, workflow_engine):
        """测试达到最大递归深度"""
        node_map = {"node1": {"id": "node1"}}
        edge_map = {"node1": ["node1"]}  # 自环，会导致无限递归
        context = {}

        with pytest.raises(ValueError, match="执行深度超过限制"):
            workflow_engine._execute_nodes(
                ["node1"], node_map, edge_map, context, max_depth=1
            )

    def test_execute_workflow_with_edges(self, workflow_engine):
        """测试执行带连接的工作流"""
        workflow_config = json.dumps(
            {
                "nodes": [
                    {"id": "node1", "type": "input"},
                    {"id": "node2", "type": "output"},
                ],
                "edges": [{"source": "node1", "target": "node2"}],
            }
        )

        with patch.object(workflow_engine, "_execute_nodes") as mock_execute:
            mock_execute.return_value = {"final": "result"}

            result = workflow_engine.execute_workflow(workflow_config, {})

            assert result["status"] == "success"

    def test_execute_workflow_with_execution_id(self, workflow_engine):
        """测试指定执行ID"""
        workflow_config = json.dumps(
            {"nodes": [{"id": "node1", "type": "input"}], "edges": []}
        )

        with patch.object(workflow_engine, "_execute_nodes") as mock_execute:
            mock_execute.return_value = {}

            result = workflow_engine.execute_workflow(
                workflow_config, {}, execution_id="custom_exec_id"
            )

            assert result["execution_id"] == "custom_exec_id"

    def test_execute_workflow_node_exception(self, workflow_engine):
        """测试节点执行异常"""
        workflow_config = json.dumps(
            {"nodes": [{"id": "node1", "type": "nonexistent"}], "edges": []}
        )

        # 不模拟执行器，让其自然失败
        result = workflow_engine.execute_workflow(workflow_config, {})

        # 应该失败
        assert result["status"] == "failed"

    def test_create_node_wrapper(self, workflow_engine):
        """测试节点包装器创建"""
        # 这个测试验证包装器能正确调用节点类
        mock_node_class = Mock()
        mock_node_instance = Mock()
        mock_node_instance.execute.return_value = {"result": "wrapped"}
        mock_node_class.return_value = mock_node_instance

        wrapper = workflow_engine._create_node_wrapper(mock_node_class)

        result = wrapper({"input": "data"}, {"config": "value"}, {"context": "data"})

        assert result["result"] == "wrapped"
        mock_node_class.assert_called_once_with(workflow_engine.db)

    def test_execute_workflow_complex(self, workflow_engine):
        """测试执行复杂工作流"""
        workflow_config = json.dumps(
            {
                "nodes": [
                    {"id": "input1", "type": "input"},
                    {"id": "process1", "type": "process"},
                    {"id": "output1", "type": "output"},
                ],
                "edges": [
                    {"source": "input1", "target": "process1"},
                    {"source": "process1", "target": "output1"},
                ],
            }
        )

        with patch.object(workflow_engine, "_execute_nodes") as mock_execute:
            mock_execute.return_value = {"final_result": "done"}

            result = workflow_engine.execute_workflow(workflow_config, {"data": "test"})

            assert result["status"] == "success"
            assert "output" in result

    def test_node_executors_registration(self, workflow_engine):
        """测试节点执行器注册"""
        # 验证默认节点执行器已注册
        assert "input" in workflow_engine.node_executors
        assert "output" in workflow_engine.node_executors
        assert (
            "process" in workflow_engine.node_executors
            or "llm_call" in workflow_engine.node_executors
        )

    def test_execute_with_missing_node_in_map(self, workflow_engine):
        """测试执行时节点不在映射中"""
        node_map = {}  # 空映射
        edge_map = {}
        context = {}

        # 应该抛出异常或返回错误
        with pytest.raises(Exception):
            workflow_engine._execute_nodes(["nonexistent"], node_map, edge_map, context)

    def test_context_management(self, workflow_engine):
        """测试执行上下文管理"""
        workflow_config = json.dumps(
            {"nodes": [{"id": "node1", "type": "input"}], "edges": []}
        )

        with patch.object(workflow_engine, "_execute_nodes") as mock_execute:

            def update_context(node_ids, node_map, edge_map, context, **kwargs):
                context["node_outputs"]["node1"] = {"output": "test"}
                return {"result": "done"}

            mock_execute.side_effect = update_context

            result = workflow_engine.execute_workflow(workflow_config, {})

            assert "context" in result
            assert "node_outputs" in result["context"]
