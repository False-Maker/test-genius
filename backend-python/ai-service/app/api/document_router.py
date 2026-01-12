"""
文档解析API路由
"""
from fastapi import APIRouter, HTTPException, UploadFile, File, Depends
from pydantic import BaseModel
from typing import Optional, Dict
import logging
import os
import tempfile
from app.services.document_parser_service import DocumentParserService

router = APIRouter()
logger = logging.getLogger(__name__)

# 创建文档解析服务实例
document_parser = DocumentParserService()


class DocumentParseResponse(BaseModel):
    """文档解析响应"""
    content: str
    structure: Dict
    metadata: Dict
    paragraph_count: Optional[int] = None
    page_count: Optional[int] = None
    char_count: int


@router.post("/parse", response_model=DocumentParseResponse)
async def parse_document(
    file: UploadFile = File(..., description="要解析的文档文件（Word或PDF）")
):
    """
    解析文档
    
    支持Word（.docx）和PDF（.pdf）格式的文档解析
    """
    import time
    
    start_time = time.time()
    file_name = file.filename or "unknown"
    file_size = 0
    
    logger.info(f"收到文档解析请求: 文件名={file_name}, 文件类型={file.content_type}")
    
    # 验证文件类型
    if not file_name:
        raise HTTPException(status_code=400, detail="文件名不能为空")
    
    file_extension = os.path.splitext(file_name)[1].lower().lstrip('.')
    if file_extension not in ["docx", "doc", "pdf"]:
        raise HTTPException(
            status_code=400,
            detail=f"不支持的文件格式: {file_extension}，仅支持 docx、doc、pdf"
        )
    
    # 创建临时文件保存上传的文件
    temp_file = None
    try:
        # 创建临时文件
        suffix = f".{file_extension}"
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            # 读取上传的文件内容
            content = await file.read()
            file_size = len(content)
            temp_file.write(content)
            temp_file_path = temp_file.name
        
        logger.info(f"文件已保存到临时文件: {temp_file_path}, 文件大小={file_size}字节")
        
        # 解析文档
        result = document_parser.parse_document(temp_file_path)
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"文档解析成功: 文件名={file_name}, "
            f"字符数={result.get('char_count', 0)}, "
            f"耗时={elapsed_time}ms"
        )
        
        return DocumentParseResponse(
            content=result.get("content", ""),
            structure=result.get("structure", {}),
            metadata=result.get("metadata", {}),
            paragraph_count=result.get("paragraph_count"),
            page_count=result.get("page_count"),
            char_count=result.get("char_count", 0)
        )
        
    except FileNotFoundError as e:
        logger.error(f"文档解析失败(文件不存在): {str(e)}")
        raise HTTPException(status_code=404, detail=f"文件不存在: {str(e)}")
    except ValueError as e:
        logger.error(f"文档解析失败(参数错误): {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except ImportError as e:
        logger.error(f"文档解析失败(依赖缺失): {str(e)}")
        raise HTTPException(status_code=500, detail=f"文档解析功能不可用: {str(e)}")
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"文档解析失败: 文件名={file_name}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"文档解析失败: {str(e)}"
        )
    finally:
        # 清理临时文件
        if temp_file and os.path.exists(temp_file_path):
            try:
                os.unlink(temp_file_path)
                logger.debug(f"临时文件已删除: {temp_file_path}")
            except Exception as e:
                logger.warning(f"删除临时文件失败: {temp_file_path}, 错误={str(e)}")


@router.post("/parse-by-path")
async def parse_document_by_path(file_path: str):
    """
    根据文件路径解析文档
    
    用于解析已上传到服务器的文档
    """
    import time
    
    start_time = time.time()
    
    logger.info(f"收到文档解析请求(按路径): 文件路径={file_path}")
    
    if not file_path:
        raise HTTPException(status_code=400, detail="文件路径不能为空")
    
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail=f"文件不存在: {file_path}")
    
    try:
        # 解析文档
        result = document_parser.parse_document(file_path)
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"文档解析成功(按路径): 文件路径={file_path}, "
            f"字符数={result.get('char_count', 0)}, "
            f"耗时={elapsed_time}ms"
        )
        
        return DocumentParseResponse(
            content=result.get("content", ""),
            structure=result.get("structure", {}),
            metadata=result.get("metadata", {}),
            paragraph_count=result.get("paragraph_count"),
            page_count=result.get("page_count"),
            char_count=result.get("char_count", 0)
        )
        
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"文档解析失败(按路径): 文件路径={file_path}, "
            f"耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"文档解析失败: {str(e)}"
        )

