"""
UI脚本生成API路由
"""
import logging
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, HTTPException, UploadFile, File, Depends
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.page_parser_service import PageParserService
from app.services.nlp_service import NLPService
from app.services.ui_script_generation_service import UIScriptGenerationService
from app.services.ui_script_repair_service import UIScriptRepairService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/ui-script", tags=["UI脚本生成与修复"])


# 请求模型
class PageParseRequest(BaseModel):
    """页面解析请求"""
    page_code_url: str
    page_url: Optional[str] = None


class NLParseRequest(BaseModel):
    """自然语言解析请求"""
    description: str
    use_llm: bool = True


class ScriptGenerationRequest(BaseModel):
    """脚本生成请求"""
    natural_language_desc: str
    page_code_url: Optional[str] = None
    page_elements: Optional[List[Dict]] = None
    script_type: str = "SELENIUM"  # SELENIUM/PLAYWRIGHT
    script_language: str = "PYTHON"  # PYTHON/JAVA/JAVASCRIPT
    page_url: Optional[str] = None
    use_llm: bool = True


# 响应模型
class PageParseResponse(BaseModel):
    """页面解析响应"""
    elements: List[Dict]
    structure: Dict
    metadata: Dict
    element_count: int


class NLParseResponse(BaseModel):
    """自然语言解析响应"""
    steps: List[Dict]
    actions: List[str]
    elements: List[str]


class ScriptGenerationResponse(BaseModel):
    """脚本生成响应"""
    script_content: str
    script_type: str
    script_language: str
    elements_used: List[Dict]
    steps: List[Dict]
    page_url: Optional[str] = None


# UI脚本修复相关请求模型
class ErrorAnalysisRequest(BaseModel):
    """错误分析请求"""
    error_log: str
    script_content: Optional[str] = None
    use_llm: bool = True


class PageChangeDetectionRequest(BaseModel):
    """页面变化检测请求"""
    old_page_code_url: Optional[str] = None
    old_page_elements: Optional[List[Dict]] = None
    new_page_code_url: Optional[str] = None
    new_page_elements: Optional[List[Dict]] = None
    script_locators: Optional[List[Dict]] = None


class ScriptRepairRequest(BaseModel):
    """脚本修复请求"""
    script_content: str
    error_log: str
    error_analysis: Optional[Dict] = None
    page_changes: Optional[Dict] = None
    new_page_code_url: Optional[str] = None
    new_page_elements: Optional[List[Dict]] = None
    script_type: str = "SELENIUM"  # SELENIUM/PLAYWRIGHT
    script_language: str = "PYTHON"  # PYTHON/JAVA/JAVASCRIPT
    use_llm: bool = True


# UI脚本修复相关响应模型
class ErrorAnalysisResponse(BaseModel):
    """错误分析响应"""
    error_type: str
    error_message: str
    error_context: Dict
    affected_element: Optional[Dict] = None
    suggestions: List[str]


class PageChangeDetectionResponse(BaseModel):
    """页面变化检测响应"""
    has_changes: bool
    changed_elements: List[Dict]
    missing_elements: List[Dict]
    new_elements: List[Dict]
    locator_changes: List[Dict]


class ScriptRepairResponse(BaseModel):
    """脚本修复响应"""
    repaired_script: str
    repair_changes: List[str]
    repair_summary: str


