-- ============================================
-- 工作流系统初始数据
-- 创建默认的用例生成工作流
-- ============================================

-- 用例生成工作流（默认）
INSERT INTO workflow_definition (
    workflow_code, workflow_name, workflow_description, workflow_type,
    workflow_config, version, is_active, is_default, execution_count
) VALUES (
    'WF-20260126-001',
    '用例生成工作流（默认）',
    '标准的用例生成工作流，包含需求分析、模板选择、提示词生成、模型调用、结果解析和用例保存',
    'CASE_GENERATION',
    '{
      "nodes": [
        {
          "id": "input_1",
          "type": "requirement_input",
          "name": "需求输入",
          "config": {}
        },
        {
          "id": "process_1",
          "type": "requirement_analysis",
          "name": "需求分析",
          "config": {}
        },
        {
          "id": "process_2",
          "type": "template_select",
          "name": "模板选择",
          "config": {}
        },
        {
          "id": "process_3",
          "type": "prompt_generate",
          "name": "提示词生成",
          "config": {}
        },
        {
          "id": "process_4",
          "type": "llm_call",
          "name": "模型调用",
          "config": {
            "model_code": "DEEPSEEK_CHAT"
          }
        },
        {
          "id": "process_5",
          "type": "result_parse",
          "name": "结果解析",
          "config": {
            "parse_type": "case"
          }
        },
        {
          "id": "output_1",
          "type": "case_save",
          "name": "用例保存",
          "config": {}
        }
      ],
      "edges": [
        {"source": "input_1", "target": "process_1"},
        {"source": "process_1", "target": "process_2"},
        {"source": "process_2", "target": "process_3"},
        {"source": "process_3", "target": "process_4"},
        {"source": "process_4", "target": "process_5"},
        {"source": "process_5", "target": "output_1"}
      ]
    }',
    1,
    true,
    true,
    0
) ON CONFLICT (workflow_code) DO NOTHING;
