"""
Agent API路由
"""
import logging
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.agent_service import AgentService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/agent", tags=["Agent"])


class CreateSessionRequest(BaseModel):
    """创建会话请求"""
    agent_id: int
    user_id: Optional[int] = None
    user_name: Optional[str] = None
    session_title: Optional[str] = None


class ChatRequest(BaseModel):
    """对话请求"""
    session_id: int
    message: str


class ChatResponse(BaseModel):
    """对话响应"""
    content: str
    tool_calls: list = []
    iterations: int = 1
    tokens_used: int = 0
    response_time: int = 0


@router.post("/sessions", summary="创建Agent会话")
async def create_session(
    request: CreateSessionRequest,
    db: Session = Depends(get_db)
):
    """
    创建Agent会话
    
    Args:
        request: 创建会话请求
        db: 数据库会话
        
    Returns:
        会话信息
    """
    try:
        agent_service = AgentService(db)
        session = agent_service.create_session(
            agent_id=request.agent_id,
            user_id=request.user_id,
            user_name=request.user_name,
            session_title=request.session_title
        )
        return {
            "code": 200,
            "message": "创建会话成功",
            "data": session
        }
    except Exception as e:
        logger.error(f"创建会话失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/chat", response_model=ChatResponse, summary="Agent对话")
async def chat(
    request: ChatRequest,
    db: Session = Depends(get_db)
):
    """
    与Agent对话
    
    Args:
        request: 对话请求
        db: 数据库会话
        
    Returns:
        Agent响应
    """
    try:
        agent_service = AgentService(db)
        response = agent_service.chat(
            session_id=request.session_id,
            user_message=request.message
        )
        return ChatResponse(**response)
    except ValueError as e:
        logger.error(f"对话失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        logger.error(f"对话失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/sessions/{session_id}/history", summary="获取对话历史")
async def get_conversation_history(
    session_id: int,
    limit: Optional[int] = None,
    db: Session = Depends(get_db)
):
    """
    获取对话历史
    
    Args:
        session_id: 会话ID
        limit: 限制返回的消息数量
        db: 数据库会话
        
    Returns:
        对话历史
    """
    try:
        agent_service = AgentService(db)
        context_service = agent_service.context_service
        history = context_service.get_conversation_history(session_id, limit)
        return {
            "code": 200,
            "message": "获取对话历史成功",
            "data": history
        }
    except Exception as e:
        logger.error(f"获取对话历史失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

