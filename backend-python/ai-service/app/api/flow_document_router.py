"""
流程文档导出API路由
"""
from fastapi import APIRouter, HTTPException, Response
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import Optional
import logging
from pathlib import Path
from app.services.flow_document_export_service import FlowDocumentExportService

router = APIRouter()
logger = logging.getLogger(__name__)

# 创建流程文档导出服务实例
export_service = FlowDocumentExportService()


class MermaidExportRequest(BaseModel):
    """Mermaid导出请求"""
    mermaid_code: str
    format: str = "png"  # png, svg, pdf
    filename: Optional[str] = None
    width: int = 1920
    height: int = 1080


class MermaidExportResponse(BaseModel):
    """Mermaid导出响应"""
    status: str
    file_name: Optional[str] = None
    file_path: Optional[str] = None
    file_size: Optional[int] = None
    format: str
    url: Optional[str] = None
    online_url: Optional[str] = None
    mermaid_code: Optional[str] = None
    message: Optional[str] = None


@router.post("/export", response_model=MermaidExportResponse)
async def export_mermaid(request: MermaidExportRequest):
    """
    导出Mermaid图表为文件
    
    支持格式：PNG、SVG、PDF
    """
    import time
    
    start_time = time.time()
    
    logger.info(f"收到Mermaid导出请求: 格式={request.format}, 文件名={request.filename}")
    
    try:
        # 验证参数
        if not request.mermaid_code or not request.mermaid_code.strip():
            raise HTTPException(status_code=400, detail="Mermaid代码不能为空")
        
        # 导出文件
        result = export_service.export_mermaid(
            mermaid_code=request.mermaid_code,
            format=request.format,
            filename=request.filename,
            width=request.width,
            height=request.height
        )
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"Mermaid导出成功: 格式={request.format}, "
            f"文件名={result.get('file_name')}, "
            f"耗时={elapsed_time}ms"
        )
        
        return MermaidExportResponse(**result)
    
    except ValueError as e:
        logger.error(f"Mermaid导出失败(参数错误): {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"Mermaid导出失败: 格式={request.format}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"Mermaid导出失败: {str(e)}"
        )


@router.get("/files/{filename}")
async def get_exported_file(filename: str):
    """
    获取导出的文件
    
    用于下载已导出的Mermaid图表文件
    """
    logger.info(f"请求获取导出文件: {filename}")
    
    try:
        file_path = export_service.get_file(filename)
        
        if not file_path or not file_path.exists():
            raise HTTPException(status_code=404, detail=f"文件不存在: {filename}")
        
        # 确定媒体类型
        media_type_map = {
            ".png": "image/png",
            ".svg": "image/svg+xml",
            ".pdf": "application/pdf"
        }
        
        suffix = file_path.suffix.lower()
        media_type = media_type_map.get(suffix, "application/octet-stream")
        
        logger.info(f"返回文件: {filename}, 类型: {media_type}")
        
        return FileResponse(
            path=str(file_path),
            filename=filename,
            media_type=media_type
        )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取导出文件失败: {filename}, 错误={str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"获取文件失败: {str(e)}"
        )


@router.post("/cleanup")
async def cleanup_old_files(max_age_hours: int = 24):
    """
    清理旧的导出文件
    
    默认清理24小时前的文件
    """
    try:
        export_service.cleanup_old_files(max_age_hours)
        return {"status": "success", "message": f"已清理 {max_age_hours} 小时前的文件"}
    except Exception as e:
        logger.error(f"清理文件失败: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"清理文件失败: {str(e)}"
        )