@router.post("/parse-page", response_model=PageParseResponse)
async def parse_page(
    request: PageParseRequest,
    db: Session = Depends(get_db)
):
    """
    解析页面代码
    
    - 输入：页面代码URL或文件路径
    - 输出：页面元素信息列表
    """
    try:
        page_parser = PageParserService()
        result = page_parser.parse_page_code(request.page_code_url)
        
        return PageParseResponse(
            elements=result.get("elements", []),
            structure=result.get("structure", {}),
            metadata=result.get("metadata", {}),
            element_count=result.get("element_count", 0)
        )
        
    except Exception as e:
        logger.error(f"页面解析失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"页面解析失败: {str(e)}")


@router.post("/parse-nl", response_model=NLParseResponse)
async def parse_natural_language(
    request: NLParseRequest,
    db: Session = Depends(get_db)
):
    """
    解析自然语言描述
    
    - 输入：自然语言描述
    - 输出：操作步骤和元素信息
    """
    try:
        nlp_service = NLPService(db)
        result = nlp_service.parse_natural_language(request.description, request.use_llm)
        
        return NLParseResponse(
            steps=result.get("steps", []),
            actions=result.get("actions", []),
            elements=result.get("elements", [])
        )
        
    except Exception as e:
        logger.error(f"自然语言解析失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"自然语言解析失败: {str(e)}")


@router.post("/generate", response_model=ScriptGenerationResponse)
async def generate_script(
    request: ScriptGenerationRequest,
    db: Session = Depends(get_db)
):
    """
    生成UI自动化脚本
    
    - 输入：自然语言描述、页面代码信息
    - 输出：可执行的UI自动化脚本
    """
    try:
        script_service = UIScriptGenerationService(db)
        result = script_service.generate_script(
            natural_language_desc=request.natural_language_desc,
            page_code_url=request.page_code_url,
            page_elements=request.page_elements,
            script_type=request.script_type,
            script_language=request.script_language,
            page_url=request.page_url,
            use_llm=request.use_llm
        )
        
        return ScriptGenerationResponse(
            script_content=result.get("script_content", ""),
            script_type=result.get("script_type", ""),
            script_language=result.get("script_language", ""),
            elements_used=result.get("elements_used", []),
            steps=result.get("steps", []),
            page_url=result.get("page_url")
        )
        
    except ValueError as e:
        logger.error(f"脚本生成失败: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"脚本生成失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"脚本生成失败: {str(e)}")


@router.post("/parse-page-upload")
async def parse_page_upload(
    file: UploadFile = File(..., description="HTML文件"),
    db: Session = Depends(get_db)
):
    """
    上传HTML文件并解析
    
    - 输入：HTML文件
    - 输出：页面元素信息列表
    """
    try:
        # 读取文件内容
        content = await file.read()
        html_content = content.decode('utf-8')
        
        # 解析HTML
        page_parser = PageParserService()
        result = page_parser.parse_html(html_content, file.filename)
        
        return PageParseResponse(
            elements=result.get("elements", []),
            structure=result.get("structure", {}),
            metadata=result.get("metadata", {}),
            element_count=result.get("element_count", 0)
        )
        
    except Exception as e:
        logger.error(f"页面解析失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"页面解析失败: {str(e)}")


# ==================== UI脚本修复相关接口 ====================

@router.post("/analyze-error", response_model=ErrorAnalysisResponse)
async def analyze_error(
    request: ErrorAnalysisRequest,
    db: Session = Depends(get_db)
):
    """
    分析错误日志
    
    - 输入：错误日志、脚本内容（可选）
    - 输出：错误类型、错误信息、错误上下文、修复建议
    """
    try:
        repair_service = UIScriptRepairService(db)
        result = repair_service.analyze_error(
            error_log=request.error_log,
            script_content=request.script_content,
            use_llm=request.use_llm
        )
        
        return ErrorAnalysisResponse(
            error_type=result.get("error_type", "UNKNOWN"),
            error_message=result.get("error_message", ""),
            error_context=result.get("error_context", {}),
            affected_element=result.get("affected_element"),
            suggestions=result.get("suggestions", [])
        )
        
    except ValueError as e:
        logger.error(f"错误分析失败: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"错误分析失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"错误分析失败: {str(e)}")


@router.post("/detect-page-changes", response_model=PageChangeDetectionResponse)
async def detect_page_changes(
    request: PageChangeDetectionRequest,
    db: Session = Depends(get_db)
):
    """
    检测页面变化
    
    - 输入：旧页面代码/元素、新页面代码/元素、脚本定位器（可选）
    - 输出：页面变化信息（变化的元素、缺失的元素、新增的元素、定位器变化）
    """
    try:
        repair_service = UIScriptRepairService(db)
        result = repair_service.detect_page_changes(
            old_page_code_url=request.old_page_code_url,
            old_page_elements=request.old_page_elements,
            new_page_code_url=request.new_page_code_url,
            new_page_elements=request.new_page_elements,
            script_locators=request.script_locators
        )
        
        return PageChangeDetectionResponse(
            has_changes=result.get("has_changes", False),
            changed_elements=result.get("changed_elements", []),
            missing_elements=result.get("missing_elements", []),
            new_elements=result.get("new_elements", []),
            locator_changes=result.get("locator_changes", [])
        )
        
    except ValueError as e:
        logger.error(f"页面变化检测失败: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"页面变化检测失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"页面变化检测失败: {str(e)}")


@router.post("/repair", response_model=ScriptRepairResponse)
async def repair_script(
    request: ScriptRepairRequest,
    db: Session = Depends(get_db)
):
    """
    修复UI自动化脚本
    
    - 输入：UI脚本、报错日志、页面代码信息
    - 输出：修复后的UI自动化脚本
    """
    try:
        repair_service = UIScriptRepairService(db)
        result = repair_service.repair_script(
            script_content=request.script_content,
            error_log=request.error_log,
            error_analysis=request.error_analysis,
            page_changes=request.page_changes,
            new_page_code_url=request.new_page_code_url,
            new_page_elements=request.new_page_elements,
            script_type=request.script_type,
            script_language=request.script_language,
            use_llm=request.use_llm
        )
        
        return ScriptRepairResponse(
            repaired_script=result.get("repaired_script", ""),
            repair_changes=result.get("repair_changes", []),
            repair_summary=result.get("repair_summary", "")
        )
        
    except ValueError as e:
        logger.error(f"脚本修复失败: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"脚本修复失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"脚本修复失败: {str(e)}")

