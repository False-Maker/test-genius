"""
文档处理管道服务
参考Dify的文档处理管道，实现完整的文档上传、解析、分块、向量化流程
"""
from typing import Dict, List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text
import logging
import uuid
from datetime import datetime
from pathlib import Path

from app.services.document_parser_service import DocumentParserService
from app.services.text_chunking_service import TextChunkingService, ChunkingStrategy
from app.services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)


class DocumentPipelineService:
    """文档处理管道服务"""
    
    def __init__(
        self,
        db: Session,
        parser: Optional[DocumentParserService] = None,
        chunking_service: Optional[TextChunkingService] = None,
        embedding_service: Optional[EmbeddingService] = None
    ):
        """
        初始化文档处理管道
        
        Args:
            db: 数据库会话
            parser: 文档解析器
            chunking_service: 分块服务
            embedding_service: 向量化服务
        """
        self.db = db
        self.parser = parser or DocumentParserService()
        self.chunking_service = chunking_service
        self.embedding_service = embedding_service or EmbeddingService(db)
        
        if self.chunking_service is None:
            self.chunking_service = TextChunkingService(
                strategy=ChunkingStrategy.PARAGRAPH,
                chunk_size=1000,
                chunk_overlap=200
            )
    
    def process_document(
        self,
        file_path: str,
        doc_code: str,
        doc_name: str,
        doc_type: str,
        kb_id: Optional[int] = None,
        doc_category: Optional[str] = None,
        chunking_strategy: ChunkingStrategy = ChunkingStrategy.PARAGRAPH,
        chunk_size: int = 1000,
        chunk_overlap: int = 200
    ) -> Dict:
        """
        处理文档（完整流程）
        
        Args:
            file_path: 文档文件路径
            doc_code: 文档编码
            doc_name: 文档名称
            doc_type: 文档类型
            kb_id: 知识库ID
            doc_category: 文档分类
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
            
        Returns:
            处理结果，包含：
            - success: 是否成功
            - doc_id: 文档ID
            - chunks: 分块数量
            - error: 错误信息
        """
        try:
            logger.info(f"开始处理文档: doc_code={doc_code}, 文件路径={file_path}")
            
            # 步骤1：提取文档内容
            parse_result = self._extract_content(file_path)
            
            # 步骤2：清理文本
            cleaned_content = self._clean_text(parse_result["content"])
            
            # 步骤3：分块内容
            chunks = self._chunk_content(
                cleaned_content,
                parse_result["metadata"],
                chunking_strategy,
                chunk_size,
                chunk_overlap
            )
            
            # 步骤4：向量化分块
            vectorized_chunks = self._vectorize_chunks(chunks)
            
            # 步骤5：存储文档和分块到数据库
            doc_id = self._store_document(
                doc_code,
                doc_name,
                doc_type,
                kb_id,
                doc_category,
                cleaned_content,
                parse_result["metadata"],
                file_path
            )
            
            if doc_id is None:
                raise Exception("文档存储失败")
            
            # 步骤6：存储分块
            chunk_ids = self._store_chunks(vectorized_chunks, doc_id)
            
            # 步骤7：构建文档索引
            self._build_document_index(doc_id, vectorized_chunks)
            
            logger.info(f"文档处理成功: doc_id={doc_id}, 分块数={len(chunks)}")
            
            return {
                "success": True,
                "doc_id": doc_id,
                "chunks": len(chunks),
                "chunk_ids": chunk_ids,
                "doc_code": doc_code
            }
            
        except Exception as e:
            logger.error(f"文档处理失败: doc_code={doc_code}, 错误={str(e)}", exc_info=True)
            return {
                "success": False,
                "doc_id": None,
                "chunks": 0,
                "error": str(e)
            }
    
    def _extract_content(self, file_path: str) -> Dict:
        """步骤1：提取文档内容"""
        logger.info(f"步骤1: 提取文档内容: {file_path}")
        
        try:
            result = self.parser.parse_document(file_path)
            logger.info(f"文档提取成功: 字符数={result.get('char_count', 0)}")
            return result
        except Exception as e:
            logger.error(f"文档提取失败: {str(e)}")
            raise
    
    def _clean_text(self, text: str) -> str:
        """步骤2：清理文本（去除特殊字符、统一编码等）"""
        logger.info("步骤2: 清理文本")
        
        # 使用分块服务的清理方法
        cleaned = self.chunking_service._clean_text(text)
        
        logger.info(f"文本清理完成: 原长度={len(text)}, 新长度={len(cleaned)}")
        return cleaned
    
    def _chunk_content(
        self,
        text: str,
        metadata: Dict,
        strategy: ChunkingStrategy,
        chunk_size: int,
        chunk_overlap: int
    ) -> List[Dict]:
        """步骤3：分块内容"""
        logger.info(f"步骤3: 分块内容, 策略={strategy.value}, 大小={chunk_size}, 重叠={chunk_overlap}")
        
        # 更新分块服务的配置
        self.chunking_service.strategy = strategy
        self.chunking_service.chunk_size = chunk_size
        self.chunking_service.chunk_overlap = chunk_overlap
        
        # 分块
        chunks = self.chunking_service.chunk_text(text, metadata)
        
        # 获取统计信息
        stats = self.chunking_service.get_statistics(chunks)
        logger.info(f"分块完成: {stats}")
        
        return chunks
    
    def _vectorize_chunks(self, chunks: List[Dict]) -> List[Dict]:
        """步骤4：向量化分块"""
        logger.info("步骤4: 向量化分块")
        
        # 提取分块内容
        contents = [chunk["content"] for chunk in chunks]
        
        # 批量向量化
        embeddings = self.embedding_service.batch_get_embeddings(contents)
        
        # 添加向量到分块
        vectorized_chunks = []
        for chunk, embedding in zip(chunks, embeddings):
            vectorized_chunks.append({
                **chunk,
                "embedding": embedding
            })
        
        successful_count = sum(1 for e in embeddings if e is not None)
        logger.info(f"向量化完成: 成功={successful_count}/{len(chunks)}")
        
        return vectorized_chunks
    
    def _store_document(
        self,
        doc_code: str,
        doc_name: str,
        doc_type: str,
        kb_id: Optional[int],
        doc_category: Optional[str],
        doc_content: str,
        metadata: Dict,
        file_path: str
    ) -> Optional[int]:
        """步骤5：存储文档到数据库"""
        logger.info("步骤5: 存储文档到数据库")
        
        try:
            # 获取文件信息
            path_obj = Path(file_path)
            file_size = path_obj.stat().st_size if path_obj.exists() else 0
            
            # 提取元数据中的字段
            language = metadata.get("language", "unknown")
            encoding = metadata.get("encoding", "")
            page_count = metadata.get("page_count", 0)
            slide_count = metadata.get("slide_count", 0)
            row_count = metadata.get("row_count", 0)
            column_count = metadata.get("column_count", 0)
            table_count = metadata.get("table_count", 0)
            
            # 构建额外元数据
            extra_metadata = {
                "title": metadata.get("title", ""),
                "author": metadata.get("author", ""),
                "created": metadata.get("created_time", ""),
                "modified": metadata.get("modified_time", ""),
                "structure": metadata.get("structure", {})
            }
            
            # 插入文档
            insert_sql = """
            INSERT INTO knowledge_document 
            (doc_code, kb_id, doc_name, doc_type, doc_category, doc_content, 
             file_size, file_path, language, encoding, page_count, slide_count, 
             row_count, column_count, table_count, metadata)
            VALUES 
            (:doc_code, :kb_id, :doc_name, :doc_type, :doc_category, :doc_content,
             :file_size, :file_path, :language, :encoding, :page_count, :slide_count,
             :row_count, :column_count, :table_count, :metadata::jsonb)
            RETURNING id
            """
            
            result = self.db.execute(
                text(insert_sql),
                {
                    "doc_code": doc_code,
                    "kb_id": kb_id,
                    "doc_name": doc_name,
                    "doc_type": doc_type,
                    "doc_category": doc_category,
                    "doc_content": doc_content,
                    "file_size": file_size,
                    "file_path": file_path,
                    "language": language,
                    "encoding": encoding,
                    "page_count": page_count,
                    "slide_count": slide_count,
                    "row_count": row_count,
                    "column_count": column_count,
                    "table_count": table_count,
                    "metadata": str(extra_metadata).replace("'", '"')
                }
            )
            
            doc_id = result.fetchone()[0]
            self.db.commit()
            
            logger.info(f"文档存储成功: doc_id={doc_id}, doc_code={doc_code}")
            return doc_id
            
        except Exception as e:
            logger.error(f"文档存储失败: {str(e)}")
            self.db.rollback()
            return None
    
    def _store_chunks(self, chunks: List[Dict], doc_id: int) -> List[str]:
        """步骤6：存储分块到数据库"""
        logger.info(f"步骤6: 存储分块到数据库, 分块数={len(chunks)}")
        
        chunk_ids = []
        
        try:
            for chunk in chunks:
                chunk_id = chunk.get("chunk_id", "")
                chunk_content = chunk.get("content", "")
                chunk_index = chunk.get("metadata", {}).get("chunk_index", 0)
                chunk_type = chunk.get("metadata", {}).get("chunk_type", "")
                chunk_strategy = chunk.get("metadata", {}).get("chunk_strategy", "")
                chunk_start = chunk.get("metadata", {}).get("chunk_start", 0)
                chunk_end = chunk.get("metadata", {}).get("chunk_end", 0)
                has_overlap = chunk.get("metadata", {}).get("has_overlap", False)
                embedding = chunk.get("embedding")
                metadata = chunk.get("metadata", {})
                
                # 转换向量格式
                embedding_list = str(embedding) if embedding else None
                
                insert_sql = """
                INSERT INTO knowledge_document_chunk 
                (doc_id, chunk_id, chunk_index, chunk_content, chunk_length, 
                 chunk_type, chunk_strategy, chunk_start, chunk_end, has_overlap, 
                 embedding, metadata)
                VALUES 
                (:doc_id, :chunk_id, :chunk_index, :chunk_content, :chunk_length,
                 :chunk_type, :chunk_strategy, :chunk_start, :chunk_end, :has_overlap,
                 :embedding::vector, :metadata::jsonb)
                ON CONFLICT (chunk_id) DO NOTHING
                """
                
                self.db.execute(
                    text(insert_sql),
                    {
                        "doc_id": doc_id,
                        "chunk_id": chunk_id,
                        "chunk_index": chunk_index,
                        "chunk_content": chunk_content,
                        "chunk_length": len(chunk_content),
                        "chunk_type": chunk_type,
                        "chunk_strategy": chunk_strategy,
                        "chunk_start": chunk_start,
                        "chunk_end": chunk_end,
                        "has_overlap": '1' if has_overlap else '0',
                        "embedding": embedding_list,
                        "metadata": str(metadata).replace("'", '"')
                    }
                )
                
                chunk_ids.append(chunk_id)
            
            self.db.commit()
            logger.info(f"分块存储成功: doc_id={doc_id}, 分块数={len(chunk_ids)}")
            return chunk_ids
            
        except Exception as e:
            logger.error(f"分块存储失败: {str(e)}")
            self.db.rollback()
            return []
    
    def _build_document_index(self, doc_id: int, chunks: List[Dict]) -> bool:
        """步骤7：构建文档索引（用于快速检索）"""
        logger.info(f"步骤7: 构建文档索引, doc_id={doc_id}")
        
        try:
            # 简单实现：提取关键词并更新到metadata中
            # 如果使用了Postgres pgvector, 向量索引已经在插入时建立
            # 这里主要为了混合检索补充关键词
            
            for chunk in chunks:
                content = chunk.get("content", "")
                if not content:
                    continue
                    
                # 提取关键词 (复用简单的频率提取逻辑)
                keywords = self._extract_keywords(content)
                
                # 更新metadata
                chunk_id = chunk.get("chunk_id")
                if chunk_id:
                    update_sql = """
                    UPDATE knowledge_document_chunk
                    SET metadata = jsonb_set(metadata, '{keywords}', :keywords::jsonb)
                    WHERE chunk_id = :chunk_id
                    """
                    
                    self.db.execute(
                        text(update_sql),
                        {
                            "keywords": str(keywords).replace("'", '"'),
                            "chunk_id": chunk_id
                        }
                    )
            
            self.db.commit()
            logger.info("文档索引构建完成 (关键词提取)")
            return True
        except Exception as e:
            logger.error(f"文档索引构建失败: {str(e)}")
            self.db.rollback()
            return False

    def _extract_keywords(self, content: str, top_k: int = 10) -> List[str]:
        """提取关键词 (简单实现)"""
        import re
        from collections import Counter
        
        # 提取中文词汇 (长度2-6)
        words = re.findall(r'[\u4e00-\u9fa5]{2,6}', content)
        # 提取英文单词 (长度3+)
        words.extend(re.findall(r'\b[a-zA-Z]{3,}\b', content.lower()))
        
        # 过滤常用停用词 (简单列表)
        stop_words = {'the', 'and', 'is', 'for', 'with', 'that', 'this', 'are', 
                     '我们', '可以', '这个', '那个', '因为', '所以', '如果', '但是',
                     '文档', '内容', '分块', '包含'}
        words = [w for w in words if w not in stop_words]
        
        counter = Counter(words)
        return [w for w, _ in counter.most_common(top_k)]
    
    def batch_process_documents(
        self,
        file_list: List[Dict],
        kb_id: Optional[int] = None,
        chunking_strategy: ChunkingStrategy = ChunkingStrategy.PARAGRAPH,
        chunk_size: int = 1000,
        chunk_overlap: int = 200
    ) -> Dict:
        """
        批量处理文档
        
        Args:
            file_list: 文件列表，每个元素包含file_path, doc_code, doc_name, doc_type
            kb_id: 知识库ID
            chunking_strategy: 分块策略
            chunk_size: 分块大小
            chunk_overlap: 分块重叠
            
        Returns:
            批量处理结果
        """
        logger.info(f"开始批量处理文档: 文件数={len(file_list)}")
        
        results = {
            "total": len(file_list),
            "success": 0,
            "failed": 0,
            "details": []
        }
        
        for file_info in file_list:
            try:
                result = self.process_document(
                    file_path=file_info["file_path"],
                    doc_code=file_info["doc_code"],
                    doc_name=file_info["doc_name"],
                    doc_type=file_info["doc_type"],
                    kb_id=kb_id,
                    chunking_strategy=chunking_strategy,
                    chunk_size=chunk_size,
                    chunk_overlap=chunk_overlap
                )
                
                results["details"].append({
                    "doc_code": file_info["doc_code"],
                    "success": result["success"],
                    "doc_id": result.get("doc_id"),
                    "chunks": result.get("chunks", 0),
                    "error": result.get("error")
                })
                
                if result["success"]:
                    results["success"] += 1
                else:
                    results["failed"] += 1
                    
            except Exception as e:
                logger.error(f"处理文档失败: {file_info.get('doc_code')}, 错误={str(e)}")
                results["details"].append({
                    "doc_code": file_info.get("doc_code"),
                    "success": False,
                    "error": str(e)
                })
                results["failed"] += 1
        
        logger.info(f"批量处理完成: 成功={results['success']}, 失败={results['failed']}")
        return results

