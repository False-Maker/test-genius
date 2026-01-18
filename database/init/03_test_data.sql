-- 测试设计助手系统测试数据脚本
-- 为各功能模块创建测试数据
-- 注意：执行此脚本前，请确保已执行01_init_tables.sql和02_init_data.sql

-- ============================================
-- 1. 测试需求数据
-- ============================================
INSERT INTO test_requirement (requirement_code, requirement_name, requirement_type, requirement_description, requirement_status, business_module, creator_id, creator_name, create_time, update_time, version) VALUES
('REQ-20240117-001', '投保功能优化需求', '优化', 
'需求描述：
1. 优化投保流程，提升用户体验
2. 增加智能推荐保险产品功能
3. 优化投保信息填写界面
4. 增加投保进度实时显示

业务规则：
- 根据用户画像推荐合适的保险产品
- 投保信息支持自动填充
- 投保进度实时更新

异常场景：
- 推荐算法异常时使用默认推荐
- 自动填充失败时允许手动输入', 
'已通过', '投保', 1, '张三', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 1),
('REQ-20240117-002', '理赔审核流程优化', '优化',
'需求描述：
1. 优化理赔审核流程，缩短审核时间
2. 增加自动审核功能（小额理赔）
3. 优化审核界面，提升审核效率
4. 增加审核历史记录查询

业务规则：
- 金额小于1000元的理赔自动审核通过
- 金额大于1000元的理赔需要人工审核
- 审核时间不超过3个工作日

异常场景：
- 自动审核失败时转为人工审核
- 审核超时自动提醒', 
'审核中', '理赔', 1, '李四', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 1),
('REQ-20240117-003', '保单查询接口开发', '新功能',
'需求描述：
1. 开发保单查询RESTful API接口
2. 支持根据保单号、身份证号查询
3. 支持分页查询
4. 支持多条件组合查询

接口规范：
- 请求方法：GET
- 请求路径：/api/v1/policy/query
- 请求参数：policyNo、idCard、page、size
- 响应格式：JSON

业务规则：
- 必须提供保单号或身份证号之一
- 只能查询当前用户相关的保单
- 分页大小限制在1-100之间

异常场景：
- 参数缺失时返回400错误
- 保单不存在时返回404错误
- 无权限查询时返回403错误', 
'已通过', '保单', 1, '王五', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 1),
('REQ-20240117-004', '核保规则引擎优化', '优化',
'需求描述：
1. 优化核保规则引擎性能
2. 增加规则配置界面
3. 支持规则版本管理
4. 增加规则执行日志

业务规则：
- 核保规则支持动态配置
- 规则变更需要审核
- 规则执行结果需要记录

异常场景：
- 规则配置错误时回滚到上一版本
- 规则执行异常时记录日志并告警', 
'草稿', '核保', 1, '赵六', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP, 1),
('REQ-20240117-005', '保全业务功能开发', '新功能',
'需求描述：
1. 开发保全业务功能模块
2. 支持保单变更、退保、续保等操作
3. 支持在线申请和审核
4. 支持保全历史记录查询

业务规则：
- 保单变更需要审核通过后才能生效
- 退保需要满足退保条件
- 续保需要在保单到期前30天申请

异常场景：
- 不满足变更条件时提示错误
- 退保金额计算错误时记录日志
- 续保申请超时自动提醒', 
'已通过', '保全', 1, '孙七', CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 1)
ON CONFLICT (requirement_code) DO NOTHING;

