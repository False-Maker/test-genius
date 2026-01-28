"""
知识库服务
管理知识库文档、向量存储和语义检索
"""
import logging
from typing import List, Dict, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)


class KnowledgeBaseService:
    """知识库服务"""
    
    def __init__(self, db: Session):
        """
        初始化知识库服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.embedding_service = EmbeddingService(db)
    
    def init_knowledge_base_tables(self) -> bool:
        """
        初始化知识库相关表结构（如果使用pgvector）
        
        Returns:
            是否初始化成功
        """
        try:
            # 检查并安装pgvector扩展
            if not self.embedding_service.install_pgvector_extension():
                logger.warning("pgvector扩展未安装，将使用关系型数据库存储")
                return False
            
            # 创建知识库文档表（带向量列）
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS knowledge_document (
                id BIGSERIAL PRIMARY KEY,
                doc_code VARCHAR(100) UNIQUE NOT NULL,
                doc_name VARCHAR(500) NOT NULL,
                doc_type VARCHAR(50), -- 文档类型：规范/业务规则/用例模板/历史用例
                doc_category VARCHAR(100), -- 文档分类
                doc_content TEXT, -- 文档内容
                doc_url VARCHAR(1000), -- 文档URL
                embedding vector(1536), -- 向量列（使用pgvector）
                is_active CHAR(1) DEFAULT '1',
                creator_id BIGINT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            -- 创建向量索引（用于快速相似度搜索）
            CREATE INDEX IF NOT EXISTS idx_knowledge_document_embedding 
            ON knowledge_document 
            USING ivfflat (embedding vector_cosine_ops)
            WITH (lists = 100);
            
            -- 创建其他索引
            CREATE INDEX IF NOT EXISTS idx_knowledge_doc_code ON knowledge_document(doc_code);
            CREATE INDEX IF NOT EXISTS idx_knowledge_doc_type ON knowledge_document(doc_type, is_active);
            CREATE INDEX IF NOT EXISTS idx_knowledge_doc_category ON knowledge_document(doc_category);
            """
            
            self.db.execute(text(create_table_sql))
            self.db.commit()
            logger.info("知识库表结构初始化成功")
            return True
            
        except Exception as e:
            logger.error(f"初始化知识库表结构失败: {str(e)}")
            self.db.rollback()
            return False
    
    def add_document(
        self,
        doc_code: str,
        doc_name: str,
        doc_type: str,
        doc_content: str,
        doc_category: Optional[str] = None,
        doc_url: Optional[str] = None,
        creator_id: Optional[int] = None
    ) -> Optional[int]:
        """
        添加知识库文档
        
        Args:
            doc_code: 文档编码
            doc_name: 文档名称
            doc_type: 文档类型
            doc_content: 文档内容
            doc_category: 文档分类
            doc_url: 文档URL
            creator_id: 创建人ID
            
        Returns:
            文档ID，如果失败返回None
        """
        try:
            # 生成文档内容的向量
            embedding = self.embedding_service.get_embedding(doc_content)
            
            if embedding is None:
                logger.warning(f"文档向量化失败，将不存储向量: {doc_code}")
                embedding_list = None
            else:
                # 将向量转换为PostgreSQL数组格式
                embedding_list = str(embedding)
            
            # 插入文档
            insert_sql = """
            INSERT INTO knowledge_document 
            (doc_code, doc_name, doc_type, doc_category, doc_content, doc_url, embedding, creator_id)
            VALUES 
            (:doc_code, :doc_name, :doc_type, :doc_category, :doc_content, :doc_url, 
             :embedding::vector, :creator_id)
            RETURNING id
            """
            
            result = self.db.execute(
                text(insert_sql),
                {
                    "doc_code": doc_code,
                    "doc_name": doc_name,
                    "doc_type": doc_type,
                    "doc_category": doc_category,
                    "doc_content": doc_content,
                    "doc_url": doc_url,
                    "embedding": embedding_list,
                    "creator_id": creator_id
                }
            )
            
            doc_id = result.fetchone()[0]
            self.db.commit()
            logger.info(f"知识库文档添加成功: {doc_code}, ID: {doc_id}")
            return doc_id
            
        except Exception as e:
            logger.error(f"添加知识库文档失败: {str(e)}")
            self.db.rollback()
            return None
    
    def search_by_semantic(
        self,
        query_text: str,
        doc_type: Optional[str] = None,
        top_k: int = 10,
        similarity_threshold: float = 0.7
    ) -> List[Dict]:
        """
        语义检索知识库文档
        
        Args:
            query_text: 查询文本
            doc_type: 文档类型过滤（可选）
            top_k: 返回前K个结果
            similarity_threshold: 相似度阈值（0-1）
            
        Returns:
            文档列表，按相似度排序
        """
        try:
            # 获取查询文本的向量
            query_embedding = self.embedding_service.get_embedding(query_text)
            
            if query_embedding is None:
                logger.warning("查询文本向量化失败，使用关键词检索")
                return self.search_by_keyword(query_text, doc_type, top_k)
            
            # 构建SQL查询（使用pgvector的余弦相似度函数）
            search_sql = """
            SELECT 
                id, doc_code, doc_name, doc_type, doc_category, 
                doc_content, doc_url, create_time,
                1 - (embedding <=> :query_embedding::vector) as similarity
            FROM knowledge_document
            WHERE is_active = '1'
            AND embedding IS NOT NULL
            """
            
            params = {
                "query_embedding": str(query_embedding),
                "similarity_threshold": similarity_threshold,
                "top_k": top_k
            }
            
            if doc_type:
                search_sql += " AND doc_type = :doc_type"
                params["doc_type"] = doc_type
            
            search_sql += """
            AND (1 - (embedding <=> :query_embedding::vector)) >= :similarity_threshold
            ORDER BY embedding <=> :query_embedding::vector
            LIMIT :top_k
            """
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "doc_type": row[3],
                    "doc_category": row[4],
                    "doc_content": row[5],
                    "doc_url": row[6],
                    "create_time": row[7],
                    "similarity": float(row[8]) if row[8] else 0.0
                })
            
            logger.info(f"语义检索完成，找到 {len(documents)} 个文档")
            return documents
            
        except Exception as e:
            logger.error(f"语义检索失败: {str(e)}")
            # 如果向量检索失败，降级到关键词检索
            return self.search_by_keyword(query_text, doc_type, top_k)
    
    def search_by_keyword(
        self,
        keyword: str,
        doc_type: Optional[str] = None,
        top_k: int = 10
    ) -> List[Dict]:
        """
        关键词检索知识库文档（降级方案）
        
        Args:
            keyword: 关键词
            doc_type: 文档类型过滤（可选）
            top_k: 返回前K个结果
            
        Returns:
            文档列表
        """
        try:
            search_sql = """
            SELECT 
                id, doc_code, doc_name, doc_type, doc_category, 
                doc_content, doc_url, create_time
            FROM knowledge_document
            WHERE is_active = '1'
            AND (doc_name LIKE :keyword OR doc_content LIKE :keyword)
            """
            
            params = {
                "keyword": f"%{keyword}%",
                "top_k": top_k
            }
            
            if doc_type:
                search_sql += " AND doc_type = :doc_type"
                params["doc_type"] = doc_type
            
            search_sql += " ORDER BY create_time DESC LIMIT :top_k"
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "doc_type": row[3],
                    "doc_category": row[4],
                    "doc_content": row[5],
                    "doc_url": row[6],
                    "create_time": row[7],
                    "similarity": 0.0  # 关键词检索没有相似度
                })
            
            logger.info(f"关键词检索完成，找到 {len(documents)} 个文档")
            return documents
            
        except Exception as e:
            logger.error(f"关键词检索失败: {str(e)}")
            return []
    
    def search_multi_kb(
        self,
        query_text: str,
        kb_ids: List[int],
        top_k: int = 10,
        similarity_threshold: float = 0.7,
        use_hybrid: bool = True,
        use_rerank: bool = True,
        doc_type: Optional[str] = None
    ) -> List[Dict]:
        """
        多路召回（从多个知识库检索）
        
        Args:
            query_text: 查询文本
            kb_ids: 知识库ID列表
            top_k: 每个知识库返回前K个结果（最终合并后取top_k）
            similarity_threshold: 相似度阈值
            use_hybrid: 是否使用混合检索（如果可用）
            use_rerank: 是否使用重排序（如果可用）
            doc_type: 文档类型过滤
            
        Returns:
            合并后的检索结果列表
        """
        logger.info(f"开始多路召回: 查询='{query_text}', 知识库数={len(kb_ids)}, top_k={top_k}")
        
        if not kb_ids:
            logger.warning("知识库ID列表为空")
            return []
        
        # 获取每个知识库的检索结果
        results_list = []
        for kb_id in kb_ids:
            try:
                # 从单个知识库检索
                kb_results = self._search_by_kb_id(
                    query_text=query_text,
                    kb_id=kb_id,
                    top_k=top_k * 2,  # 获取更多结果以便合并
                    similarity_threshold=similarity_threshold,
                    use_hybrid=use_hybrid,
                    doc_type=doc_type
                )
                results_list.append(kb_results)
                logger.info(f"知识库{kb_id}检索到{len(kb_results)}个结果")
            except Exception as e:
                logger.error(f"从知识库{kb_id}检索失败: {str(e)}")
                results_list.append([])
        
        # 合并多个检索结果
        if len(results_list) > 1:
            # 使用RRF方法合并结果
            merged_results = self.merge_results(results_list, method="rrf")
            # 限制最终返回数量
            merged_results = merged_results[:top_k]
        else:
            merged_results = results_list[0] if results_list else []
        
        # 使用重排序（如果启用）
        if use_rerank and merged_results:
            try:
                # 导入重排序服务
                from app.services.reranker_service import RerankerService
                
                # 创建重排序器（如果尚未创建）
                if not hasattr(self, 'reranker_service'):
                    self.reranker_service = RerankerService()
                
                # 重排序
                merged_results = self.reranker_service.rerank(
                    query=query_text,
                    documents=merged_results,
                    top_k=top_k
                )
                logger.info(f"重排序完成")
            except Exception as e:
                logger.warning(f"重排序失败: {str(e)}，跳过重排序")
        
        logger.info(f"多路召回完成，最终返回{len(merged_results)}个结果")
        return merged_results
    
    def _search_by_kb_id(
        self,
        query_text: str,
        kb_id: int,
        top_k: int = 10,
        similarity_threshold: float = 0.7,
        use_hybrid: bool = True,
        doc_type: Optional[str] = None
    ) -> List[Dict]:
        """
        从指定知识库检索
        
        Args:
            query_text: 查询文本
            kb_id: 知识库ID
            top_k: 返回前K个结果
            similarity_threshold: 相似度阈值
            use_hybrid: 是否使用混合检索
            doc_type: 文档类型过滤
            
        Returns:
            检索结果列表
        """
        try:
            # 构建SQL查询（带kb_id过滤）
            search_sql = """
            SELECT 
                id, doc_code, doc_name, doc_type, doc_category, 
                doc_content, doc_url, create_time,
                1 - (embedding <=> :query_embedding::vector) as similarity
            FROM knowledge_document
            WHERE is_active = '1'
            AND kb_id = :kb_id
            """
            
            # 获取查询向量
            query_embedding = self.embedding_service.get_embedding(query_text)
            
            if query_embedding is None:
                logger.warning(f"查询文本向量化失败，使用关键词检索: kb_id={kb_id}")
                # 降级到关键词检索
                return self._search_by_kb_id_keyword(query_text, kb_id, top_k, doc_type)
            
            params = {
                "kb_id": kb_id,
                "query_embedding": str(query_embedding),
                "similarity_threshold": similarity_threshold,
                "top_k": top_k
            }
            
            if doc_type:
                search_sql += " AND doc_type = :doc_type"
                params["doc_type"] = doc_type
            
            search_sql += """
            AND (1 - (embedding <=> :query_embedding::vector)) >= :similarity_threshold
            ORDER BY embedding <=> :query_embedding::vector
            LIMIT :top_k
            """
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "doc_type": row[3],
                    "doc_category": row[4],
                    "doc_content": row[5],
                    "doc_url": row[6],
                    "create_time": row[7],
                    "similarity": float(row[8]) if row[8] else 0.0,
                    "kb_id": kb_id
                })
            
            logger.info(f"知识库{kb_id}语义检索完成: 找到{len(documents)}个文档")
            return documents
            
        except Exception as e:
            logger.error(f"知识库{kb_id}检索失败: {str(e)}")
            # 降级到关键词检索
            return self._search_by_kb_id_keyword(query_text, kb_id, top_k, doc_type)
    
    def _search_by_kb_id_keyword(
        self,
        query_text: str,
        kb_id: int,
        top_k: int = 10,
        doc_type: Optional[str] = None
    ) -> List[Dict]:
        """
        从指定知识库关键词检索（降级方案）
        
        Args:
            query_text: 查询文本
            kb_id: 知识库ID
            top_k: 返回前K个结果
            doc_type: 文档类型过滤
            
        Returns:
            检索结果列表
        """
        try:
            search_sql = """
            SELECT 
                id, doc_code, doc_name, doc_type, doc_category, 
                doc_content, doc_url, create_time
            FROM knowledge_document
            WHERE is_active = '1'
            AND kb_id = :kb_id
            AND (doc_name LIKE :keyword OR doc_content LIKE :keyword)
            """
            
            params = {
                "kb_id": kb_id,
                "keyword": f"%{query_text}%",
                "top_k": top_k
            }
            
            if doc_type:
                search_sql += " AND doc_type = :doc_type"
                params["doc_type"] = doc_type
            
            search_sql += " ORDER BY create_time DESC LIMIT :top_k"
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "doc_type": row[3],
                    "doc_category": row[4],
                    "doc_content": row[5],
                    "doc_url": row[6],
                    "create_time": row[7],
                    "similarity": 0.0,  # 关键词检索没有相似度
                    "kb_id": kb_id
                })
            
            logger.info(f"知识库{kb_id}关键词检索完成: 找到{len(documents)}个文档")
            return documents
            
        except Exception as e:
            logger.error(f"知识库{kb_id}关键词检索失败: {str(e)}")
            return []
    
    def merge_results(
        self,
        results_list: List[List[Dict]],
        method: str = "rrf",
        k: int = 60
    ) -> List[Dict]:
        """
        合并多个检索结果
        
        Args:
            results_list: 检索结果列表（每个元素是一个知识库的结果）
            method: 合并方法（rrf/weighted/max）
            k: RRF常数
            
        Returns:
            合并后的结果列表
        """
        if not results_list:
            return []
        
        logger.info(f"开始合并{len(results_list)}个检索结果，方法={method}")
        
        if method == "rrf":
            merged = self._merge_rrf(results_list, k)
        elif method == "weighted":
            merged = self._merge_weighted(results_list)
        elif method == "max":
            merged = self._merge_max(results_list)
        else:
            logger.warning(f"未知的合并方法: {method}，使用rrf")
            merged = self._merge_rrf(results_list, k)
        
        logger.info(f"合并完成，结果数={len(merged)}")
        return merged
    
    def _merge_rrf(
        self,
        results_list: List[List[Dict]],
        k: int = 60
    ) -> List[Dict]:
        """
        使用Reciprocal Rank Fusion合并结果
        
        Args:
            results_list: 检索结果列表
            k: RRF常数
            
        Returns:
            合并后的结果
        """
        # 构建文档RRF分数映射
        doc_rrf_scores = {}  # doc_id -> (rrf_score, document, sources)
        
        # 处理每个知识库的结果
        for kb_idx, results in enumerate(results_list):
            for rank, result in enumerate(results):
                doc_id = result["id"]
                rrf_score = 1.0 / (k + rank + 1)
                
                if doc_id not in doc_rrf_scores:
                    doc_rrf_scores[doc_id] = {
                        "score": rrf_score,
                        "document": result.copy(),
                        "sources": [kb_idx]
                    }
                else:
                    doc_rrf_scores[doc_id]["score"] += rrf_score
                    doc_rrf_scores[doc_id]["sources"].append(kb_idx)
        
        # 按RRF分数排序
        sorted_docs = sorted(
            doc_rrf_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 返回结果
        merged_results = []
        for doc_id, data in sorted_docs:
            result = data["document"]
            result["rrf_score"] = data["score"]
            result["kb_sources"] = data["sources"]  # 来源知识库索引列表
            merged_results.append(result)
        
        return merged_results
    
    def _merge_weighted(self, results_list: List[List[Dict]]) -> List[Dict]:
        """
        使用加权融合合并结果
        
        Args:
            results_list: 检索结果列表
            
        Returns:
            合并后的结果
        """
        weight = 1.0 / len(results_list)  # 每个知识库的权重
        
        # 构建文档分数映射
        doc_scores = {}  # doc_id -> (score, document, sources)
        
        # 处理每个知识库的结果
        for kb_idx, results in enumerate(results_list):
            for result in results:
                doc_id = result["id"]
                # 使用相似度或默认分数
                score = result.get("similarity", 0.0) * weight
                
                if doc_id not in doc_scores:
                    doc_scores[doc_id] = {
                        "score": score,
                        "document": result.copy(),
                        "sources": [kb_idx]
                    }
                else:
                    doc_scores[doc_id]["score"] += score
                    if kb_idx not in doc_scores[doc_id]["sources"]:
                        doc_scores[doc_id]["sources"].append(kb_idx)
        
        # 按分数排序
        sorted_docs = sorted(
            doc_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 返回结果
        merged_results = []
        for doc_id, data in sorted_docs:
            result = data["document"]
            result["weighted_score"] = data["score"]
            result["kb_sources"] = data["sources"]
            merged_results.append(result)
        
        return merged_results
    
    def _merge_max(self, results_list: List[List[Dict]]) -> List[Dict]:
        """
        使用最大分数融合合并结果
        
        Args:
            results_list: 检索结果列表
            
        Returns:
            合并后的结果
        """
        # 构建文档最大分数映射
        doc_max_scores = {}  # doc_id -> (max_score, document, sources)
        
        # 处理每个知识库的结果
        for kb_idx, results in enumerate(results_list):
            for result in results:
                doc_id = result["id"]
                score = result.get("similarity", 0.0)
                
                if doc_id not in doc_max_scores:
                    doc_max_scores[doc_id] = {
                        "score": score,
                        "document": result.copy(),
                        "sources": [kb_idx]
                    }
                else:
                    # 取最大值
                    if score > doc_max_scores[doc_id]["score"]:
                        doc_max_scores[doc_id]["score"] = score
                        doc_max_scores[doc_id]["document"] = result.copy()
                    if kb_idx not in doc_max_scores[doc_id]["sources"]:
                        doc_max_scores[doc_id]["sources"].append(kb_idx)
        
        # 按最大分数排序
        sorted_docs = sorted(
            doc_max_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 返回结果
        merged_results = []
        for doc_id, data in sorted_docs:
            result = data["document"]
            result["max_score"] = data["score"]
            result["kb_sources"] = data["sources"]
            merged_results.append(result)
        
        return merged_results

