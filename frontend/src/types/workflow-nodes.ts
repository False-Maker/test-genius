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
  /** Input source: 'manual' for manual paste, 'repository' for case library, 'file' for file upload */
  source: 'manual' | 'repository' | 'file'
  /** Manual content when source is 'manual' */
  content?: string
  /** Case ID when source is 'repository' */
  caseId?: string
}

export interface FileUploadConfig {
  /** Accept file types ['.pdf', '.docx', '.txt', '.md'] */
  acceptTypes: ('.pdf' | '.docx' | '.txt' | '.md')[]
  /** Maximum file size in MB */
  maxSize: number
  /** Parse mode: 'auto' for automatic detection, 'ocr' for OCR recognition, 'text' for text extraction */
  parseMode: 'auto' | 'ocr' | 'text'
}

// 处理节点配置
export interface RequirementAnalysisConfig {
  /** Analysis dimensions to include: 'functional' for functional requirements, 'non-functional' for non-functional requirements, 'security' for security requirements, 'performance' for performance requirements */
  dimensions: ('functional' | 'non-functional' | 'security' | 'performance')[]
  /** Analysis depth level (1-5) */
  depth: number
  /** Language preference for output: 'zh' for Chinese, 'en' for English */
  language: 'zh' | 'en'
}

export interface TemplateSelectConfig {
  /** Test type: 'functional' for functional testing, 'api' for API testing, 'ui' for UI automation, 'security' for security testing */
  testType: 'functional' | 'api' | 'ui' | 'security'
  /** Template ID for the selected template */
  templateId?: string
  /** Custom fields in JSON format for template customization */
  customFields?: string
}

export interface PromptGenerateConfig {
  /** Generation strategy: 'zero_shot' for direct generation, 'few_shot' for few-shot learning, 'cot' for chain-of-thought */
  strategy: 'zero_shot' | 'few_shot' | 'cot'
  /** Whether to inject knowledge base context */
  injectContext: boolean
  /** Custom prompt template with {{variable}} placeholders */
  template?: string
}

export interface LLMCallConfig {
  /** Model code to use for LLM calls: 'DEEPSEEK_CHAT', 'GPT4', 'CLAUDE_35' */
  model_code: 'DEEPSEEK_CHAT' | 'GPT4' | 'CLAUDE_35'
  /** Temperature parameter for creativity (0-1) */
  temperature: number
  /** Maximum number of tokens */
  max_tokens: number
  /** System-level prompt template */
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
  /** Data cleaning rules to apply: 'trim' for whitespace, 'remove_empty' for empty fields, 'remove_duplicates' for duplicates, 'normalize_date' for date standardization */
  rules: ('trim' | 'remove_empty' | 'remove_duplicates' | 'normalize_date')[]
  /** Data masking options for sensitive information: 'phone' for phone numbers, 'id_card' for ID cards, 'email' for emails, 'name' for names */
  masking?: ('phone' | 'id_card' | 'email' | 'name')[]
  /** Custom cleaning script in JavaScript */
  customScript?: string
}

export interface DataMergeConfig {
  /** Merge strategy: 'append' for sequential concatenation, 'key_merge' for merge by key, 'override' for value override */
  strategy: 'append' | 'key_merge' | 'override'
  /** Primary key field when strategy is 'key_merge' */
  primaryKey?: string
  /** Conflict resolution strategy: 'use_first' to keep first value, 'use_last' to use last value, 'error' to throw error */
  conflictResolution: 'use_first' | 'use_last' | 'error'
}

// 输出节点配置
export interface CaseSaveConfig {
  /** Save target: 'case_repo' for test case library, 'temp_file' for temporary files, 'api' for external API */
  target: 'case_repo' | 'temp_file' | 'api'
  /** Associated project ID */
  projectId?: string
  /** Tags for the saved test cases */
  tags: string[]
}

export interface ReportGenerateConfig {
  /** Report type: 'plan' for test plan report, 'review' for review report, 'execution' for execution report */
  reportType: 'plan' | 'review' | 'execution'
  /** Report sections to include: 'summary' for summary, 'statistics' for statistics, 'details' for details, 'risks' for risk analysis */
  sections: ('summary' | 'statistics' | 'details' | 'risks')[]
  /** Export format: 'html' for HTML, 'pdf' for PDF, 'markdown' for Markdown */
  format: 'html' | 'pdf' | 'markdown'
}

export interface FileExportConfig {
  /** File format: 'xlsx' for Excel, 'csv' for CSV, 'json' for JSON, 'xmind' for XMind */
  fileFormat: 'xlsx' | 'csv' | 'json' | 'xmind'
  /** File name pattern with support for {date} placeholder */
  fileName?: string
  /** Whether to include header row in exported files */
  includeHeader: boolean
}

// 控制节点配置
export interface ConditionConfig {
  /** Condition expression for evaluation */
  condition?: string
  /** Evaluation mode: 'expression' for expression evaluation, 'script' for script execution */
  mode: 'expression' | 'script'
  /** Whether to include a default branch */
  hasDefaultBranch: boolean
}

