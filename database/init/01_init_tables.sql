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
    is_active VARCHAR(1) DEFAULT '1'
);

-- 测试设计方法表
CREATE TABLE IF NOT EXISTS test_design_method (
    id BIGSERIAL PRIMARY KEY,
    method_code VARCHAR(50) UNIQUE NOT NULL,
    method_name VARCHAR(100) NOT NULL,
    method_description TEXT,
    applicable_layers VARCHAR(500),
    example TEXT,
    is_active VARCHAR(1) DEFAULT '1'
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
    is_active VARCHAR(1) DEFAULT '1',
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
    is_active VARCHAR(1) DEFAULT '1',
    priority INT,
    daily_limit INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用例生成任务表
CREATE TABLE IF NOT EXISTS case_generation_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(100) UNIQUE NOT NULL,
    requirement_id BIGINT NOT NULL,
    layer_id BIGINT,
    method_id BIGINT,
    template_id BIGINT,
    model_code VARCHAR(100),
    task_status VARCHAR(50) DEFAULT 'PENDING',
    progress INT DEFAULT 0,
    total_cases INT DEFAULT 0,
    success_cases INT DEFAULT 0,
    fail_cases INT DEFAULT 0,
    error_message TEXT,
    result_data TEXT,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    complete_time TIMESTAMP,
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id),
    FOREIGN KEY (template_id) REFERENCES prompt_template(id)
);

-- 创建索引
-- 需求表索引
CREATE INDEX IF NOT EXISTS idx_requirement_code ON test_requirement(requirement_code);
CREATE INDEX IF NOT EXISTS idx_requirement_status ON test_requirement(requirement_status);
CREATE INDEX IF NOT EXISTS idx_requirement_name ON test_requirement(requirement_name);
CREATE INDEX IF NOT EXISTS idx_requirement_create_time ON test_requirement(create_time);
CREATE INDEX IF NOT EXISTS idx_requirement_status_name ON test_requirement(requirement_status, requirement_name);

-- 用例表索引
CREATE INDEX IF NOT EXISTS idx_case_code ON test_case(case_code);
CREATE INDEX IF NOT EXISTS idx_case_requirement_id ON test_case(requirement_id);
CREATE INDEX IF NOT EXISTS idx_case_name ON test_case(case_name);
CREATE INDEX IF NOT EXISTS idx_case_status ON test_case(case_status);
CREATE INDEX IF NOT EXISTS idx_case_layer_id ON test_case(layer_id);
CREATE INDEX IF NOT EXISTS idx_case_method_id ON test_case(method_id);
CREATE INDEX IF NOT EXISTS idx_case_status_name ON test_case(case_status, case_name);
CREATE INDEX IF NOT EXISTS idx_case_create_time ON test_case(create_time);

-- 测试分层表索引
CREATE INDEX IF NOT EXISTS idx_layer_code ON test_layer(layer_code);
CREATE INDEX IF NOT EXISTS idx_layer_active ON test_layer(is_active, layer_order);

-- 测试设计方法表索引
CREATE INDEX IF NOT EXISTS idx_method_code ON test_design_method(method_code);
CREATE INDEX IF NOT EXISTS idx_method_active ON test_design_method(is_active);

-- 提示词模板表索引
CREATE INDEX IF NOT EXISTS idx_template_code ON prompt_template(template_code);
CREATE INDEX IF NOT EXISTS idx_template_active ON prompt_template(is_active);
CREATE INDEX IF NOT EXISTS idx_template_category ON prompt_template(template_category);

-- 模型配置表索引
CREATE INDEX IF NOT EXISTS idx_model_code ON model_config(model_code);
CREATE INDEX IF NOT EXISTS idx_model_active ON model_config(is_active, priority);
CREATE INDEX IF NOT EXISTS idx_model_type ON model_config(model_type, is_active);

-- 用例生成任务表索引
CREATE INDEX IF NOT EXISTS idx_task_code ON case_generation_task(task_code);
CREATE INDEX IF NOT EXISTS idx_task_status ON case_generation_task(task_status);
CREATE INDEX IF NOT EXISTS idx_task_requirement_id ON case_generation_task(requirement_id);
CREATE INDEX IF NOT EXISTS idx_task_create_time ON case_generation_task(create_time);
CREATE INDEX IF NOT EXISTS idx_task_status_time ON case_generation_task(task_status, create_time);

