-- 测试执行模块数据库表结构
-- 创建测试执行相关的表结构

-- 测试执行任务表
CREATE TABLE IF NOT EXISTS test_execution_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(100) UNIQUE NOT NULL,
    task_name VARCHAR(500) NOT NULL,
    task_type VARCHAR(50) NOT NULL, -- 任务类型：AUTO_SCRIPT_GENERATION/AUTO_SCRIPT_REPAIR/MANUAL_EXECUTION
    requirement_id BIGINT, -- 关联需求ID
    case_id BIGINT, -- 关联用例ID
    case_suite_id BIGINT, -- 关联测试套件ID
    script_type VARCHAR(50), -- 脚本类型：SELENIUM/PLAYWRIGHT/PUPPETEER
    script_content TEXT, -- 脚本内容
    script_language VARCHAR(50), -- 脚本语言：PYTHON/JAVA/JAVASCRIPT
    page_code_url VARCHAR(1000), -- 页面代码URL（文件路径或URL）
    natural_language_desc TEXT, -- 自然语言描述（用于脚本生成）
    error_log TEXT, -- 错误日志（用于脚本修复）
    execution_config TEXT, -- 执行配置（JSON格式）
    task_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 任务状态：PENDING/PROCESSING/SUCCESS/FAILED
    progress INT DEFAULT 0, -- 任务进度（0-100）
    success_count INT DEFAULT 0, -- 成功数量
    fail_count INT DEFAULT 0, -- 失败数量
    result_data TEXT, -- 结果数据（JSON格式）
    error_message TEXT, -- 错误信息
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finish_time TIMESTAMP, -- 完成时间
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
);

