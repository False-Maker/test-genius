-- RAG增强相关表
-- 创建日期: 2026-01-28
-- 说明: 知识库、文档分块、BM25索引、权限等RAG增强功能相关表

-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    kb_code VARCHAR(100) UNIQUE NOT NULL,
    kb_name VARCHAR(500) NOT NULL,
    kb_description TEXT,
    kb_type VARCHAR(50), -- 公共知识库/私有知识库/项目知识库
    embedding_model VARCHAR(100),
    chunking_strategy VARCHAR(50),
    chunk_size INT,
    chunk_overlap INT,
    is_active CHAR(1) DEFAULT '1',
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档表（增强版，支持分块）
CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    doc_code VARCHAR(100) UNIQUE NOT NULL,
    kb_id BIGINT,
    doc_name VARCHAR(500) NOT NULL,
    doc_type VARCHAR(50), -- 文档类型：规范/业务规则/用例模板/历史用例/其他
    doc_category VARCHAR(100), -- 文档分类
    doc_content TEXT, -- 文档内容
    doc_url VARCHAR(1000), -- 文档URL
    file_size BIGINT, -- 文件大小（字节）
    file_path VARCHAR(1000), -- 文件路径
    language VARCHAR(20), -- 文档语言：zh/en/mixed/unknown
    encoding VARCHAR(50), -- 文件编码
    page_count INT, -- 页数（PDF）
    slide_count INT, -- 幻灯片数（PPT）
    row_count INT, -- 行数（CSV）
    column_count INT, -- 列数（CSV）
    table_count INT, -- 表格数（HTML）
    metadata JSONB, -- 额外元数据
    is_active CHAR(1) DEFAULT '1',
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档分块表
CREATE TABLE IF NOT EXISTS knowledge_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    chunk_id VARCHAR(100) UNIQUE NOT NULL,
    chunk_index INT NOT NULL,
    chunk_content TEXT,
    chunk_length INT,
    chunk_type VARCHAR(50), -- paragraph/sentence/fixed_size/semantic/recursive
    chunk_strategy VARCHAR(50),
    chunk_start INT, -- 分块在原文中的起始位置
    chunk_end INT, -- 分块在原文中的结束位置
    has_overlap CHAR(1) DEFAULT '0', -- 是否有重叠
    embedding vector(1536), -- 向量列
    metadata JSONB, -- 分块元数据
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BM25索引表
CREATE TABLE IF NOT EXISTS bm25_index (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    term VARCHAR(100) NOT NULL,
    doc_id BIGINT NOT NULL,
    chunk_id BIGINT, -- 如果是分块级别的索引
    frequency INT NOT NULL, -- 术语频率
    postings JSONB, -- 倒排索引数据
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(kb_id, term, doc_id)
);

-- 知识库权限表
CREATE TABLE IF NOT EXISTS knowledge_base_permission (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission_type VARCHAR(20) NOT NULL, -- read/write/admin
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(kb_id, user_id, permission_type)
);