-- 知识库文档表（如果使用pgvector扩展）
-- 尝试安装pgvector扩展（如果可用）
DO $$
BEGIN
    CREATE EXTENSION IF NOT EXISTS vector;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'pgvector extension not available, using TEXT for embedding column';
END $$;

-- 创建知识库文档表（如果pgvector可用则使用vector类型，否则使用TEXT）
CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    doc_code VARCHAR(100) UNIQUE NOT NULL,
    doc_name VARCHAR(500) NOT NULL,
    doc_type VARCHAR(50), -- 文档类型：规范/业务规则/用例模板/历史用例
    doc_category VARCHAR(100), -- 文档分类
    doc_content TEXT, -- 文档内容
    doc_url VARCHAR(1000), -- 文档URL
    embedding TEXT, -- 向量列（如果pgvector可用，后续可以ALTER TABLE修改为vector类型）
    is_active VARCHAR(1) DEFAULT '1',
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档表索引
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_code ON knowledge_document(doc_code);
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_type ON knowledge_document(doc_type, is_active);
CREATE INDEX IF NOT EXISTS idx_knowledge_doc_category ON knowledge_document(doc_category);
-- 向量索引（如果使用pgvector，需要手动创建）
-- CREATE INDEX IF NOT EXISTS idx_knowledge_document_embedding 
-- ON knowledge_document USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 用例套件表
CREATE TABLE IF NOT EXISTS test_case_suite (
    id BIGSERIAL PRIMARY KEY,
    suite_code VARCHAR(100) UNIQUE NOT NULL,
    suite_name VARCHAR(500) NOT NULL,
    suite_description TEXT,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 测试套件用例关联表
CREATE TABLE IF NOT EXISTS test_suite_case (
    id BIGSERIAL PRIMARY KEY,
    suite_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    case_order INT DEFAULT 0,
    FOREIGN KEY (suite_id) REFERENCES test_case_suite(id) ON DELETE CASCADE,
    FOREIGN KEY (case_id) REFERENCES test_case(id) ON DELETE CASCADE
);

-- 用例套件表索引
CREATE INDEX IF NOT EXISTS idx_suite_code ON test_case_suite(suite_code);
CREATE INDEX IF NOT EXISTS idx_suite_case_suite_id ON test_suite_case(suite_id);
CREATE INDEX IF NOT EXISTS idx_suite_case_case_id ON test_suite_case(case_id);

-- 安全审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(100),
    operation_type VARCHAR(50) NOT NULL, -- 操作类型：CREATE/UPDATE/DELETE/APPROVE/LOGIN/LOGOUT等
    operation_module VARCHAR(100), -- 操作模块：REQUIREMENT/CASE/TEMPLATE等
    operation_target VARCHAR(500), -- 操作目标：实体名称或ID
    operation_content TEXT, -- 操作内容详情
    operation_result VARCHAR(50), -- 操作结果：SUCCESS/FAILURE
    error_message TEXT, -- 错误信息（如果失败）
    ip_address VARCHAR(50), -- IP地址
    user_agent VARCHAR(500), -- 用户代理
    request_url VARCHAR(1000), -- 请求URL
    request_method VARCHAR(10), -- 请求方法：GET/POST/PUT/DELETE
    request_params TEXT, -- 请求参数（JSON格式）
    response_status INT, -- 响应状态码
    execution_time BIGINT, -- 执行时间（毫秒）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 审计日志表索引
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_operation_type ON audit_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_operation_module ON audit_log(operation_module);
CREATE INDEX IF NOT EXISTS idx_audit_log_create_time ON audit_log(create_time);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_time ON audit_log(user_id, create_time);
CREATE INDEX IF NOT EXISTS idx_audit_log_module_type ON audit_log(operation_module, operation_type, create_time);

-- ============================================
-- 测试执行模块相关表（第3-4周：UI脚本生成）
-- ============================================

-- 测试执行任务表
CREATE TABLE IF NOT EXISTS test_execution_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(100) UNIQUE NOT NULL,
    task_name VARCHAR(500) NOT NULL,
    task_type VARCHAR(50) NOT NULL, -- AUTO_SCRIPT_GENERATION/AUTO_SCRIPT_REPAIR/MANUAL_EXECUTION
    requirement_id BIGINT,
    case_id BIGINT,
    case_suite_id BIGINT,
    script_type VARCHAR(50), -- SELENIUM/PLAYWRIGHT/PUPPETEER
    script_content TEXT,
    script_language VARCHAR(50), -- PYTHON/JAVA/JAVASCRIPT
    page_code_url VARCHAR(1000), -- 页面代码URL（文件路径或URL）
    natural_language_desc TEXT, -- 自然语言描述（用于脚本生成）
    error_log TEXT, -- 错误日志（用于脚本修复）
    execution_config TEXT, -- 执行配置（JSON格式）
    task_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING/PROCESSING/SUCCESS/FAILED
    progress INT DEFAULT 0, -- 任务进度（0-100）
    success_count INT DEFAULT 0,
    fail_count INT DEFAULT 0,
    result_data TEXT, -- 结果数据（JSON格式）
    error_message TEXT,
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finish_time TIMESTAMP,
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
);

