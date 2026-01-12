"""
大模型调用API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional, List
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.llm_service import LLMService

router = APIRouter()


class LLMRequest(BaseModel):
    """模型调用请求"""
    model_code: str
    prompt: str
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None


class LLMResponse(BaseModel):
    """模型响应"""
    content: str
    model_code: str
    tokens_used: Optional[int] = None
    response_time: Optional[int] = None


class BatchLLMRequest(BaseModel):
    """批量模型调用请求"""
    requests: List[LLMRequest]


@router.post("/call", response_model=LLMResponse)
async def call_llm(request: LLMRequest, db: Session = Depends(get_db)):
    """
    调用大模型生成内容
    
    Args:
        request: 模型调用请求
        db: 数据库会话
        
    Returns:
        模型响应结果
    """
    try:
        llm_service = LLMService(db)
        result = llm_service.call_model(
            model_code=request.model_code,
            prompt=request.prompt,
            max_tokens=request.max_tokens,
            temperature=request.temperature
        )
        return LLMResponse(**result)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"模型调用失败: {str(e)}")


@router.post("/batch-call")
async def batch_call_llm(batch_request: BatchLLMRequest, db: Session = Depends(get_db)):
    """
    批量调用大模型
    
    Args:
        batch_request: 批量请求
        db: 数据库会话
        
    Returns:
        响应列表
    """
    try:
        llm_service = LLMService(db)
        
        # 转换请求格式
        requests = [
            {
                "model_code": req.model_code,
                "prompt": req.prompt,
                "max_tokens": req.max_tokens,
                "temperature": req.temperature
            }
            for req in batch_request.requests
        ]
        
        results = llm_service.batch_call(requests)
        return {"results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"批量调用失败: {str(e)}")