export interface LoopConfig {
  /** Loop type: 'count' for count-based loops, 'collection' for collection iteration, 'while' for conditional loops */
  loopType: 'count' | 'collection' | 'while'
  /** Loop count when loopType is 'count' */
  count?: number
  /** Collection variable name when loopType is 'collection' */
  collectionVar?: string
  /** Condition expression when loopType is 'while' */
  condition?: string
  /** Maximum number of iterations to prevent infinite loops */
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

// Additional config types to reach 17 total configuration interfaces

/**
 * Configuration for specification check nodes
 */
export interface SpecificationCheckConfig {
  /** Check level: 'BASIC' for basic validation, 'DETAILED' for comprehensive analysis */
  checkLevel: 'BASIC' | 'DETAILED'
  /** Whether to automatically fix issues found */
  autoFix: boolean
}

/**
 * Configuration for test coverage analysis nodes
 */
export interface TestCoverageAnalysisConfig {
  /** Coverage type: 'functional' for functional coverage, 'code' for code coverage, 'requirements' for requirements coverage */
  coverageType: 'functional' | 'code' | 'requirements'
  /** Baseline data for comparison */
  baselineData?: string
}

/**
 * Configuration for test risk assessment nodes
 */
export interface TestRiskAssessmentConfig {
  /** Risk assessment model to use: 'SEVERITY_IMPACT' for severity-impact matrix, 'PROBABILITY_IMPACT' for probability-impact matrix, 'CUSTOM_MATRIX' for custom risk matrix */
  riskModel: 'SEVERITY_IMPACT' | 'PROBABILITY_IMPACT' | 'CUSTOM_MATRIX'
  /** Whether to include mitigation suggestions */
  mitigationSuggestions: boolean
}

/**
 * Configuration for test case quality nodes
 */
export interface TestCaseQualityConfig {
  /** Quality metrics to evaluate: 'completeness' for completeness, 'traceability' for traceability, 'reusability' for reusability, 'maintainability' for maintainability */
  qualityMetrics: ('completeness' | 'traceability' | 'reusability' | 'maintainability')[]
  /** Benchmark data for comparison */
  benchmarkData?: string
}

/**
 * Configuration for test report template nodes
 */
export interface TestReportTemplateConfig {
  /** Template format: 'HTML' for HTML templates, 'DOCX' for Word templates */
  templateFormat: 'HTML' | 'DOCX'
  /** Report sections to include: 'executive_summary' for executive summary, 'test_coverage' for test coverage, 'test_results' for test results, 'defect_analysis' for defect analysis, 'recommendations' for recommendations */
  sections: ('executive_summary' | 'test_coverage' | 'test_results' | 'defect_analysis' | 'recommendations')[]
  /** Custom styles configuration */
  customStyles?: string
}

/**
 * Configuration for test specification nodes
 */
export interface TestSpecificationConfig {
  /** Specification format: 'JSON' for JSON specifications, 'MARKDOWN' for Markdown specifications */
  specFormat: 'JSON' | 'MARKDOWN'
  /** Whether to include test examples */
  includeExamples: boolean
}

/**
 * Configuration for data document nodes
 */
export interface DataDocumentConfig {
  /** Document format: 'PDF' for PDF documents, 'WORD' for Word documents, 'MARKDOWN' for Markdown */
  docFormat: 'PDF' | 'WORD' | 'MARKDOWN'
  /** Whether to include data dictionary */
  includeDataDictionary: boolean
}

/**
 * Configuration for UI script template nodes
 */
export interface UIScriptTemplateConfig {
  /** Script type: 'ELEMENTS' for element operations, 'PAGES' for page operations, 'WORKFLOWS' for workflow operations */
  scriptType: 'ELEMENTS' | 'PAGES' | 'WORKFLOWS'
  /** Target framework for script generation: 'SELENIUM' for Selenium, 'PLAYWRIGHT' for Playwright, 'CYPRESS' for Cypress, 'ROBOT_FRAMEWORK' for Robot Framework */
  targetFramework: 'SELENIUM' | 'PLAYWRIGHT' | 'CYPRESS' | 'ROBOT_FRAMEWORK'
  /** Locator strategy for element identification: 'CSS_SELECTOR' for CSS selectors, 'XPATH' for XPath, 'ID' for ID, 'TEXT' for text */
  locatorStrategy: 'CSS_SELECTOR' | 'XPATH' | 'ID' | 'TEXT'
}

/**
 * Configuration for UI script generation nodes
 */
export interface UIScriptGenerationConfig {
  /** Generation mode: 'MANUAL' for manual creation, 'RECORDING' for recording-based, 'TEMPLATE' for template-based */
  generationMode: 'MANUAL' | 'RECORDING' | 'TEMPLATE'
  /** Target application type: 'WEB' for web applications, 'MOBILE_WEB' for mobile web applications, 'DESKTOP' for desktop applications, 'HYBRID' for hybrid applications */
  targetApp: 'WEB' | 'MOBILE_WEB' | 'DESKTOP' | 'HYBRID'
  /** Recording strategy for automation: 'FULL' for full recording, 'SMART' for smart recording, 'COMPRESSED' for compressed recording */
  recordingStrategy: 'FULL' | 'SMART' | 'COMPRESSED'
}

/**
 * Configuration for UI script repair nodes
 */
export interface UIScriptRepairConfig {
  /** Repair mode: 'AUTO' for automatic repair, 'SEMIAUTO' for semi-automatic repair, 'MANUAL' for manual repair */
  repairMode: 'AUTO' | 'SEMIAUTO' | 'MANUAL'
  /** Fallback strategy when repair fails: 'SKIP' to skip problematic elements, 'LOG' to log the issue, 'ALERT' to show alert, 'ROLLBACK' to rollback to previous working state */
  fallbackStrategy: 'SKIP' | 'LOG' | 'ALERT' | 'ROLLBACK'
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
  | SpecificationCheckConfig
  | TestCoverageAnalysisConfig
  | TestRiskAssessmentConfig
  | TestCaseQualityConfig
  | TestReportTemplateConfig
  | TestSpecificationConfig
  | DataDocumentConfig
  | UIScriptTemplateConfig
  | UIScriptGenerationConfig
  | UIScriptRepairConfig

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
  // Additional node types can be added here as needed
}