-- 测试执行记录表
CREATE TABLE IF NOT EXISTS test_execution_record (
    id BIGSERIAL PRIMARY KEY,
    record_code VARCHAR(100) UNIQUE NOT NULL,
    task_id BIGINT NOT NULL,
    case_id BIGINT,
    execution_type VARCHAR(50) NOT NULL, -- MANUAL/AUTOMATED
    execution_status VARCHAR(50) NOT NULL, -- PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
    execution_result TEXT,
    execution_log TEXT,
    error_message TEXT,
    execution_duration INT, -- 执行耗时（毫秒）
    executed_by BIGINT,
    executed_by_name VARCHAR(100),
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finish_time TIMESTAMP,
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
    template_type VARCHAR(50) NOT NULL, -- SELENIUM/PLAYWRIGHT/PUPPETEER
    script_language VARCHAR(50) NOT NULL, -- PYTHON/JAVA/JAVASCRIPT
    template_content TEXT NOT NULL,
    template_variables TEXT, -- 模板变量定义（JSON格式）
    applicable_scenarios TEXT, -- 适用场景描述
    template_description TEXT,
    version INT DEFAULT 1,
    is_active VARCHAR(1) DEFAULT '1',
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 页面元素信息表
CREATE TABLE IF NOT EXISTS page_element_info (
    id BIGSERIAL PRIMARY KEY,
    element_code VARCHAR(100) UNIQUE NOT NULL,
    page_url VARCHAR(1000) NOT NULL,
    element_type VARCHAR(50), -- BUTTON/INPUT/LINK/SELECT等
    element_locator_type VARCHAR(50), -- ID/CLASS/XPATH/CSS_SELECTOR
    element_locator_value VARCHAR(500),
    element_text VARCHAR(500),
    element_attributes TEXT, -- 元素属性（JSON格式）
    page_structure TEXT, -- 页面结构（JSON格式）
    screenshot_url VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 测试执行任务表索引
CREATE INDEX IF NOT EXISTS idx_execution_task_code ON test_execution_task(task_code);
CREATE INDEX IF NOT EXISTS idx_execution_task_status ON test_execution_task(task_status);
CREATE INDEX IF NOT EXISTS idx_execution_task_requirement_id ON test_execution_task(requirement_id);
CREATE INDEX IF NOT EXISTS idx_execution_task_case_id ON test_execution_task(case_id);
CREATE INDEX IF NOT EXISTS idx_execution_task_create_time ON test_execution_task(create_time);

-- 测试执行记录表索引
CREATE INDEX IF NOT EXISTS idx_execution_record_code ON test_execution_record(record_code);
CREATE INDEX IF NOT EXISTS idx_execution_record_task_id ON test_execution_record(task_id);
CREATE INDEX IF NOT EXISTS idx_execution_record_case_id ON test_execution_record(case_id);
CREATE INDEX IF NOT EXISTS idx_execution_record_status ON test_execution_record(execution_status);
CREATE INDEX IF NOT EXISTS idx_execution_record_time ON test_execution_record(execution_time);

-- UI脚本模板表索引
CREATE INDEX IF NOT EXISTS idx_ui_script_template_code ON ui_script_template(template_code);
CREATE INDEX IF NOT EXISTS idx_ui_script_template_type ON ui_script_template(template_type);
CREATE INDEX IF NOT EXISTS idx_ui_script_template_active ON ui_script_template(is_active);

-- 页面元素信息表索引
CREATE INDEX IF NOT EXISTS idx_page_element_code ON page_element_info(element_code);
CREATE INDEX IF NOT EXISTS idx_page_element_url ON page_element_info(page_url);

-- ============================================
-- 测试评估模块相关表（第9-11周：测试报告生成）
-- ============================================

-- 测试报告模板表
CREATE TABLE IF NOT EXISTS test_report_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(100) UNIQUE NOT NULL,
    template_name VARCHAR(500) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_content TEXT NOT NULL,
    template_variables TEXT,
    file_format VARCHAR(50),
    template_description TEXT,
    is_default VARCHAR(1) DEFAULT '0',
    is_active VARCHAR(1) DEFAULT '1',
    version INT DEFAULT 1,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 测试报告表
CREATE TABLE IF NOT EXISTS test_report (
    id BIGSERIAL PRIMARY KEY,
    report_code VARCHAR(100) UNIQUE NOT NULL,
    report_name VARCHAR(500) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    template_id BIGINT,
    requirement_id BIGINT,
    execution_task_id BIGINT,
    report_content TEXT,
    report_summary TEXT,
    report_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    generate_config TEXT,
    file_url VARCHAR(1000),
    file_format VARCHAR(50),
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publish_time TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES test_report_template(id),
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id),
    FOREIGN KEY (execution_task_id) REFERENCES test_execution_task(id)
);

-- 测试报告模板表索引
CREATE INDEX IF NOT EXISTS idx_report_template_code ON test_report_template(template_code);
CREATE INDEX IF NOT EXISTS idx_report_template_type ON test_report_template(template_type);
CREATE INDEX IF NOT EXISTS idx_report_template_active ON test_report_template(is_active);

-- 测试报告表索引
CREATE INDEX IF NOT EXISTS idx_report_code ON test_report(report_code);
CREATE INDEX IF NOT EXISTS idx_report_type ON test_report(report_type);
CREATE INDEX IF NOT EXISTS idx_report_status ON test_report(report_status);
CREATE INDEX IF NOT EXISTS idx_report_requirement_id ON test_report(requirement_id);
CREATE INDEX IF NOT EXISTS idx_report_template_id ON test_report(template_id);
CREATE INDEX IF NOT EXISTS idx_report_execution_task_id ON test_report(execution_task_id);
CREATE INDEX IF NOT EXISTS idx_report_create_time ON test_report(create_time);

-- ============================================
-- 测试覆盖分析相关表（第12-13周：测试覆盖分析）
-- ============================================

-- 测试覆盖分析表
CREATE TABLE IF NOT EXISTS test_coverage_analysis (
    id BIGSERIAL PRIMARY KEY,
    analysis_code VARCHAR(100) UNIQUE NOT NULL,
    analysis_name VARCHAR(500) NOT NULL,
    requirement_id BIGINT,
    coverage_type VARCHAR(50) NOT NULL,
    total_items INT,
    covered_items INT,
    coverage_rate DECIMAL(5,2),
    uncovered_items TEXT,
    coverage_details TEXT,
    analysis_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    analyzer_id BIGINT,
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id)
);