-- 知识库同步记录表
CREATE TABLE IF NOT EXISTS knowledge_base_sync_log (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    sync_type VARCHAR(20) NOT NULL, -- incremental/full
    source_path VARCHAR(1000),
    added_count INT DEFAULT 0,
    updated_count INT DEFAULT 0,
    deleted_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- pending/running/success/failed
    error_message TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_kb_code ON knowledge_base(kb_code);
CREATE INDEX IF NOT EXISTS idx_kb_type ON knowledge_base(kb_type);
CREATE INDEX IF NOT EXISTS idx_kb_creator ON knowledge_base(creator_id);
CREATE INDEX IF NOT EXISTS idx_kb_active ON knowledge_base(is_active);

CREATE INDEX IF NOT EXISTS idx_kb_doc_kb_id ON knowledge_document(kb_id);
CREATE INDEX IF NOT EXISTS idx_kb_doc_code ON knowledge_document(doc_code);
CREATE INDEX IF NOT EXISTS idx_kb_doc_type ON knowledge_document(doc_type, is_active);
CREATE INDEX IF NOT EXISTS idx_kb_doc_category ON knowledge_document(doc_category);
CREATE INDEX IF NOT EXISTS idx_kb_doc_active ON knowledge_document(is_active);
CREATE INDEX IF NOT EXISTS idx_kb_doc_creator ON knowledge_document(creator_id);

CREATE INDEX IF NOT EXISTS idx_kb_chunk_doc_id ON knowledge_document_chunk(doc_id);
CREATE INDEX IF NOT EXISTS idx_kb_chunk_index ON knowledge_document_chunk(chunk_index);
CREATE INDEX IF NOT EXISTS idx_kb_chunk_embedding ON knowledge_document_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX IF NOT EXISTS idx_kb_chunk_strategy ON knowledge_document_chunk(chunk_strategy);

CREATE INDEX IF NOT EXISTS idx_bm25_kb_id ON bm25_index(kb_id);
CREATE INDEX IF NOT EXISTS idx_bm25_term ON bm25_index(term);
CREATE INDEX IF NOT EXISTS idx_bm25_doc_id ON bm25_index(doc_id);
CREATE INDEX IF NOT EXISTS idx_bm25_chunk_id ON bm25_index(chunk_id);
CREATE INDEX IF NOT EXISTS idx_bm25_term_doc ON bm25_index(kb_id, term);

CREATE INDEX IF NOT EXISTS idx_kb_permission_kb_id ON knowledge_base_permission(kb_id);
CREATE INDEX IF NOT EXISTS idx_kb_permission_user_id ON knowledge_base_permission(user_id);
CREATE INDEX IF NOT EXISTS idx_kb_permission_type ON knowledge_base_permission(permission_type);

CREATE INDEX IF NOT EXISTS idx_kb_sync_kb_id ON knowledge_base_sync_log(kb_id);
CREATE INDEX IF NOT EXISTS idx_kb_sync_status ON knowledge_base_sync_log(status);
CREATE INDEX IF NOT EXISTS idx_kb_sync_type ON knowledge_base_sync_log(sync_type);

-- 添加表注释
COMMENT ON TABLE knowledge_base IS '知识库表';
COMMENT ON TABLE knowledge_document IS '知识库文档表';
COMMENT ON TABLE knowledge_document_chunk IS '文档分块表';
COMMENT ON TABLE bm25_index IS 'BM25索引表';
COMMENT ON TABLE knowledge_base_permission IS '知识库权限表';
COMMENT ON TABLE knowledge_base_sync_log IS '知识库同步记录表';

-- 添加字段注释
COMMENT ON COLUMN knowledge_base.kb_code IS '知识库编码（唯一）';
COMMENT ON COLUMN knowledge_base.kb_name IS '知识库名称';
COMMENT ON COLUMN knowledge_base.kb_type IS '知识库类型：公共/私有/项目';
COMMENT ON COLUMN knowledge_base.embedding_model IS '嵌入模型';
COMMENT ON COLUMN knowledge_base.chunking_strategy IS '分块策略';
COMMENT ON COLUMN knowledge_document.doc_code IS '文档编码（唯一）';
COMMENT ON COLUMN knowledge_document.kb_id IS '所属知识库ID';
COMMENT ON COLUMN knowledge_document.doc_type IS '文档类型';
COMMENT ON COLUMN knowledge_document.doc_category IS '文档分类';
COMMENT ON COLUMN knowledge_document.language IS '文档语言';
COMMENT ON COLUMN knowledge_document.encoding IS '文件编码';
COMMENT ON COLUMN knowledge_document_chunk.chunk_id IS '分块ID（唯一）';
COMMENT ON COLUMN knowledge_document_chunk.chunk_index IS '分块索引';
COMMENT ON COLUMN knowledge_document_chunk.chunk_type IS '分块类型';
COMMENT ON COLUMN knowledge_document_chunk.embedding IS '向量表示';
COMMENT ON COLUMN knowledge_document_chunk.metadata IS '分块元数据';
COMMENT ON COLUMN bm25_index.kb_id IS '知识库ID';
COMMENT ON COLUMN bm25_index.term IS '术语';
COMMENT ON COLUMN bm25_index.doc_id IS '文档ID';
COMMENT ON COLUMN bm25_index.frequency IS '术语频率';
COMMENT ON COLUMN knowledge_base_permission.kb_id IS '知识库ID';
COMMENT ON COLUMN knowledge_base_permission.user_id IS '用户ID';
COMMENT ON COLUMN knowledge_base_permission.permission_type IS '权限类型：read/write/admin';
COMMENT ON COLUMN knowledge_base_sync_log.sync_type IS '同步类型：incremental/full';
COMMENT ON COLUMN knowledge_base_sync_log.status IS '同步状态：pending/running/success/failed';

