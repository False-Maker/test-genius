-- Agent能力相关表结构
-- 第五阶段：Agent能力

-- Agent定义表
CREATE TABLE IF NOT EXISTS agent (
    id BIGSERIAL PRIMARY KEY,
    agent_code VARCHAR(100) UNIQUE NOT NULL,
    agent_name VARCHAR(200) NOT NULL,
    agent_type VARCHAR(50) NOT NULL,  -- TEST_DESIGN_ASSISTANT, CASE_OPTIMIZATION, CUSTOM
    agent_description TEXT,
    agent_config JSONB,  -- Agent配置（模型、提示词、工具列表等）
    system_prompt TEXT,  -- 系统提示词
    max_iterations INT DEFAULT 10,  -- 最大迭代次数
    max_tokens INT DEFAULT 4000,  -- 最大token数
    temperature DECIMAL(3,2) DEFAULT 0.7,  -- 温度参数
    is_active CHAR(1) DEFAULT '1',
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent会话表
CREATE TABLE IF NOT EXISTS agent_session (
    id BIGSERIAL PRIMARY KEY,
    session_code VARCHAR(100) UNIQUE NOT NULL,
    agent_id BIGINT NOT NULL,
    user_id BIGINT,
    user_name VARCHAR(100),
    session_title VARCHAR(500),  -- 会话标题（自动生成或手动设置）
    context_data JSONB,  -- 上下文数据（对话历史、工具调用历史等）
    status VARCHAR(50) DEFAULT 'ACTIVE',  -- ACTIVE, CLOSED, EXPIRED
    total_tokens INT DEFAULT 0,  -- 总token使用量
    total_iterations INT DEFAULT 0,  -- 总迭代次数
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent(id)
);

-- Agent消息表（对话历史）
CREATE TABLE IF NOT EXISTS agent_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    message_type VARCHAR(50) NOT NULL,  -- USER, ASSISTANT, TOOL, SYSTEM
    role VARCHAR(50) NOT NULL,  -- user, assistant, tool, system
    content TEXT NOT NULL,  -- 消息内容
    tool_calls JSONB,  -- 工具调用信息（如果是tool类型）
    tool_results JSONB,  -- 工具执行结果
    tokens_used INT DEFAULT 0,  -- token使用量
    response_time INT,  -- 响应时间（毫秒）
    model_code VARCHAR(50),  -- 使用的模型
    iteration_number INT,  -- 迭代次数
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES agent_session(id)
);

-- Agent工具表
CREATE TABLE IF NOT EXISTS agent_tool (
    id BIGSERIAL PRIMARY KEY,
    tool_code VARCHAR(100) UNIQUE NOT NULL,
    tool_name VARCHAR(200) NOT NULL,
    tool_type VARCHAR(50) NOT NULL,  -- TEST_RELATED, GENERAL, CUSTOM
    tool_description TEXT NOT NULL,
    tool_schema JSONB NOT NULL,  -- 工具schema（OpenAPI格式）
    tool_implementation VARCHAR(500),  -- 工具实现类/函数路径
    tool_config JSONB,  -- 工具配置
    is_builtin CHAR(1) DEFAULT '0',  -- 是否内置工具
    is_active CHAR(1) DEFAULT '1',
    permission_level VARCHAR(50) DEFAULT 'NORMAL',  -- 权限级别：NORMAL, ADMIN
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agent工具调用记录表
CREATE TABLE IF NOT EXISTS agent_tool_call (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    message_id BIGINT,  -- 关联的消息ID
    tool_code VARCHAR(100) NOT NULL,
    tool_name VARCHAR(200) NOT NULL,
    call_arguments JSONB NOT NULL,  -- 调用参数
    call_result JSONB,  -- 调用结果
    call_status VARCHAR(50) NOT NULL,  -- SUCCESS, FAILED, TIMEOUT
    error_message TEXT,  -- 错误信息
    execution_time INT,  -- 执行时间（毫秒）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES agent_session(id),
    FOREIGN KEY (message_id) REFERENCES agent_message(id)
);

-- Agent工具关联表（Agent可用的工具）
CREATE TABLE IF NOT EXISTS agent_tool_relation (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    is_enabled CHAR(1) DEFAULT '1',
    tool_order INT DEFAULT 0,  -- 工具排序
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent(id),
    FOREIGN KEY (tool_id) REFERENCES agent_tool(id),
    UNIQUE(agent_id, tool_id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_code ON agent(agent_code);
CREATE INDEX IF NOT EXISTS idx_agent_type ON agent(agent_type);
CREATE INDEX IF NOT EXISTS idx_agent_active ON agent(is_active);

CREATE INDEX IF NOT EXISTS idx_agent_session_code ON agent_session(session_code);
CREATE INDEX IF NOT EXISTS idx_agent_session_agent_id ON agent_session(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_session_user_id ON agent_session(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_session_status ON agent_session(status);
CREATE INDEX IF NOT EXISTS idx_agent_session_last_active ON agent_session(last_active_time);

CREATE INDEX IF NOT EXISTS idx_agent_message_session_id ON agent_message(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_message_type ON agent_message(message_type);
CREATE INDEX IF NOT EXISTS idx_agent_message_create_time ON agent_message(create_time);

CREATE INDEX IF NOT EXISTS idx_agent_tool_code ON agent_tool(tool_code);
CREATE INDEX IF NOT EXISTS idx_agent_tool_type ON agent_tool(tool_type);
CREATE INDEX IF NOT EXISTS idx_agent_tool_active ON agent_tool(is_active);

CREATE INDEX IF NOT EXISTS idx_agent_tool_call_session_id ON agent_tool_call(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_tool_call_tool_code ON agent_tool_call(tool_code);
CREATE INDEX IF NOT EXISTS idx_agent_tool_call_status ON agent_tool_call(call_status);

CREATE INDEX IF NOT EXISTS idx_agent_tool_relation_agent_id ON agent_tool_relation(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_tool_relation_tool_id ON agent_tool_relation(tool_id);

