"""
用例生成API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional, List
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.case_generation_service import CaseGenerationService

router = APIRouter()


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
    
    request_id = str(uuid.uuid4())
    
    try:
        # 验证请求参数
        if not request.requirement_text and not request.requirement_id:
            raise HTTPException(
                status_code=400,
                detail="必须提供需求文本(requirement_text)或需求ID(requirement_id)"
            )
        
        if not request.requirement_text:
            # 如果只提供了需求ID，这里应该从数据库查询需求文本
            # 目前暂时要求必须提供需求文本
            raise HTTPException(
                status_code=400,
                detail="当前版本必须提供需求文本(requirement_text)"
            )
        
        # 创建用例生成服务
        case_service = CaseGenerationService(db)
        
        # 生成用例
        cases = case_service.generate_cases(
            requirement_text=request.requirement_text,
            layer_code=request.layer_code,
            method_code=request.method_code,
            model_code=request.model_code,
            template_id=request.template_id,
            requirement_id=request.requirement_id
        )
        
        return CaseGenerationResponse(
            cases=cases,
            request_id=request_id,
            status="success",
            message=f"成功生成 {len(cases)} 个用例"
        )
        
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
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
    try:
        case_service = CaseGenerationService(db)
        cases = case_service.parse_cases(request.content)
        
        return CaseParseResponse(
            cases=cases,
            count=len(cases)
        )
        
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"解析用例失败: {str(e)}"
        )
