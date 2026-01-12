-- 初始化基础数据

-- 插入测试分层数据
INSERT INTO test_layer (layer_code, layer_name, layer_description, layer_order, is_active) VALUES
('INDIVIDUAL', '个人级测试', '针对个人用户相关功能的测试', 1, '1'),
('BUSINESS_CASE', '业务案例测试', '针对完整业务流程的端到端测试', 2, '1'),
('FUNCTIONAL_CASE', '功能案例测试', '针对具体功能模块的测试', 3, '1'),
('INTERFACE_CASE', '接口案例测试', '针对API接口的测试', 4, '1'),
('SCENARIO_CASE', '场景案例测试', '针对特定业务场景的测试', 5, '1')
ON CONFLICT (layer_code) DO NOTHING;

-- 插入测试设计方法数据
INSERT INTO test_design_method (method_code, method_name, method_description, applicable_layers, is_active) VALUES
('EQUIVALENCE_PARTITIONING', '等价类划分法', '将输入数据划分为等价类', 'FUNCTIONAL_CASE,INTERFACE_CASE', '1'),
('BOUNDARY_VALUE_ANALYSIS', '边界值分析法', '测试边界值', 'FUNCTIONAL_CASE,INTERFACE_CASE', '1'),
('SCENARIO_METHOD', '场景法', '基于业务场景设计用例', 'BUSINESS_CASE,SCENARIO_CASE', '1'),
('DECISION_TABLE', '决策表法', '基于规则组合设计用例', 'FUNCTIONAL_CASE', '1'),
('STATE_TRANSITION', '状态转换法', '基于状态转换设计用例', 'FUNCTIONAL_CASE,SCENARIO_CASE', '1')
ON CONFLICT (method_code) DO NOTHING;

-- 插入提示词模板初始化数据
INSERT INTO prompt_template (template_code, template_name, template_category, template_type, template_content, template_variables, applicable_layers, applicable_methods, applicable_modules, template_description, version, is_active, creator_id, create_time, update_time) VALUES
('TMP-20240101-001', '功能测试用例生成模板', '用例生成', 'CASE_GENERATION', 
'请根据以下需求信息生成测试用例：

需求名称：{requirementName}
需求描述：{requirementDescription}
业务模块：{businessModule}
测试分层：{layerName}
测试方法：{methodName}

请按照以下格式生成测试用例：
1. 用例名称：{caseName}
2. 前置条件：{preCondition}
3. 测试步骤：
   {testStep}
4. 预期结果：{expectedResult}

请确保用例覆盖正常场景、异常场景和边界场景。',
'{"requirementName":"需求名称","requirementDescription":"需求描述","businessModule":"业务模块","layerName":"测试分层","methodName":"测试方法","caseName":"用例名称","preCondition":"前置条件","testStep":"测试步骤","expectedResult":"预期结果"}',
'FUNCTIONAL_CASE,INTERFACE_CASE', 'EQUIVALENCE_PARTITIONING,BOUNDARY_VALUE_ANALYSIS', 'ALL',
'用于生成功能测试用例的标准模板', 1, '1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TMP-20240101-002', '业务场景测试用例生成模板', '用例生成', 'CASE_GENERATION',
'请根据以下业务需求生成场景测试用例：

需求名称：{requirementName}
需求描述：{requirementDescription}
业务模块：{businessModule}

请按照业务场景法生成测试用例，包括：
1. 主流程场景
2. 异常流程场景
3. 边界场景

每个用例应包含：
- 用例名称：{caseName}
- 前置条件：{preCondition}
- 测试步骤：{testStep}
- 预期结果：{expectedResult}',
'{"requirementName":"需求名称","requirementDescription":"需求描述","businessModule":"业务模块","caseName":"用例名称","preCondition":"前置条件","testStep":"测试步骤","expectedResult":"预期结果"}',
'BUSINESS_CASE,SCENARIO_CASE', 'SCENARIO_METHOD', 'ALL',
'用于生成业务场景测试用例的模板', 1, '1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TMP-20240101-003', '接口测试用例生成模板', '用例生成', 'CASE_GENERATION',
'请根据以下接口信息生成接口测试用例：

接口名称：{interfaceName}
接口描述：{interfaceDescription}
请求方法：{httpMethod}
请求路径：{requestPath}
请求参数：{requestParams}
响应格式：{responseFormat}

请生成以下类型的测试用例：
1. 正常请求测试
2. 参数校验测试
3. 异常场景测试
4. 边界值测试

每个用例应包含：
- 用例名称：{caseName}
- 前置条件：{preCondition}
- 测试步骤：{testStep}
- 预期结果：{expectedResult}',
'{"interfaceName":"接口名称","interfaceDescription":"接口描述","httpMethod":"请求方法","requestPath":"请求路径","requestParams":"请求参数","responseFormat":"响应格式","caseName":"用例名称","preCondition":"前置条件","testStep":"测试步骤","expectedResult":"预期结果"}',
'INTERFACE_CASE', 'EQUIVALENCE_PARTITIONING,BOUNDARY_VALUE_ANALYSIS', 'ALL',
'用于生成接口测试用例的模板', 1, '1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (template_code) DO NOTHING;

-- 插入模型配置初始化数据
INSERT INTO model_config (model_code, model_name, model_type, api_endpoint, api_key, model_version, max_tokens, temperature, is_active, priority, daily_limit, create_time, update_time) VALUES
('DEEPSEEK-001', 'DeepSeek Chat', 'DEEPSEEK', 'https://api.deepseek.com/v1/chat/completions', '', 'deepseek-chat', 4096, 0.7, '1', 1, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DOUBAO-001', '豆包大模型', 'DOUBAO', 'https://ark.cn-beijing.volces.com/api/v3/chat/completions', '', 'doubao-pro-4k', 4096, 0.7, '1', 2, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('KIMI-001', 'Kimi Chat', 'KIMI', 'https://api.moonshot.cn/v1/chat/completions', '', 'moonshot-v1-8k', 8192, 0.7, '1', 3, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('QIANWEN-001', '通义千问', 'QIANWEN', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation', '', 'qwen-turbo', 2000, 0.7, '1', 4, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (model_code) DO NOTHING;

