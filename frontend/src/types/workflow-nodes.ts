/**
 * 工作流节点配置类型定义
 * 包含17个工作流节点组件的详细配置接口
 */

// 输入节点配置
export interface RequirementInputConfig {
  /** 输入方式 */
  inputType: 'text' | 'param'
  /** 默认内容（文本输入时使用） */
  defaultValue?: string
  /** 参数名称（参数传递时使用） */
  paramName?: string
}

export interface TestCaseInputConfig {
  /** 输入来源 */
  source: 'manual' | 'repository' | 'file'
  /** 用例内容（手动粘贴时使用） */
  content?: string
  /** 用例ID（用例库导入时使用） */
  caseId?: string
}

export interface FileUploadConfig {
  /** 接受的文件类型 */
  acceptTypes: string[]
  /** 最大文件大小（MB） */
  maxSize: number
  /** 解析模式 */
  parseMode: 'auto' | 'ocr' | 'text'
}

// 处理节点配置
export interface RequirementAnalysisConfig {
  /** 分析维度 */
  dimensions: ('functional' | 'non-functional' | 'security' | 'performance')[]
  /** 输出深度（1-5级） */
  depth: number
  /** 语言偏好 */
  language: 'zh' | 'en'
}

export interface TemplateSelectConfig {
  /** 测试类型 */
  testType: 'functional' | 'api' | 'ui' | 'security'
  /** 模板ID */
  templateId: string
  /** 自定义字段（JSON格式） */
  customFields?: string
}

export interface PromptGenerateConfig {
  /** 生成策略 */
  strategy: 'zero_shot' | 'few_shot' | 'cot'
  /** 是否注入上下文 */
  injectContext: boolean
  /** 提示词模板 */
  template: string
}

export interface LLMCallConfig {
  /** 模型代码 */
  model_code: string
  /** 温度参数（0-1） */
  temperature: number
  /** 最大Token数 */
  max_tokens: number
  /** 系统提示 */
  system_prompt?: string
}

export interface ResultParseConfig {
  /** 解析格式 */
  format: 'json' | 'markdown_table' | 'text' | 'xml'
  /** 提取规则 */
  rule?: string
  /** 失败处理策略 */
  onFailure: 'error' | 'raw' | 'default'
}

// 转换节点配置
export interface FormatTransformConfig {
  /** 源格式 */
  sourceFormat: 'json' | 'xml' | 'yaml' | 'csv'
  /** 目标格式 */
  targetFormat: 'json' | 'xml' | 'yaml' | 'html'
  /** 映射模板 */
  mappingTemplate?: string
}

export interface DataCleanConfig {
  /** 清洗规则 */
  rules: ('trim' | 'remove_empty' | 'remove_duplicates' | 'normalize_date')[]
  /** 敏感脱敏字段 */
  masking?: string[]
  /** 自定义清洗脚本 */
  customScript?: string
  /** 数据类型 */
  dataType?: 'json' | 'text' | 'csv'
}

export interface DataMergeConfig {
  /** 合并策略 */
  strategy: 'append' | 'key_merge' | 'override'
  /** 主键字段（按键合并时使用） */
  primaryKey?: string
  /** 冲突处理策略 */
  conflictResolution: 'use_first' | 'use_last' | 'error'
  /** 分隔符 */
  separator?: string
}

// 输出节点配置
export interface CaseSaveConfig {
  /** 保存目标 */
  target: 'case_repo' | 'temp_file' | 'api'
  /** 项目ID */
  projectId?: string
  /** 标签 */
  tags: string[]
}

export interface ReportGenerateConfig {
  /** 报告类型 */
  reportType: 'plan' | 'review' | 'execution'
  /** 包含章节 */
  sections: ('summary' | 'statistics' | 'details' | 'risks')[]
  /** 导出格式 */
  format: 'html' | 'pdf' | 'markdown'
}

