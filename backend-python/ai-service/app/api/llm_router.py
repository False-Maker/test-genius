"""
大模型调用API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import Optional, List
from sqlalchemy.orm import Session
import logging
import time
from app.database import get_db
from app.services.llm_service import LLMService

router = APIRouter()
logger = logging.getLogger(__name__)


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
    start_time = time.time()
    prompt_length = len(request.prompt) if request.prompt else 0
    
    logger.info(
        f"收到模型调用请求: model_code={request.model_code}, "
        f"prompt_length={prompt_length}, "
        f"max_tokens={request.max_tokens}, "
        f"temperature={request.temperature}"
    )
    
    try:
        if not request.model_code:
            logger.warning("参数验证失败: 缺少模型代码")
            raise HTTPException(status_code=400, detail="必须提供模型代码(model_code)")
        
        if not request.prompt or not request.prompt.strip():
            logger.warning("参数验证失败: 提示词为空")
            raise HTTPException(status_code=400, detail="提示词不能为空")
        
        llm_service = LLMService(db)
        result = llm_service.call_model(
            model_code=request.model_code,
            prompt=request.prompt,
            max_tokens=request.max_tokens,
            temperature=request.temperature
        )
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"模型调用成功: model_code={request.model_code}, "
            f"响应时间={result.get('response_time', elapsed_time)}ms, "
            f"tokens={result.get('tokens_used')}"
        )
        
        return LLMResponse(**result)
    except HTTPException:
        # 重新抛出HTTP异常
        raise
    except ValueError as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"模型调用失败(参数错误): model_code={request.model_code}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"模型调用失败: model_code={request.model_code}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
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
    start_time = time.time()
    request_count = len(batch_request.requests) if batch_request.requests else 0
    
    logger.info(f"收到批量模型调用请求: 请求数量={request_count}")
    
    try:
        if not batch_request.requests or len(batch_request.requests) == 0:
            logger.warning("参数验证失败: 批量请求列表为空")
            raise HTTPException(status_code=400, detail="批量请求列表不能为空")
        
        if len(batch_request.requests) > 10:
            logger.warning(f"参数验证失败: 批量请求数量过多({request_count})")
            raise HTTPException(status_code=400, detail="批量请求数量不能超过10个")
        
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
        
        elapsed_time = int((time.time() - start_time) * 1000)
        success_count = sum(1 for r in results if "error" not in r)
        logger.info(
            f"批量模型调用完成: 请求数量={request_count}, "
            f"成功={success_count}, "
            f"失败={request_count - success_count}, "
            f"耗时={elapsed_time}ms"
        )
        
        return {"results": results}
    except HTTPException:
        # 重新抛出HTTP异常
        raise
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"批量模型调用失败: 请求数量={request_count}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(status_code=500, detail=f"批量调用失败: {str(e)}")
