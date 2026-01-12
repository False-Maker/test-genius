"""
用例生成API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel, Field
from typing import Optional, List
from sqlalchemy.orm import Session
import logging
from app.database import get_db
from app.services.case_generation_service import CaseGenerationService

router = APIRouter()
logger = logging.getLogger(__name__)


class CaseGenerationRequest(BaseModel):
    """用例生成请求"""
    requirement_id: Optional[int] = None
    requirement_text: Optional[str] = None
    layer_code: str
    method_code: str
    model_code: str
    template_id: Optional[int] = None


class CaseGenerationResponse(BaseModel):
    """用例生成响应"""
    cases: List[dict]
    request_id: str
    status: str
    message: Optional[str] = None


class CaseParseRequest(BaseModel):
    """用例解析请求"""
    content: str


class CaseParseResponse(BaseModel):
    """用例解析响应"""
    cases: List[dict]
    count: int


@router.post("/generate", response_model=CaseGenerationResponse)
async def generate_cases(
    request: CaseGenerationRequest,
    db: Session = Depends(get_db)
):
    """
    生成测试用例
    
    根据需求文本、测试分层、测试方法和模型配置生成测试用例
    """
    import uuid
    import time
    
    request_id = str(uuid.uuid4())
    start_time = time.time()
    
    logger.info(
        f"收到用例生成请求: request_id={request_id}, "
        f"requirement_id={request.requirement_id}, "
        f"layer_code={request.layer_code}, "
        f"method_code={request.method_code}, "
        f"model_code={request.model_code}"
    )
    
    try:
        # 验证请求参数
        if not request.requirement_text and not request.requirement_id:
            logger.warning(f"参数验证失败: 缺少需求文本或需求ID, request_id={request_id}")
            raise HTTPException(
                status_code=400,
                detail="必须提供需求文本(requirement_text)或需求ID(requirement_id)"
            )
        
        if not request.requirement_text:
            # 如果只提供了需求ID，这里应该从数据库查询需求文本
            # 目前暂时要求必须提供需求文本
            logger.warning(f"参数验证失败: 缺少需求文本, request_id={request_id}")
            raise HTTPException(
                status_code=400,
                detail="当前版本必须提供需求文本(requirement_text)"
            )
        
        if not request.layer_code or not request.method_code:
            logger.warning(f"参数验证失败: 缺少测试分层或测试方法, request_id={request_id}")
            raise HTTPException(
                status_code=400,
                detail="必须提供测试分层(layer_code)和测试方法(method_code)"
            )
        
        if not request.model_code:
            logger.warning(f"参数验证失败: 缺少模型代码, request_id={request_id}")
            raise HTTPException(
                status_code=400,
                detail="必须提供模型代码(model_code)"
            )
        
        # 创建用例生成服务
        case_service = CaseGenerationService(db)
        
        # 生成用例
        logger.info(f"开始生成用例, request_id={request_id}")
        cases = case_service.generate_cases(
            requirement_text=request.requirement_text,
            layer_code=request.layer_code,
            method_code=request.method_code,
            model_code=request.model_code,
            template_id=request.template_id,
            requirement_id=request.requirement_id
        )
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"用例生成成功: request_id={request_id}, "
            f"用例数量={len(cases)}, "
            f"耗时={elapsed_time}ms"
        )
        
        return CaseGenerationResponse(
            cases=cases,
            request_id=request_id,
            status="success",
            message=f"成功生成 {len(cases)} 个用例"
        )
        
    except HTTPException:
        # 重新抛出HTTP异常
        raise
    except ValueError as e:
        logger.error(f"用例生成失败(参数错误): request_id={request_id}, 错误={str(e)}", exc_info=True)
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"用例生成失败: request_id={request_id}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"生成用例失败: {str(e)}"
        )


@router.post("/parse", response_model=CaseParseResponse)
async def parse_cases(
    request: CaseParseRequest,
    db: Session = Depends(get_db)
):
    """
    解析用例内容
    
    将大模型生成的用例文本解析为结构化数据
    """
    import time
    
    start_time = time.time()
    content_length = len(request.content) if request.content else 0
    
    logger.info(f"收到用例解析请求: 内容长度={content_length}")
    
    try:
        if not request.content or not request.content.strip():
            logger.warning("用例解析失败: 内容为空")
            raise HTTPException(
                status_code=400,
                detail="用例内容不能为空"
            )
        
        case_service = CaseGenerationService(db)
        cases = case_service.parse_cases(request.content)
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"用例解析成功: 用例数量={len(cases)}, "
            f"耗时={elapsed_time}ms"
        )
        
        return CaseParseResponse(
            cases=cases,
            count=len(cases)
        )
        
    except HTTPException:
        # 重新抛出HTTP异常
        raise
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"用例解析失败: 耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"解析用例失败: {str(e)}"
        )
