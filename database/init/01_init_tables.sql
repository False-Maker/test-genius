-- 测试设计助手系统数据库初始化脚本
-- 创建核心表结构

-- 测试需求表
CREATE TABLE IF NOT EXISTS test_requirement (
    id BIGSERIAL PRIMARY KEY,
    requirement_code VARCHAR(100) UNIQUE NOT NULL,
    requirement_name VARCHAR(500) NOT NULL,
    requirement_type VARCHAR(50),
    requirement_description TEXT,
    requirement_doc_url VARCHAR(1000),
    requirement_status VARCHAR(50),
    business_module VARCHAR(100),
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 1
);

-- 测试用例表
CREATE TABLE IF NOT EXISTS test_case (
    id BIGSERIAL PRIMARY KEY,
    case_code VARCHAR(100) UNIQUE NOT NULL,
    case_name VARCHAR(500) NOT NULL,
    requirement_id BIGINT,
    layer_id BIGINT,
    method_id BIGINT,
    case_type VARCHAR(50),
    case_priority VARCHAR(50),
    pre_condition TEXT,
    test_step TEXT,
    expected_result TEXT,
    case_status VARCHAR(50),
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 1,
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id)
);

-- 测试分层表
CREATE TABLE IF NOT EXISTS test_layer (
    id BIGSERIAL PRIMARY KEY,
    layer_code VARCHAR(50) UNIQUE NOT NULL,
    layer_name VARCHAR(100) NOT NULL,
    layer_description TEXT,
    layer_order INT,
    is_active CHAR(1) DEFAULT '1'
);

-- 测试设计方法表
CREATE TABLE IF NOT EXISTS test_design_method (
    id BIGSERIAL PRIMARY KEY,
    method_code VARCHAR(50) UNIQUE NOT NULL,
    method_name VARCHAR(100) NOT NULL,
    method_description TEXT,
    applicable_layers VARCHAR(500),
    example TEXT,
    is_active CHAR(1) DEFAULT '1'
);

-- 提示词模板表
CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(100) UNIQUE NOT NULL,
    template_name VARCHAR(500) NOT NULL,
    template_category VARCHAR(100),
    template_type VARCHAR(50),
    template_content TEXT NOT NULL,
    template_variables TEXT,
    applicable_layers VARCHAR(500),
    applicable_methods VARCHAR(500),
    applicable_modules VARCHAR(500),
    template_description TEXT,
    version INT DEFAULT 1,
    is_active CHAR(1) DEFAULT '1',
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 模型配置表
CREATE TABLE IF NOT EXISTS model_config (
    id BIGSERIAL PRIMARY KEY,
    model_code VARCHAR(100) UNIQUE NOT NULL,
    model_name VARCHAR(200) NOT NULL,
    model_type VARCHAR(50),
    api_endpoint VARCHAR(500),
    api_key VARCHAR(500),
    model_version VARCHAR(50),
    max_tokens INT,
    temperature DECIMAL(3,2),
    is_active CHAR(1) DEFAULT '1',
    priority INT,
    daily_limit INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_requirement_code ON test_requirement(requirement_code);
CREATE INDEX IF NOT EXISTS idx_requirement_status ON test_requirement(requirement_status);
CREATE INDEX IF NOT EXISTS idx_case_code ON test_case(case_code);
CREATE INDEX IF NOT EXISTS idx_case_requirement_id ON test_case(requirement_id);
CREATE INDEX IF NOT EXISTS idx_layer_code ON test_layer(layer_code);
CREATE INDEX IF NOT EXISTS idx_method_code ON test_design_method(method_code);
CREATE INDEX IF NOT EXISTS idx_template_code ON prompt_template(template_code);
CREATE INDEX IF NOT EXISTS idx_model_code ON model_config(model_code);

