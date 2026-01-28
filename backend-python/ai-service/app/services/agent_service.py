"""
Agent服务
整合Agent引擎、上下文管理、工具等
"""
import json
import logging
import uuid
from typing import Dict, Any, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.services.agent_engine import AgentEngine
from app.services.agent_context_service import AgentContextService
from app.services.agent_tool_registry import get_registry

logger = logging.getLogger(__name__)


class AgentService:
    """Agent服务"""
    
    def __init__(self, db: Session):
        """
        初始化Agent服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.context_service = AgentContextService(db)
    
    def get_agent_config(self, agent_id: int) -> Dict[str, Any]:
        """
        获取Agent配置
        
        Args:
            agent_id: Agent ID
            
        Returns:
            Agent配置
        """
        try:
            result = self.db.execute(
                text("""
                    SELECT agent_code, agent_name, agent_type, agent_config, system_prompt,
                           max_iterations, max_tokens, temperature
                    FROM agent WHERE id = :agent_id AND is_active = '1'
                """),
                {"agent_id": agent_id}
            )
            row = result.fetchone()
            
            if not row:
                raise ValueError(f"Agent {agent_id} 不存在或未启用")
            
            config = {
                "agent_id": agent_id,
                "agent_code": row[0],
                "agent_name": row[1],
                "agent_type": row[2],
                "agent_config": json.loads(row[3]) if row[3] else {},
                "system_prompt": row[4],
                "max_iterations": row[5] or 10,
                "max_tokens": row[6] or 4000,
                "temperature": float(row[7]) if row[7] else 0.7,
                "mode": "function_calling"  # 默认使用Function Calling模式
            }
            
            # 合并agent_config中的配置
            if config["agent_config"]:
                config.update(config["agent_config"])
            
            return config
        except Exception as e:
            logger.error(f"获取Agent配置失败: {str(e)}", exc_info=True)
            raise
    
    def get_agent_tools(self, agent_id: int) -> list:
        """
        获取Agent可用的工具列表
        
        Args:
            agent_id: Agent ID
            
        Returns:
            工具列表
        """
        try:
            result = self.db.execute(
                text("""
                    SELECT t.tool_code, t.tool_name, t.tool_type, t.tool_description,
                           t.tool_schema, t.tool_implementation, t.tool_config
                    FROM agent_tool t
                    INNER JOIN agent_tool_relation r ON t.id = r.tool_id
                    WHERE r.agent_id = :agent_id AND r.is_enabled = '1' AND t.is_active = '1'
                    ORDER BY r.tool_order ASC
                """),
                {"agent_id": agent_id}
            )
            
            tools = []
            for row in result:
                tools.append({
                    "tool_code": row[0],
                    "tool_name": row[1],
                    "tool_type": row[2],
                    "tool_description": row[3],
                    "tool_schema": json.loads(row[4]) if row[4] else {},
                    "tool_implementation": row[5],
                    "tool_config": json.loads(row[6]) if row[6] else {}
                })
            
            return tools
        except Exception as e:
            logger.error(f"获取Agent工具列表失败: {str(e)}", exc_info=True)
            return []
    
    def create_session(self, agent_id: int, user_id: Optional[int] = None, 
                      user_name: Optional[str] = None, session_title: Optional[str] = None) -> Dict[str, Any]:
        """
        创建Agent会话
        
        Args:
            agent_id: Agent ID
            user_id: 用户ID
            user_name: 用户姓名
            session_title: 会话标题
            
        Returns:
            会话信息
        """
        try:
            # 生成会话编码
            session_code = f"SESSION-{uuid.uuid4().hex[:16].upper()}"
            
            # 插入会话记录
            result = self.db.execute(
                text("""
                    INSERT INTO agent_session (session_code, agent_id, user_id, user_name, session_title, status, create_time, update_time, last_active_time)
                    VALUES (:session_code, :agent_id, :user_id, :user_name, :session_title, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    RETURNING id
                """),
                {
                    "session_code": session_code,
                    "agent_id": agent_id,
                    "user_id": user_id,
                    "user_name": user_name,
                    "session_title": session_title
                }
            )
            session_id = result.fetchone()[0]
            
            # 初始化上下文
            empty_context = {
                "conversation_history": [],
                "tool_calls_history": [],
                "metadata": {}
            }
            self.context_service.save_session_context(session_id, empty_context)
            
            self.db.commit()
            
            return {
                "session_id": session_id,
                "session_code": session_code,
                "agent_id": agent_id
            }
        except Exception as e:
            logger.error(f"创建会话失败: {str(e)}", exc_info=True)
            self.db.rollback()
            raise
    
    def chat(self, session_id: int, user_message: str) -> Dict[str, Any]:
        """
        与Agent对话
        
        Args:
            session_id: 会话ID
            user_message: 用户消息
            
        Returns:
            Agent响应
        """
        try:
            # 获取会话信息
            session_result = self.db.execute(
                text("SELECT agent_id FROM agent_session WHERE id = :session_id"),
                {"session_id": session_id}
            )
            session_row = session_result.fetchone()
            if not session_row:
                raise ValueError(f"会话 {session_id} 不存在")
            
            agent_id = session_row[0]
            
            # 获取Agent配置
            agent_config = self.get_agent_config(agent_id)
            
            # 获取Agent工具
            agent_tools_config = self.get_agent_tools(agent_id)
            
            # 创建Agent引擎
            engine = AgentEngine(self.db, agent_config)
            
            # 使用工具注册表
            registry = get_registry()
            
            # 注册测试相关工具
            from app.services.agent_tools.test_tools import (
                SearchTestCasesTool, GetRequirementDetailsTool, 
                ValidateTestCaseTool, GenerateTestDataTool
            )
            
            # 注册通用工具
            from app.services.agent_tools.general_tools import (
                WebSearchTool, CodeAnalysisTool, DocumentParserTool
            )
            
            # 注册工具
            java_api_url = agent_config.get("java_api_base_url", "http://localhost:8080")
            engine.register_tool(SearchTestCasesTool(java_api_url))
            engine.register_tool(GetRequirementDetailsTool(java_api_url))
            engine.register_tool(ValidateTestCaseTool(java_api_url))
            engine.register_tool(GenerateTestDataTool())
            engine.register_tool(WebSearchTool())
            engine.register_tool(CodeAnalysisTool())
            engine.register_tool(DocumentParserTool())
            
            # 获取上下文
            context = self.context_service.get_session_context(session_id)
            
            # 管理上下文窗口
            estimated_tokens = self.context_service.estimate_context_tokens(context)
            context = self.context_service.manage_context_window(context, estimated_tokens)
            
            # 添加用户消息
            self.context_service.add_message(session_id, "user", user_message, "USER")
            
            # 执行Agent
            response = engine.execute(user_message, context)
            
            # 保存工具调用记录
            tool_calls = response.get("tool_calls", [])
            for tool_call in tool_calls:
                tool_name = tool_call.get("tool")
                tool_args = tool_call.get("arguments", {})
                tool_result = tool_call.get("result", {})
                
                self.db.execute(
                    text("""
                        INSERT INTO agent_tool_call (session_id, tool_code, tool_name, call_arguments, call_result, call_status, execution_time, create_time)
                        VALUES (:session_id, :tool_code, :tool_name, :call_arguments, :call_result, :call_status, :execution_time, CURRENT_TIMESTAMP)
                    """),
                    {
                        "session_id": session_id,
                        "tool_code": tool_name,
                        "tool_name": tool_name,
                        "call_arguments": json.dumps(tool_args, ensure_ascii=False),
                        "call_result": json.dumps(tool_result, ensure_ascii=False),
                        "call_status": "SUCCESS" if tool_result.get("success") else "FAILED",
                        "execution_time": 0  # 可以从tool_result中获取
                    }
                )
            
            # 添加Agent回复
            assistant_content = response.get("content", "")
            self.context_service.add_message(
                session_id, 
                "assistant", 
                assistant_content, 
                "ASSISTANT",
                tool_calls=tool_calls if tool_calls else None
            )
            
            # 更新会话统计
            self.db.execute(
                text("""
                    UPDATE agent_session 
                    SET total_tokens = total_tokens + :tokens,
                        total_iterations = total_iterations + :iterations,
                        update_time = CURRENT_TIMESTAMP,
                        last_active_time = CURRENT_TIMESTAMP
                    WHERE id = :session_id
                """),
                {
                    "session_id": session_id,
                    "tokens": response.get("tokens_used", 0),
                    "iterations": response.get("iterations", 1)
                }
            )
            
            self.db.commit()
            
            return {
                "content": assistant_content,
                "tool_calls": tool_calls,
                "iterations": response.get("iterations", 1),
                "tokens_used": response.get("tokens_used", 0),
                "response_time": response.get("response_time", 0)
            }
        except Exception as e:
            logger.error(f"Agent对话失败: {str(e)}", exc_info=True)
            self.db.rollback()
            raise

