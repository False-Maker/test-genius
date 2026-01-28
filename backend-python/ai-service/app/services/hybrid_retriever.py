"""
混合检索器
参考Dify的混合检索实现，结合向量检索和BM25检索
"""
from typing import List, Dict, Optional
from app.services.bm25_retriever import BM25Retriever
from app.services.knowledge_base_service import KnowledgeBaseService
import logging

logger = logging.getLogger(__name__)


class HybridRetriever:
    """混合检索器"""
    
    def __init__(
        self,
        knowledge_base_service: KnowledgeBaseService,
        bm25_retriever: BM25Retriever,
        vector_weight: float = 0.7,
        bm25_weight: float = 0.3
    ):
        """
        初始化混合检索器
        
        Args:
            knowledge_base_service: 知识库服务（向量检索）
            bm25_retriever: BM25检索器
            vector_weight: 向量检索权重（0-1）
            bm25_weight: BM25检索权重（0-1）
        """
        self.kb_service = knowledge_base_service
        self.bm25_retriever = bm25_retriever
        self.vector_weight = vector_weight
        self.bm25_weight = bm25_weight
        
        # 验证权重和为1
        total_weight = vector_weight + bm25_weight
        if abs(total_weight - 1.0) > 0.01:
            logger.warning(f"权重和不等于1.0 (当前={total_weight:.2f})，将进行归一化")
            self.vector_weight = vector_weight / total_weight
            self.bm25_weight = bm25_weight / total_weight
        
        logger.info(f"混合检索器初始化完成: vector_weight={self.vector_weight:.2f}, bm25_weight={self.bm25_weight:.2f}")
    
    def search(
        self,
        query: str,
        doc_type: Optional[str] = None,
        top_k: int = 10,
        method: str = "weighted",
        similarity_threshold: float = 0.7
    ) -> List[Dict]:
        """
        混合检索
        
        Args:
            query: 查询文本
            doc_type: 文档类型过滤
            top_k: 返回前K个结果
            method: 融合方法（weighted/rrf/max）
            similarity_threshold: 向量检索相似度阈值
            
        Returns:
            检索结果列表
        """
        logger.info(f"开始混合检索: query='{query}', method={method}, top_k={top_k}")
        
        # 1. 向量检索
        vector_results = self.kb_service.search_by_semantic(
            query_text=query,
            doc_type=doc_type,
            top_k=top_k * 2,  # 获取更多候选结果
            similarity_threshold=similarity_threshold
        )
        
        # 2. BM25检索
        # 需要先构建BM25索引
        if not self.bm25_retriever.corpus:
            logger.warning("BM25索引为空，尝试从知识库构建")
            self._build_bm25_index_from_kb()
        
        bm25_results = self.bm25_retriever.search(query, top_k=top_k * 2)
        
        logger.info(f"向量检索结果数: {len(vector_results)}, BM25检索结果数: {len(bm25_results)}")
        
        # 3. 根据方法选择融合策略
        if method == "weighted":
            results = self._weighted_fusion(vector_results, bm25_results, top_k)
        elif method == "rrf":
            results = self._rrf_fusion(vector_results, bm25_results, top_k)
        elif method == "max":
            results = self._max_fusion(vector_results, bm25_results, top_k)
        else:
            logger.warning(f"未知的融合方法: {method}，使用默认的weighted方法")
            results = self._weighted_fusion(vector_results, bm25_results, top_k)
        
        logger.info(f"混合检索完成，返回结果数: {len(results)}")
        return results
    
    def _weighted_fusion(
        self,
        vector_results: List[Dict],
        bm25_results: List[Dict],
        top_k: int
    ) -> List[Dict]:
        """
        加权融合
        
        Args:
            vector_results: 向量检索结果
            bm25_results: BM25检索结果
            top_k: 返回前K个结果
            
        Returns:
            融合后的结果列表
        """
        logger.info("使用加权融合策略")
        
        # 1. 归一化分数
        normalized_vector = self._normalize_scores(vector_results, "similarity")
        normalized_bm25 = self._normalize_scores(bm25_results, "score")
        
        # 2. 构建文档分数映射
        doc_scores = {}  # doc_id -> (weighted_score, document)
        
        # 处理向量检索结果
        for result in normalized_vector:
            doc_id = result["id"]
            score = result["similarity"] * self.vector_weight
            doc_scores[doc_id] = {
                "score": score,
                "document": result,
                "sources": ["vector"]
            }
        
        # 处理BM25检索结果
        for result in normalized_bm25:
            doc_id = result["doc_id"]
            score = result["score"] * self.bm25_weight
            
            if doc_id in doc_scores:
                # 文档已存在，累加分数
                doc_scores[doc_id]["score"] += score
                doc_scores[doc_id]["sources"].append("bm25")
            else:
                # 新文档
                # 将BM25结果转换为统一格式
                doc_data = result.get("document", {})
                doc_scores[doc_id] = {
                    "score": score,
                    "document": {
                        "id": doc_id,
                        "doc_code": doc_data.get("doc_code", ""),
                        "doc_name": doc_data.get("doc_name", ""),
                        "doc_type": doc_data.get("doc_type", ""),
                        "doc_category": doc_data.get("doc_category", ""),
                        "doc_content": doc_data.get("content", ""),
                        "doc_url": doc_data.get("doc_url", ""),
                        "create_time": doc_data.get("create_time", ""),
                        "similarity": 0.0
                    },
                    "sources": ["bm25"]
                }
        
        # 3. 按分数排序
        sorted_docs = sorted(
            doc_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 4. 返回top_k结果
        results = []
        for doc_id, data in sorted_docs[:top_k]:
            result = data["document"].copy()
            result["hybrid_score"] = data["score"]
            result["sources"] = data["sources"]
            results.append(result)
        
        logger.info(f"加权融合完成: 合并了{len(doc_scores)}个唯一文档")
        return results
    
    def _rrf_fusion(
        self,
        vector_results: List[Dict],
        bm25_results: List[Dict],
        top_k: int,
        k: int = 60
    ) -> List[Dict]:
        """
        Reciprocal Rank Fusion
        参考Dify的RRF实现
        
        Args:
            vector_results: 向量检索结果
            bm25_results: BM25检索结果
            top_k: 返回前K个结果
            k: 常数，用于调节排序的影响（通常取60）
            
        Returns:
            融合后的结果列表
        """
        logger.info(f"使用RRF融合策略 (k={k})")
        
        # 1. 构建文档RRF分数映射
        doc_rrf_scores = {}  # doc_id -> (rrf_score, document, sources)
        
        # 处理向量检索结果
        for rank, result in enumerate(vector_results):
            doc_id = result["id"]
            rrf_score = 1.0 / (k + rank + 1)
            
            if doc_id not in doc_rrf_scores:
                doc_rrf_scores[doc_id] = {
                    "score": rrf_score,
                    "document": result,
                    "sources": ["vector"]
                }
            else:
                doc_rrf_scores[doc_id]["score"] += rrf_score
                doc_rrf_scores[doc_id]["sources"].append("vector")
        
        # 处理BM25检索结果
        for rank, result in enumerate(bm25_results):
            doc_id = result["doc_id"]
            rrf_score = 1.0 / (k + rank + 1)
            
            if doc_id in doc_rrf_scores:
                doc_rrf_scores[doc_id]["score"] += rrf_score
                doc_rrf_scores[doc_id]["sources"].append("bm25")
            else:
                # 将BM25结果转换为统一格式
                doc_data = result.get("document", {})
                doc_rrf_scores[doc_id] = {
                    "score": rrf_score,
                    "document": {
                        "id": doc_id,
                        "doc_code": doc_data.get("doc_code", ""),
                        "doc_name": doc_data.get("doc_name", ""),
                        "doc_type": doc_data.get("doc_type", ""),
                        "doc_category": doc_data.get("doc_category", ""),
                        "doc_content": doc_data.get("content", ""),
                        "doc_url": doc_data.get("doc_url", ""),
                        "create_time": doc_data.get("create_time", ""),
                        "similarity": 0.0
                    },
                    "sources": ["bm25"]
                }
        
        # 2. 按RRF分数排序
        sorted_docs = sorted(
            doc_rrf_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 3. 返回top_k结果
        results = []
        for doc_id, data in sorted_docs[:top_k]:
            result = data["document"].copy()
            result["rrf_score"] = data["score"]
            result["sources"] = data["sources"]
            results.append(result)
        
        logger.info(f"RRF融合完成: 合并了{len(doc_rrf_scores)}个唯一文档")
        return results
    
    def _max_fusion(
        self,
        vector_results: List[Dict],
        bm25_results: List[Dict],
        top_k: int
    ) -> List[Dict]:
        """
        最大分数融合
        
        Args:
            vector_results: 向量检索结果
            bm25_results: BM25检索结果
            top_k: 返回前K个结果
            
        Returns:
            融合后的结果列表
        """
        logger.info("使用最大分数融合策略")
        
        # 1. 归一化分数
        normalized_vector = self._normalize_scores(vector_results, "similarity")
        normalized_bm25 = self._normalize_scores(bm25_results, "score")
        
        # 2. 构建文档最大分数映射
        doc_max_scores = {}  # doc_id -> (max_score, document, sources)
        
        # 处理向量检索结果
        for result in normalized_vector:
            doc_id = result["id"]
            doc_max_scores[doc_id] = {
                "score": result["similarity"],
                "document": result,
                "sources": ["vector"]
            }
        
        # 处理BM25检索结果
        for result in normalized_bm25:
            doc_id = result["doc_id"]
            
            if doc_id in doc_max_scores:
                # 取最大值
                max_score = max(doc_max_scores[doc_id]["score"], result["score"])
                doc_max_scores[doc_id]["score"] = max_score
                if "bm25" not in doc_max_scores[doc_id]["sources"]:
                    doc_max_scores[doc_id]["sources"].append("bm25")
            else:
                # 新文档
                doc_data = result.get("document", {})
                doc_max_scores[doc_id] = {
                    "score": result["score"],
                    "document": {
                        "id": doc_id,
                        "doc_code": doc_data.get("doc_code", ""),
                        "doc_name": doc_data.get("doc_name", ""),
                        "doc_type": doc_data.get("doc_type", ""),
                        "doc_category": doc_data.get("doc_category", ""),
                        "doc_content": doc_data.get("content", ""),
                        "doc_url": doc_data.get("doc_url", ""),
                        "create_time": doc_data.get("create_time", ""),
                        "similarity": 0.0
                    },
                    "sources": ["bm25"]
                }
        
        # 3. 按最大分数排序
        sorted_docs = sorted(
            doc_max_scores.items(),
            key=lambda x: x[1]["score"],
            reverse=True
        )
        
        # 4. 返回top_k结果
        results = []
        for doc_id, data in sorted_docs[:top_k]:
            result = data["document"].copy()
            result["max_score"] = data["score"]
            result["sources"] = data["sources"]
            results.append(result)
        
        logger.info(f"最大分数融合完成: 合并了{len(doc_max_scores)}个唯一文档")
        return results
    
    def _normalize_scores(
        self,
        results: List[Dict],
        score_key: str
    ) -> List[Dict]:
        """
        归一化分数到[0, 1]区间
        
        Args:
            results: 检索结果列表
            score_key: 分数字段名
            
        Returns:
            归一化后的结果列表
        """
        if not results:
            return results
        
        # 提取所有分数
        scores = [result.get(score_key, 0.0) for result in results]
        
        # 计算最小值和最大值
        min_score = min(scores)
        max_score = max(scores)
        
        # 如果所有分数相同，返回原始结果
        if max_score == min_score:
            return results
        
        # 归一化
        for result in results:
            score = result.get(score_key, 0.0)
            normalized = (score - min_score) / (max_score - min_score)
            result[score_key] = normalized
        
        return results
    
    def _build_bm25_index_from_kb(self) -> bool:
        """
        从知识库构建BM25索引
        
        Returns:
            是否构建成功
        """
        try:
            # 从数据库查询所有文档
            from sqlalchemy import text
            
            query_sql = """
            SELECT id, doc_code, doc_name, doc_content
            FROM knowledge_document
            WHERE is_active = '1'
            AND doc_content IS NOT NULL
            """
            
            result = self.kb_service.db.execute(text(query_sql))
            rows = result.fetchall()
            
            # 构建文档列表
            documents = []
            for row in rows:
                documents.append({
                    "id": row[0],
                    "doc_code": row[1],
                    "doc_name": row[2],
                    "content": row[3]
                })
            
            if documents:
                # 构建BM25索引
                self.bm25_retriever.build_index(documents)
                logger.info(f"从知识库构建BM25索引成功，文档数={len(documents)}")
                return True
            else:
                logger.warning("知识库中没有文档")
                return False
                
        except Exception as e:
            logger.error(f"从知识库构建BM25索引失败: {str(e)}")
            return False
    
    def set_weights(self, vector_weight: float, bm25_weight: float) -> None:
        """
        设置检索权重
        
        Args:
            vector_weight: 向量检索权重
            bm25_weight: BM25检索权重
        """
        total_weight = vector_weight + bm25_weight
        if total_weight > 0:
            self.vector_weight = vector_weight / total_weight
            self.bm25_weight = bm25_weight / total_weight
            logger.info(f"权重已更新: vector={self.vector_weight:.2f}, bm25={self.bm25_weight:.2f}")
        else:
            logger.warning("权重和必须大于0")

