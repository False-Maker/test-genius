import os

# 默认模型代码
DEFAULT_MODEL_CODE = os.getenv("DEFAULT_MODEL_CODE", "DEEPSEEK_CHAT")

# Java后端API地址
JAVA_API_BASE_URL = os.getenv("JAVA_API_BASE_URL", "http://localhost:8080")

# =============================================================================
# RAG (Retrieval-Augmented Generation) 配置
# =============================================================================

# RAG 总开关
RAG_ENABLED = os.getenv("RAG_ENABLED", "true").lower() == "true"

# 检索参数
RAG_TOP_K = int(os.getenv("RAG_TOP_K", "5"))  # 返回前K个相关片段
RAG_SIMILARITY_THRESHOLD = float(os.getenv("RAG_SIMILARITY_THRESHOLD", "0.7"))  # 相似度阈值

# =============================================================================
# Embedding 模型配置
# =============================================================================

# 嵌入提供者: "local" (sentence-transformers) 或 "openai"
EMBEDDING_PROVIDER = os.getenv("EMBEDDING_PROVIDER", "local")

# 本地模型配置 (sentence-transformers)
# 使用 BAAI/bge-small-zh-v1.5 作为默认模型，针对中文优化
EMBEDDING_MODEL_NAME = os.getenv("EMBEDDING_MODEL_NAME", "BAAI/bge-small-zh-v1.5")

# OpenAI 嵌入配置 (当 EMBEDDING_PROVIDER=openai 时使用)
EMBEDDING_API_KEY = os.getenv("EMBEDDING_API_KEY", "")
EMBEDDING_API_BASE = os.getenv("EMBEDDING_API_BASE", "https://api.openai.com/v1")
OPENAI_EMBEDDING_MODEL = os.getenv("OPENAI_EMBEDDING_MODEL", "text-embedding-ada-002")

# 向量维度 (根据模型自动设置，但可以手动覆盖)
# BAAI/bge-small-zh-v1.5: 512维
# text-embedding-ada-002: 1536维
EMBEDDING_DIMENSION = int(os.getenv("EMBEDDING_DIMENSION", "512"))

# =============================================================================
# 向量数据库配置 (PGVector)
# =============================================================================

PGVECTOR_ENABLED = os.getenv("PGVECTOR_ENABLED", "true").lower() == "true"