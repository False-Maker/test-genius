-- 提示词模板版本管理表
-- 用于存储提示词模板的历史版本

CREATE TABLE IF NOT EXISTS prompt_template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    version_name VARCHAR(200),
    version_description TEXT,
    template_content TEXT NOT NULL,
    template_variables TEXT,
    change_log TEXT,
    is_current VARCHAR(1) DEFAULT '0',
    created_by BIGINT,
    created_by_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES prompt_template(id) ON DELETE CASCADE,
    UNIQUE(template_id, version_number)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_prompt_template_version_template_id ON prompt_template_version(template_id);
CREATE INDEX IF NOT EXISTS idx_prompt_template_version_version_number ON prompt_template_version(version_number);
CREATE INDEX IF NOT EXISTS idx_prompt_template_version_is_current ON prompt_template_version(is_current);
CREATE INDEX IF NOT EXISTS idx_prompt_template_version_create_time ON prompt_template_version(create_time);

-- 添加注释
COMMENT ON TABLE prompt_template_version IS '提示词模板版本历史表';
COMMENT ON COLUMN prompt_template_version.id IS '主键ID';
COMMENT ON COLUMN prompt_template_version.template_id IS '模板ID';
COMMENT ON COLUMN prompt_template_version.version_number IS '版本号';
COMMENT ON COLUMN prompt_template_version.version_name IS '版本名称';
COMMENT ON COLUMN prompt_template_version.version_description IS '版本描述';
COMMENT ON COLUMN prompt_template_version.template_content IS '模板内容';
COMMENT ON COLUMN prompt_template_version.template_variables IS '模板变量定义（JSON格式）';
COMMENT ON COLUMN prompt_template_version.change_log IS '变更日志';
COMMENT ON COLUMN prompt_template_version.is_current IS '是否当前版本：1-是，0-否';
COMMENT ON COLUMN prompt_template_version.created_by IS '创建人ID';
COMMENT ON COLUMN prompt_template_version.created_by_name IS '创建人姓名';
COMMENT ON COLUMN prompt_template_version.create_time IS '创建时间';
