"""
文本向量化服务
使用Embedding模型将文本转换为向量，支持语义检索

支持两种模式：
1. 本地模式 (EMBEDDING_PROVIDER=local): 使用 sentence-transformers
2. OpenAI模式 (EMBEDDING_PROVIDER=openai): 使用 OpenAI Embeddings API
"""
import logging
from typing import List, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text
from app import config

logger = logging.getLogger(__name__)

# 延迟加载模型，避免启动时阻塞
_local_model = None
_openai_client = None


def _get_local_model():
    """
    延迟加载本地 sentence-transformers 模型
    """
    global _local_model
    if _local_model is None:
        try:
            from sentence_transformers import SentenceTransformer
            model_name = config.EMBEDDING_MODEL_NAME
            logger.info(f"正在加载本地嵌入模型: {model_name}")
            _local_model = SentenceTransformer(model_name)
            logger.info(f"本地嵌入模型加载成功: {model_name}, 维度: {_local_model.get_sentence_embedding_dimension()}")
        except ImportError:
            logger.error("sentence-transformers 未安装，请运行: pip install sentence-transformers")
            raise
        except Exception as e:
            logger.error(f"加载本地嵌入模型失败: {str(e)}")
            raise
    return _local_model


def _get_openai_client():
    """
    延迟加载 OpenAI 客户端
    """
    global _openai_client
    if _openai_client is None:
        try:
            import openai
            if not config.EMBEDDING_API_KEY:
                raise ValueError("使用 OpenAI 嵌入需要设置 EMBEDDING_API_KEY 环境变量")
            _openai_client = openai.OpenAI(
                api_key=config.EMBEDDING_API_KEY,
                base_url=config.EMBEDDING_API_BASE
            )
            logger.info(f"OpenAI 客户端初始化成功，API Base: {config.EMBEDDING_API_BASE}")
        except ImportError:
            logger.error("openai 未安装，请运行: pip install openai")
            raise
        except Exception as e:
            logger.error(f"初始化 OpenAI 客户端失败: {str(e)}")
            raise
    return _openai_client


class EmbeddingService:
    """文本向量化服务"""
    
    def __init__(self, db: Session = None):
        """
        初始化向量化服务
        
        Args:
            db: 数据库会话（可选，用于 pgvector 操作）
        """
        self.db = db
        self.provider = config.EMBEDDING_PROVIDER
        
        # 根据提供者设置模型名称和维度
        if self.provider == "local":
            self.embedding_model = config.EMBEDDING_MODEL_NAME
            # BGE中文模型维度映射 (推荐用于中文场景)
            dimension_map = {
                # BGE 中文模型系列
                "BAAI/bge-small-zh-v1.5": 512,
                "BAAI/bge-base-zh-v1.5": 768,
                "BAAI/bge-large-zh-v1.5": 1024,
                # BGE 多语言模型
                "BAAI/bge-m3": 1024,
            }
            self.embedding_dimension = dimension_map.get(
                self.embedding_model, 
                config.EMBEDDING_DIMENSION
            )
        else:  # openai
            self.embedding_model = config.OPENAI_EMBEDDING_MODEL
            # OpenAI 模型维度映射
            openai_dimension_map = {
                "text-embedding-ada-002": 1536,
                "text-embedding-3-small": 1536,
                "text-embedding-3-large": 3072,
            }
            self.embedding_dimension = openai_dimension_map.get(
                self.embedding_model,
                config.EMBEDDING_DIMENSION
            )
        
        logger.info(f"EmbeddingService 初始化: provider={self.provider}, model={self.embedding_model}, dimension={self.embedding_dimension}")
    
    def get_embedding(self, text: str) -> Optional[List[float]]:
        """
        获取文本的向量表示
        
        Args:
            text: 待向量化的文本
            
        Returns:
            向量列表，如果失败返回None
        """
        if not text or not text.strip():
            logger.warning("输入文本为空")
            return None
        
        try:
            if self.provider == "local":
                return self._get_local_embedding(text)
            else:
                return self._get_openai_embedding(text)
        except Exception as e:
            logger.error(f"向量化失败: {str(e)}")
            return None
    
    def _get_local_embedding(self, text: str) -> List[float]:
        """
        使用本地 sentence-transformers 模型获取向量
        """
        model = _get_local_model()
        embedding = model.encode(text, convert_to_numpy=True)
        return embedding.tolist()
    
    def _get_openai_embedding(self, text: str) -> List[float]:
        """
        使用 OpenAI API 获取向量
        """
        client = _get_openai_client()
        response = client.embeddings.create(
            model=self.embedding_model,
            input=text
        )
        return response.data[0].embedding
    
    def batch_get_embeddings(self, texts: List[str]) -> List[Optional[List[float]]]:
        """
        批量获取文本向量
        
        Args:
            texts: 待向量化的文本列表
            
        Returns:
            向量列表，每个元素对应一个文本的向量
        """
        if not texts:
            return []
        
        try:
            if self.provider == "local":
                return self._batch_get_local_embeddings(texts)
            else:
                return self._batch_get_openai_embeddings(texts)
        except Exception as e:
            logger.error(f"批量向量化失败: {str(e)}")
            # 降级为逐个处理
            return [self.get_embedding(text) for text in texts]
    
    def _batch_get_local_embeddings(self, texts: List[str]) -> List[List[float]]:
        """
        批量使用本地模型获取向量
        """
        model = _get_local_model()
        # sentence-transformers 原生支持批量编码
        embeddings = model.encode(texts, convert_to_numpy=True, show_progress_bar=False)
        return [emb.tolist() for emb in embeddings]
    
    def _batch_get_openai_embeddings(self, texts: List[str]) -> List[List[float]]:
        """
        批量使用 OpenAI API 获取向量
        """
        client = _get_openai_client()
        response = client.embeddings.create(
            model=self.embedding_model,
            input=texts
        )
        # 按照输入顺序排列结果
        embeddings = [None] * len(texts)
        for item in response.data:
            embeddings[item.index] = item.embedding
        return embeddings
    
    def check_pgvector_extension(self) -> bool:
        """
        检查PostgreSQL是否安装了pgvector扩展
        
        Returns:
            是否已安装pgvector扩展
        """
        if not self.db:
            logger.warning("数据库会话未初始化")
            return False
        
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
        if not self.db:
            logger.warning("数据库会话未初始化")
            return False
        
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
    
    def get_model_info(self) -> dict:
        """
        获取当前嵌入模型信息
        
        Returns:
            模型信息字典
        """
        return {
            "provider": self.provider,
            "model_name": self.embedding_model,
            "embedding_dimension": self.embedding_dimension
        }


# 工厂函数，便于创建服务实例
def create_embedding_service(db: Session = None) -> EmbeddingService:
    """
    创建 EmbeddingService 实例
    
    Args:
        db: 数据库会话（可选）
        
    Returns:
        EmbeddingService 实例
    """
    return EmbeddingService(db=db)
