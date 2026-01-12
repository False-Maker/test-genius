"""
文本向量化服务
使用Embedding模型将文本转换为向量，支持语义检索
"""
import logging
from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class EmbeddingService:
    """文本向量化服务"""
    
    def __init__(self, db: Session):
        """
        初始化向量化服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        # 默认使用OpenAI兼容的Embedding API
        # 实际使用时可以通过配置选择不同的Embedding模型
        self.embedding_model = "text-embedding-ada-002"
        self.embedding_dimension = 1536  # OpenAI ada-002的向量维度
    
    def get_embedding(self, text: str) -> Optional[List[float]]:
        """
        获取文本的向量表示
        
        Args:
            text: 待向量化的文本
            
        Returns:
            向量列表，如果失败返回None
        """
        try:
            # 这里使用简单的模拟实现
            # 实际使用时需要调用Embedding API（如OpenAI、通义千问等）
            # 或者使用本地模型（如sentence-transformers）
            
            # 模拟向量生成（实际应该调用API）
            # 为了演示，这里返回一个固定维度的零向量
            # 实际使用时需要替换为真实的Embedding API调用
            logger.warning("使用模拟向量生成，实际使用时需要替换为真实的Embedding API")
            
            # 示例：调用OpenAI Embedding API
            # import openai
            # response = openai.embeddings.create(
            #     model=self.embedding_model,
            #     input=text
            # )
            # return response.data[0].embedding
            
            # 临时返回模拟向量
            return [0.0] * self.embedding_dimension
            
        except Exception as e:
            logger.error(f"向量化失败: {str(e)}")
            return None
    
    def batch_get_embeddings(self, texts: List[str]) -> List[Optional[List[float]]]:
        """
        批量获取文本向量
        
        Args:
            texts: 待向量化的文本列表
            
        Returns:
            向量列表，每个元素对应一个文本的向量
        """
        embeddings = []
        for text_item in texts:
            embedding = self.get_embedding(text_item)
            embeddings.append(embedding)
        return embeddings
    
    def check_pgvector_extension(self) -> bool:
        """
        检查PostgreSQL是否安装了pgvector扩展
        
        Returns:
            是否已安装pgvector扩展
        """
        try:
            result = self.db.execute(text("SELECT 1 FROM pg_extension WHERE extname = 'vector'"))
            return result.fetchone() is not None
        except Exception as e:
            logger.error(f"检查pgvector扩展失败: {str(e)}")
            return False
    
    def install_pgvector_extension(self) -> bool:
        """
        安装pgvector扩展（如果未安装）
        
        Returns:
            是否安装成功
        """
        try:
            if not self.check_pgvector_extension():
                self.db.execute(text("CREATE EXTENSION IF NOT EXISTS vector"))
                self.db.commit()
                logger.info("pgvector扩展安装成功")
                return True
            else:
                logger.info("pgvector扩展已存在")
                return True
        except Exception as e:
            logger.error(f"安装pgvector扩展失败: {str(e)}")
            self.db.rollback()
            return False
    
    def cosine_similarity(self, vec1: List[float], vec2: List[float]) -> float:
        """
        计算两个向量的余弦相似度
        
        Args:
            vec1: 向量1
            vec2: 向量2
            
        Returns:
            余弦相似度（0-1之间）
        """
        if len(vec1) != len(vec2):
            return 0.0
        
        dot_product = sum(a * b for a, b in zip(vec1, vec2))
        magnitude1 = sum(a * a for a in vec1) ** 0.5
        magnitude2 = sum(b * b for b in vec2) ** 0.5
        
        if magnitude1 == 0 or magnitude2 == 0:
            return 0.0
        
        return dot_product / (magnitude1 * magnitude2)

