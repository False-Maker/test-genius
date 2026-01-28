"""
BM25关键词检索器
参考Dify的BM25实现，使用BM25算法进行关键词检索
"""
from typing import List, Dict, Optional
from collections import defaultdict
import math
import logging

try:
    import jieba
    JIEBA_AVAILABLE = True
except ImportError:
    JIEBA_AVAILABLE = False
    logging.warning("jieba未安装，BM25检索功能不可用")

logger = logging.getLogger(__name__)


class BM25Retriever:
    """BM25检索器"""
    
    def __init__(self, k1: float = 1.5, b: float = 0.75):
        """
        初始化BM25检索器
        
        Args:
            k1: 调节参数，控制词频饱和度
            b: 调节参数，控制文档长度归一化
        """
        if not JIEBA_AVAILABLE:
            raise ImportError("请先安装jieba: pip install jieba")
        
        self.k1 = k1
        self.b = b
        self.corpus = []  # 文档列表
        self.doc_freqs = []  # 文档词频
        self.idf = {}  # 逆文档频率
        self.doc_len = []  # 文档长度
        self.avgdl = 0  # 平均文档长度
        self.term_doc_index = defaultdict(list)  # 术语-文档索引
        self.documents = {}  # 文档映射：doc_id -> document
    
    def build_index(self, documents: List[Dict]) -> None:
        """
        构建BM25索引
        
        Args:
            documents: 文档列表，每个文档包含id和content
        """
        logger.info(f"开始构建BM25索引，文档数={len(documents)}")
        
        self.corpus = documents
        self.doc_freqs = []
        self.doc_len = []
        self.documents = {}
        
        # 统计词频
        N = len(documents)
        df = defaultdict(int)  # 文档频率
        term_total_freq = defaultdict(int)  # 术语总频率
        
        for idx, doc in enumerate(documents):
            doc_id = doc.get("id", idx)
            content = doc.get("content", "")
            
            # 分词
            tokens = self._tokenize(content)
            
            # 计算文档词频
            freq = defaultdict(int)
            for token in tokens:
                freq[token] += 1
                term_total_freq[token] += 1
            
            self.doc_freqs.append(freq)
            self.doc_len.append(len(tokens))
            self.documents[doc_id] = doc
            
            # 构建术语-文档索引
            for token in set(tokens):
                self.term_doc_index[token].append(doc_id)
                df[token] += 1
        
        # 计算平均文档长度
        self.avgdl = sum(self.doc_len) / N if N > 0 else 0
        
        # 计算逆文档频率（IDF）
        self.idf = {}
        for term, freq in df.items():
            self.idf[term] = math.log(N / (freq + 0.5)) + 1  # 加1避免负值
        
        logger.info(f"BM25索引构建完成: 词汇量={len(self.idf)}, 平均文档长度={self.avgdl:.2f}")
    
    def _tokenize(self, text: str) -> List[str]:
        """分词（使用jieba）"""
        if not text:
            return []
        
        # 使用jieba分词
        tokens = jieba.lcut(text)
        
        # 过滤停用词和短词
        tokens = [t.strip() for t in tokens if len(t.strip()) >= 2]
        
        return tokens
    
    def _calculate_idf(self) -> None:
        """计算逆文档频率（IDF）"""
        N = len(self.doc_freqs)
        
        # 计算每个术语的文档频率
        df = defaultdict(int)
        for freq in self.doc_freqs:
            for term in freq.keys():
                df[term] += 1
        
        # 计算IDF
        for term, freq in df.items():
            self.idf[term] = math.log(N / (freq + 0.5)) + 1
    
    def search(self, query: str, top_k: int = 10) -> List[Dict]:
        """
        检索文档
        
        Args:
            query: 查询文本
            top_k: 返回前K个结果
            
        Returns:
            检索结果列表，包含doc_id和score
        """
        if not self.corpus:
            logger.warning("索引为空，请先构建索引")
            return []
        
        # 分词
        query_tokens = self._tokenize(query)
        
        if not query_tokens:
            logger.warning("查询分词后为空")
            return []
        
        # 去重
        query_tokens = list(set(query_tokens))
        
        # 过滤不在词典中的术语
        query_tokens = [t for t in query_tokens if t in self.idf]
        
        if not query_tokens:
            logger.warning("查询中没有有效术语")
            return []
        
        logger.info(f"开始BM25检索: 查询='{query}', 有效术语数={len(query_tokens)}")
        
        # 计算每个文档的BM25分数
        scores = []
        for idx in range(len(self.corpus)):
            score = self._calculate_score(query_tokens, idx)
            if score > 0:
                doc_id = self.corpus[idx].get("id", idx)
                scores.append((doc_id, score, idx))
        
        # 按分数排序
        scores.sort(key=lambda x: x[1], reverse=True)
        
        # 返回top_k结果
        results = []
        for doc_id, score, idx in scores[:top_k]:
            doc = self.corpus[idx]
            results.append({
                "doc_id": doc_id,
                "score": float(score),
                "document": doc
            })
        
        logger.info(f"BM25检索完成: 查询='{query}', 返回结果数={len(results)}")
        return results
    
    def _calculate_score(self, query_tokens: List[str], doc_idx: int) -> float:
        """计算BM25分数"""
        score = 0.0
        
        for term in query_tokens:
            if term not in self.idf:
                continue
            
            # 获取文档中该术语的频率
            term_freq = self.doc_freqs[doc_idx].get(term, 0)
            
            if term_freq == 0:
                continue
            
            # BM25公式
            idf = self.idf[term]
            numerator = term_freq * (self.k1 + 1)
            denominator = term_freq + self.k1 * (1 - self.b + self.b * (self.doc_len[doc_idx] / self.avgdl))
            
            score += idf * (numerator / denominator)
        
        return score
    
    def add_document(self, doc_id: int, content: str, doc_data: Optional[Dict] = None) -> None:
        """添加文档到索引"""
        logger.info(f"添加文档到索引: doc_id={doc_id}")
        
        if doc_data is None:
            doc_data = {"id": doc_id, "content": content}
        else:
            doc_data["id"] = doc_id
            doc_data["content"] = content
        
        # 分词
        tokens = self._tokenize(content)
        
        # 计算词频
        freq = defaultdict(int)
        for token in tokens:
            freq[token] += 1
        
        # 添加到文档列表
        self.corpus.append(doc_data)
        self.doc_freqs.append(freq)
        self.doc_len.append(len(tokens))
        self.documents[doc_id] = doc_data
        
        # 更新术语-文档索引
        for token in set(tokens):
            self.term_doc_index[token].append(doc_id)
        
        # 重新计算IDF和平均文档长度
        self._calculate_idf()
        self.avgdl = sum(self.doc_len) / len(self.doc_len) if self.doc_len else 0
        
        logger.info(f"文档添加完成，当前文档数={len(self.corpus)}")
    
    def update_document(self, doc_id: int, content: str, doc_data: Optional[Dict] = None) -> None:
        """更新索引中的文档"""
        logger.info(f"更新文档索引: doc_id={doc_id}")
        
        # 先删除旧文档
        self.delete_document(doc_id)
        
        # 添加新文档
        self.add_document(doc_id, content, doc_data)
    
    def delete_document(self, doc_id: int) -> None:
        """从索引中删除文档"""
        logger.info(f"从索引中删除文档: doc_id={doc_id}")
        
        # 找到文档在列表中的位置
        doc_idx = None
        for idx, doc in enumerate(self.corpus):
            if doc.get("id") == doc_id:
                doc_idx = idx
                break
        
        if doc_idx is None:
            logger.warning(f"文档不存在: doc_id={doc_id}")
            return
        
        # 获取文档的术语
        freq = self.doc_freqs[doc_idx]
        tokens = list(freq.keys())
        
        # 从术语-文档索引中移除
        for token in tokens:
            if doc_id in self.term_doc_index[token]:
                self.term_doc_index[token].remove(doc_id)
        
        # 从文档列表中移除
        self.corpus.pop(doc_idx)
        self.doc_freqs.pop(doc_idx)
        self.doc_len.pop(doc_idx)
        
        # 从文档映射中移除
        if doc_id in self.documents:
            del self.documents[doc_id]
        
        # 重新计算IDF和平均文档长度
        self._calculate_idf()
        self.avgdl = sum(self.doc_len) / len(self.doc_len) if self.doc_len else 0
        
        logger.info(f"文档删除完成，当前文档数={len(self.corpus)}")
    
    def get_statistics(self) -> Dict:
        """获取索引统计信息"""
        return {
            "total_documents": len(self.corpus),
            "vocabulary_size": len(self.idf),
            "avg_document_length": self.avgdl,
            "min_document_length": min(self.doc_len) if self.doc_len else 0,
            "max_document_length": max(self.doc_len) if self.doc_len else 0,
            "k1": self.k1,
            "b": self.b
        }
    
    def get_document_by_id(self, doc_id: int) -> Optional[Dict]:
        """根据ID获取文档"""
        return self.documents.get(doc_id)
    
    def get_term_frequency(self, term: str) -> Dict:
        """获取术语频率统计"""
        df = len(self.term_doc_index.get(term, []))
        total_freq = 0
        
        for freq in self.doc_freqs:
            total_freq += freq.get(term, 0)
        
        return {
            "term": term,
            "document_frequency": df,  # 包含该术语的文档数
            "total_frequency": total_freq,  # 术语在所有文档中出现的总次数
            "idf": self.idf.get(term, 0)
        }

