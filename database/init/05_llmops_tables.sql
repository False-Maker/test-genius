-- ============================================
-- LLMOps 和监控模块相关表
-- 参考 Dify 框架设计，用于记录应用日志、性能监控和成本统计
-- ============================================

-- 应用日志表（记录所有模型调用和应用操作）
CREATE TABLE IF NOT EXISTS app_log (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) UNIQUE NOT NULL,  -- 唯一请求ID
    user_id BIGINT,  -- 用户ID
    user_name VARCHAR(100),  -- 用户名
    app_type VARCHAR(50),  -- 应用类型：CASE_GENERATION/UI_SCRIPT_GENERATION/DOCUMENT_PARSING等
    model_code VARCHAR(50),  -- 模型代码
    model_name VARCHAR(200),  -- 模型名称
    prompt TEXT,  -- 提示词
    prompt_length INT,  -- 提示词长度
    response TEXT,  -- 模型响应
    response_length INT,  -- 响应长度
    tokens_input INT,  -- 输入token数
    tokens_output INT,  -- 输出token数
    tokens_total INT,  -- 总token数
    response_time INT,  -- 响应时间（毫秒）
    cost DECIMAL(10, 6),  -- 成本（元）
    status VARCHAR(20) NOT NULL DEFAULT 'success',  -- 状态：success/failed
    error TEXT,  -- 错误信息
    error_code VARCHAR(50),  -- 错误代码
    ip_address VARCHAR(50),  -- IP地址
    user_agent VARCHAR(500),  -- 用户代理
    request_url VARCHAR(1000),  -- 请求URL
    request_method VARCHAR(10),  -- 请求方法
    request_params TEXT,  -- 请求参数（JSON格式）
    response_data TEXT,  -- 响应数据（JSON格式，包含完整响应信息）
    metadata TEXT,  -- 元数据（JSON格式，用于存储额外信息）
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- 应用日志表索引
CREATE INDEX IF NOT EXISTS idx_app_log_request_id ON app_log(request_id);
CREATE INDEX IF NOT EXISTS idx_app_log_timestamp ON app_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_app_log_user_id ON app_log(user_id);
CREATE INDEX IF NOT EXISTS idx_app_log_app_type ON app_log(app_type);
CREATE INDEX IF NOT EXISTS idx_app_log_model_code ON app_log(model_code);
CREATE INDEX IF NOT EXISTS idx_app_log_status ON app_log(status);
CREATE INDEX IF NOT EXISTS idx_app_log_user_timestamp ON app_log(user_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_app_log_app_timestamp ON app_log(app_type, timestamp);
CREATE INDEX IF NOT EXISTS idx_app_log_model_timestamp ON app_log(model_code, timestamp);

-- 用户反馈表
CREATE TABLE IF NOT EXISTS user_feedback (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL,  -- 关联的请求ID
    log_id BIGINT,  -- 关联的日志ID
    user_id BIGINT,  -- 用户ID
    user_name VARCHAR(100),  -- 用户名
    rating INT,  -- 评分：1-5分
    comment TEXT,  -- 反馈内容
    feedback_type VARCHAR(50),  -- 反馈类型：POSITIVE/NEGATIVE/NEUTRAL
    tags VARCHAR(500),  -- 标签（逗号分隔）
    is_resolved BOOLEAN DEFAULT FALSE,  -- 是否已处理
    resolved_by BIGINT,  -- 处理人ID
    resolved_at TIMESTAMP,  -- 处理时间
    feedback_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 反馈时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- 用户反馈表索引
CREATE INDEX IF NOT EXISTS idx_user_feedback_request_id ON user_feedback(request_id);
CREATE INDEX IF NOT EXISTS idx_user_feedback_log_id ON user_feedback(log_id);
CREATE INDEX IF NOT EXISTS idx_user_feedback_user_id ON user_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_user_feedback_rating ON user_feedback(rating);
CREATE INDEX IF NOT EXISTS idx_user_feedback_feedback_time ON user_feedback(feedback_time);
CREATE INDEX IF NOT EXISTS idx_user_feedback_resolved ON user_feedback(is_resolved);

-- 模型成本配置表
CREATE TABLE IF NOT EXISTS model_cost_config (
    id BIGSERIAL PRIMARY KEY,
    model_code VARCHAR(50) UNIQUE NOT NULL,  -- 模型代码
    model_name VARCHAR(200) NOT NULL,  -- 模型名称
    input_price_per_1k_tokens DECIMAL(10, 6) DEFAULT 0,  -- 输入token价格（每1k tokens，单位：元）
    output_price_per_1k_tokens DECIMAL(10, 6) DEFAULT 0,  -- 输出token价格（每1k tokens，单位：元）
    currency VARCHAR(10) DEFAULT 'CNY',  -- 货币单位
    is_active BOOLEAN DEFAULT TRUE,  -- 是否启用
    description TEXT,  -- 描述
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

-- 模型成本配置表索引
CREATE INDEX IF NOT EXISTS idx_model_cost_config_model_code ON model_cost_config(model_code);
CREATE INDEX IF NOT EXISTS idx_model_cost_config_is_active ON model_cost_config(is_active);

-- 告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(100) UNIQUE NOT NULL,  -- 规则代码
    rule_name VARCHAR(200) NOT NULL,  -- 规则名称
    rule_type VARCHAR(50) NOT NULL,  -- 规则类型：FAILURE_RATE/RESPONSE_TIME/COST等
    alert_condition VARCHAR(50) NOT NULL,  -- 告警条件：GT/GTE/LT/LTE/EQ
    threshold_value DECIMAL(10, 2) NOT NULL,  -- 阈值
    threshold_unit VARCHAR(20),  -- 阈值单位：PERCENT/MS/CNY等
    time_window INT DEFAULT 300,  -- 时间窗口（秒）
    check_interval INT DEFAULT 60,  -- 检查间隔（秒）
    target_scope VARCHAR(50),  -- 目标范围：ALL/MODEL/APP/USER
    target_value VARCHAR(200),  -- 目标值（模型代码、应用类型、用户ID等）
    is_enabled BOOLEAN DEFAULT TRUE,  -- 是否启用
    notification_channels TEXT,  -- 通知渠道（JSON格式：["email", "sms", "webhook"]）
    notification_recipients TEXT,  -- 通知接收人（JSON格式）
    description TEXT,  -- 描述
    creator_id BIGINT,  -- 创建人ID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

-- 告警规则表索引
CREATE INDEX IF NOT EXISTS idx_alert_rule_rule_code ON alert_rule(rule_code);
CREATE INDEX IF NOT EXISTS idx_alert_rule_rule_type ON alert_rule(rule_type);
CREATE INDEX IF NOT EXISTS idx_alert_rule_is_enabled ON alert_rule(is_enabled);

-- 告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL,  -- 告警规则ID
    rule_code VARCHAR(100) NOT NULL,  -- 告警规则代码
    alert_level VARCHAR(20) NOT NULL,  -- 告警级别：INFO/WARNING/ERROR/CRITICAL
    alert_title VARCHAR(200) NOT NULL,  -- 告警标题
    alert_message TEXT,  -- 告警消息
    current_value DECIMAL(10, 2),  -- 当前值
    threshold_value DECIMAL(10, 2),  -- 阈值
    target_scope VARCHAR(50),  -- 目标范围
    target_value VARCHAR(200),  -- 目标值
    alert_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 告警时间
    is_resolved BOOLEAN DEFAULT FALSE,  -- 是否已解决
    resolved_by BIGINT,  -- 解决人ID
    resolved_at TIMESTAMP,  -- 解决时间
    resolved_note TEXT,  -- 解决说明
    notification_sent BOOLEAN DEFAULT FALSE,  -- 是否已发送通知
    notification_channels TEXT,  -- 已发送的通知渠道
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    FOREIGN KEY (rule_id) REFERENCES alert_rule(id)
);

-- 告警记录表索引
CREATE INDEX IF NOT EXISTS idx_alert_record_rule_id ON alert_record(rule_id);
CREATE INDEX IF NOT EXISTS idx_alert_record_rule_code ON alert_record(rule_code);
CREATE INDEX IF NOT EXISTS idx_alert_record_alert_level ON alert_record(alert_level);
CREATE INDEX IF NOT EXISTS idx_alert_record_alert_time ON alert_record(alert_time);
CREATE INDEX IF NOT EXISTS idx_alert_record_is_resolved ON alert_record(is_resolved);
CREATE INDEX IF NOT EXISTS idx_alert_record_target ON alert_record(target_scope, target_value);

-- 添加注释
COMMENT ON TABLE app_log IS '应用日志表，记录所有模型调用和应用操作的详细信息';
COMMENT ON TABLE user_feedback IS '用户反馈表，记录用户对模型响应质量的反馈';
COMMENT ON TABLE model_cost_config IS '模型成本配置表，配置各模型的token价格';
COMMENT ON TABLE alert_rule IS '告警规则表，定义各种告警规则';
COMMENT ON TABLE alert_record IS '告警记录表，记录触发的告警信息';
