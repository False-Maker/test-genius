"""
知识库API路由（第四阶段增强）
"""
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
import time
import logging
from app.database import get_db
from app.services.knowledge_base_service import KnowledgeBaseService
from app.services.kb_permission_service import KBPermissionService
from app.services.kb_sync_service import KBSyncService

logger = logging.getLogger(__name__)
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


class DocumentCountRequest(BaseModel):
    """文档数量查询请求"""
    kb_id: int


@router.post("/documents/count")
async def count_documents(
    request: DocumentCountRequest,
    db: Session = Depends(get_db)
):
    """
    统计知识库中的文档数量
    """
    try:
        from sqlalchemy import text
        count_sql = """
        SELECT COUNT(*) FROM knowledge_document
        WHERE kb_id = :kb_id AND is_active = '1'
        """
        result = db.execute(text(count_sql), {"kb_id": request.kb_id})
        count = result.fetchone()[0]
        
        return {
            "success": True,
            "count": count
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"查询文档数量失败: {str(e)}")


@router.post("/statistics")
async def get_knowledge_base_statistics(
    request: DocumentCountRequest,
    db: Session = Depends(get_db)
):
    """
    获取知识库统计信息（文档数量、分块数量、最后同步时间）
    """
    try:
        from sqlalchemy import text
        
        # 查询文档数量
        doc_count_sql = """
        SELECT COUNT(*) FROM knowledge_document
        WHERE kb_id = :kb_id AND is_active = '1'
        """
        doc_result = db.execute(text(doc_count_sql), {"kb_id": request.kb_id})
        doc_count = doc_result.fetchone()[0]
        
        # 查询分块数量
        chunk_count_sql = """
        SELECT COUNT(*) FROM knowledge_document_chunk
        WHERE doc_id IN (
            SELECT id FROM knowledge_document 
            WHERE kb_id = :kb_id AND is_active = '1'
        )
        """
        chunk_result = db.execute(text(chunk_count_sql), {"kb_id": request.kb_id})
        chunk_count = chunk_result.fetchone()[0]
        
        # 查询最后同步时间
        last_sync_sql = """
        SELECT MAX(end_time) FROM knowledge_base_sync_log
        WHERE kb_id = :kb_id AND status = 'success'
        """
        sync_result = db.execute(text(last_sync_sql), {"kb_id": request.kb_id})
        last_sync_time = sync_result.fetchone()[0]
        
        return {
            "success": True,
            "document_count": doc_count,
            "chunk_count": chunk_count,
            "last_sync_time": last_sync_time.isoformat() if last_sync_time else None
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"查询统计信息失败: {str(e)}")


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
        import os
        from pathlib import Path
        from app.services.document_pipeline_service import DocumentPipelineService
        from app.services.embedding_service import EmbeddingService
        from app.services.text_chunking_service import ChunkingStrategy

        file_bytes = base64.b64decode(file_content)

        # 步骤1: 保存文件到本地存储
        upload_dir = Path("data/uploads")
        upload_dir.mkdir(parents=True, exist_ok=True)

        # 生成唯一文件名
        timestamp = int(time.time())
        safe_filename = f"{timestamp}_{file_name}"
        file_path = upload_dir / safe_filename

        # 写入文件
        with open(file_path, "wb") as f:
            f.write(file_bytes)

        logger.info(f"文件保存成功: {file_path}")

        # 步骤2: 调用文档处理管道服务
        # 实例化服务
        embedding_service = EmbeddingService(db)
        pipeline_service = DocumentPipelineService(
            db=db,
            embedding_service=embedding_service
        )

        # 步骤3: 处理文档（解析、分块、向量化、入库）
        doc_code = f"DOC-{kb_id}-{timestamp}"
        file_ext = Path(file_name).suffix[1:].lower() if Path(file_name).suffix else "txt"

        result = pipeline_service.process_document(
            file_path=str(file_path),
            doc_code=doc_code,
            doc_name=file_name,
            doc_type=file_ext,
            kb_id=kb_id,
            doc_category=None,
            chunking_strategy=ChunkingStrategy.PARAGRAPH,
            chunk_size=1000,
            chunk_overlap=200
        )

        # 步骤4: 检查处理结果
        if not result.get("success"):
            # 处理失败，删除已保存的文件
            if file_path.exists():
                os.remove(file_path)
            raise HTTPException(
                status_code=500,
                detail=f"文档处理失败: {result.get('error', '未知错误')}"
            )

        # 步骤5: 记录同步日志（如果知识库同步日志表存在）
        try:
            from sqlalchemy import text
            insert_log_sql = """
            INSERT INTO knowledge_base_sync_log
            (kb_id, sync_type, source_path, added_count, updated_count,
             deleted_count, failed_count, status, start_time, end_time)
            VALUES
            (:kb_id, :sync_type, :source_path, :added_count, :updated_count,
             :deleted_count, :failed_count, :status, NOW(), NOW())
            """
            db.execute(
                text(insert_log_sql),
                {
                    "kb_id": kb_id,
                    "sync_type": "upload",
                    "source_path": str(file_path),
                    "added_count": 1,
                    "updated_count": 0,
                    "deleted_count": 0,
                    "failed_count": 0,
                    "status": "success"
                }
            )
            db.commit()
            logger.info("同步日志记录成功")
        except Exception as e:
            logger.warning(f"记录同步日志失败: {str(e)}")
            # 不影响主流程

        logger.info(f"文档上传成功: doc_code={doc_code}, doc_id={result.get('doc_id')}, chunks={result.get('chunks')}")

        return {
            "success": True,
            "doc_id": result.get("doc_id"),
            "doc_code": doc_code,
            "chunks": result.get("chunks"),
            "message": "文档上传成功"
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"上传文档失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"上传文档失败: {str(e)}")

