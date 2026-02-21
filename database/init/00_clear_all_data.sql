-- 清除所有历史数据脚本
-- 注意：此脚本会删除所有业务数据，包括基础配置数据
-- 执行前请确保已备份重要数据

-- 禁用外键检查（PostgreSQL不支持，需要按顺序删除）
-- 按照外键依赖关系，从子表到父表依次删除

BEGIN;

-- ============================================
-- 第一步：删除所有子表数据（有外键依赖的表）
-- ============================================

DO $$
DECLARE
    r text;
BEGIN
    FOREACH r IN ARRAY ARRAY[
        'test_execution_record',
        'test_execution_task',
        'test_risk_assessment',
        'test_report',
        'test_coverage_analysis',
        'test_suite_case',
        'case_generation_task',
        'test_case',
        'spec_version',
        'field_test_point',
        'logic_test_point',
        'test_requirement',
        'test_specification',
        'test_case_suite',
        'audit_log',
        'knowledge_document',
        'page_element_info',
        'ui_script_template',
        'test_report_template',
        'prompt_template',
        'model_config',
        'test_layer',
        'test_design_method'
    ] LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name = r
        ) THEN
            EXECUTE 'TRUNCATE TABLE ' || quote_ident(r) || ' CASCADE';
        END IF;
    END LOOP;
END $$;

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
