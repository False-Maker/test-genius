-- 提示词模板A/B测试相关表
-- 用于管理提示词模板的A/B测试配置和效果统计

-- A/B测试配置表
CREATE TABLE IF NOT EXISTS prompt_template_ab_test (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    test_description TEXT,
    version_a_id BIGINT NOT NULL,  -- 版本A的版本ID
    version_b_id BIGINT NOT NULL,  -- 版本B的版本ID
    traffic_split_a INT DEFAULT 50,  -- 版本A的流量分配比例（0-100）
    traffic_split_b INT DEFAULT 50,  -- 版本B的流量分配比例（0-100）
    start_time TIMESTAMP,  -- 测试开始时间
    end_time TIMESTAMP,  -- 测试结束时间
    status VARCHAR(20) DEFAULT 'draft',  -- 状态：draft/running/paused/completed
    auto_select_enabled VARCHAR(1) DEFAULT '0',  -- 是否启用自动选择：1-是，0-否
    min_samples INT DEFAULT 100,  -- 最小样本数（达到此数量后才进行自动选择）
    selection_criteria VARCHAR(50) DEFAULT 'success_rate',  -- 选择标准：success_rate/response_time/user_rating
    created_by BIGINT,
    created_by_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES prompt_template(id) ON DELETE CASCADE,
    FOREIGN KEY (version_a_id) REFERENCES prompt_template_version(id) ON DELETE CASCADE,
    FOREIGN KEY (version_b_id) REFERENCES prompt_template_version(id) ON DELETE CASCADE,
    CHECK (traffic_split_a + traffic_split_b = 100),
    CHECK (traffic_split_a >= 0 AND traffic_split_a <= 100),
    CHECK (traffic_split_b >= 0 AND traffic_split_b <= 100)
);

-- A/B测试执行记录表（记录每次请求使用的版本）
CREATE TABLE IF NOT EXISTS prompt_template_ab_test_execution (
    id BIGSERIAL PRIMARY KEY,
    ab_test_id BIGINT NOT NULL,
    request_id VARCHAR(100) NOT NULL,  -- 关联app_log的request_id
    version_id BIGINT NOT NULL,  -- 使用的版本ID
    version_label VARCHAR(10) NOT NULL,  -- 版本标签：A或B
    user_id BIGINT,
    prompt TEXT,  -- 使用的提示词
    response TEXT,  -- 模型响应
    response_time INT,  -- 响应时间（毫秒）
    tokens_used INT,  -- 使用的token数
    status VARCHAR(20) DEFAULT 'success',  -- 状态：success/failed
    error TEXT,  -- 错误信息
    user_rating INT,  -- 用户评分（1-5分，可选）
    user_feedback TEXT,  -- 用户反馈（可选）
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ab_test_id) REFERENCES prompt_template_ab_test(id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES prompt_template_version(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ab_test_template_id ON prompt_template_ab_test(template_id);
CREATE INDEX IF NOT EXISTS idx_ab_test_status ON prompt_template_ab_test(status);
CREATE INDEX IF NOT EXISTS idx_ab_test_execution_ab_test_id ON prompt_template_ab_test_execution(ab_test_id);
CREATE INDEX IF NOT EXISTS idx_ab_test_execution_version_id ON prompt_template_ab_test_execution(version_id);
CREATE INDEX IF NOT EXISTS idx_ab_test_execution_request_id ON prompt_template_ab_test_execution(request_id);
CREATE INDEX IF NOT EXISTS idx_ab_test_execution_execution_time ON prompt_template_ab_test_execution(execution_time);

-- 添加注释
COMMENT ON TABLE prompt_template_ab_test IS '提示词模板A/B测试配置表';
COMMENT ON TABLE prompt_template_ab_test_execution IS 'A/B测试执行记录表';
