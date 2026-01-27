"""
控制节点实现（条件节点、循环节点）
"""
import re
import logging
from typing import Dict, Any, List
from .base_node import BaseNode

logger = logging.getLogger(__name__)


class ConditionNode(BaseNode):
    """条件判断节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行条件判断节点
        
        Args:
            input_data: 输入数据
            config: 节点配置
                - condition: 条件表达式（支持Python表达式，如 "value > 10", "status == 'success'"）
                - condition_expression: 旧版配置（兼容）
                - field: 字段名（简化配置）
                - operator: 操作符（eq/ne/gt/gte/lt/lte/in/contains）
                - value: 比较值
            context: 执行上下文
            
        Returns:
            条件判断结果，包含condition_result字段
        """
        # 支持多种配置方式
        condition = config.get("condition") or config.get("condition_expression")
        
        if condition:
            # 使用条件表达式
            result = self._evaluate_expression(condition, input_data, context)
        else:
            # 使用简化配置（field + operator + value）
            field = config.get("field")
            operator = config.get("operator", "eq")
            value = config.get("value")
            
            if field:
                result = self._evaluate_field_condition(input_data, field, operator, value)
            else:
                # 默认返回True
                result = True
        
        # 返回结果，保留原数据并添加条件判断结果
        output = input_data.copy() if isinstance(input_data, dict) else {"data": input_data}
        output["condition_result"] = result
        output["_condition_metadata"] = {
            "condition": condition or f"{field} {operator} {value}",
            "result": result
        }
        
        return output
    
    def _evaluate_expression(self, expression: str, input_data: Any, context: Dict[str, Any]) -> bool:
        """
        评估条件表达式
        
        支持：
        - 简单比较：value > 10, status == 'success'
        - 字段访问：data.value, context.execution_id
        - 逻辑运算：and, or, not
        """
        try:
            # 准备评估环境
            eval_context = {
                "input_data": input_data,
                "context": context,
                "data": input_data if isinstance(input_data, dict) else {"value": input_data},
                "len": len,
                "str": str,
                "int": int,
                "float": float,
                "bool": bool
            }
            
            # 安全评估（限制可用的内置函数）
            # 注意：这里使用eval，在生产环境中应该使用更安全的表达式解析器
            result = eval(expression, {"__builtins__": {}}, eval_context)
            return bool(result)
        except Exception as e:
            logger.error(f"条件表达式评估失败: {expression}, 错误: {str(e)}")
            return False
    
    def _evaluate_field_condition(self, input_data: Any, field: str, operator: str, value: Any) -> bool:
        """评估字段条件"""
        if not isinstance(input_data, dict):
            return False
        
        if field not in input_data:
            return False
        
        field_value = input_data[field]
        return self._evaluate_condition(field_value, operator, value)
    
    def _evaluate_condition(self, field_value: Any, operator: str, value: Any) -> bool:
        """评估条件"""
        try:
            if operator == "eq" or operator == "==":
                return field_value == value
            elif operator == "ne" or operator == "!=":
                return field_value != value
            elif operator == "gt" or operator == ">":
                return field_value > value
            elif operator == "gte" or operator == ">=":
                return field_value >= value
            elif operator == "lt" or operator == "<":
                return field_value < value
            elif operator == "lte" or operator == "<=":
                return field_value <= value
            elif operator == "in":
                return field_value in value if isinstance(value, (list, tuple, str)) else False
            elif operator == "contains":
                if isinstance(field_value, str) and isinstance(value, str):
                    return value in field_value
                elif isinstance(field_value, (list, tuple)):
                    return value in field_value
                return False
            elif operator == "not_in":
                return field_value not in value if isinstance(value, (list, tuple, str)) else True
            elif operator == "is_empty":
                return not field_value or (isinstance(field_value, str) and len(field_value.strip()) == 0)
            elif operator == "is_not_empty":
                return bool(field_value) and (not isinstance(field_value, str) or len(field_value.strip()) > 0)
            else:
                logger.warning(f"未知的操作符: {operator}")
                return True
        except Exception as e:
            logger.error(f"条件评估失败: {field_value} {operator} {value}, 错误: {str(e)}")
            return False


