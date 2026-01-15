"""
提示词API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional, Dict, List, Any
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.prompt_service import PromptService

router = APIRouter()


class PromptGenerateRequest(BaseModel):
    """提示词生成请求"""
    template_id: int
    variables: Dict[str, Any] = {}


class PromptGenerateResponse(BaseModel):
    """提示词生成响应"""
    prompt: str
    template_id: int
    template_name: Optional[str] = None


class TemplateVariablesResponse(BaseModel):
    """模板变量定义响应"""
    template_id: int
    variables: Dict[str, Any]


@router.post("/generate", response_model=PromptGenerateResponse)
async def generate_prompt(
    request: PromptGenerateRequest,
    db: Session = Depends(get_db)
):
    """
    生成提示词
    
    根据模板ID和变量生成提示词
    """
    try:
        prompt_service = PromptService(db)
        
        # 生成提示词
        prompt = prompt_service.generate_prompt(
            template_id=request.template_id,
            variables=request.variables
        )
        
        # 获取模板信息
        template = prompt_service.load_template(request.template_id)
        template_name = template.get("template_name") if template else None
        
        return PromptGenerateResponse(
            prompt=prompt,
            template_id=request.template_id,
            template_name=template_name
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"生成提示词失败: {str(e)}")


@router.get("/{template_id}/variables", response_model=TemplateVariablesResponse)
async def get_template_variables(
    template_id: int,
    db: Session = Depends(get_db)
):
    """
    获取模板变量定义
    
    返回模板中定义的变量及其说明
    """
    try:
        prompt_service = PromptService(db)
        variables = prompt_service.get_template_variables(template_id)
        
        if variables is None:
            raise HTTPException(status_code=404, detail="模板不存在或未启用")
        
        return TemplateVariablesResponse(
            template_id=template_id,
            variables=variables
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取模板变量失败: {str(e)}")


@router.get("/applicable")
async def get_applicable_templates(
    layer_code: Optional[str] = None,
    method_code: Optional[str] = None,
    module_code: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    获取适用的模板列表
    
    根据测试分层、测试方法、业务模块筛选适用的模板
    """
    try:
        prompt_service = PromptService(db)
        templates = prompt_service.find_applicable_templates(
            layer_code=layer_code,
            method_code=method_code,
            module_code=module_code
        )
        
        return {
            "templates": templates,
            "count": len(templates)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取适用模板失败: {str(e)}")
