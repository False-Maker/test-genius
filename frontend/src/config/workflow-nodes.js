// 节点类型定义
export const NODE_TYPES = {
  // 输入类
  REQUIREMENT_INPUT: 'requirement_input',
  TEST_CASE_INPUT: 'test_case_input',
  FILE_UPLOAD: 'file_upload',

  // 处理类
  REQUIREMENT_ANALYSIS: 'requirement_analysis',
  TEMPLATE_SELECT: 'template_select',
  PROMPT_GENERATE: 'prompt_generate',
  LLM_CALL: 'llm_call',
  RESULT_PARSE: 'result_parse',

  // 转换类
  FORMAT_TRANSFORM: 'format_transform',
  DATA_CLEAN: 'data_clean',
  DATA_MERGE: 'data_merge',

  // 输出类
  CASE_SAVE: 'case_save',
  REPORT_GENERATE: 'report_generate',
  FILE_EXPORT: 'file_export',

  // 控制类
  CONDITION: 'condition',
  LOOP: 'loop'
}

// 节点配置定义
export const NODE_CONFIGS = {
  [NODE_TYPES.REQUIREMENT_INPUT]: {
    name: '需求输入',
    category: 'input',
    color: '#67C23A',
    description: '输入测试需求文本'
  },
  [NODE_TYPES.TEST_CASE_INPUT]: {
    name: '用例输入',
    category: 'input',
    color: '#67C23A',
    description: '输入现有测试用例'
  },
  [NODE_TYPES.FILE_UPLOAD]: {
    name: '文件上传',
    category: 'input',
    color: '#67C23A',
    description: '上传需求或数据文件'
  },

  [NODE_TYPES.REQUIREMENT_ANALYSIS]: {
    name: '需求分析',
    category: 'process',
    color: '#409EFF',
    description: '智能分析需求结构'
  },
  [NODE_TYPES.TEMPLATE_SELECT]: {
    name: '模板选择',
    category: 'process',
    color: '#409EFF',
    description: '选择用例生成模板'
  },
  [NODE_TYPES.PROMPT_GENERATE]: {
    name: '提示词生成',
    category: 'process',
    color: '#409EFF',
    description: '生成LLM提示词'
  },
  [NODE_TYPES.LLM_CALL]: {
    name: '模型调用',
    category: 'process',
    color: '#409EFF',
    description: '调用大模型生成内容'
  },
  [NODE_TYPES.RESULT_PARSE]: {
    name: '结果解析',
    category: 'process',
    color: '#409EFF',
    description: '解析模型返回结果'
  },

  [NODE_TYPES.FORMAT_TRANSFORM]: {
    name: '格式转换',
    category: 'transform',
    color: '#E6A23C',
    description: '数据格式转换'
  },
  [NODE_TYPES.DATA_CLEAN]: {
    name: '数据清洗',
    category: 'transform',
    color: '#E6A23C',
    description: '清洗无效数据'
  },
  [NODE_TYPES.DATA_MERGE]: {
    name: '数据合并',
    category: 'transform',
    color: '#E6A23C',
    description: '合并多源数据'
  },

  [NODE_TYPES.CASE_SAVE]: {
    name: '用例保存',
    category: 'output',
    color: '#F56C6C',
    description: '保存用例到数据库'
  },
  [NODE_TYPES.REPORT_GENERATE]: {
    name: '报告生成',
    category: 'output',
    color: '#F56C6C',
    description: '生成测试分析报告'
  },
  [NODE_TYPES.FILE_EXPORT]: {
    name: '文件导出',
    category: 'output',
    color: '#F56C6C',
    description: '导出结果文件'
  },

  [NODE_TYPES.CONDITION]: {
    name: '条件判断',
    category: 'control',
    color: '#909399',
    description: '逻辑分支判断'
  },
  [NODE_TYPES.LOOP]: {
    name: '循环节点',
    category: 'control',
    color: '#909399',
    description: '循环执行流程'
  }
}

// 节点分类列表（用于左侧面板）
export const NODE_CATEGORIES = [
  { key: 'input', title: '输入节点' },
  { key: 'process', title: '处理节点' },
  { key: 'transform', title: '转换节点' },
  { key: 'output', title: '输出节点' },
  { key: 'control', title: '控制节点' }
]
