-- 模型配置表升级脚本
-- 添加智能模型选择相关字段
-- 第六阶段升级

-- 添加性能评分字段
ALTER TABLE model_config ADD COLUMN IF NOT EXISTS performance_score DECIMAL(5,2) DEFAULT 0.00;

-- 添加支持的任务类型字段
ALTER TABLE model_config ADD COLUMN IF NOT EXISTS task_types TEXT;

-- 添加是否推荐模型字段
ALTER TABLE model_config ADD COLUMN IF NOT EXISTS is_recommended CHAR(1) DEFAULT '0';

-- 添加最后评分更新时间字段
ALTER TABLE model_config ADD COLUMN IF NOT EXISTS last_score_update_time TIMESTAMP NULL;

-- 为新字段创建索引
CREATE INDEX IF NOT EXISTS idx_model_config_performance_score ON model_config(performance_score DESC);
CREATE INDEX IF NOT EXISTS idx_model_config_is_recommended ON model_config(is_recommended);

-- 更新现有数据：设置默认的任务类型支持
UPDATE model_config SET task_types = '["CASE_GENERATION", "UI_SCRIPT_GENERATION", "AGENT_CHAT", "KNOWLEDGE_RETRIEVAL", "DATA_EXTRACTION"]' WHERE task_types IS NULL;

