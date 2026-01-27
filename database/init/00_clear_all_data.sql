-- 清除所有历史数据脚本
-- 注意：此脚本会删除所有业务数据，包括基础配置数据
-- 执行前请确保已备份重要数据

-- 禁用外键检查（PostgreSQL不支持，需要按顺序删除）
-- 按照外键依赖关系，从子表到父表依次删除

BEGIN;

-- ============================================
-- 第一步：删除所有子表数据（有外键依赖的表）
-- ============================================

-- 删除测试执行记录（依赖 test_execution_task, test_case）
TRUNCATE TABLE test_execution_record CASCADE;

-- 删除测试执行任务（依赖 test_requirement, test_case）
TRUNCATE TABLE test_execution_task CASCADE;

-- 删除风险评估（依赖 test_requirement, test_execution_task）
TRUNCATE TABLE test_risk_assessment CASCADE;

-- 删除测试报告（依赖 test_report_template, test_requirement, test_execution_task）
TRUNCATE TABLE test_report CASCADE;

-- 删除测试覆盖分析（依赖 test_requirement）
TRUNCATE TABLE test_coverage_analysis CASCADE;

-- 删除测试套件用例关联（依赖 test_case_suite, test_case）
TRUNCATE TABLE test_suite_case CASCADE;

-- 删除用例生成任务（依赖 test_requirement, prompt_template）
TRUNCATE TABLE case_generation_task CASCADE;

-- 删除测试用例（依赖 test_requirement）
TRUNCATE TABLE test_case CASCADE;

-- 删除规约版本管理（依赖 test_specification）
TRUNCATE TABLE spec_version CASCADE;

-- 删除字段测试要点（依赖 test_specification）
TRUNCATE TABLE field_test_point CASCADE;

-- 删除逻辑测试要点（依赖 test_specification）
TRUNCATE TABLE logic_test_point CASCADE;

-- ============================================
-- 第二步：删除主表数据
-- ============================================

-- 删除测试需求
TRUNCATE TABLE test_requirement CASCADE;

-- 删除测试规约
TRUNCATE TABLE test_specification CASCADE;

-- 删除测试套件
TRUNCATE TABLE test_case_suite CASCADE;

-- 删除审计日志
TRUNCATE TABLE audit_log CASCADE;

-- 删除知识库文档
TRUNCATE TABLE knowledge_document CASCADE;

-- 删除页面元素信息
TRUNCATE TABLE page_element_info CASCADE;

-- 删除UI脚本模板
TRUNCATE TABLE ui_script_template CASCADE;

-- 删除测试报告模板
TRUNCATE TABLE test_report_template CASCADE;

-- 删除提示词模板
TRUNCATE TABLE prompt_template CASCADE;

-- 删除模型配置
TRUNCATE TABLE model_config CASCADE;

-- 删除测试分层
TRUNCATE TABLE test_layer CASCADE;

-- 删除测试设计方法
TRUNCATE TABLE test_design_method CASCADE;

-- ============================================
-- 第三步：重置序列（可选，如果需要ID从1开始）
-- ============================================

-- 重置所有序列
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP
        EXECUTE 'ALTER SEQUENCE ' || r.sequence_name || ' RESTART WITH 1';
    END LOOP;
END $$;

COMMIT;

-- 显示清除结果
SELECT '所有历史数据已清除完成！' AS message;
