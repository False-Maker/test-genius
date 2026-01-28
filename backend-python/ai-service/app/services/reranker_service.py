"""
重排序服务
参考Dify的重排序实现，使用交叉编码器对检索结果重排序
"""
from typing import List, Dict, Optional
import logging

try:
    from sentence_transformers import CrossEncoder
    CROSS_ENCODER_AVAILABLE = True
except ImportError:
    CROSS_ENCODER_AVAILABLE = False
    logging.warning("sentence-transformers未安装，重排序功能将使用降级方案")

logger = logging.getLogger(__name__)


class RerankerService:
    """重排序服务"""
    
    def __init__(self, model_name: str = "BAAI/bge-reranker-large", use_gpu: bool = False):
        """
        初始化重排序服务
        
        Args:
            model_name: 交叉编码器模型名称
                       - BAAI/bge-reranker-large（推荐，中文效果好）
                       - BAAI/bge-reranker-base（较小）
                       - cross-encoder/ms-marco-MiniLM-L-6-v2（英文）
            use_gpu: 是否使用GPU加速
        """
        self.model_name = model_name
        self.use_gpu = use_gpu
        self.model = None
        
        if CROSS_ENCODER_AVAILABLE:
            try:
                device = "cuda" if use_gpu else "cpu"
                self.model = CrossEncoder(model_name, device=device)
                logger.info(f"重排序模型加载成功: {model_name}, device={device}")
            except Exception as e:
                logger.warning(f"加载重排序模型失败: {str(e)}，将使用降级方案")
                self.model = None
        else:
            logger.warning("sentence-transformers未安装，将使用降级方案（基于关键词匹配的重排序）")
    
    def rerank(
        self,
        query: str,
        documents: List[Dict],
        top_k: int = 10,
        content_key: str = "content"
    ) -> List[Dict]:
        """
        重排序文档
        
        Args:
            query: 查询文本
            documents: 文档列表，每个文档包含content和其他字段
            top_k: 返回前K个结果
            content_key: 内容字段名（用于重排序）
            
        Returns:
            重排序后的文档列表
        """
        if not documents:
            logger.warning("文档列表为空")
            return documents
        
        if len(documents) <= 1:
            logger.info("只有一个文档，无需重排序")
            return documents
        
        logger.info(f"开始重排序: 查询='{query}', 文档数={len(documents)}, top_k={top_k}")
        
        # 如果模型未加载，使用降级方案
        if self.model is None:
            logger.info("使用降级方案进行重排序（基于关键词匹配）")
            return self._fallback_rerank(query, documents, top_k, content_key)
        
        try:
            # 1. 计算相关性分数
            scores = self._calculate_scores(query, documents, content_key)
            
            # 2. 根据分数排序
            reranked_docs = self._sort_by_scores(documents, scores, top_k)
            
            logger.info(f"重排序完成，返回前{len(reranked_docs)}个结果")
            return reranked_docs
            
        except Exception as e:
            logger.error(f"重排序失败: {str(e)}，使用降级方案")
            return self._fallback_rerank(query, documents, top_k, content_key)
    
    def _calculate_scores(
        self,
        query: str,
        documents: List[Dict],
        content_key: str = "content"
    ) -> List[float]:
        """
        计算相关性分数
        
        Args:
            query: 查询文本
            documents: 文档列表
            content_key: 内容字段名
            
        Returns:
            相关性分数列表
        """
        # 提取文档内容
        doc_contents = []
        for doc in documents:
            # 尝试从多个可能的字段中获取内容
            content = doc.get(content_key)
            if content is None:
                content = doc.get("doc_content") or doc.get("text") or ""
            doc_contents.append(content)
        
        # 构造query-document对
        pairs = [[query, content] for content in doc_contents]
        
        # 计算分数（批量处理）
        logger.info(f"使用模型计算相关性分数，batch_size={len(pairs)}")
        scores = self.model.predict(pairs)
        
        return scores.tolist()
    
    def _sort_by_scores(
        self,
        documents: List[Dict],
        scores: List[float],
        top_k: int
    ) -> List[Dict]:
        """
        根据分数排序
        
        Args:
            documents: 文档列表
            scores: 分数列表
            top_k: 返回前K个结果
            
        Returns:
            排序后的文档列表
        """
        # 将文档和分数配对
        scored_docs = list(zip(documents, scores))
        
        # 按分数降序排序
        scored_docs.sort(key=lambda x: x[1], reverse=True)
        
        # 取top_k并添加分数信息
        reranked_docs = []
        for doc, score in scored_docs[:top_k]:
            result = doc.copy()
            result["rerank_score"] = float(score)
            reranked_docs.append(result)
        
        return reranked_docs
    
    def _fallback_rerank(
        self,
        query: str,
        documents: List[Dict],
        top_k: int,
        content_key: str = "content"
    ) -> List[Dict]:
        """
        降级重排序方案（基于关键词匹配）
        当sentence-transformers未安装或加载失败时使用
        
        Args:
            query: 查询文本
            documents: 文档列表
            top_k: 返回前K个结果
            content_key: 内容字段名
            
        Returns:
            重排序后的文档列表
        """
        try:
            # 尝试导入jieba分词
            import jieba
            query_tokens = set(jieba.lcut(query.lower()))
        except ImportError:
            # 如果jieba未安装，使用简单的单词匹配
            query_tokens = set(query.lower().split())
        
        # 计算每个文档的关键词匹配分数
        scored_docs = []
        for doc in documents:
            # 获取文档内容
            content = doc.get(content_key)
            if content is None:
                content = doc.get("doc_content") or doc.get("text") or ""
            
            content_lower = content.lower()
            
            # 计算匹配分数
            score = 0.0
            for token in query_tokens:
                if token in content_lower:
                    score += content_lower.count(token)
            
            # 如果有相似度分数，也考虑进去
            if "similarity" in doc:
                score += doc["similarity"] * 10
            
            scored_docs.append((doc, score))
        
        # 按分数降序排序
        scored_docs.sort(key=lambda x: x[1], reverse=True)
        
        # 取top_k并添加分数信息
        reranked_docs = []
        for doc, score in scored_docs[:top_k]:
            result = doc.copy()
            result["rerank_score"] = float(score)
            reranked_docs.append(result)
        
        return reranked_docs
    
    def rerank_batch(
        self,
        queries: List[str],
        documents_list: List[List[Dict]],
        top_k: int = 10,
        content_key: str = "content"
    ) -> List[List[Dict]]:
        """
        批量重排序
        
        Args:
            queries: 查询文本列表
            documents_list: 文档列表的列表（每个查询对应一组文档）
            top_k: 每个查询返回前K个结果
            content_key: 内容字段名
            
        Returns:
            重排序后的文档列表的列表
        """
        if len(queries) != len(documents_list):
            raise ValueError("queries和documents_list的长度必须相同")
        
        logger.info(f"开始批量重排序: 查询数={len(queries)}")
        
        # 如果模型未加载，使用降级方案逐个处理
        if self.model is None:
            return [
                self._fallback_rerank(query, docs, top_k, content_key)
                for query, docs in zip(queries, documents_list)
            ]
        
        # 批量重排序
        results = []
        for query, documents in zip(queries, documents_list):
            reranked = self.rerank(query, documents, top_k, content_key)
            results.append(reranked)
        
        logger.info(f"批量重排序完成")
        return results
    
    def get_model_info(self) -> Dict:
        """
        获取模型信息
        
        Returns:
            模型信息字典
        """
        return {
            "model_name": self.model_name,
            "model_loaded": self.model is not None,
            "use_gpu": self.use_gpu,
            "cross_encoder_available": CROSS_ENCODER_AVAILABLE
        }


# 为了向后兼容，创建一个简单的工厂函数
def create_reranker_service(
    model_name: str = "BAAI/bge-reranker-large",
    use_gpu: bool = False
) -> RerankerService:
    """
    创建重排序服务
    
    Args:
        model_name: 模型名称
        use_gpu: 是否使用GPU
        
    Returns:
        RerankerService实例
    """
    return RerankerService(model_name=model_name, use_gpu=use_gpu)

