-- ============================================
-- 工作流系统相关表
-- 参考 Dify 框架设计，实现可视化可配置的工作流
-- ============================================

-- 工作流定义表
CREATE TABLE IF NOT EXISTS workflow_definition (
    id BIGSERIAL PRIMARY KEY,
    workflow_code VARCHAR(100) UNIQUE NOT NULL,  -- 工作流代码（唯一标识）
    workflow_name VARCHAR(200) NOT NULL,  -- 工作流名称
    workflow_description TEXT,  -- 工作流描述
    workflow_type VARCHAR(50),  -- 工作流类型：CASE_GENERATION/UI_SCRIPT_GENERATION/REPORT_GENERATION等
    workflow_config TEXT NOT NULL,  -- 工作流配置（JSON格式，包含nodes和edges）
    version INT NOT NULL DEFAULT 1,  -- 版本号
    is_active BOOLEAN DEFAULT TRUE,  -- 是否启用
    is_default BOOLEAN DEFAULT FALSE,  -- 是否为默认工作流
    creator_id BIGINT,  -- 创建人ID
    creator_name VARCHAR(100),  -- 创建人姓名
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    last_execution_time TIMESTAMP,  -- 最后执行时间
    execution_count INT DEFAULT 0  -- 执行次数
);

-- 工作流定义表索引
CREATE INDEX IF NOT EXISTS idx_workflow_definition_code ON workflow_definition(workflow_code);
CREATE INDEX IF NOT EXISTS idx_workflow_definition_type ON workflow_definition(workflow_type);
CREATE INDEX IF NOT EXISTS idx_workflow_definition_is_active ON workflow_definition(is_active);
CREATE INDEX IF NOT EXISTS idx_workflow_definition_is_default ON workflow_definition(is_default);

-- 工作流版本表（用于版本管理）
CREATE TABLE IF NOT EXISTS workflow_version (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,  -- 工作流定义ID
    workflow_code VARCHAR(100) NOT NULL,  -- 工作流代码
    version INT NOT NULL,  -- 版本号
    workflow_config TEXT NOT NULL,  -- 工作流配置（JSON格式）
    version_description TEXT,  -- 版本描述
    creator_id BIGINT,  -- 创建人ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE CASCADE,
    UNIQUE(workflow_id, version)  -- 同一工作流的版本号唯一
);

-- 工作流版本表索引
CREATE INDEX IF NOT EXISTS idx_workflow_version_workflow_id ON workflow_version(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_version_code_version ON workflow_version(workflow_code, version);

-- 工作流执行记录表
CREATE TABLE IF NOT EXISTS workflow_execution (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(100) UNIQUE NOT NULL,  -- 执行ID（唯一标识）
    workflow_id BIGINT NOT NULL,  -- 工作流定义ID
    workflow_code VARCHAR(100) NOT NULL,  -- 工作流代码
    workflow_version INT NOT NULL,  -- 工作流版本
    execution_type VARCHAR(50) NOT NULL,  -- 执行类型：MANUAL/SCHEDULED/API
    input_data TEXT,  -- 输入数据（JSON格式）
    output_data TEXT,  -- 输出数据（JSON格式）
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- 状态：PENDING/RUNNING/SUCCESS/FAILED/CANCELLED
    progress INT DEFAULT 0,  -- 进度（0-100）
    current_node_id VARCHAR(100),  -- 当前执行节点ID
    error_message TEXT,  -- 错误信息
    error_node_id VARCHAR(100),  -- 错误节点ID
    execution_log TEXT,  -- 执行日志（JSON格式，记录各节点执行情况）
    start_time TIMESTAMP,  -- 开始时间
    end_time TIMESTAMP,  -- 结束时间
    duration INT,  -- 执行耗时（毫秒）
    creator_id BIGINT,  -- 创建人ID
    creator_name VARCHAR(100),  -- 创建人姓名
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id)
);

-- 工作流执行记录表索引
CREATE INDEX IF NOT EXISTS idx_workflow_execution_execution_id ON workflow_execution(execution_id);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_workflow_id ON workflow_execution(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_workflow_code ON workflow_execution(workflow_code);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_status ON workflow_execution(status);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_create_time ON workflow_execution(create_time);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_creator_id ON workflow_execution(creator_id);

-- 工作流节点执行记录表（记录每个节点的执行情况）
CREATE TABLE IF NOT EXISTS workflow_node_execution (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(100) NOT NULL,  -- 工作流执行ID
    node_id VARCHAR(100) NOT NULL,  -- 节点ID
    node_type VARCHAR(50) NOT NULL,  -- 节点类型：input/process/transform/output/condition/loop
    node_name VARCHAR(200),  -- 节点名称
    input_data TEXT,  -- 节点输入数据（JSON格式）
    output_data TEXT,  -- 节点输出数据（JSON格式）
    status VARCHAR(50) NOT NULL,  -- 状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
    error_message TEXT,  -- 错误信息
    start_time TIMESTAMP,  -- 开始时间
    end_time TIMESTAMP,  -- 结束时间
    duration INT,  -- 执行耗时（毫秒）
    retry_count INT DEFAULT 0,  -- 重试次数
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (execution_id) REFERENCES workflow_execution(execution_id) ON DELETE CASCADE
);

-- 工作流节点执行记录表索引
CREATE INDEX IF NOT EXISTS idx_workflow_node_execution_execution_id ON workflow_node_execution(execution_id);
CREATE INDEX IF NOT EXISTS idx_workflow_node_execution_node_id ON workflow_node_execution(node_id);
CREATE INDEX IF NOT EXISTS idx_workflow_node_execution_status ON workflow_node_execution(status);

-- 添加注释
COMMENT ON TABLE workflow_definition IS '工作流定义表，存储工作流的配置信息';
COMMENT ON TABLE workflow_version IS '工作流版本表，用于版本管理和回滚';
COMMENT ON TABLE workflow_execution IS '工作流执行记录表，记录每次工作流执行的详细信息';
COMMENT ON TABLE workflow_node_execution IS '工作流节点执行记录表，记录每个节点的执行情况';