-- ============================================
-- 2. 测试用例数据
-- ============================================
INSERT INTO test_case (case_code, case_name, requirement_id, layer_id, method_id, case_type, case_priority, pre_condition, test_step, expected_result, case_status, creator_id, creator_name, create_time, update_time, version) VALUES
('CASE-20240117-001', '投保功能-正常投保流程测试', 
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 '正常', '高',
 '1. 用户已登录系统
2. 用户已选择保险产品
3. 用户已填写投保人基本信息',
 '1. 进入投保页面
2. 填写投保人信息（姓名、身份证号、年龄30岁、手机号）
3. 填写被保险人信息（与被保险人关系：本人）
4. 选择保险产品（重疾险）
5. 选择保额（50万）
6. 上传身份证照片
7. 确认投保信息
8. 选择支付方式（微信支付）
9. 完成支付',
 '1. 投保信息填写成功
2. 身份证上传成功
3. 支付成功
4. 生成电子保单
5. 保单状态为"已生效"
6. 保单号格式正确',
 '已审核', 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 1),
('CASE-20240117-002', '投保功能-年龄边界值测试（最小值）',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'BOUNDARY_VALUE_ANALYSIS'),
 '边界', '中',
 '1. 用户已登录系统
2. 用户已选择保险产品',
 '1. 进入投保页面
2. 填写投保人年龄为17岁
3. 填写其他必填信息
4. 点击提交',
 '1. 系统提示"投保人年龄必须在18-65岁之间"
2. 无法提交投保申请
3. 年龄输入框高亮显示错误',
 '待审核', 1, '张三', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 1),
('CASE-20240117-003', '投保功能-年龄边界值测试（最大值）',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'BOUNDARY_VALUE_ANALYSIS'),
 '边界', '中',
 '1. 用户已登录系统
2. 用户已选择保险产品',
 '1. 进入投保页面
2. 填写投保人年龄为66岁
3. 填写其他必填信息
4. 点击提交',
 '1. 系统提示"投保人年龄必须在18-65岁之间"
2. 无法提交投保申请
3. 年龄输入框高亮显示错误',
 '待审核', 1, '张三', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 1),
