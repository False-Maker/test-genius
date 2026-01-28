-- ============================================
-- 应用管理 & 工作流 A/B 测试（第四阶段 4.3）
-- 应用：逻辑应用，关联工作流/提示词；版本管理复用 workflow_version / prompt_template_version
-- 工作流 A/B 测试：对 workflow_version 做 A/B 对比，结构对齐 prompt_template_ab_test
-- ============================================

-- 应用表
CREATE TABLE IF NOT EXISTS application (
    id BIGSERIAL PRIMARY KEY,
    app_code VARCHAR(100) UNIQUE NOT NULL,
    app_name VARCHAR(200) NOT NULL,
    app_type VARCHAR(50) NOT NULL,  -- CASE_GENERATION / UI_SCRIPT_GENERATION / REPORT_GENERATION / AGENT 等
    workflow_id BIGINT,
    prompt_template_id BIGINT,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE SET NULL,
    FOREIGN KEY (prompt_template_id) REFERENCES prompt_template(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_application_app_code ON application(app_code);
CREATE INDEX IF NOT EXISTS idx_application_app_type ON application(app_type);
CREATE INDEX IF NOT EXISTS idx_application_workflow_id ON application(workflow_id);
CREATE INDEX IF NOT EXISTS idx_application_prompt_template_id ON application(prompt_template_id);
COMMENT ON TABLE application IS '应用管理表，关联工作流/提示词模板，支持版本管理与A/B测试';

-- 工作流 A/B 测试配置表
CREATE TABLE IF NOT EXISTS workflow_ab_test (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    test_description TEXT,
    version_a_id BIGINT NOT NULL,
    version_b_id BIGINT NOT NULL,
    traffic_split_a INT DEFAULT 50,
    traffic_split_b INT DEFAULT 50,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'draft',  -- draft / running / paused / completed
    created_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE CASCADE,
    FOREIGN KEY (version_a_id) REFERENCES workflow_version(id) ON DELETE CASCADE,
    FOREIGN KEY (version_b_id) REFERENCES workflow_version(id) ON DELETE CASCADE,
    CHECK (traffic_split_a + traffic_split_b = 100),
    CHECK (traffic_split_a >= 0 AND traffic_split_a <= 100),
    CHECK (traffic_split_b >= 0 AND traffic_split_b <= 100)
);

CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_workflow_id ON workflow_ab_test(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_status ON workflow_ab_test(status);
COMMENT ON TABLE workflow_ab_test IS '工作流 A/B 测试配置表';

-- 工作流 A/B 测试执行记录表
CREATE TABLE IF NOT EXISTS workflow_ab_test_execution (
    id BIGSERIAL PRIMARY KEY,
    ab_test_id BIGINT NOT NULL,
    request_id VARCHAR(100) NOT NULL,
    version_id BIGINT NOT NULL,
    version_label VARCHAR(10) NOT NULL,  -- A / B
    workflow_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'success',
    response_time INT,
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ab_test_id) REFERENCES workflow_ab_test(id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES workflow_version(id) ON DELETE CASCADE,
    FOREIGN KEY (workflow_id) REFERENCES workflow_definition(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_execution_ab_test_id ON workflow_ab_test_execution(ab_test_id);
CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_execution_version_id ON workflow_ab_test_execution(version_id);
CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_execution_request_id ON workflow_ab_test_execution(request_id);
CREATE INDEX IF NOT EXISTS idx_workflow_ab_test_execution_execution_time ON workflow_ab_test_execution(execution_time);
COMMENT ON TABLE workflow_ab_test_execution IS '工作流 A/B 测试执行记录表';
