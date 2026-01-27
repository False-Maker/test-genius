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


class ParallelCallRequest(BaseModel):
    """并行调用请求（用于模型性能对比）"""
    prompt: str
    model_codes: List[str] = Field(..., min_items=1, max_items=10, description="模型代码列表")
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    max_workers: int = Field(default=5, ge=1, le=10, description="最大并发数")


class ParallelCallResponse(BaseModel):
    """并行调用响应"""
    results: List[LLMResponse]
    total_time: int = Field(description="总耗时（毫秒）")
    success_count: int = Field(description="成功数量")
    fail_count: int = Field(description="失败数量")


@router.post("/parallel-call", response_model=ParallelCallResponse)
async def parallel_call_llm(request: ParallelCallRequest, db: Session = Depends(get_db)):
    """
    并行调用多个模型（用于性能对比）
    
    同时调用多个模型，返回所有模型的响应，用于对比性能和效果。
    
    Args:
        request: 并行调用请求
        db: 数据库会话
        
    Returns:
        所有模型的响应结果
    """
    start_time = time.time()
    model_count = len(request.model_codes) if request.model_codes else 0
    
    logger.info(
        f"收到并行模型调用请求: 模型数量={model_count}, "
        f"prompt_length={len(request.prompt) if request.prompt else 0}"
    )
    
    try:
        if not request.prompt or not request.prompt.strip():
            logger.warning("参数验证失败: 提示词为空")
            raise HTTPException(status_code=400, detail="提示词不能为空")
        
        if not request.model_codes or len(request.model_codes) == 0:
            logger.warning("参数验证失败: 模型代码列表为空")
            raise HTTPException(status_code=400, detail="模型代码列表不能为空")
        
        if len(request.model_codes) > 10:
            logger.warning(f"参数验证失败: 模型数量过多({model_count})")
            raise HTTPException(status_code=400, detail="模型数量不能超过10个")
        
        llm_service = LLMService(db)
        
        # 并行调用多个模型
        results = llm_service.parallel_call(
            prompt=request.prompt,
            model_codes=request.model_codes,
            max_tokens=request.max_tokens,
            temperature=request.temperature,
            max_workers=request.max_workers
        )
        
        # 转换为响应格式
        response_results = []
        success_count = 0
        fail_count = 0
        
        for result in results:
            if "error" in result:
                fail_count += 1
                response_results.append(LLMResponse(
                    content="",
                    model_code=result.get("model_code", ""),
                    tokens_used=result.get("tokens_used"),
                    response_time=result.get("response_time")
                ))
            else:
                success_count += 1
                response_results.append(LLMResponse(**result))
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"并行模型调用完成: 模型数量={model_count}, "
            f"成功={success_count}, "
            f"失败={fail_count}, "
            f"总耗时={elapsed_time}ms"
        )
        
        return ParallelCallResponse(
            results=response_results,
            total_time=elapsed_time,
            success_count=success_count,
            fail_count=fail_count
        )
    except HTTPException:
        raise
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"并行模型调用失败: 模型数量={model_count}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(status_code=500, detail=f"并行调用失败: {str(e)}")
