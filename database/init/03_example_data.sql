-- 示例数据
-- 用于演示和测试系统功能

-- 插入示例需求数据
INSERT INTO test_requirement (requirement_code, requirement_name, requirement_description, requirement_type, requirement_status, creator_id, create_time, update_time) VALUES
('REQ-EXAMPLE-001', '投保功能需求', 
'需求描述：
1. 用户可以通过APP或Web端进行投保操作
2. 支持在线填写投保信息，包括：投保人信息、被保险人信息、受益人信息、保险产品选择、保额选择等
3. 支持上传身份证、银行卡等附件
4. 支持在线支付保费
5. 投保成功后生成电子保单

业务规则：
- 投保人年龄必须在18-65岁之间
- 保额不能超过产品规定的最高保额
- 必须填写完整的投保人和被保险人信息
- 支付成功后保单立即生效

异常场景：
- 年龄不符合要求时提示错误
- 保额超限时提示错误
- 必填信息缺失时提示错误
- 支付失败时回滚投保信息', 
'功能需求', 'APPROVED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REQ-EXAMPLE-002', '理赔申请功能需求',
'需求描述：
1. 用户可以通过APP或Web端提交理赔申请
2. 支持上传理赔相关材料（医疗发票、诊断证明、身份证等）
3. 支持在线填写理赔信息，包括：出险时间、出险原因、理赔金额等
4. 支持查看理赔进度
5. 理赔审核通过后自动转账到指定账户

业务规则：
- 理赔申请必须在出险后30天内提交
- 理赔金额不能超过保单保额
- 必须上传完整的理赔材料
- 理赔审核需要3-5个工作日

异常场景：
- 超过30天提交时提示错误
- 理赔金额超限时提示错误
- 材料不完整时提示错误
- 审核不通过时通知用户并说明原因',
'功能需求', 'APPROVED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('REQ-EXAMPLE-003', '保单查询接口',
'需求描述：
1. 提供RESTful API接口供外部系统调用
2. 支持根据保单号查询保单详情
3. 支持根据投保人身份证号查询保单列表
4. 支持根据保单状态筛选查询
5. 返回JSON格式的保单信息

接口规范：
- 请求方法：GET
- 请求路径：/api/v1/policy/query
- 请求参数：policyNo（保单号，必填）或 idCard（身份证号，必填）
- 响应格式：JSON

业务规则：
- 必须提供保单号或身份证号之一
- 只能查询当前用户相关的保单
- 保单信息包含：保单号、投保人、被保险人、产品名称、保额、保费、生效日期、到期日期、保单状态等

异常场景：
- 参数缺失时返回400错误
- 保单不存在时返回404错误
- 无权限查询时返回403错误
- 系统异常时返回500错误',
'接口需求', 'APPROVED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (requirement_code) DO NOTHING;

-- 插入示例用例数据（基于需求REQ-EXAMPLE-001）
INSERT INTO test_case (case_code, requirement_id, layer_id, method_id, case_name, case_type, case_priority, pre_condition, test_step, expected_result, case_status, version, creator_id, create_time, update_time) VALUES
('CASE-EXAMPLE-001', (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-EXAMPLE-001'), 
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 '投保功能-正常投保流程', '正常', '高',
 '1. 用户已登录系统
2. 用户已选择保险产品
3. 用户已填写投保人基本信息',
 '1. 进入投保页面
2. 填写投保人信息（姓名、身份证号、年龄25岁、手机号）
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
6. 保单号格式正确（如：POL-20240101-001）',
 'DRAFT', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CASE-EXAMPLE-002', (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-EXAMPLE-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'BOUNDARY_VALUE_ANALYSIS'),
 '投保功能-年龄边界值测试', '边界', '中',
 '1. 用户已登录系统
2. 用户已选择保险产品',
 '1. 进入投保页面
2. 填写投保人年龄为17岁
3. 填写其他必填信息
4. 点击提交',
 '1. 系统提示"投保人年龄必须在18-65岁之间"
2. 无法提交投保申请
3. 年龄输入框高亮显示错误',
 'DRAFT', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CASE-EXAMPLE-003', (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-EXAMPLE-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'BOUNDARY_VALUE_ANALYSIS'),
 '投保功能-保额边界值测试', '边界', '中',
 '1. 用户已登录系统
2. 用户已选择保险产品（最高保额100万）',
 '1. 进入投保页面
2. 填写投保人信息（年龄25岁）
3. 选择保额为101万
4. 填写其他必填信息
5. 点击提交',
 '1. 系统提示"保额不能超过产品规定的最高保额100万"
2. 无法提交投保申请
3. 保额输入框高亮显示错误',
 'DRAFT', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (case_code) DO NOTHING;

-- 插入示例用例生成任务数据
INSERT INTO case_generation_task (task_code, requirement_id, layer_id, method_id, template_id, model_code, task_status, progress, total_cases, success_cases, fail_cases, creator_id, create_time, update_time, complete_time) VALUES
('TASK-EXAMPLE-001', (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-EXAMPLE-001'),
 (SELECT id FROM test_layer WHERE layer_code = 'FUNCTIONAL_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 (SELECT id FROM prompt_template WHERE template_code = 'TMP-20240101-001'),
 'DEEPSEEK-001', 'SUCCESS', 100, 5, 5, 0, 1, 
 CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
('TASK-EXAMPLE-002', (SELECT id FROM test_requirement WHERE requirement_code = 'REQ-EXAMPLE-002'),
 (SELECT id FROM test_layer WHERE layer_code = 'BUSINESS_CASE'),
 (SELECT id FROM test_design_method WHERE method_code = 'SCENARIO_METHOD'),
 (SELECT id FROM prompt_template WHERE template_code = 'TMP-20240101-002'),
 'DOUBAO-001', 'SUCCESS', 100, 3, 3, 0, 1,
 CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour')
ON CONFLICT (task_code) DO NOTHING;