-- 测试覆盖分析表索引
CREATE INDEX IF NOT EXISTS idx_coverage_analysis_code ON test_coverage_analysis(analysis_code);
CREATE INDEX IF NOT EXISTS idx_coverage_analysis_requirement_id ON test_coverage_analysis(requirement_id);
CREATE INDEX IF NOT EXISTS idx_coverage_analysis_type ON test_coverage_analysis(coverage_type);
CREATE INDEX IF NOT EXISTS idx_coverage_analysis_time ON test_coverage_analysis(analysis_time);

-- ============================================
-- 测试规约相关表（第15-16周：测试规约管理）
-- ============================================

-- 测试规约表
CREATE TABLE IF NOT EXISTS test_specification (
    id BIGSERIAL PRIMARY KEY,
    spec_code VARCHAR(100) UNIQUE NOT NULL,
    spec_name VARCHAR(500) NOT NULL,
    spec_type VARCHAR(50) NOT NULL,
    spec_category VARCHAR(100),
    spec_description TEXT,
    spec_content TEXT,
    applicable_modules VARCHAR(500),
    applicable_layers VARCHAR(500),
    applicable_methods VARCHAR(500),
    current_version VARCHAR(50),
    is_active CHAR(1) DEFAULT '1',
    effective_date DATE,
    expire_date DATE,
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 1
);

