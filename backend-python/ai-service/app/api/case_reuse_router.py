"""
用例复用API路由
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from app.database import get_db
from app.services.case_reuse_service import CaseReuseService

router = APIRouter(prefix="/api/case-reuse", tags=["用例复用"])


class SimilarCaseSearchRequest(BaseModel):
    """相似用例搜索请求"""
    case_text: str
    layer_id: Optional[int] = None
    method_id: Optional[int] = None
    top_k: int = 10
    similarity_threshold: float = 0.7


class CaseSuiteCreateRequest(BaseModel):
    """用例套件创建请求"""
    suite_name: str
    case_ids: List[int]
    creator_id: Optional[int] = None


@router.post("/init")
async def init_case_vector_table(db: Session = Depends(get_db)):
    """
    初始化用例向量表
    """
    try:
        service = CaseReuseService(db)
        success = service.init_case_vector_table()
        if success:
            return {"success": True, "message": "用例向量表初始化成功"}
        else:
            return {"success": False, "message": "用例向量表初始化失败（可能pgvector未安装）"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"初始化失败: {str(e)}")


@router.post("/cases/{case_id}/embedding")
async def update_case_embedding(
    case_id: int,
    db: Session = Depends(get_db)
):
    """
    更新用例的向量表示
    """
    try:
        service = CaseReuseService(db)
        success = service.update_case_embedding(case_id)
        if success:
            return {"success": True, "message": "用例向量更新成功"}
        else:
            return {"success": False, "message": "用例向量更新失败"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"更新失败: {str(e)}")


@router.post("/cases/search/similar")
async def search_similar_cases(
    request: SimilarCaseSearchRequest,
    db: Session = Depends(get_db)
):
    """
    搜索相似用例（基于语义相似度）
    """
    try:
        service = CaseReuseService(db)
        cases = service.search_similar_cases(
            case_text=request.case_text,
            layer_id=request.layer_id,
            method_id=request.method_id,
            top_k=request.top_k,
            similarity_threshold=request.similarity_threshold
        )
        return {
            "success": True,
            "count": len(cases),
            "cases": cases
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")


@router.get("/cases/search/keyword/{keyword}")
async def search_cases_by_keyword(
    keyword: str,
    layer_id: Optional[int] = None,
    method_id: Optional[int] = None,
    top_k: int = 10,
    db: Session = Depends(get_db)
):
    """
    关键词检索用例
    """
    try:
        service = CaseReuseService(db)
        cases = service.search_cases_by_keyword(
            keyword=keyword,
            layer_id=layer_id,
            method_id=method_id,
            top_k=top_k
        )
        return {
            "success": True,
            "count": len(cases),
            "cases": cases
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")


@router.get("/cases/{case_id}/recommend")
async def recommend_similar_cases(
    case_id: int,
    top_k: int = 5,
    db: Session = Depends(get_db)
):
    """
    推荐相似用例（基于现有用例）
    """
    try:
        service = CaseReuseService(db)
        cases = service.recommend_similar_cases(
            case_id=case_id,
            top_k=top_k
        )
        return {
            "success": True,
            "count": len(cases),
            "cases": cases
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"推荐失败: {str(e)}")


@router.post("/suites")
async def create_case_suite(
    request: CaseSuiteCreateRequest,
    db: Session = Depends(get_db)
):
    """
    创建用例组合（测试套件）
    """
    try:
        service = CaseReuseService(db)
        suite_id = service.create_case_suite(
            suite_name=request.suite_name,
            case_ids=request.case_ids,
            creator_id=request.creator_id
        )
        if suite_id:
            return {"success": True, "suite_id": suite_id, "message": "用例套件创建成功"}
        else:
            return {"success": False, "message": "用例套件创建失败"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"创建失败: {str(e)}")

