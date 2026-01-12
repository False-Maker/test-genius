"""
知识库API路由
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from app.database import get_db
from app.services.knowledge_base_service import KnowledgeBaseService

router = APIRouter(prefix="/api/knowledge", tags=["知识库"])


class DocumentCreateRequest(BaseModel):
    """文档创建请求"""
    doc_code: str
    doc_name: str
    doc_type: str
    doc_content: str
    doc_category: Optional[str] = None
    doc_url: Optional[str] = None
    creator_id: Optional[int] = None


class DocumentSearchRequest(BaseModel):
    """文档搜索请求"""
    query_text: str
    doc_type: Optional[str] = None
    top_k: int = 10
    similarity_threshold: float = 0.7


@router.post("/init")
async def init_knowledge_base(db: Session = Depends(get_db)):
    """
    初始化知识库表结构
    """
    try:
        service = KnowledgeBaseService(db)
        success = service.init_knowledge_base_tables()
        if success:
            return {"success": True, "message": "知识库初始化成功"}
        else:
            return {"success": False, "message": "知识库初始化失败（可能pgvector未安装）"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"初始化失败: {str(e)}")


@router.post("/documents")
async def add_document(
    request: DocumentCreateRequest,
    db: Session = Depends(get_db)
):
    """
    添加知识库文档
    """
    try:
        service = KnowledgeBaseService(db)
        doc_id = service.add_document(
            doc_code=request.doc_code,
            doc_name=request.doc_name,
            doc_type=request.doc_type,
            doc_content=request.doc_content,
            doc_category=request.doc_category,
            doc_url=request.doc_url,
            creator_id=request.creator_id
        )
        if doc_id:
            return {"success": True, "doc_id": doc_id, "message": "文档添加成功"}
        else:
            return {"success": False, "message": "文档添加失败"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"添加文档失败: {str(e)}")


@router.post("/documents/search")
async def search_documents(
    request: DocumentSearchRequest,
    db: Session = Depends(get_db)
):
    """
    语义检索知识库文档
    """
    try:
        service = KnowledgeBaseService(db)
        documents = service.search_by_semantic(
            query_text=request.query_text,
            doc_type=request.doc_type,
            top_k=request.top_k,
            similarity_threshold=request.similarity_threshold
        )
        return {
            "success": True,
            "count": len(documents),
            "documents": documents
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")


@router.get("/documents/keyword/{keyword}")
async def search_documents_by_keyword(
    keyword: str,
    doc_type: Optional[str] = None,
    top_k: int = 10,
    db: Session = Depends(get_db)
):
    """
    关键词检索知识库文档
    """
    try:
        service = KnowledgeBaseService(db)
        documents = service.search_by_keyword(
            keyword=keyword,
            doc_type=doc_type,
            top_k=top_k
        )
        return {
            "success": True,
            "count": len(documents),
            "documents": documents
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检索失败: {str(e)}")