-- 规约版本管理表
CREATE TABLE IF NOT EXISTS spec_version (
    id BIGSERIAL PRIMARY KEY,
    spec_id BIGINT NOT NULL,
    version_number VARCHAR(50) NOT NULL,
    version_name VARCHAR(200),
    version_description TEXT,
    spec_content TEXT,
    change_log TEXT,
    is_current CHAR(1) DEFAULT '0',
    created_by BIGINT,
    created_by_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spec_id) REFERENCES test_specification(id),
    UNIQUE(spec_id, version_number)
);

-- 字段测试要点表
CREATE TABLE IF NOT EXISTS field_test_point (
    id BIGSERIAL PRIMARY KEY,
    point_code VARCHAR(100) UNIQUE NOT NULL,
    spec_id BIGINT,
    field_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(50),
    test_requirement TEXT,
    test_method VARCHAR(100),
    test_cases TEXT,
    validation_rules TEXT,
    is_required CHAR(1) DEFAULT '0',
    is_active CHAR(1) DEFAULT '1',
    display_order INT,
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spec_id) REFERENCES test_specification(id)
);

-- 逻辑测试要点表
CREATE TABLE IF NOT EXISTS logic_test_point (
    id BIGSERIAL PRIMARY KEY,
    point_code VARCHAR(100) UNIQUE NOT NULL,
    spec_id BIGINT,
    logic_name VARCHAR(200) NOT NULL,
    logic_type VARCHAR(50),
    logic_description TEXT,
    test_requirement TEXT,
    test_method VARCHAR(100),
    test_cases TEXT,
    validation_rules TEXT,
    applicable_scenarios TEXT,
    is_active CHAR(1) DEFAULT '1',
    display_order INT,
    creator_id BIGINT,
    creator_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spec_id) REFERENCES test_specification(id)
);

-- 测试规约表索引
CREATE INDEX IF NOT EXISTS idx_spec_code ON test_specification(spec_code);
CREATE INDEX IF NOT EXISTS idx_spec_type ON test_specification(spec_type);
CREATE INDEX IF NOT EXISTS idx_spec_active ON test_specification(is_active);
CREATE INDEX IF NOT EXISTS idx_spec_create_time ON test_specification(create_time);

-- 规约版本管理表索引
CREATE INDEX IF NOT EXISTS idx_spec_version_spec_id ON spec_version(spec_id);
CREATE INDEX IF NOT EXISTS idx_spec_version_number ON spec_version(version_number);
CREATE INDEX IF NOT EXISTS idx_spec_version_current ON spec_version(is_current);
CREATE INDEX IF NOT EXISTS idx_spec_version_create_time ON spec_version(create_time);

-- 字段测试要点表索引
CREATE INDEX IF NOT EXISTS idx_field_point_code ON field_test_point(point_code);
CREATE INDEX IF NOT EXISTS idx_field_point_spec_id ON field_test_point(spec_id);
CREATE INDEX IF NOT EXISTS idx_field_point_field_name ON field_test_point(field_name);
CREATE INDEX IF NOT EXISTS idx_field_point_active ON field_test_point(is_active);

-- 逻辑测试要点表索引
CREATE INDEX IF NOT EXISTS idx_logic_point_code ON logic_test_point(point_code);
CREATE INDEX IF NOT EXISTS idx_logic_point_spec_id ON logic_test_point(spec_id);
CREATE INDEX IF NOT EXISTS idx_logic_point_logic_name ON logic_test_point(logic_name);
CREATE INDEX IF NOT EXISTS idx_logic_point_active ON logic_test_point(is_active);