export interface FileExportConfig {
  /** 文件格式 */
  fileFormat: 'xlsx' | 'csv' | 'json' | 'xmind'
  /** 文件名称 */
  fileName?: string
  /** 包含表头 */
  includeHeader: boolean
}

// 控制节点配置
export interface ConditionConfig {
  /** 判断条件 */
  condition: string
  /** 比较模式 */
  mode: 'expression' | 'script'
  /** 是否有默认分支 */
  hasDefaultBranch: boolean
}

export interface LoopConfig {
  /** 循环类型 */
  loopType: 'count' | 'collection' | 'while'
  /** 循环次数（按次数循环时使用） */
  count?: number
  /** 集合变量名（遍历集合时使用） */
  collectionVar?: string
  /** 终止条件（条件循环时使用） */
  condition?: string
  /** 最大迭代次数 */
  maxIterations: number
}

// 通用配置类型
export interface NodeConfigBase {
  /** 节点ID */
  id: string
  /** 节点名称 */
  name: string
  /** 节点描述 */
  description?: string
  /** 是否启用 */
  enabled: boolean
  /** 超时时间（秒） */
  timeout?: number
}

/**
 * 工作流节点配置联合类型
 * 可以用于处理任意类型的节点配置
 */
export type WorkflowNodeConfig = 
  | RequirementInputConfig
  | TestCaseInputConfig
  | FileUploadConfig
  | RequirementAnalysisConfig
  | TemplateSelectConfig
  | PromptGenerateConfig
  | LLMCallConfig
  | ResultParseConfig
  | FormatTransformConfig
  | DataCleanConfig
  | DataMergeConfig
  | CaseSaveConfig
  | ReportGenerateConfig
  | FileExportConfig
  | ConditionConfig
  | LoopConfig

/**
 * 节点类型枚举
 */
export enum NodeType {
  REQUIREMENT_INPUT = 'requirement_input',
  TEST_CASE_INPUT = 'test_case_input',
  FILE_UPLOAD = 'file_upload',
  REQUIREMENT_ANALYSIS = 'requirement_analysis',
  TEMPLATE_SELECT = 'template_select',
  PROMPT_GENERATE = 'prompt_generate',
  LLM_CALL = 'llm_call',
  RESULT_PARSE = 'result_parse',
  FORMAT_TRANSFORM = 'format_transform',
  DATA_CLEAN = 'data_clean',
  DATA_MERGE = 'data_merge',
  CASE_SAVE = 'case_save',
  REPORT_GENERATE = 'report_generate',
  FILE_EXPORT = 'file_export',
  CONDITION = 'condition',
  LOOP = 'loop'
}

/**
 * 节点配置映射类型
 * 根据节点类型获取对应的配置接口
 */
export interface NodeConfigMap {
  [NodeType.REQUIREMENT_INPUT]: RequirementInputConfig
  [NodeType.TEST_CASE_INPUT]: TestCaseInputConfig
  [NodeType.FILE_UPLOAD]: FileUploadConfig
  [NodeType.REQUIREMENT_ANALYSIS]: RequirementAnalysisConfig
  [NodeType.TEMPLATE_SELECT]: TemplateSelectConfig
  [NodeType.PROMPT_GENERATE]: PromptGenerateConfig
  [NodeType.LLM_CALL]: LLMCallConfig
  [NodeType.RESULT_PARSE]: ResultParseConfig
  [NodeType.FORMAT_TRANSFORM]: FormatTransformConfig
  [NodeType.DATA_CLEAN]: DataCleanConfig
  [NodeType.DATA_MERGE]: DataMergeConfig
  [NodeType.CASE_SAVE]: CaseSaveConfig
  [NodeType.REPORT_GENERATE]: ReportGenerateConfig
  [NodeType.FILE_EXPORT]: FileExportConfig
  [NodeType.CONDITION]: ConditionConfig
  [NodeType.LOOP]: LoopConfig
}