-- 测试执行记录表
CREATE TABLE IF NOT EXISTS test_execution_record (
    id BIGSERIAL PRIMARY KEY,
    record_code VARCHAR(100) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL, -- 执行任务ID
    case_id BIGINT, -- 关联用例ID
    execution_type VARCHAR(50) NOT NULL, -- 执行类型：MANUAL/AUTOMATED
    execution_status VARCHAR(50) NOT NULL, -- 执行状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
    execution_result TEXT, -- 执行结果详情
    execution_log TEXT, -- 执行日志
    error_message TEXT, -- 错误信息
    execution_duration INT, -- 执行耗时（毫秒）
    executed_by BIGINT, -- 执行人ID
    executed_by_name VARCHAR(100), -- 执行人姓名
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 执行时间
    finish_time TIMESTAMP, -- 完成时间
    screenshot_url VARCHAR(1000), -- 截图URL（失败时）
    video_url VARCHAR(1000), -- 视频URL（可选）
    FOREIGN KEY (task_id) REFERENCES test_execution_task(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
);

-- UI脚本模板表
CREATE TABLE IF NOT EXISTS ui_script_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(100) UNIQUE NOT NULL,
    template_name VARCHAR(500) NOT NULL,
    template_type VARCHAR(50) NOT NULL, -- 模板类型：SELENIUM/PLAYWRIGHT/PUPPETEER
    script_language VARCHAR(50) NOT NULL, -- 脚本语言：PYTHON/JAVA/JAVASCRIPT
    template_content TEXT NOT NULL, -- 模板内容
    template_variables TEXT, -- 模板变量定义（JSON格式）
    applicable_scenarios TEXT, -- 适用场景描述
    template_description TEXT, -- 模板描述
    version INT DEFAULT 1, -- 版本号
    is_active VARCHAR(1) DEFAULT '1', -- 是否启用：1-启用，0-禁用
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 页面元素信息表
CREATE TABLE IF NOT EXISTS page_element_info (
    id BIGSERIAL PRIMARY KEY,
    element_code VARCHAR(100) UNIQUE NOT NULL, -- 元素编码
    page_url VARCHAR(1000) NOT NULL, -- 页面URL
    element_type VARCHAR(50), -- 元素类型：BUTTON/INPUT/LINK/SELECT等
    element_locator_type VARCHAR(50), -- 定位方式：ID/CLASS/XPATH/CSS_SELECTOR
    element_locator_value VARCHAR(500), -- 定位值
    element_text VARCHAR(500), -- 元素文本
    element_attributes TEXT, -- 元素属性（JSON格式）
    page_structure TEXT, -- 页面结构（JSON格式）
    screenshot_url VARCHAR(1000), -- 截图URL
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
-- 测试执行任务表索引
CREATE INDEX IF NOT EXISTS idx_execution_task_code ON test_execution_task(task_code);
CREATE INDEX IF NOT EXISTS idx_execution_task_status ON test_execution_task(task_status);
CREATE INDEX IF NOT EXISTS idx_execution_task_requirement_id ON test_execution_task(requirement_id);
CREATE INDEX IF NOT EXISTS idx_execution_task_case_id ON test_execution_task(case_id);
CREATE INDEX IF NOT EXISTS idx_execution_task_create_time ON test_execution_task(create_time);
CREATE INDEX IF NOT EXISTS idx_execution_task_status_time ON test_execution_task(task_status, create_time);

-- 测试执行记录表索引
CREATE INDEX IF NOT EXISTS idx_execution_record_code ON test_execution_record(record_code);
CREATE INDEX IF NOT EXISTS idx_execution_record_task_id ON test_execution_record(task_id);
CREATE INDEX IF NOT EXISTS idx_execution_record_case_id ON test_execution_record(case_id);
CREATE INDEX IF NOT EXISTS idx_execution_record_status ON test_execution_record(execution_status);
CREATE INDEX IF NOT EXISTS idx_execution_record_time ON test_execution_record(execution_time);
CREATE INDEX IF NOT EXISTS idx_execution_record_status_time ON test_execution_record(execution_status, execution_time);

-- UI脚本模板表索引
CREATE INDEX IF NOT EXISTS idx_ui_script_template_code ON ui_script_template(template_code);
CREATE INDEX IF NOT EXISTS idx_ui_script_template_type ON ui_script_template(template_type);
CREATE INDEX IF NOT EXISTS idx_ui_script_template_active ON ui_script_template(is_active);
CREATE INDEX IF NOT EXISTS idx_ui_script_template_type_active ON ui_script_template(template_type, is_active);

-- 页面元素信息表索引
CREATE INDEX IF NOT EXISTS idx_page_element_code ON page_element_info(element_code);
CREATE INDEX IF NOT EXISTS idx_page_element_url ON page_element_info(page_url);
CREATE INDEX IF NOT EXISTS idx_page_element_type ON page_element_info(element_type);

-- 风险评估表
CREATE TABLE IF NOT EXISTS test_risk_assessment (
    id BIGSERIAL PRIMARY KEY,
    assessment_code VARCHAR(100) UNIQUE NOT NULL, -- 评估编码（RISK-YYYYMMDD-序号）
    assessment_name VARCHAR(500) NOT NULL, -- 评估名称
    requirement_id BIGINT, -- 关联需求ID
    execution_task_id BIGINT, -- 关联执行任务ID
    risk_level VARCHAR(50), -- 风险等级：HIGH/MEDIUM/LOW
    risk_score DECIMAL(5,2), -- 风险评分（0-100）
    risk_items TEXT, -- 风险项列表（JSON格式）
    feasibility_score DECIMAL(5,2), -- 上线可行性评分（0-100）
    feasibility_recommendation TEXT, -- 上线建议
    assessment_details TEXT, -- 评估详情（JSON格式）
    assessment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 评估时间
    assessor_id BIGINT, -- 评估人ID
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id),
    FOREIGN KEY (execution_task_id) REFERENCES test_execution_task(id)
);

-- 风险评估表索引
CREATE INDEX IF NOT EXISTS idx_risk_assessment_code ON test_risk_assessment(assessment_code);
CREATE INDEX IF NOT EXISTS idx_risk_assessment_requirement_id ON test_risk_assessment(requirement_id);
CREATE INDEX IF NOT EXISTS idx_risk_assessment_task_id ON test_risk_assessment(execution_task_id);
CREATE INDEX IF NOT EXISTS idx_risk_assessment_level ON test_risk_assessment(risk_level);
CREATE INDEX IF NOT EXISTS idx_risk_assessment_time ON test_risk_assessment(assessment_time);