('CASE-20240117-004', '理赔审核-自动审核功能测试',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 (SELECT id FROM test_layer WHERE layer_code = 'BUSINESS_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 '正常', '高',
 '1. 用户已登录系统
2. 用户已有有效保单
3. 理赔金额小于1000元',
 '1. 进入理赔申请页面
2. 填写理赔信息（理赔金额：800元）
3. 上传理赔材料（医疗发票、诊断证明）
4. 提交理赔申请
5. 等待自动审核',
 '1. 理赔申请提交成功
2. 系统自动审核通过
3. 理赔金额自动转账到指定账户
4. 理赔状态更新为"已完成"
5. 发送理赔完成通知',
 '已审核', 1, '李四', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('CASE-20240117-005', '保单查询接口-正常查询测试',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-003'),
 (SELECT id FROM test_layer WHERE layer_code = 'INTERFACE_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'EQUIVALENCE_PARTITIONING'),
 '正常', '高',
 '1. 系统中有有效的保单数据
2. 已获取有效的访问令牌',
 '1. 调用保单查询接口
2. 传入参数：policyNo=POL-20240117-001
3. 发送GET请求到/api/v1/policy/query
4. 接收响应',
 '1. 接口返回200状态码
2. 响应体包含保单详细信息
3. 保单信息格式正确（JSON格式）
4. 包含保单号、投保人、被保险人、产品名称、保额、保费、生效日期、到期日期、保单状态等字段',
 '已审核', 1, '王五', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('CASE-20240117-006', '保单查询接口-参数缺失测试',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-003'),
 (SELECT id FROM test_layer WHERE layer_code = 'INTERFACE_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'EQUIVALENCE_PARTITIONING'),
 '异常', '中',
 '1. 已获取有效的访问令牌',
 '1. 调用保单查询接口
2. 不传入任何参数
3. 发送GET请求到/api/v1/policy/query
4. 接收响应',
 '1. 接口返回400状态码
2. 响应体包含错误信息："必须提供保单号或身份证号之一"
3. 错误信息格式正确（JSON格式）',
 '已审核', 1, '王五', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 1)
ON CONFLICT (case_code) DO NOTHING;

-- ============================================
-- 3. 测试执行任务数据
-- ============================================
INSERT INTO test_execution_task (task_code, task_name, task_type, requirement_id, case_id, script_type, script_language, task_status, progress, success_count, fail_count, creator_id, creator_name, create_time, update_time, finish_time) VALUES
('TASK-20240117-001', '投保功能自动化测试执行', 'MANUAL_EXECUTION',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-001'),
 'SELENIUM', 'PYTHON', 'SUCCESS', 100, 5, 0,
 1, '张三', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('TASK-20240117-002', '理赔审核功能自动化测试执行', 'MANUAL_EXECUTION',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-004'),
 'PLAYWRIGHT', 'JAVASCRIPT', 'PROCESSING', 60, 2, 1,
 1, '李四', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),
('TASK-20240117-003', '保单查询接口自动化测试执行', 'MANUAL_EXECUTION',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-003'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-005'),
 'SELENIUM', 'PYTHON', 'PENDING', 0, 0, 0,
 1, '王五', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours', NULL)
ON CONFLICT (task_code) DO NOTHING;

-- ============================================
-- 4. 测试执行记录数据
-- ============================================
INSERT INTO test_execution_record (record_code, task_id, case_id, execution_type, execution_status, execution_result, execution_log, error_message, execution_duration, executed_by, executed_by_name, execution_time, finish_time) VALUES
('REC-20240117-001', 
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-001'),
 'AUTOMATED', 'SUCCESS',
 '{"result": "通过", "screenshot": "/screenshots/20240117/001.png", "duration": 45000}',
 '2024-01-17 10:00:00 - 开始执行测试用例
2024-01-17 10:00:05 - 打开投保页面
2024-01-17 10:00:10 - 填写投保人信息
2024-01-17 10:00:15 - 选择保险产品
2024-01-17 10:00:20 - 完成支付
2024-01-17 10:00:45 - 验证保单生成成功
2024-01-17 10:00:45 - 测试用例执行完成',
 NULL, 45000, 1, '张三', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('REC-20240117-002',
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-002'),
 'AUTOMATED', 'SUCCESS',
 '{"result": "通过", "screenshot": "/screenshots/20240117/002.png", "duration": 30000}',
 '2024-01-17 10:05:00 - 开始执行测试用例
2024-01-17 10:05:05 - 打开投保页面
2024-01-17 10:05:10 - 填写投保人年龄为17岁
2024-01-17 10:05:15 - 验证错误提示显示
2024-01-17 10:05:30 - 测试用例执行完成',
 NULL, 30000, 1, '张三', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('REC-20240117-003',
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-002'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-004'),
 'AUTOMATED', 'FAILED',
 '{"result": "失败", "screenshot": "/screenshots/20240117/003.png", "duration": 60000}',
 '2024-01-17 11:00:00 - 开始执行测试用例
2024-01-17 11:00:05 - 打开理赔申请页面
2024-01-17 11:00:10 - 填写理赔信息
2024-01-17 11:00:15 - 上传理赔材料
2024-01-17 11:00:20 - 提交理赔申请
2024-01-17 11:00:30 - 等待自动审核
2024-01-17 11:01:00 - 验证审核结果失败：审核状态未更新',
 '元素定位失败：无法找到审核状态元素', 60000, 1, '李四', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (record_code) DO NOTHING;

-- ============================================
-- 5. UI脚本模板数据
-- ============================================
INSERT INTO ui_script_template (template_code, template_name, template_type, script_language, template_content, template_variables, applicable_scenarios, template_description, version, is_active, creator_id, create_time, update_time) VALUES
('TMP-20240117-001', 'Selenium Python基础脚本模板', 'SELENIUM', 'PYTHON',
'from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

def test_{function_name}():
    driver = webdriver.Chrome()
    try:
        # 打开页面
        driver.get("{page_url}")
        
        # 执行操作
        {operations}
        
        # 验证结果
        {assertions}
    finally:
        driver.quit()',
'{"function_name": "函数名称", "page_url": "页面URL", "operations": "操作步骤", "assertions": "断言验证"}',
'适用于Selenium Python自动化测试的基础脚本模板', 'Selenium Python基础脚本模板，包含页面打开、操作执行、结果验证等基础功能', 1, '1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TMP-20240117-002', 'Playwright JavaScript基础脚本模板', 'PLAYWRIGHT', 'JAVASCRIPT',
'const { test, expect } = require("@playwright/test");

test("{test_name}", async ({ page }) => {
    // 打开页面
    await page.goto("{page_url}");
    
    // 执行操作
    {operations}
    
    // 验证结果
    {assertions}
});',
'{"test_name": "测试名称", "page_url": "页面URL", "operations": "操作步骤", "assertions": "断言验证"}',
'适用于Playwright JavaScript自动化测试的基础脚本模板', 'Playwright JavaScript基础脚本模板，包含页面打开、操作执行、结果验证等基础功能', 1, '1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (template_code) DO NOTHING;

-- ============================================
-- 6. 测试报告模板数据
-- ============================================
INSERT INTO test_report_template (template_code, template_name, template_type, template_content, template_variables, file_format, template_description, is_default, is_active, version, creator_id, create_time, update_time) VALUES
('TMP-20240117-003', '标准测试报告模板', 'STANDARD',
'# 测试报告

## 1. 测试概述
- 测试需求：{requirement_name}
- 测试时间：{test_time}
- 测试人员：{tester_name}
- 测试环境：{test_environment}

## 2. 测试统计
- 总用例数：{total_cases}
- 通过用例数：{passed_cases}
- 失败用例数：{failed_cases}
- 跳过用例数：{skipped_cases}
- 通过率：{pass_rate}%

## 3. 测试结果详情
{test_results}

## 4. 问题汇总
{issues}

## 5. 测试结论
{conclusion}',
'{"requirement_name": "需求名称", "test_time": "测试时间", "tester_name": "测试人员", "test_environment": "测试环境", "total_cases": "总用例数", "passed_cases": "通过用例数", "failed_cases": "失败用例数", "skipped_cases": "跳过用例数", "pass_rate": "通过率", "test_results": "测试结果详情", "issues": "问题汇总", "conclusion": "测试结论"}',
'WORD', '标准测试报告模板，适用于大多数测试场景', '1', '1', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TMP-20240117-004', '详细测试报告模板', 'DETAILED',
'# 详细测试报告

## 1. 测试概述
{overview}

## 2. 测试计划
{test_plan}

## 3. 测试执行
{test_execution}

## 4. 测试结果分析
{test_analysis}

## 5. 风险评估
{risk_assessment}

## 6. 改进建议
{recommendations}',
'{"overview": "测试概述", "test_plan": "测试计划", "test_execution": "测试执行", "test_analysis": "测试结果分析", "risk_assessment": "风险评估", "recommendations": "改进建议"}',
'PDF', '详细测试报告模板，包含完整的测试信息和分析', '0', '1', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (template_code) DO NOTHING;

-- ============================================
-- 7. 测试报告数据
-- ============================================
INSERT INTO test_report (report_code, report_name, report_type, template_id, requirement_id, execution_task_id, report_content, report_summary, report_status, file_format, creator_id, creator_name, create_time, update_time, publish_time) VALUES
('RPT-20240117-001', '投保功能测试报告', 'STANDARD',
 (SELECT id FROM test_report_template WHERE template_code = 'TMP-20240117-003'),
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-001'),
 '{"total_cases": 5, "passed_cases": 5, "failed_cases": 0, "skipped_cases": 0, "pass_rate": 100.0, "test_duration": 120000, "test_results": [{"case_code": "CASE-20240117-001", "case_name": "投保功能-正常投保流程测试", "status": "通过", "duration": 45000}, {"case_code": "CASE-20240117-002", "case_name": "投保功能-年龄边界值测试（最小值）", "status": "通过", "duration": 30000}]}',
 '测试执行完成，所有用例均通过，功能正常', 'PUBLISHED', 'WORD',
 1, '张三', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('RPT-20240117-002', '理赔审核功能测试报告', 'STANDARD',
 (SELECT id FROM test_report_template WHERE template_code = 'TMP-20240117-003'),
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-002'),
 '{"total_cases": 3, "passed_cases": 2, "failed_cases": 1, "skipped_cases": 0, "pass_rate": 66.67, "test_duration": 180000, "test_results": [{"case_code": "CASE-20240117-004", "case_name": "理赔审核-自动审核功能测试", "status": "失败", "duration": 60000, "error": "元素定位失败"}]}',
 '测试执行中，部分用例失败，需要修复', 'DRAFT', 'WORD',
 1, '李四', CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '12 hours', NULL)
ON CONFLICT (report_code) DO NOTHING;

-- ============================================
-- 8. 测试覆盖分析数据
-- ============================================
INSERT INTO test_coverage_analysis (analysis_code, analysis_name, requirement_id, coverage_type, total_items, covered_items, coverage_rate, uncovered_items, coverage_details, analyzer_id, analysis_time) VALUES
('COV-20240117-001', '投保功能需求覆盖分析', 
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 'REQUIREMENT', 10, 8, 80.00,
 '["投保信息自动填充功能", "投保进度实时显示功能"]',
 '{"coverage_details": [{"item": "正常投保流程", "covered": true, "case_count": 3}, {"item": "年龄边界值测试", "covered": true, "case_count": 2}, {"item": "保额边界值测试", "covered": true, "case_count": 1}, {"item": "支付流程测试", "covered": true, "case_count": 1}, {"item": "保单生成测试", "covered": true, "case_count": 1}, {"item": "投保信息自动填充功能", "covered": false, "case_count": 0}, {"item": "投保进度实时显示功能", "covered": false, "case_count": 0}]}',
 1, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('COV-20240117-002', '理赔审核功能覆盖分析',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 'FUNCTION', 8, 6, 75.00,
 '["审核历史记录查询功能", "审核超时提醒功能"]',
 '{"coverage_details": [{"item": "自动审核功能", "covered": true, "case_count": 1}, {"item": "人工审核功能", "covered": true, "case_count": 2}, {"item": "审核界面优化", "covered": true, "case_count": 1}, {"item": "小额理赔自动审核", "covered": true, "case_count": 1}, {"item": "大额理赔人工审核", "covered": true, "case_count": 1}, {"item": "审核历史记录查询功能", "covered": false, "case_count": 0}, {"item": "审核超时提醒功能", "covered": false, "case_count": 0}]}',
 1, CURRENT_TIMESTAMP - INTERVAL '12 hours')
ON CONFLICT (analysis_code) DO NOTHING;

-- ============================================
-- 9. 风险评估数据
-- ============================================
INSERT INTO test_risk_assessment (assessment_code, assessment_name, requirement_id, execution_task_id, risk_level, risk_score, risk_items, feasibility_score, feasibility_recommendation, assessment_details, assessor_id, assessment_time) VALUES
('RISK-20240117-001', '投保功能上线风险评估',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-001'),
 'LOW', 25.00,
 '{"risk_items": [{"type": "用例数量风险", "level": "LOW", "description": "用例数量充足，覆盖主要功能"}, {"type": "覆盖率风险", "level": "LOW", "description": "需求覆盖率达到80%，满足上线要求"}, {"type": "执行失败风险", "level": "LOW", "description": "测试执行通过率100%，无失败用例"}]}',
 95.00,
 '建议上线。测试覆盖充分，执行结果良好，风险较低。',
 '{"assessment_summary": "投保功能测试完成，所有用例通过，功能正常，建议上线", "coverage_analysis": "需求覆盖率达到80%，主要功能均已覆盖", "execution_analysis": "测试执行通过率100%，无失败用例", "risk_analysis": "风险等级为LOW，风险评分25分，上线可行性评分95分"}',
 1, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('RISK-20240117-002', '理赔审核功能上线风险评估',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 (SELECT id FROM test_execution_task WHERE task_code = 'TASK-20240117-002'),
 'MEDIUM', 55.00,
 '{"risk_items": [{"type": "用例数量风险", "level": "MEDIUM", "description": "用例数量基本充足，但部分功能未覆盖"}, {"type": "覆盖率风险", "level": "MEDIUM", "description": "需求覆盖率达到75%，部分功能未覆盖"}, {"type": "执行失败风险", "level": "MEDIUM", "description": "测试执行通过率66.67%，存在失败用例"}]}',
 65.00,
 '建议修复问题后上线。存在部分用例失败，需要修复后重新测试。',
 '{"assessment_summary": "理赔审核功能测试进行中，部分用例失败，需要修复", "coverage_analysis": "需求覆盖率达到75%，部分功能未覆盖", "execution_analysis": "测试执行通过率66.67%，存在失败用例，需要修复", "risk_analysis": "风险等级为MEDIUM，风险评分55分，上线可行性评分65分"}',
 1, CURRENT_TIMESTAMP - INTERVAL '12 hours')
ON CONFLICT (assessment_code) DO NOTHING;

-- ============================================
-- 10. 测试规约数据
-- ============================================
INSERT INTO test_specification (spec_code, spec_name, spec_type, spec_category, spec_description, spec_content, applicable_modules, applicable_layers, applicable_methods, current_version, is_active, effective_date, expire_date, creator_id, creator_name, create_time, update_time, version) VALUES
('SPEC-20240117-001', '投保业务测试规约', 'APPLICATION', '投保',
 '投保业务测试规约，定义投保功能测试的标准和要求',
 '{"spec_content": "投保业务测试规约内容，包括字段测试要点、逻辑测试要点等"}',
 '投保', 'FUNCTIONAL_CASE,BUSINESS_CASE', 'SCENARIO_METHOD,BOUNDARY_VALUE_ANALYSIS',
 'v1.0', '1', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year',
 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 1),
('SPEC-20240117-002', '理赔业务测试规约', 'APPLICATION', '理赔',
 '理赔业务测试规约，定义理赔功能测试的标准和要求',
 '{"spec_content": "理赔业务测试规约内容，包括字段测试要点、逻辑测试要点等"}',
 '理赔', 'FUNCTIONAL_CASE,BUSINESS_CASE', 'SCENARIO_METHOD,EQUIVALENCE_PARTITIONING',
 'v1.0', '1', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year',
 1, '李四', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 1),
('SPEC-20240117-003', '公共测试规约', 'PUBLIC', '通用',
 '公共测试规约，适用于所有业务模块的通用测试要求',
 '{"spec_content": "公共测试规约内容，包括通用字段测试要点、通用逻辑测试要点等"}',
 'ALL', 'ALL', 'ALL',
 'v1.0', '1', CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year',
 1, '王五', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 1)
ON CONFLICT (spec_code) DO NOTHING;

-- ============================================
-- 11. 规约版本数据
-- ============================================
INSERT INTO spec_version (spec_id, version_number, version_name, version_description, spec_content, change_log, is_current, created_by, created_by_name, create_time) VALUES
((SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-001'), 'v1.0', '初始版本', '投保业务测试规约初始版本',
 '{"spec_content": "投保业务测试规约v1.0内容"}', '初始版本创建', '1', 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days'),
((SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-002'), 'v1.0', '初始版本', '理赔业务测试规约初始版本',
 '{"spec_content": "理赔业务测试规约v1.0内容"}', '初始版本创建', '1', 1, '李四', CURRENT_TIMESTAMP - INTERVAL '4 days'),
((SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-003'), 'v1.0', '初始版本', '公共测试规约初始版本',
 '{"spec_content": "公共测试规约v1.0内容"}', '初始版本创建', '1', 1, '王五', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (spec_id, version_number) DO NOTHING;

-- ============================================
-- 12. 字段测试要点数据
-- ============================================
INSERT INTO field_test_point (point_code, spec_id, field_name, field_type, test_requirement, test_method, test_cases, validation_rules, is_required, is_active, display_order, creator_id, creator_name, create_time, update_time) VALUES
('FTP-20240117-001',
 (SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-001'),
 '投保人年龄', 'INTEGER',
 '投保人年龄必须在18-65岁之间',
 'BOUNDARY_VALUE_ANALYSIS',
 '1. 测试最小值边界：17岁（应提示错误）
2. 测试最小值有效值：18岁（应通过）
3. 测试最大值有效值：65岁（应通过）
4. 测试最大值边界：66岁（应提示错误）',
 '{"min": 18, "max": 65, "type": "integer"}',
 '1', '1', 1, 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('FTP-20240117-002',
 (SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-001'),
 '保额', 'DECIMAL',
 '保额不能超过产品规定的最高保额',
 'BOUNDARY_VALUE_ANALYSIS',
 '1. 测试保额为0（应提示错误）
2. 测试保额为产品最高保额（应通过）
3. 测试保额超过产品最高保额（应提示错误）',
 '{"min": 0, "type": "decimal", "max_depends_on": "product"}',
 '1', '1', 2, 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days')
ON CONFLICT (point_code) DO NOTHING;

-- ============================================
-- 13. 逻辑测试要点数据
-- ============================================
INSERT INTO logic_test_point (point_code, spec_id, logic_name, logic_type, logic_description, test_requirement, test_method, test_cases, validation_rules, applicable_scenarios, is_active, display_order, creator_id, creator_name, create_time, update_time) VALUES
('LTP-20240117-001',
 (SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-001'),
 '投保流程主逻辑', 'WORKFLOW',
 '投保流程主逻辑：填写信息 -> 选择产品 -> 选择保额 -> 上传材料 -> 确认信息 -> 支付 -> 生成保单',
 '必须覆盖投保流程的所有步骤',
 'SCENARIO_METHOD',
 '1. 正常投保流程测试
2. 投保信息填写不完整测试
3. 支付失败流程测试
4. 保单生成失败流程测试',
 '{"steps": ["填写信息", "选择产品", "选择保额", "上传材料", "确认信息", "支付", "生成保单"], "required_steps": ["填写信息", "选择产品", "支付"]}',
 '投保业务场景', '1', 1, 1, '张三', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('LTP-20240117-002',
 (SELECT id FROM test_specification WHERE spec_code = 'SPEC-20240117-002'),
 '理赔审核逻辑', 'BUSINESS_RULE',
 '理赔审核逻辑：金额小于1000元自动审核通过，金额大于1000元需要人工审核',
 '必须覆盖自动审核和人工审核两种场景',
 'SCENARIO_METHOD',
 '1. 小额理赔自动审核测试（金额<1000元）
2. 大额理赔人工审核测试（金额>=1000元）
3. 审核通过测试
4. 审核不通过测试',
 '{"auto_review_threshold": 1000, "auto_review_rule": "金额<1000元自动审核通过", "manual_review_rule": "金额>=1000元需要人工审核"}',
 '理赔业务场景', '1', 1, 1, '李四', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days')
ON CONFLICT (point_code) DO NOTHING;

-- ============================================
-- 14. 用例生成任务数据
-- ============================================
INSERT INTO case_generation_task (task_code, requirement_id, layer_id, method_id, template_id, model_code, task_status, progress, total_cases, success_cases, fail_cases, creator_id, create_time, update_time, complete_time) VALUES
('TASK-20240117-004',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 (SELECT id FROM prompt_template WHERE template_code = 'TMP-20240101-001'),
 'DEEPSEEK-001', 'SUCCESS', 100, 5, 5, 0,
 1, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days'),
('TASK-20240117-005',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-002'),
 (SELECT id FROM test_layer WHERE layer_code = 'BUSINESS_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 (SELECT id FROM prompt_template WHERE template_code = 'TMP-20240101-002'),
 'DOUBAO-001', 'SUCCESS', 100, 3, 3, 0,
 1, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('TASK-20240117-006',
 (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-20240117-003'),
 (SELECT id FROM test_layer WHERE layer_code = 'INTERFACE_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'EQUIVALENCE_PARTITIONING'),
 (SELECT id FROM prompt_template WHERE template_code = 'TMP-20240101-003'),
 'KIMI-001', 'PROCESSING', 60, 5, 3, 2,
 1, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL)
ON CONFLICT (task_code) DO NOTHING;

-- ============================================
-- 15. 知识库文档数据
-- ============================================
INSERT INTO knowledge_document (doc_code, doc_name, doc_type, doc_category, doc_content, doc_url, is_active, creator_id, create_time, update_time) VALUES
('DOC-20240117-001', '保险业务测试规范', '规范', '测试规范',
 '保险业务测试规范文档内容，包括测试标准、测试方法、测试要求等',
 '/docs/test-specification.pdf', '1', 1, CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '7 days'),
('DOC-20240117-002', '投保业务规则文档', '业务规则', '业务规则',
 '投保业务规则文档内容，包括投保规则、审核规则、支付规则等',
 '/docs/underwriting-rules.pdf', '1', 1, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days'),
('DOC-20240117-003', '理赔业务规则文档', '业务规则', '业务规则',
 '理赔业务规则文档内容，包括理赔规则、审核规则、支付规则等',
 '/docs/claim-rules.pdf', '1', 1, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days')
ON CONFLICT (doc_code) DO NOTHING;

-- ============================================
-- 16. 用例套件数据
-- ============================================
INSERT INTO test_case_suite (suite_code, suite_name, suite_description, creator_id, create_time, update_time) VALUES
('SUITE-20240117-001', '投保功能完整测试套件', '包含投保功能的所有测试用例', 1, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('SUITE-20240117-002', '理赔功能完整测试套件', '包含理赔功能的所有测试用例', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (suite_code) DO NOTHING;

-- ============================================
-- 17. 测试套件用例关联数据
-- ============================================
INSERT INTO test_suite_case (suite_id, case_id, case_order) VALUES
((SELECT id FROM test_case_suite WHERE suite_code = 'SUITE-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-001'), 1),
((SELECT id FROM test_case_suite WHERE suite_code = 'SUITE-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-002'), 2),
((SELECT id FROM test_case_suite WHERE suite_code = 'SUITE-20240117-001'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-003'), 3),
((SELECT id FROM test_case_suite WHERE suite_code = 'SUITE-20240117-002'),
 (SELECT id FROM test_case WHERE case_code = 'CASE-20240117-004'), 1)
ON CONFLICT DO NOTHING;

-- ============================================
-- 数据插入完成提示
-- ============================================
SELECT '测试数据插入完成！' AS message,
       (SELECT COUNT(*) FROM test_requirement) AS requirement_count,
       (SELECT COUNT(*) FROM test_case) AS case_count,
       (SELECT COUNT(*) FROM test_execution_task) AS execution_task_count,
       (SELECT COUNT(*) FROM test_execution_record) AS execution_record_count,
       (SELECT COUNT(*) FROM ui_script_template) AS script_template_count,
       (SELECT COUNT(*) FROM test_report_template) AS report_template_count,
       (SELECT COUNT(*) FROM test_report) AS report_count,
       (SELECT COUNT(*) FROM test_coverage_analysis) AS coverage_analysis_count,
       (SELECT COUNT(*) FROM test_risk_assessment) AS risk_assessment_count,
       (SELECT COUNT(*) FROM test_specification) AS specification_count,
       (SELECT COUNT(*) FROM field_test_point) AS field_test_point_count,
       (SELECT COUNT(*) FROM logic_test_point) AS logic_test_point,
       (SELECT COUNT(*) FROM case_generation_task) AS generation_task_count,
       (SELECT COUNT(*) FROM knowledge_document) AS knowledge_document_count,
       (SELECT COUNT(*) FROM test_case_suite) AS suite_count;

