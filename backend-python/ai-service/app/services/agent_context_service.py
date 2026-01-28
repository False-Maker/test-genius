"""
Agent上下文管理服务
负责多轮对话上下文、上下文窗口管理、持久化
"""
import json
import logging
from typing import Dict, Any, List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class AgentContextService:
    """Agent上下文管理服务"""
    
    def __init__(self, db: Session):
        """
        初始化上下文服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.max_context_tokens = 8000  # 最大上下文token数（可根据模型调整）
        self.max_history_messages = 20  # 最大历史消息数
    
    def get_session_context(self, session_id: int) -> Dict[str, Any]:
        """
        获取会话上下文
        
        Args:
            session_id: 会话ID
            
        Returns:
            上下文数据
        """
        try:
            # 从数据库查询会话上下文
            result = self.db.execute(
                text("SELECT context_data FROM agent_session WHERE id = :session_id"),
                {"session_id": session_id}
            )
            row = result.fetchone()
            
            if row and row[0]:
                context_data = row[0]
                if isinstance(context_data, str):
                    return json.loads(context_data)
                return context_data
            
            return {
                "conversation_history": [],
                "tool_calls_history": [],
                "metadata": {}
            }
        except Exception as e:
            logger.error(f"获取会话上下文失败: {str(e)}", exc_info=True)
            return {
                "conversation_history": [],
                "tool_calls_history": [],
                "metadata": {}
            }
    
    def save_session_context(self, session_id: int, context: Dict[str, Any]):
        """
        保存会话上下文
        
        Args:
            session_id: 会话ID
            context: 上下文数据
        """
        try:
            context_json = json.dumps(context, ensure_ascii=False)
            self.db.execute(
                text("UPDATE agent_session SET context_data = :context_data, update_time = CURRENT_TIMESTAMP WHERE id = :session_id"),
                {"session_id": session_id, "context_data": context_json}
            )
            self.db.commit()
        except Exception as e:
            logger.error(f"保存会话上下文失败: {str(e)}", exc_info=True)
            self.db.rollback()
            raise
    
    def add_message(self, session_id: int, role: str, content: str, message_type: str = "USER", 
                   tool_calls: Optional[List[Dict[str, Any]]] = None, tool_results: Optional[List[Dict[str, Any]]] = None):
        """
        添加消息到对话历史
        
        Args:
            session_id: 会话ID
            role: 角色（user/assistant/tool/system）
            content: 消息内容
            message_type: 消息类型（USER/ASSISTANT/TOOL/SYSTEM）
            tool_calls: 工具调用信息
            tool_results: 工具执行结果
        """
        try:
            # 插入消息记录
            self.db.execute(
                text("""
                    INSERT INTO agent_message (session_id, message_type, role, content, tool_calls, tool_results, create_time)
                    VALUES (:session_id, :message_type, :role, :content, :tool_calls, :tool_results, CURRENT_TIMESTAMP)
                """),
                {
                    "session_id": session_id,
                    "message_type": message_type,
                    "role": role,
                    "content": content,
                    "tool_calls": json.dumps(tool_calls, ensure_ascii=False) if tool_calls else None,
                    "tool_results": json.dumps(tool_results, ensure_ascii=False) if tool_results else None
                }
            )
            
            # 更新会话上下文
            context = self.get_session_context(session_id)
            conversation_history = context.get("conversation_history", [])
            
            message = {
                "role": role,
                "content": content
            }
            if tool_calls:
                message["tool_calls"] = tool_calls
            if tool_results:
                message["tool_results"] = tool_results
            
            conversation_history.append(message)
            
            # 限制历史消息数量
            if len(conversation_history) > self.max_history_messages:
                conversation_history = conversation_history[-self.max_history_messages:]
            
            context["conversation_history"] = conversation_history
            self.save_session_context(session_id, context)
            
            self.db.commit()
        except Exception as e:
            logger.error(f"添加消息失败: {str(e)}", exc_info=True)
            self.db.rollback()
            raise
    
    def get_conversation_history(self, session_id: int, limit: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        获取对话历史
        
        Args:
            session_id: 会话ID
            limit: 限制返回的消息数量（None表示不限制）
            
        Returns:
            对话历史列表
        """
        try:
            context = self.get_session_context(session_id)
            history = context.get("conversation_history", [])
            
            if limit:
                return history[-limit:]
            return history
        except Exception as e:
            logger.error(f"获取对话历史失败: {str(e)}", exc_info=True)
            return []
    
    def manage_context_window(self, context: Dict[str, Any], estimated_tokens: int) -> Dict[str, Any]:
        """
        管理上下文窗口（如果超出限制，进行压缩）
        
        Args:
            context: 上下文数据
            estimated_tokens: 估计的token数
            
        Returns:
            压缩后的上下文
        """
        if estimated_tokens <= self.max_context_tokens:
            return context
        
        # 超出限制，需要压缩
        logger.warning(f"上下文token数 {estimated_tokens} 超出限制 {self.max_context_tokens}，进行压缩")
        
        conversation_history = context.get("conversation_history", [])
        
        # 保留系统消息和最近的对话
        # 策略：保留前2条和后10条消息
        if len(conversation_history) > 12:
            compressed_history = conversation_history[:2] + conversation_history[-10:]
            context["conversation_history"] = compressed_history
            logger.info(f"压缩对话历史: {len(conversation_history)} -> {len(compressed_history)}")
        
        return context
    
    def estimate_tokens(self, text: str) -> int:
        """
        估算文本的token数（简单估算：1 token ≈ 4字符）
        
        Args:
            text: 文本内容
            
        Returns:
            估算的token数
        """
        return len(text) // 4
    
    def estimate_context_tokens(self, context: Dict[str, Any]) -> int:
        """
        估算上下文的token数
        
        Args:
            context: 上下文数据
            
        Returns:
            估算的token数
        """
        total = 0
        
        # 计算对话历史的token数
        conversation_history = context.get("conversation_history", [])
        for msg in conversation_history:
            content = msg.get("content", "")
            total += self.estimate_tokens(content)
            
            # 工具调用和结果
            if "tool_calls" in msg:
                total += self.estimate_tokens(json.dumps(msg["tool_calls"], ensure_ascii=False))
            if "tool_results" in msg:
                total += self.estimate_tokens(json.dumps(msg["tool_results"], ensure_ascii=False))
        
        # 计算元数据的token数
        metadata = context.get("metadata", {})
        total += self.estimate_tokens(json.dumps(metadata, ensure_ascii=False))
        
        return total
    
    def clear_context(self, session_id: int):
        """
        清空会话上下文
        
        Args:
            session_id: 会话ID
        """
        try:
            empty_context = {
                "conversation_history": [],
                "tool_calls_history": [],
                "metadata": {}
            }
            self.save_session_context(session_id, empty_context)
        except Exception as e:
            logger.error(f"清空上下文失败: {str(e)}", exc_info=True)
            raise