class LoopNode(BaseNode):
    """循环节点"""
    
    def execute(self, input_data: Any, config: Dict[str, Any], context: Dict[str, Any]) -> Any:
        """
        执行循环节点
        
        Args:
            input_data: 输入数据
            config: 节点配置
                - loop_type: 循环类型（for/while/foreach）
                - max_iterations: 最大迭代次数（默认100）
                - condition: while循环的条件表达式
                - item_var: 循环变量名（默认item）
                - index_var: 索引变量名（默认index）
            context: 执行上下文
            
        Returns:
            循环处理后的结果
        """
        loop_type = config.get("loop_type", "for")
        max_iterations = config.get("max_iterations", 100)
        item_var = config.get("item_var", "item")
        index_var = config.get("index_var", "index")
        
        if loop_type == "for" or loop_type == "foreach":
            # for循环：遍历列表
            return self._execute_for_loop(input_data, max_iterations, item_var, index_var, context)
        elif loop_type == "while":
            # while循环：根据条件循环
            return self._execute_while_loop(input_data, config, max_iterations, context)
        else:
            logger.warning(f"未知的循环类型: {loop_type}")
            return input_data
    
    def _execute_for_loop(
        self, 
        input_data: Any, 
        max_iterations: int, 
        item_var: str, 
        index_var: str,
        context: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """执行for循环"""
        if not isinstance(input_data, list):
            # 如果不是列表，尝试转换为列表
            if isinstance(input_data, dict):
                # 如果是字典，遍历值
                input_data = list(input_data.values())
            else:
                # 其他类型，包装成列表
                input_data = [input_data]
        
        results = []
        loop_context = context.get("loop_context", {})
        
        for i, item in enumerate(input_data[:max_iterations]):
            # 创建循环迭代上下文
            iteration_context = {
                **context,
                "loop_context": {
                    **loop_context,
                    item_var: item,
                    index_var: i,
                    "current_index": i,
                    "total_count": len(input_data),
                    "is_first": i == 0,
                    "is_last": i == len(input_data) - 1
                }
            }
            
            # 存储迭代结果
            iteration_result = {
                "index": i,
                item_var: item,
                "iteration_context": iteration_context["loop_context"]
            }
            results.append(iteration_result)
        
        # 更新上下文
        context["loop_context"] = loop_context
        context["loop_results"] = results
        
        return {
            "loop_type": "for",
            "iterations": len(results),
            "items": results,
            "last_item": results[-1] if results else None
        }
    
    def _execute_while_loop(
        self, 
        input_data: Any, 
        config: Dict[str, Any], 
        max_iterations: int,
        context: Dict[str, Any]
    ) -> Any:
        """执行while循环"""
        condition = config.get("condition")
        if not condition:
            logger.warning("while循环缺少condition配置")
            return input_data
        
        results = []
        current_data = input_data
        iteration = 0
        loop_context = context.get("loop_context", {})
        
        while iteration < max_iterations:
            # 评估条件
            try:
                eval_context = {
                    "input_data": current_data,
                    "context": context,
                    "data": current_data if isinstance(current_data, dict) else {"value": current_data},
                    "iteration": iteration,
                    "len": len,
                    "str": str,
                    "int": int,
                    "float": float,
                    "bool": bool
                }
                condition_result = eval(condition, {"__builtins__": {}}, eval_context)
                
                if not bool(condition_result):
                    break
            except Exception as e:
                logger.error(f"while循环条件评估失败: {condition}, 错误: {str(e)}")
                break
            
            # 执行循环体（这里只是记录，实际执行由工作流引擎处理）
            iteration_context = {
                **context,
                "loop_context": {
                    **loop_context,
                    "iteration": iteration,
                    "current_data": current_data,
                    "is_first": iteration == 0
                }
            }
            
            results.append({
                "iteration": iteration,
                "data": current_data,
                "iteration_context": iteration_context["loop_context"]
            })
            
            iteration += 1
        
        # 更新上下文
        context["loop_context"] = loop_context
        context["loop_results"] = results
        
        return {
            "loop_type": "while",
            "iterations": iteration,
            "items": results,
            "last_item": results[-1] if results else None,
            "final_data": current_data
        }
