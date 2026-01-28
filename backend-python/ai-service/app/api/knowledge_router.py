"""
知识库API路由（第四阶段增强）
"""
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from app.database import get_db
from app.services.knowledge_base_service import KnowledgeBaseService
from app.services.kb_permission_service import KBPermissionService
from app.services.kb_sync_service import KBSyncService

router = APIRouter(prefix="/knowledge", tags=["知识库"])


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


# ==================== 知识库管理相关API（第四阶段增强）====================

class KBPermissionRequest(BaseModel):
    """知识库权限请求"""
    kb_id: int
    user_id: int
    permission_type: str


class KBSyncRequest(BaseModel):
    """知识库同步请求"""
    kb_id: int
    sync_type: str  # incremental/full
    source_path: str
    chunking_strategy: str = "paragraph"
    chunk_size: int = 1000
    chunk_overlap: int = 200


@router.post("/permission/grant")
async def grant_permission(
    request: KBPermissionRequest,
    db: Session = Depends(get_db)
):
    """
    授予知识库访问权限
    """
    try:
        service = KBPermissionService(db)
        success = service.grant_permission(
            kb_id=request.kb_id,
            user_id=request.user_id,
            permission_type=request.permission_type
        )
        if success:
            return {"success": True, "message": "权限授予成功"}
        else:
            return {"success": False, "message": "权限授予失败"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"授予权限失败: {str(e)}")


@router.delete("/permission/revoke")
async def revoke_permission(
    kb_id: int,
    user_id: int,
    permission_type: str,
    db: Session = Depends(get_db)
):
    """
    撤销知识库访问权限
    """
    try:
        service = KBPermissionService(db)
        success = service.revoke_permission(
            kb_id=kb_id,
            user_id=user_id,
            permission_type=permission_type
        )
        if success:
            return {"success": True, "message": "权限撤销成功"}
        else:
            return {"success": False, "message": "权限撤销失败"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"撤销权限失败: {str(e)}")


@router.get("/permission/check")
async def check_permission(
    kb_id: int,
    user_id: int,
    permission_type: str,
    db: Session = Depends(get_db)
):
    """
    检查用户是否有指定权限
    """
    try:
        service = KBPermissionService(db)
        has_permission = service.check_permission(
            kb_id=kb_id,
            user_id=user_id,
            permission_type=permission_type
        )
        return {
            "success": True,
            "has_permission": has_permission
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"检查权限失败: {str(e)}")


@router.get("/user/{user_id}/knowledge-bases")
async def get_user_knowledge_bases(
    user_id: int,
    permission_type: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """
    获取用户有权限的知识库列表
    """
    try:
        service = KBPermissionService(db)
        kb_list = service.get_user_kb_list(
            user_id=user_id,
            permission_type=permission_type
        )
        return {
            "success": True,
            "count": len(kb_list),
            "knowledge_bases": kb_list
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取知识库列表失败: {str(e)}")


@router.get("/knowledge-base/{kb_id}/permissions")
async def get_kb_permissions(
    kb_id: int,
    db: Session = Depends(get_db)
):
    """
    获取知识库的权限列表
    """
    try:
        service = KBPermissionService(db)
        permissions = service.get_kb_permissions(kb_id=kb_id)
        return {
            "success": True,
            "count": len(permissions),
            "permissions": permissions
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取权限列表失败: {str(e)}")


@router.post("/sync")
async def sync_knowledge_base(
    request: KBSyncRequest,
    db: Session = Depends(get_db)
):
    """
    同步知识库（增量/全量）
    """
    try:
        service = KBSyncService(db)
        
        if request.sync_type == "incremental":
            result = service.sync_incremental(
                kb_id=request.kb_id,
                source_path=request.source_path,
                chunking_strategy=request.chunking_strategy,
                chunk_size=request.chunk_size,
                chunk_overlap=request.chunk_overlap
            )
        elif request.sync_type == "full":
            result = service.sync_full(
                kb_id=request.kb_id,
                source_path=request.source_path,
                chunking_strategy=request.chunking_strategy,
                chunk_size=request.chunk_size,
                chunk_overlap=request.chunk_overlap
            )
        else:
            raise HTTPException(status_code=400, detail=f"不支持的同步类型: {request.sync_type}")
        
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"同步失败: {str(e)}")


@router.get("/knowledge-base/{kb_id}/sync-logs")
async def get_sync_logs(
    kb_id: int,
    db: Session = Depends(get_db)
):
    """
    获取知识库的同步日志列表
    """
    try:
        service = KBSyncService(db)
        logs = service._get_sync_logs(kb_id=kb_id)
        return {
            "success": True,
            "count": len(logs),
            "logs": logs
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取同步日志失败: {str(e)}")


@router.post("/upload")
async def upload_document(
    kb_id: int = Form(...),
    file_name: str = Form(...),
    file_content: str = Form(...),
    creator_id: int = Form(None),
    db: Session = Depends(get_db)
):
    """
    上传文档到知识库
    """
    try:
        # 解码Base64内容
        import base64
        file_bytes = base64.b64decode(file_content)
        
        # TODO: 保存文件并调用文档处理服务
        # 这里需要实现文件保存和文档处理的完整逻辑
        
        return {
            "success": True,
            "doc_code": f"DOC-{kb_id}-{file_name}",
            "message": "文档上传成功"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"上传文档失败: {str(e)}")

