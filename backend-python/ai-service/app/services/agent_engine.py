"""
Agent执行引擎
支持Function Calling和ReAct模式
"""
import json
import logging
import time
from typing import Dict, Any, List, Optional, Callable
from abc import ABC, abstractmethod
from sqlalchemy.orm import Session
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


class BaseTool(ABC):
    """工具基类"""
    
    def __init__(self, name: str, description: str, schema: Dict[str, Any]):
        """
        初始化工具
        
        Args:
            name: 工具名称
            description: 工具描述
            schema: 工具schema（OpenAPI格式）
        """
        self.name = name
        self.description = description
        self.schema = schema
    
    @abstractmethod
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行工具
        
        Args:
            arguments: 工具参数
            context: 执行上下文
            
        Returns:
            工具执行结果
        """
        pass
    
    def get_schema(self) -> Dict[str, Any]:
        """获取工具schema"""
        return self.schema


class AgentEngine:
    """Agent执行引擎"""
    
    def __init__(self, db: Session, agent_config: Dict[str, Any]):
        """
        初始化Agent引擎
        
        Args:
            db: 数据库会话
            agent_config: Agent配置
        """
        self.db = db
        self.agent_config = agent_config
        self.llm_service = LLMService(db)
        self.tools: Dict[str, BaseTool] = {}
        self.max_iterations = agent_config.get("max_iterations", 10)
        self.max_tokens = agent_config.get("max_tokens", 4000)
        self.temperature = agent_config.get("temperature", 0.7)
        self.model_code = agent_config.get("model_code")
        self.system_prompt = agent_config.get("system_prompt", "")
        self.mode = agent_config.get("mode", "function_calling")  # function_calling 或 react
    
    def register_tool(self, tool: BaseTool):
        """注册工具"""
        self.tools[tool.name] = tool
        logger.info(f"注册工具: {tool.name}")
    
    def register_tools(self, tools: List[BaseTool]):
        """批量注册工具"""
        for tool in tools:
            self.register_tool(tool)
    
    def _build_tools_schema(self) -> List[Dict[str, Any]]:
        """构建工具schema列表（用于Function Calling）"""
        return [tool.get_schema() for tool in self.tools.values()]
    
    def _build_system_prompt(self) -> str:
        """构建系统提示词"""
        prompt = self.system_prompt
        
        if self.tools:
            prompt += "\n\n你可以使用以下工具：\n"
            for tool in self.tools.values():
                prompt += f"- {tool.name}: {tool.description}\n"
        
        if self.mode == "react":
            prompt += "\n\n请使用ReAct模式：思考(Thought) -> 行动(Action) -> 观察(Observation) -> 思考(Thought) -> ... -> 最终答案(Final Answer)"
        
        return prompt
    
    def _parse_function_call(self, response: str) -> Optional[Dict[str, Any]]:
        """
        解析模型响应中的函数调用
        支持JSON格式的函数调用
        
        Args:
            response: 模型响应
            
        Returns:
            函数调用信息，格式：{"name": "tool_name", "arguments": {...}}
        """
        try:
            # 尝试解析JSON格式的函数调用
            if "```json" in response:
                json_start = response.find("```json") + 7
                json_end = response.find("```", json_start)
                if json_end > json_start:
                    json_str = response[json_start:json_end].strip()
                    func_call = json.loads(json_str)
                    if "name" in func_call and "arguments" in func_call:
                        return func_call
            elif "{" in response and "}" in response:
                # 尝试提取JSON对象
                json_start = response.find("{")
                json_end = response.rfind("}") + 1
                if json_end > json_start:
                    json_str = response[json_start:json_end]
                    func_call = json.loads(json_str)
                    if "name" in func_call and "arguments" in func_call:
                        return func_call
        except Exception as e:
            logger.warning(f"解析函数调用失败: {str(e)}")
        
        return None
    
    def _execute_tool(self, tool_name: str, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行工具
        
        Args:
            tool_name: 工具名称
            arguments: 工具参数
            context: 执行上下文
            
        Returns:
            工具执行结果
        """
        if tool_name not in self.tools:
            return {
                "success": False,
                "error": f"工具 {tool_name} 不存在"
            }
        
        try:
            tool = self.tools[tool_name]
            result = tool.execute(arguments, context)
            return {
                "success": True,
                "result": result
            }
        except Exception as e:
            logger.error(f"工具执行失败: {tool_name}, 错误: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }
    
    def execute_function_calling(self, user_message: str, context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行Function Calling模式
        
        Args:
            user_message: 用户消息
            context: 上下文（包含对话历史等）
            
        Returns:
            Agent响应结果
        """
        conversation_history = context.get("conversation_history", []) if context else []
        
        # 构建提示词
        system_prompt = self._build_system_prompt()
        tools_schema = self._build_tools_schema()
        
        # 构建完整提示词（包含工具信息）
        prompt = system_prompt + "\n\n"
        
        # 添加对话历史
        if conversation_history:
            prompt += "对话历史：\n"
            for msg in conversation_history[-5:]:  # 只保留最近5轮对话
                role = msg.get("role", "user")
                content = msg.get("content", "")
                prompt += f"{role}: {content}\n"
        
        prompt += f"\n用户: {user_message}\n助手: "
        
        # 添加工具使用说明
        if tools_schema:
            prompt += "\n\n如果需要使用工具，请以JSON格式回复，格式：\n"
            prompt += '{"name": "tool_name", "arguments": {"param1": "value1", ...}}\n'
            prompt += "如果不需要使用工具，直接回复答案。\n"
        
        iteration = 0
        tool_calls_history = []
        
        while iteration < self.max_iterations:
            iteration += 1
            logger.info(f"Function Calling迭代 {iteration}/{self.max_iterations}")
            
            # 调用模型
            try:
                response = self.llm_service.call_model(
                    model_code=self.model_code,
                    prompt=prompt,
                    max_tokens=self.max_tokens,
                    temperature=self.temperature
                )
                content = response.get("content", "")
                
                # 尝试解析函数调用
                func_call = self._parse_function_call(content)
                
                if func_call:
                    # 需要调用工具
                    tool_name = func_call.get("name")
                    tool_args = func_call.get("arguments", {})
                    
                    logger.info(f"检测到工具调用: {tool_name}, 参数: {tool_args}")
                    
                    # 执行工具
                    tool_result = self._execute_tool(tool_name, tool_args, context)
                    tool_calls_history.append({
                        "tool": tool_name,
                        "arguments": tool_args,
                        "result": tool_result
                    })
                    
                    # 将工具结果添加到提示词中，继续迭代
                    prompt += content + "\n"
                    prompt += f"工具 {tool_name} 执行结果: {json.dumps(tool_result, ensure_ascii=False)}\n"
                    prompt += "请根据工具执行结果继续处理，如果需要使用其他工具，请继续调用；如果已经得到答案，请直接回复最终答案。\n助手: "
                else:
                    # 不需要调用工具，返回最终答案
                    logger.info("获得最终答案，结束迭代")
                    return {
                        "content": content,
                        "tool_calls": tool_calls_history,
                        "iterations": iteration,
                        "model_code": response.get("model_code"),
                        "tokens_used": response.get("tokens_used"),
                        "response_time": response.get("response_time")
                    }
            except Exception as e:
                logger.error(f"Function Calling迭代失败: {str(e)}", exc_info=True)
                return {
                    "content": f"处理过程中发生错误: {str(e)}",
                    "tool_calls": tool_calls_history,
                    "iterations": iteration,
                    "error": str(e)
                }
        
        # 达到最大迭代次数
        logger.warning(f"达到最大迭代次数 {self.max_iterations}")
        return {
            "content": "已达到最大迭代次数，请简化问题或增加迭代次数限制",
            "tool_calls": tool_calls_history,
            "iterations": iteration,
            "error": "max_iterations_exceeded"
        }
    
    def execute_react(self, user_message: str, context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行ReAct模式
        
        Args:
            user_message: 用户消息
            context: 上下文
            
        Returns:
            Agent响应结果
        """
        conversation_history = context.get("conversation_history", []) if context else []
        
        # 构建提示词
        system_prompt = self._build_system_prompt()
        
        prompt = system_prompt + "\n\n"
        
        # 添加对话历史
        if conversation_history:
            prompt += "对话历史：\n"
            for msg in conversation_history[-5:]:
                role = msg.get("role", "user")
                content = msg.get("content", "")
                prompt += f"{role}: {content}\n"
        
        prompt += f"\n用户: {user_message}\n\n"
        prompt += "请使用ReAct模式回答，格式：\n"
        prompt += "Thought: [你的思考]\n"
        prompt += "Action: [工具名称] 或 Final Answer\n"
        prompt += "Action Input: [工具参数，如果是工具调用]\n"
        prompt += "Observation: [工具执行结果，如果是工具调用]\n"
        prompt += "...\n"
        prompt += "Final Answer: [最终答案]\n\n"
        prompt += "开始：\n"
        
        iteration = 0
        tool_calls_history = []
        full_response = ""
        
        while iteration < self.max_iterations:
            iteration += 1
            logger.info(f"ReAct迭代 {iteration}/{self.max_iterations}")
            
            try:
                # 调用模型
                response = self.llm_service.call_model(
                    model_code=self.model_code,
                    prompt=prompt,
                    max_tokens=self.max_tokens,
                    temperature=self.temperature
                )
                content = response.get("content", "")
                full_response += content + "\n"
                
                # 解析ReAct响应
                if "Final Answer:" in content:
                    # 找到最终答案
                    final_answer_start = content.find("Final Answer:") + len("Final Answer:")
                    final_answer = content[final_answer_start:].strip()
                    logger.info("获得最终答案，结束迭代")
                    return {
                        "content": final_answer,
                        "full_response": full_response,
                        "tool_calls": tool_calls_history,
                        "iterations": iteration,
                        "model_code": response.get("model_code"),
                        "tokens_used": response.get("tokens_used"),
                        "response_time": response.get("response_time")
                    }
                elif "Action:" in content:
                    # 需要执行动作
                    action_start = content.find("Action:") + len("Action:")
                    action_end = content.find("\n", action_start)
                    if action_end == -1:
                        action_end = len(content)
                    action = content[action_start:action_end].strip()
                    
                    # 提取Action Input
                    action_input = ""
                    if "Action Input:" in content:
                        input_start = content.find("Action Input:") + len("Action Input:")
                        input_end = content.find("\n", input_start)
                        if input_end == -1:
                            input_end = len(content)
                        action_input = content[input_start:input_end].strip()
                    
                    # 如果是工具调用
                    if action in self.tools:
                        try:
                            tool_args = json.loads(action_input) if action_input else {}
                        except:
                            tool_args = {}
                        
                        logger.info(f"ReAct工具调用: {action}, 参数: {tool_args}")
                        
                        # 执行工具
                        tool_result = self._execute_tool(action, tool_args, context)
                        tool_calls_history.append({
                            "tool": action,
                            "arguments": tool_args,
                            "result": tool_result
                        })
                        
                        # 将观察结果添加到提示词
                        prompt += content + "\n"
                        prompt += f"Observation: {json.dumps(tool_result, ensure_ascii=False)}\n\n"
                    else:
                        # 不是工具调用，可能是其他动作
                        prompt += content + "\n"
                        prompt += f"Observation: 未知动作 {action}\n\n"
                else:
                    # 继续思考
                    prompt += content + "\n"
            except Exception as e:
                logger.error(f"ReAct迭代失败: {str(e)}", exc_info=True)
                return {
                    "content": f"处理过程中发生错误: {str(e)}",
                    "full_response": full_response,
                    "tool_calls": tool_calls_history,
                    "iterations": iteration,
                    "error": str(e)
                }
        
        # 达到最大迭代次数
        logger.warning(f"达到最大迭代次数 {self.max_iterations}")
        return {
            "content": "已达到最大迭代次数，请简化问题或增加迭代次数限制",
            "full_response": full_response,
            "tool_calls": tool_calls_history,
            "iterations": iteration,
            "error": "max_iterations_exceeded"
        }
    
    def execute(self, user_message: str, context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行Agent（根据模式选择Function Calling或ReAct）
        
        Args:
            user_message: 用户消息
            context: 上下文
            
        Returns:
            Agent响应结果
        """
        if self.mode == "react":
            return self.execute_react(user_message, context)
        else:
            return self.execute_function_calling(user_message, context)

