"""
工作流执行引擎
负责解析和执行工作流定义
"""
import json
import logging
import uuid
from typing import Dict, Any, List, Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class WorkflowEngine:
    """工作流执行引擎"""
    
    def __init__(self, db: Session):
        """
        初始化工作流引擎
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.node_executors = {}  # 节点执行器注册表
        self._register_default_executors()
    
    def _register_default_executors(self):
        """注册默认节点执行器"""
        from .workflow_nodes.input_nodes import RequirementInputNode, TestCaseInputNode, FileUploadNode
        from .workflow_nodes.process_nodes import RequirementAnalysisNode, TemplateSelectNode, PromptGenerateNode, LLMCallNode, ResultParseNode
        from .workflow_nodes.transform_nodes import FormatTransformNode, DataCleanNode, DataMergeNode
        from .workflow_nodes.output_nodes import CaseSaveNode, ReportGenerateNode, FileExportNode
        from .workflow_nodes.control_nodes import ConditionNode, LoopNode
        
        # 注册输入节点
        self.register_node_executor("input", self._create_node_wrapper(RequirementInputNode))
        self.register_node_executor("requirement_input", self._create_node_wrapper(RequirementInputNode))
        self.register_node_executor("test_case_input", self._create_node_wrapper(TestCaseInputNode))
        self.register_node_executor("file_upload", self._create_node_wrapper(FileUploadNode))
        
        # 注册处理节点
        self.register_node_executor("requirement_analysis", self._create_node_wrapper(RequirementAnalysisNode))
        self.register_node_executor("template_select", self._create_node_wrapper(TemplateSelectNode))
        self.register_node_executor("prompt_generate", self._create_node_wrapper(PromptGenerateNode))
        self.register_node_executor("llm_call", self._create_node_wrapper(LLMCallNode))
        self.register_node_executor("result_parse", self._create_node_wrapper(ResultParseNode))
        
        # 注册转换节点
        self.register_node_executor("format_transform", self._create_node_wrapper(FormatTransformNode))
        self.register_node_executor("data_clean", self._create_node_wrapper(DataCleanNode))
        self.register_node_executor("data_merge", self._create_node_wrapper(DataMergeNode))
        
        # 注册输出节点
        self.register_node_executor("case_save", self._create_node_wrapper(CaseSaveNode))
        self.register_node_executor("report_generate", self._create_node_wrapper(ReportGenerateNode))
        self.register_node_executor("file_export", self._create_node_wrapper(FileExportNode))
        
        # 注册控制节点
        self.register_node_executor("condition", self._create_node_wrapper(ConditionNode))
        self.register_node_executor("loop", self._create_node_wrapper(LoopNode))
    
    def _create_node_wrapper(self, node_class):
        """创建节点包装器"""
        def wrapper(input_data, config, context):
            node = node_class(self.db)
            return node.execute(input_data, config, context)
        return wrapper
    
    def register_node_executor(self, node_type: str, executor):
        """
        注册节点执行器
        
        Args:
            node_type: 节点类型
            executor: 执行器函数
        """
        self.node_executors[node_type] = executor
    
    def execute_workflow(
        self,
        workflow_config: str,
        input_data: Dict[str, Any],
        execution_id: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        执行工作流
        
        Args:
            workflow_config: 工作流配置（JSON字符串）
            input_data: 输入数据
            execution_id: 执行ID（可选，如果不提供则自动生成）
            
        Returns:
            执行结果
        """
        if execution_id is None:
            execution_id = str(uuid.uuid4())
        
        try:
            # 解析工作流配置
            config = json.loads(workflow_config)
            nodes = config.get("nodes", [])
            edges = config.get("edges", [])
            
            if not nodes:
                raise ValueError("工作流必须包含至少一个节点")
            
            # 构建节点映射
            node_map = {node["id"]: node for node in nodes}
            
            # 构建边映射（用于查找下一个节点）
            edge_map = {}
            for edge in edges:
                source = edge.get("source")
                target = edge.get("target")
                if source not in edge_map:
                    edge_map[source] = []
                edge_map[source].append(target)
            
            # 查找起始节点（没有入边的节点）
            start_nodes = self._find_start_nodes(nodes, edges)
            if not start_nodes:
                raise ValueError("工作流必须包含至少一个起始节点")
            
            # 执行上下文
            context = {
                "execution_id": execution_id,
                "input_data": input_data,
                "node_outputs": {},  # 存储每个节点的输出
                "current_node": None
            }
            
            # 执行工作流
            result = self._execute_nodes(start_nodes, node_map, edge_map, context)
            
            return {
                "execution_id": execution_id,
                "status": "success",
                "output": result,
                "context": context
            }
            
        except Exception as e:
            logger.error(f"工作流执行失败: {execution_id}, 错误: {str(e)}", exc_info=True)
            return {
                "execution_id": execution_id,
                "status": "failed",
                "error": str(e),
                "error_node": context.get("current_node") if 'context' in locals() else None
            }
    
    def _find_start_nodes(self, nodes: List[Dict], edges: List[Dict]) -> List[str]:
        """
        查找起始节点（没有入边的节点）
        
        Args:
            nodes: 节点列表
            edges: 边列表
            
        Returns:
            起始节点ID列表
        """
        all_node_ids = {node["id"] for node in nodes}
        target_node_ids = {edge.get("target") for edge in edges if edge.get("target")}
        
        # 起始节点是没有作为target的节点
        start_nodes = list(all_node_ids - target_node_ids)
        return start_nodes
    
    def _execute_nodes(
        self,
        node_ids: List[str],
        node_map: Dict[str, Dict],
        edge_map: Dict[str, List[str]],
        context: Dict[str, Any],
        max_depth: int = 1000
    ) -> Any:
        """
        执行节点列表
        
        Args:
            node_ids: 要执行的节点ID列表
            node_map: 节点映射
            edge_map: 边映射
            context: 执行上下文
            max_depth: 最大递归深度（防止无限循环）
            
        Returns:
            执行结果
        """
        if max_depth <= 0:
            raise ValueError("执行深度超过限制，可能存在无限循环")
        
        results = []
        
        for node_id in node_ids:
            if node_id not in node_map:
                raise ValueError(f"节点不存在: {node_id}")
            
            node = node_map[node_id]
            node_type = node.get("type")
            context["current_node"] = node_id
            
            # 执行节点
            node_result = self._execute_single_node(node, context)
            context["node_outputs"][node_id] = node_result
            results.append(node_result)
            
            # 处理条件分支
            if node_type == "condition":
                next_node_ids = self._handle_condition_branch(node_id, node_result, edge_map, context)
            # 处理循环节点
            elif node_type == "loop":
                next_node_ids = self._handle_loop_branch(node_id, node_result, edge_map, context)
                
                # 如果返回了循环体节点，需要执行循环体并处理迭代
                if next_node_ids and len(next_node_ids) > 0:
                    loop_body_node_id = next_node_ids[0]
                    
                    # 检查是否是循环体节点
                    if loop_body_node_id in node_map:
                        # 执行循环体（可能多次迭代）
                        self._execute_loop_body(
                            loop_node_id=node_id,
                            loop_body_node_id=loop_body_node_id,
                            node_map=node_map,
                            edge_map=edge_map,
                            context=context,
                            max_depth=max_depth - 1
                        )
                        
                        # 循环体执行完成后，检查是否有后续节点
                        loop_body_edges = edge_map.get(loop_body_node_id, [])
                        # 过滤掉回到循环节点的边
                        next_node_ids = [nid for nid in loop_body_edges if nid != node_id]
                    else:
                        # 不是循环体节点，正常处理
                        pass
            else:
                # 普通节点：查找下一个节点
                next_node_ids = edge_map.get(node_id, [])
            
            if next_node_ids:
                # 递归执行下一个节点
                next_results = self._execute_nodes(next_node_ids, node_map, edge_map, context, max_depth - 1)
                results.extend(next_results)
        
        return results[-1] if results else None
    
    def _execute_loop_body(
        self,
        loop_node_id: str,
        loop_body_node_id: str,
        node_map: Dict[str, Dict],
        edge_map: Dict[str, List[str]],
        context: Dict[str, Any],
        max_depth: int
    ):
        """
        执行循环体（支持多次迭代）
        
        Args:
            loop_node_id: 循环节点ID
            loop_body_node_id: 循环体节点ID
            node_map: 节点映射
            edge_map: 边映射
            context: 执行上下文
            max_depth: 最大递归深度
        """
        loop_body_node = node_map[loop_body_node_id]
        loop_contexts = context.get("loop_contexts", {})
        loop_context_key = f"loop_{loop_node_id}"
        
        # 持续执行循环体，直到循环完成
        while True:
            if max_depth <= 0:
                logger.warning(f"循环执行深度超过限制，强制退出: {loop_node_id}")
                break
            
            # 检查循环是否完成
            if loop_context_key not in loop_contexts:
                break
            
            loop_state = loop_contexts[loop_context_key]
            if loop_state.get("completed", False):
                break
            
            # 执行循环体节点
            context["current_node"] = loop_body_node_id
            loop_body_result = self._execute_single_node(loop_body_node, context)
            context["node_outputs"][loop_body_node_id] = loop_body_result
            
            # 检查循环体是否有出边回到循环节点
            loop_body_edges = edge_map.get(loop_body_node_id, [])
            
            if loop_node_id in loop_body_edges:
                # 循环体执行完成，更新循环状态
                self._update_loop_state(loop_node_id, context)
                
                # 重新处理循环分支，检查是否还有下一次迭代
                loop_node_result = context["node_outputs"].get(loop_node_id)
                next_node_ids = self._handle_loop_branch(loop_node_id, loop_node_result, edge_map, context)
                
                # 如果还有下一次迭代，继续执行循环体
                if next_node_ids and len(next_node_ids) > 0 and next_node_ids[0] == loop_body_node_id:
                    max_depth -= 1
                    continue
                else:
                    # 循环完成，退出
                    break
            else:
                # 循环体有独立的出边，执行后续节点后退出
                if loop_body_edges:
                    self._execute_nodes(loop_body_edges, node_map, edge_map, context, max_depth - 1)
                break
    
    def _update_loop_state(self, loop_node_id: str, context: Dict[str, Any]):
        """
        更新循环状态（增加迭代计数）
        
        Args:
            loop_node_id: 循环节点ID
            context: 执行上下文
        """
        loop_contexts = context.get("loop_contexts", {})
        loop_context_key = f"loop_{loop_node_id}"
        
        if loop_context_key in loop_contexts:
            loop_state = loop_contexts[loop_context_key]
            
            if loop_state["type"] == "for":
                # for循环：增加索引
                loop_state["current_index"] += 1
            elif loop_state["type"] == "while":
                # while循环：增加迭代次数
                loop_state["current_iteration"] += 1
    
    def _handle_condition_branch(
        self,
        node_id: str,
        node_result: Any,
        edge_map: Dict[str, List[str]],
        context: Dict[str, Any]
    ) -> List[str]:
        """
        处理条件分支
        
        Args:
            node_id: 条件节点ID
            node_result: 条件节点执行结果
            edge_map: 边映射
            
        Returns:
            下一个要执行的节点ID列表
        """
        # 获取条件判断结果
        condition_result = False
        if isinstance(node_result, dict):
            condition_result = node_result.get("condition_result", False)
        
        # 查找所有出边
        all_next_nodes = edge_map.get(node_id, [])
        
        # 根据条件结果选择分支
        # 约定：第一个边是true分支，第二个边是false分支（如果有）
        if condition_result and len(all_next_nodes) > 0:
            # 执行true分支（第一个边）
            return [all_next_nodes[0]]
        elif not condition_result and len(all_next_nodes) > 1:
            # 执行false分支（第二个边）
            return [all_next_nodes[1]]
        elif not condition_result and len(all_next_nodes) > 0:
            # 如果没有false分支，但有true分支，不执行任何分支
            return []
        else:
            # 没有出边，返回空列表
            return []
    
    def _handle_loop_branch(
        self,
        node_id: str,
        node_result: Any,
        edge_map: Dict[str, List[str]],
        context: Dict[str, Any]
    ) -> List[str]:
        """
        处理循环分支
        
        Args:
            node_id: 循环节点ID
            node_result: 循环节点执行结果
            edge_map: 边映射
            context: 执行上下文
            
        Returns:
            下一个要执行的节点ID列表（循环体节点）
        """
        # 查找循环体的出边（第一个出边通常是循环体，第二个可能是循环结束后的节点）
        all_next_nodes = edge_map.get(node_id, [])
        
        if not all_next_nodes:
            return []
        
        loop_body_node = all_next_nodes[0]
        
        # 初始化循环上下文（如果还没有）
        if "loop_contexts" not in context:
            context["loop_contexts"] = {}  # 存储每个循环节点的状态
        
        loop_context_key = f"loop_{node_id}"
        
        # 获取循环数据
        if isinstance(node_result, dict):
            loop_type = node_result.get("loop_type", "for")
            
            if loop_type == "for" or loop_type == "foreach":
                # for循环：遍历列表
                loop_items = node_result.get("items", [])
                
                if loop_context_key not in context["loop_contexts"]:
                    # 初始化循环状态
                    context["loop_contexts"][loop_context_key] = {
                        "type": "for",
                        "items": loop_items,
                        "current_index": 0,
                        "total_count": len(loop_items),
                        "item_var": node_result.get("item_var", "item"),
                        "index_var": node_result.get("index_var", "index"),
                        "completed": False
                    }
                
                loop_state = context["loop_contexts"][loop_context_key]
                
                # 检查是否还有未处理的项
                if loop_state["current_index"] < loop_state["total_count"]:
                    # 设置当前迭代的上下文变量
                    current_item = loop_state["items"][loop_state["current_index"]]
                    context["current_loop_item"] = current_item
                    context["current_loop_index"] = loop_state["current_index"]
                    context["loop_item_var"] = loop_state["item_var"]
                    context["loop_index_var"] = loop_state["index_var"]
                    context["is_first_iteration"] = loop_state["current_index"] == 0
                    context["is_last_iteration"] = loop_state["current_index"] == loop_state["total_count"] - 1
                    
                    # 返回循环体节点，执行一次迭代
                    return [loop_body_node]
                else:
                    # 循环完成，继续执行后续节点
                    loop_state["completed"] = True
                    context["loop_contexts"].pop(loop_context_key, None)
                    # 如果有第二个出边，表示循环结束后的节点
                    if len(all_next_nodes) > 1:
                        return [all_next_nodes[1]]
                    return []
                    
            elif loop_type == "while":
                # while循环：根据条件循环
                condition = node_result.get("condition")
                max_iterations = node_result.get("max_iterations", 100)
                
                if loop_context_key not in context["loop_contexts"]:
                    # 初始化循环状态
                    context["loop_contexts"][loop_context_key] = {
                        "type": "while",
                        "condition": condition,
                        "current_iteration": 0,
                        "max_iterations": max_iterations,
                        "completed": False
                    }
                
                loop_state = context["loop_contexts"][loop_context_key]
                
                # 检查是否超过最大迭代次数
                if loop_state["current_iteration"] >= loop_state["max_iterations"]:
                    logger.warning(f"while循环达到最大迭代次数: {max_iterations}")
                    loop_state["completed"] = True
                    context["loop_contexts"].pop(loop_context_key, None)
                    if len(all_next_nodes) > 1:
                        return [all_next_nodes[1]]
                    return []
                
                # 评估条件
                try:
                    eval_context = {
                        "input_data": context.get("input_data"),
                        "context": context,
                        "data": context.get("node_outputs", {}),
                        "iteration": loop_state["current_iteration"],
                        "len": len,
                        "str": str,
                        "int": int,
                        "float": float,
                        "bool": bool
                    }
                    condition_result = eval(condition, {"__builtins__": {}}, eval_context)
                    
                    if bool(condition_result):
                        # 条件满足，执行循环体
                        context["current_loop_iteration"] = loop_state["current_iteration"]
                        context["is_first_iteration"] = loop_state["current_iteration"] == 0
                        return [loop_body_node]
                    else:
                        # 条件不满足，循环结束
                        loop_state["completed"] = True
                        context["loop_contexts"].pop(loop_context_key, None)
                        if len(all_next_nodes) > 1:
                            return [all_next_nodes[1]]
                        return []
                except Exception as e:
                    logger.error(f"while循环条件评估失败: {condition}, 错误: {str(e)}")
                    loop_state["completed"] = True
                    context["loop_contexts"].pop(loop_context_key, None)
                    if len(all_next_nodes) > 1:
                        return [all_next_nodes[1]]
                    return []
        
        # 默认返回循环体节点
        return [loop_body_node]
    
    def _execute_single_node(self, node: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行单个节点
        
        Args:
            node: 节点配置
            context: 执行上下文
            
        Returns:
            节点执行结果
        """
        node_type = node.get("type")
        node_id = node.get("id")
        node_config = node.get("config", {})
        
        logger.info(f"执行节点: {node_id}, 类型: {node_type}")
        
        # 获取节点执行器
        executor = self.node_executors.get(node_type)
        if executor is None:
            raise ValueError(f"未找到节点类型 {node_type} 的执行器")
        
        # 准备节点输入数据
        input_data = self._prepare_node_input(node, context)
        
        # 执行节点
        try:
            result = executor(input_data, node_config, context)
            logger.info(f"节点执行成功: {node_id}")
            return result
        except Exception as e:
            logger.error(f"节点执行失败: {node_id}, 错误: {str(e)}", exc_info=True)
            raise
    
    def _prepare_node_input(self, node: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        准备节点输入数据
        
        Args:
            node: 节点配置
            context: 执行上下文
            
        Returns:
            节点输入数据
        """
        node_type = node.get("type")
        
        # 输入节点使用全局输入数据
        if node_type == "input":
            return context.get("input_data")
        
        # 其他节点从上游节点获取数据
        # 这里简化处理，实际应该根据边的配置获取上游节点的输出
        # 暂时返回上下文中的最新输出
        node_outputs = context.get("node_outputs", {})
        if node_outputs:
            # 返回最后一个节点的输出
            last_output = list(node_outputs.values())[-1]
            return last_output
        
        return context.get("input_data")
    
    def record_execution(
        self,
        execution_id: str,
        workflow_id: int,
        workflow_code: str,
        workflow_version: int,
        status: str,
        input_data: Optional[Dict] = None,
        output_data: Optional[Dict] = None,
        error_message: Optional[str] = None,
        error_node_id: Optional[str] = None,
        execution_log: Optional[Dict] = None
    ):
        """
        记录工作流执行记录到数据库
        
        Args:
            execution_id: 执行ID
            workflow_id: 工作流定义ID
            workflow_code: 工作流代码
            workflow_version: 工作流版本
            status: 执行状态
            input_data: 输入数据
            output_data: 输出数据
            error_message: 错误信息
            error_node_id: 错误节点ID
            execution_log: 执行日志
        """
        try:
            sql = text("""
                INSERT INTO workflow_execution (
                    execution_id, workflow_id, workflow_code, workflow_version,
                    execution_type, input_data, output_data, status,
                    error_message, error_node_id, execution_log,
                    start_time, create_time
                ) VALUES (
                    :execution_id, :workflow_id, :workflow_code, :workflow_version,
                    :execution_type, :input_data, :output_data, :status,
                    :error_message, :error_node_id, :execution_log,
                    :start_time, :create_time
                )
            """)
            
            self.db.execute(sql, {
                'execution_id': execution_id,
                'workflow_id': workflow_id,
                'workflow_code': workflow_code,
                'workflow_version': workflow_version,
                'execution_type': 'API',
                'input_data': json.dumps(input_data, ensure_ascii=False) if input_data else None,
                'output_data': json.dumps(output_data, ensure_ascii=False) if output_data else None,
                'status': status,
                'error_message': error_message,
                'error_node_id': error_node_id,
                'execution_log': json.dumps(execution_log, ensure_ascii=False) if execution_log else None,
                'start_time': datetime.now(),
                'create_time': datetime.now()
            })
            
            self.db.commit()
            
        except Exception as e:
            logger.error(f"记录工作流执行失败: {execution_id}, 错误: {str(e)}", exc_info=True)
            self.db.rollback()
