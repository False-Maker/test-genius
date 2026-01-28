"""
知识库同步服务
实现知识库文档的增量同步和全量同步
"""
import logging
import os
import hashlib
from typing import List, Dict, Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class KBSyncService:
    """知识库同步服务"""
    
    def __init__(self, db: Session):
        """
        初始化同步服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
    
    def sync_incremental(
        self,
        kb_id: int,
        source_path: str,
        chunking_strategy: str = "paragraph",
        chunk_size: int = 1000,
        chunk_overlap: int = 200
    ) -> Dict:
        """
        增量同步（只同步新增和修改的文件）
        
        Args:
            kb_id: 知识库ID
            source_path: 源文件路径
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
            
        Returns:
            同步结果
        """
        logger.info(f"开始增量同步: kb_id={kb_id}, source_path={source_path}")
        
        try:
            # 检查知识库是否存在
            if not self._check_kb_exists(kb_id):
                raise ValueError(f"知识库不存在: {kb_id}")
            
            # 检测文件变化
            changes = self._detect_changes(kb_id, source_path)
            
            added_count = 0
            updated_count = 0
            deleted_count = 0
            failed_count = 0
            errors = []
            
            # 处理新增文件
            for file_path in changes.get("added_files", []):
                try:
                    self._add_document(
                        kb_id=kb_id,
                        file_path=file_path,
                        chunking_strategy=chunking_strategy,
                        chunk_size=chunk_size,
                        chunk_overlap=chunk_overlap
                    )
                    added_count += 1
                    logger.info(f"新增文档成功: {file_path}")
                except Exception as e:
                    failed_count += 1
                    errors.append(f"新增文件失败 {file_path}: {str(e)}")
                    logger.error(f"新增文档失败: {file_path}, 错误: {str(e)}")
            
            # 处理更新文件
            for file_path in changes.get("updated_files", []):
                try:
                    self._update_document(
                        kb_id=kb_id,
                        file_path=file_path,
                        chunking_strategy=chunking_strategy,
                        chunk_size=chunk_size,
                        chunk_overlap=chunk_overlap
                    )
                    updated_count += 1
                    logger.info(f"更新文档成功: {file_path}")
                except Exception as e:
                    failed_count += 1
                    errors.append(f"更新文件失败 {file_path}: {str(e)}")
                    logger.error(f"更新文档失败: {file_path}, 错误: {str(e)}")
            
            # 处理删除文件
            for file_path in changes.get("deleted_files", []):
                try:
                    self._delete_document(kb_id, file_path)
                    deleted_count += 1
                    logger.info(f"删除文档成功: {file_path}")
                except Exception as e:
                    failed_count += 1
                    errors.append(f"删除文件失败 {file_path}: {str(e)}")
                    logger.error(f"删除文档失败: {file_path}, 错误: {str(e)}")
            
            # 记录同步日志
            self._create_sync_log(
                kb_id=kb_id,
                sync_type="incremental",
                source_path=source_path,
                added_count=added_count,
                updated_count=updated_count,
                deleted_count=deleted_count,
                failed_count=failed_count,
                status="success",
                error_message="; ".join(errors) if errors else None
            )
            
            result = {
                "success": True,
                "added_count": added_count,
                "updated_count": updated_count,
                "deleted_count": deleted_count,
                "failed_count": failed_count,
                "errors": errors
            }
            
            logger.info(f"增量同步完成: {result}")
            return result
            
        except Exception as e:
            logger.error(f"增量同步失败: {str(e)}")
            # 记录失败日志
            self._create_sync_log(
                kb_id=kb_id,
                sync_type="incremental",
                source_path=source_path,
                added_count=0,
                updated_count=0,
                deleted_count=0,
                failed_count=0,
                status="failed",
                error_message=str(e)
            )
            return {
                "success": False,
                "error": str(e)
            }
    
    def sync_full(
        self,
        kb_id: int,
        source_path: str,
        chunking_strategy: str = "paragraph",
        chunk_size: int = 1000,
        chunk_overlap: int = 200
    ) -> Dict:
        """
        全量同步（删除所有文档后重新导入）
        
        Args:
            kb_id: 知识库ID
            source_path: 源文件路径
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
            
        Returns:
            同步结果
        """
        logger.info(f"开始全量同步: kb_id={kb_id}, source_path={source_path}")
        
        try:
            # 检查知识库是否存在
            if not self._check_kb_exists(kb_id):
                raise ValueError(f"知识库不存在: {kb_id}")
            
            # 删除知识库的所有文档
            deleted_count = self._delete_all_documents(kb_id)
            logger.info(f"删除文档数量: {deleted_count}")
            
            # 获取源目录下的所有文件
            files = self._get_all_files(source_path)
            logger.info(f"发现文件数量: {len(files)}")
            
            added_count = 0
            failed_count = 0
            errors = []
            
            # 导入所有文件
            for file_path in files:
                try:
                    self._add_document(
                        kb_id=kb_id,
                        file_path=file_path,
                        chunking_strategy=chunking_strategy,
                        chunk_size=chunk_size,
                        chunk_overlap=chunk_overlap
                    )
                    added_count += 1
                except Exception as e:
                    failed_count += 1
                    errors.append(f"文件失败 {file_path}: {str(e)}")
                    logger.error(f"导入文件失败: {file_path}, 错误: {str(e)}")
            
            # 记录同步日志
            self._create_sync_log(
                kb_id=kb_id,
                sync_type="full",
                source_path=source_path,
                added_count=added_count,
                updated_count=0,
                deleted_count=deleted_count,
                failed_count=failed_count,
                status="success",
                error_message="; ".join(errors) if errors else None
            )
            
            result = {
                "success": True,
                "added_count": added_count,
                "updated_count": 0,
                "deleted_count": deleted_count,
                "failed_count": failed_count,
                "errors": errors
            }
            
            logger.info(f"全量同步完成: {result}")
            return result
            
        except Exception as e:
            logger.error(f"全量同步失败: {str(e)}")
            # 记录失败日志
            self._create_sync_log(
                kb_id=kb_id,
                sync_type="full",
                source_path=source_path,
                added_count=0,
                updated_count=0,
                deleted_count=0,
                failed_count=0,
                status="failed",
                error_message=str(e)
            )
            return {
                "success": False,
                "error": str(e)
            }
    
    def _detect_changes(
        self,
        kb_id: int,
        source_path: str
    ) -> Dict:
        """
        检测文件变化
        
        Args:
            kb_id: 知识库ID
            source_path: 源文件路径
            
        Returns:
            变化列表
        """
        changes = {
            "added_files": [],
            "updated_files": [],
            "deleted_files": []
        }
        
        try:
            # 获取数据库中的文档记录
            db_docs = self._get_kb_documents(kb_id)
            db_doc_paths = {doc["doc_code"] for doc in db_docs}  # 使用doc_code作为路径标识
            
            # 获取源目录下的所有文件
            source_files = self._get_all_files(source_path)
            source_file_paths = set(source_files)
            
            # 检测新增和更新的文件
            for file_path in source_files:
                # 计算文件哈希
                file_hash = self._calculate_file_hash(file_path)
                
                # 检查文件是否在数据库中
                doc_code = os.path.basename(file_path)
                if doc_code in db_doc_paths:
                    # 检查文件是否有变化（比较哈希）
                    db_doc = next((doc for doc in db_docs if doc["doc_code"] == doc_code), None)
                    if db_doc and db_doc.get("file_hash") != file_hash:
                        changes["updated_files"].append(file_path)
                        logger.info(f"检测到文件更新: {file_path}")
                else:
                    # 新增文件
                    changes["added_files"].append(file_path)
                    logger.info(f"检测到新增文件: {file_path}")
            
            # 检测删除的文件
            for doc_code in db_doc_paths:
                if doc_code not in source_file_paths:
                    # 删除文件
                    file_path = os.path.join(source_path, doc_code)
                    changes["deleted_files"].append(file_path)
                    logger.info(f"检测到删除文件: {file_path}")
            
            logger.info(f"文件变化检测完成: 新增={len(changes['added_files'])}, "
                       f"更新={len(changes['updated_files'])}, 删除={len(changes['deleted_files'])}")
            
            return changes
            
        except Exception as e:
            logger.error(f"检测文件变化失败: {str(e)}")
            return changes
    
    def _get_all_files(self, source_path: str) -> List[str]:
        """
        获取源目录下的所有文件
        
        Args:
            source_path: 源文件路径
            
        Returns:
            文件路径列表
        """
        files = []
        
        try:
            if not os.path.exists(source_path):
                logger.warning(f"源目录不存在: {source_path}")
                return files
            
            # 支持的文件扩展名
            supported_extensions = ['.txt', '.md', '.pdf', '.doc', '.docx', '.ppt', '.pptx', '.html', '.csv']
            
            # 递归遍历目录
            for root, dirs, filenames in os.walk(source_path):
                for filename in filenames:
                    file_path = os.path.join(root, filename)
                    file_ext = os.path.splitext(filename)[1].lower()
                    
                    if file_ext in supported_extensions:
                        files.append(file_path)
            
            logger.info(f"找到 {len(files)} 个支持的文件")
            return files
            
        except Exception as e:
            logger.error(f"获取文件列表失败: {str(e)}")
            return files
    
    def _calculate_file_hash(self, file_path: str) -> str:
        """
        计算文件哈希（用于检测文件变化）
        
        Args:
            file_path: 文件路径
            
        Returns:
            文件哈希值
        """
        try:
            hash_md5 = hashlib.md5()
            with open(file_path, "rb") as f:
                # 读取文件内容计算哈希
                for chunk in iter(lambda: f.read(4096), b""):
                    hash_md5.update(chunk)
            return hash_md5.hexdigest()
        except Exception as e:
            logger.error(f"计算文件哈希失败: {file_path}, 错误: {str(e)}")
            return ""
    
    def _add_document(
        self,
        kb_id: int,
        file_path: str,
        chunking_strategy: str,
        chunk_size: int,
        chunk_overlap: int
    ):
        """
        添加文档到知识库
        
        Args:
            kb_id: 知识库ID
            file_path: 文件路径
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
        """
        # 导入文档处理服务
        from app.services.document_pipeline_service import DocumentPipelineService
        from app.services.text_chunking_service import ChunkingStrategy, TextChunkingService
        from app.services.embedding_service import EmbeddingService
        from app.services.document_parser_service import DocumentParserService

        # 创建文档处理管道
        parser = DocumentParserService()  # 实例化文档解析器
        chunking_service = TextChunkingService(  # 实例化分块服务
            strategy=ChunkingStrategy.PARAGRAPH,
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap
        )
        embedding_service = EmbeddingService(self.db)

        pipeline = DocumentPipelineService(
            db=self.db,
            parser=parser,
            chunking_service=chunking_service,
            embedding_service=embedding_service
        )
        
        # 处理文档
        filename = os.path.basename(file_path)
        file_ext = os.path.splitext(filename)[1][1:].lower()
        
        result = pipeline.process_document(
            file_path=file_path,
            doc_code=filename,  # 使用文件名作为文档编码
            doc_name=filename,
            doc_type=file_ext,
            kb_id=kb_id,
            chunking_strategy=ChunkingStrategy.PARAGRAPH,
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap
        )
        
        if not result.get("success"):
            raise Exception(f"文档处理失败: {result.get('error')}")
    
    def _update_document(
        self,
        kb_id: int,
        file_path: str,
        chunking_strategy: str,
        chunk_size: int,
        chunk_overlap: int
    ):
        """
        更新文档（删除旧的，添加新的）
        
        Args:
            kb_id: 知识库ID
            file_path: 文件路径
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
        """
        filename = os.path.basename(file_path)
        
        # 删除旧文档
        self._delete_document(kb_id, filename)
        
        # 添加新文档
        self._add_document(kb_id, file_path, chunking_strategy, chunk_size, chunk_overlap)
    
    def _delete_document(self, kb_id: int, doc_code: str):
        """
        删除文档
        
        Args:
            kb_id: 知识库ID
            doc_code: 文档编码
        """
        try:
            # 删除文档分块
            delete_chunks_sql = """
            DELETE FROM knowledge_document_chunk
            WHERE doc_id IN (
                SELECT id FROM knowledge_document 
                WHERE kb_id = :kb_id AND doc_code = :doc_code
            )
            """
            self.db.execute(
                text(delete_chunks_sql),
                {"kb_id": kb_id, "doc_code": doc_code}
            )
            
            # 删除文档
            delete_doc_sql = """
            DELETE FROM knowledge_document
            WHERE kb_id = :kb_id AND doc_code = :doc_code
            """
            self.db.execute(
                text(delete_doc_sql),
                {"kb_id": kb_id, "doc_code": doc_code}
            )
            
            self.db.commit()
            logger.info(f"删除文档成功: {doc_code}")
            
        except Exception as e:
            self.db.rollback()
            logger.error(f"删除文档失败: {doc_code}, 错误: {str(e)}")
            raise
    
    def _delete_all_documents(self, kb_id: int) -> int:
        """
        删除知识库的所有文档
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            删除的文档数量
        """
        try:
            # 获取文档数量
            count_sql = """
            SELECT COUNT(*) FROM knowledge_document
            WHERE kb_id = :kb_id
            """
            result = self.db.execute(text(count_sql), {"kb_id": kb_id})
            count = result.fetchone()[0]
            
            # 删除文档分块
            delete_chunks_sql = """
            DELETE FROM knowledge_document_chunk
            WHERE doc_id IN (
                SELECT id FROM knowledge_document 
                WHERE kb_id = :kb_id
            )
            """
            self.db.execute(text(delete_chunks_sql), {"kb_id": kb_id})
            
            # 删除文档
            delete_docs_sql = """
            DELETE FROM knowledge_document
            WHERE kb_id = :kb_id
            """
            self.db.execute(text(delete_docs_sql), {"kb_id": kb_id})
            
            self.db.commit()
            logger.info(f"删除知识库所有文档: kb_id={kb_id}, 数量={count}")
            return count
            
        except Exception as e:
            self.db.rollback()
            logger.error(f"删除文档失败: kb_id={kb_id}, 错误: {str(e)}")
            raise
    
    def _get_kb_documents(self, kb_id: int) -> List[Dict]:
        """
        获取知识库的文档列表
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            文档列表
        """
        try:
            query_sql = """
            SELECT 
                id, doc_code, doc_name, doc_type, file_path, file_hash
            FROM knowledge_document
            WHERE kb_id = :kb_id
            AND is_active = '1'
            """
            
            result = self.db.execute(text(query_sql), {"kb_id": kb_id})
            rows = result.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "doc_type": row[3],
                    "file_path": row[4],
                    "file_hash": row[5]
                })
            
            return documents
            
        except Exception as e:
            logger.error(f"获取知识库文档列表失败: {str(e)}")
            return []
    
    def _check_kb_exists(self, kb_id: int) -> bool:
        """
        检查知识库是否存在
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            是否存在
        """
        try:
            check_sql = """
            SELECT id FROM knowledge_base WHERE id = :kb_id
            """
            result = self.db.execute(text(check_sql), {"kb_id": kb_id})
            return result.fetchone() is not None
        except Exception as e:
            logger.error(f"检查知识库存在性失败: {str(e)}")
            return False
    
    def _create_sync_log(
        self,
        kb_id: int,
        sync_type: str,
        source_path: str,
        added_count: int,
        updated_count: int,
        deleted_count: int,
        failed_count: int,
        status: str,
        error_message: Optional[str] = None
    ):
        """
        创建同步日志
        
        Args:
            kb_id: 知识库ID
            sync_type: 同步类型
            source_path: 源文件路径
            added_count: 新增数量
            updated_count: 更新数量
            deleted_count: 删除数量
            failed_count: 失败数量
            status: 状态
            error_message: 错误信息
        """
        try:
            insert_sql = """
            INSERT INTO knowledge_base_sync_log
            (kb_id, sync_type, source_path, added_count, updated_count,
             deleted_count, failed_count, status, error_message, start_time, end_time)
            VALUES
            (:kb_id, :sync_type, :source_path, :added_count, :updated_count,
             :deleted_count, :failed_count, :status, :error_message, NOW(), NOW())
            """
            
            self.db.execute(
                text(insert_sql),
                {
                    "kb_id": kb_id,
                    "sync_type": sync_type,
                    "source_path": source_path,
                    "added_count": added_count,
                    "updated_count": updated_count,
                    "deleted_count": deleted_count,
                    "failed_count": failed_count,
                    "status": status,
                    "error_message": error_message
                }
            )
            self.db.commit()
            
        except Exception as e:
            logger.error(f"创建同步日志失败: {str(e)}")
            self.db.rollback()
    
    def _get_sync_logs(self, kb_id: int) -> List[Dict]:
        """
        获取知识库的同步日志列表
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            同步日志列表
        """
        try:
            query_sql = """
            SELECT 
                id, kb_id, sync_type, source_path,
                added_count, updated_count, deleted_count, failed_count,
                status, error_message, start_time, end_time, create_time
            FROM knowledge_base_sync_log
            WHERE kb_id = :kb_id
            ORDER BY create_time DESC
            LIMIT 100
            """
            
            result = self.db.execute(text(query_sql), {"kb_id": kb_id})
            rows = result.fetchall()
            
            logs = []
            for row in rows:
                logs.append({
                    "id": row[0],
                    "kb_id": row[1],
                    "sync_type": row[2],
                    "source_path": row[3],
                    "added_count": row[4],
                    "updated_count": row[5],
                    "deleted_count": row[6],
                    "failed_count": row[7],
                    "status": row[8],
                    "error_message": row[9],
                    "start_time": row[10].isoformat() if row[10] else None,
                    "end_time": row[11].isoformat() if row[11] else None,
                    "create_time": row[12].isoformat() if row[12] else None
                })
            
            logger.info(f"获取同步日志成功: kb_id={kb_id}, 数量={len(logs)}")
            return logs
            
        except Exception as e:
            logger.error(f"获取同步日志失败: {str(e)}")
            return []

