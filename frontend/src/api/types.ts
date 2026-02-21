// 统一响应结果类型
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp?: number
}

/**
 * 分页结果类型
 * @template T - 页面数据项类型
 */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

/**
 * 常用响应类型
 */
export interface EmptyResponse {}

export interface IdResponse {
  id: number
}

export interface MessageResponse {
  message: string
}

export interface SuccessResponse {
  success: boolean
}

// ============================================
// 需求相关类型定义
// ============================================

/**
 * 需求基本信息
 */
export interface TestRequirement {
  id?: number
  requirementCode?: string
  requirementName: string
  requirementType?: string
  requirementDescription?: string
  requirementDocUrl?: string
  requirementStatus?: string
  businessModule?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  version?: number
}

/**
 * 需求查询参数
 */
export interface RequirementListParams {
  page?: number
  size?: number
  requirementName?: string
  requirementStatus?: string
}

// ============================================
// 测试用例相关类型定义
// ============================================

/**
 * 测试用例信息
 */
export interface TestCase {
  id?: number
  caseCode?: string
  caseName: string
  requirementId?: number
  layerId?: number
  methodId?: number
  caseType?: string
  casePriority?: string
  preCondition?: string
  testStep?: string
  expectedResult?: string
  caseStatus?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  version?: number
}

/**
 * 测试用例查询参数
 */
export interface TestCaseListParams {
  page?: number
  size?: number
  caseName?: string
  caseStatus?: string
  requirementId?: number
}

/**
 * 测试用例导入结果
 */
export interface TestCaseImportResult {
  successCount: number
  failCount: number
  errors: Array<{
    row: number
    message: string
  }>
}

// ============================================
// 需求分析相关类型定义
// ============================================

/**
 * 测试要点类型
 */
export interface TestPoint {
  name: string
  point: string
  description?: string
  priority?: string
}

/**
 * 业务规则类型
 */
export interface BusinessRule {
  name: string
  rule: string
  description?: string
  type?: string
}

/**
 * 关键信息类型
 */
export interface KeyInfo {
  [key: string]: unknown
}

/**
 * 需求分析结果类型
 */
export interface RequirementAnalysisResult {
  requirementId: number
  requirementName: string
  requirementText?: string
  keywords: string[]
  contentLength: number
  sentenceCount: number
  analysisTime: string
  testPoints: TestPoint[]
  businessRules: BusinessRule[]
  keyInfo?: KeyInfo
}

/**
 * 需求分析请求类型
 */
export interface RequirementAnalysisRequest {
  requirementId: number
  keywords?: string[]
  analyzeDepth?: 'basic' | 'detailed' | 'comprehensive'
  includeTestPoints?: boolean
  includeBusinessRules?: boolean
  includeKeywords?: boolean
}

// ============================================
// 文档相关类型定义
// ============================================

/**
 * 文档信息
 */
export interface DataDocument {
  id?: number
  documentName: string
  documentType?: string
  documentUrl?: string
  fileSize?: number
  mimeType?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 文档查询参数
 */
export interface DocumentListParams {
  page?: number
  size?: number
  documentName?: string
  documentType?: string
}

/**
 * 文档上传结果
 */
export interface FileUploadResult {
  success: boolean
  fileId: string
  fileName: string
  fileSize: number
  fileUrl: string
  message?: string
}

// ============================================
// 测试报告相关类型定义
// ============================================

/**
 * 测试报告信息
 */
export interface TestReport {
  id?: number
  reportName: string
  reportType?: string
  testExecutionId?: number
  totalCases: number
  passedCases: number
  failedCases: number
  skippedCases: number
  successRate: number
  executionTime?: string
  reportUrl?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试报告查询参数
 */
export interface ReportListParams {
  page?: number
  size?: number
  reportName?: string
  reportType?: string
  testExecutionId?: number
}

/**
 * 测试报告模板信息
 */
export interface TestReportTemplate {
  id?: number
  templateName: string
  templateContent: string
  templateType?: string
  isDefault?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试报告模板查询参数
 */
export interface ReportTemplateListParams {
  page?: number
  size?: number
  templateName?: string
  templateType?: string
}

// ============================================
// 测试执行相关类型定义
// ============================================

/**
 * 测试执行信息
 */
export interface TestExecution {
  id?: number
  executionName: string
  executionStatus?: 'pending' | 'running' | 'completed' | 'failed' | 'cancelled'
  testPlanId?: number
  totalCases: number
  executedCases: number
  passedCases: number
  failedCases: number
  skippedCases: number
  startTime?: string
  endTime?: string
  duration?: string
  executorId?: number
  executorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试执行查询参数
 */
export interface ExecutionListParams {
  page?: number
  size?: number
  executionName?: string
  executionStatus?: string
  testPlanId?: number
}

/**
 * 测试执行结果
 */
export interface TestExecutionResult {
  executionId: number
  caseId: number
  result: 'passed' | 'failed' | 'skipped' | 'error'
  executionTime?: string
  errorMessage?: string
  screenshotUrl?: string
  logs?: string
}

// ============================================
// 测试覆盖率相关类型定义
// ============================================

/**
 * 测试覆盖率信息
 */
export interface TestCoverage {
  id?: number
  coverageName: string
  coverageType?: string
  totalLines: number
  coveredLines: number
  coveragePercentage: number
  module?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试覆盖率查询参数
 */
export interface CoverageListParams {
  page?: number
  size?: number
  coverageName?: string
  coverageType?: string
  module?: string
}

/**
 * 代码覆盖率详情
 */
export interface CodeCoverageDetail {
  filePath: string
  totalLines: number
  coveredLines: number
  coveragePercentage: number
  lines: Array<{
    lineNumber: number
    isCovered: boolean
    content?: string
  }>
}

// ============================================
// 测试风险评估相关类型定义
// ============================================

/**
 * 测试风险信息
 */
export interface TestRiskAssessment {
  id?: number
  riskName: string
  riskType?: 'high' | 'medium' | 'low'
  riskLevel?: 'critical' | 'high' | 'medium' | 'low'
  riskDescription?: string
  mitigationStrategy?: string
  status?: 'open' | 'mitigated' | 'closed'
  assigneeId?: number
  assigneeName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试风险评估查询参数
 */
export interface RiskListParams {
  page?: number
  size?: number
  riskName?: string
  riskType?: string
  riskLevel?: string
  status?: string
}

/**
 * 风险评估结果
 */
export interface RiskAssessmentResult {
  riskId: number
  riskScore: number
  riskFactors: Array<{
    factor: string
    weight: number
    score: number
  }>
  recommendations: string[]
}

// ============================================
// 测试规范相关类型定义
// ============================================

/**
 * 测试规范信息
 */
export interface TestSpecification {
  id?: number
  specificationName: string
  specificationType?: string
  specificationContent: string
  version?: string
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 测试规范查询参数
 */
export interface SpecificationListParams {
  page?: number
  size?: number
  specificationName?: string
  specificationType?: string
  isActive?: boolean
}

// ============================================
// 用例生成相关类型定义
// ============================================

/**
 * 用例生成参数
 */
export interface TestCaseGenerationParams {
  requirementId: number
  generationType?: 'manual' | 'ai' | 'template'
  templateId?: number
  priority?: 'high' | 'medium' | 'low'
  includePreconditions?: boolean
  includeExpectedResults?: boolean
  maxCases?: number
}

/**
 * 用例生成结果
 */
export interface TestCaseGenerationResult {
  generatedCases: TestCase[]
  totalCases: number
  generationTime: string
  generationMethod: string
  suggestions?: string[]
}

/**
 * 用例重用参数
 */
export interface TestCaseReuseParams {
  sourceRequirementId: number
  targetRequirementId: number
  reuseStrategy?: 'full' | 'partial' | 'adapt'
  adaptationRules?: string[]
}

/**
 * 用例重用结果
 */
export interface TestCaseReuseResult {
  reusedCases: TestCase[]
  adaptedCases: TestCase[]
  totalCases: number
  adaptationRatio: number
  suggestions?: string[]
}

// ============================================
// LLM调用相关类型定义
// ============================================

/**
 * LLM调用配置
 */
export interface LLMConfig {
  model: string
  temperature?: number
  maxTokens?: number
  topP?: number
  frequencyPenalty?: number
  presencePenalty?: number
}

/**
 * LLM调用请求
 */
export interface LLMRequest {
  prompt: string
  context?: string
  config?: LLMConfig
}

/**
 * LLM调用响应
 */
export interface LLMResponse {
  response: string
  usage: {
    promptTokens: number
    completionTokens: number
    totalTokens: number
  }
  model: string
  timestamp: string
}

/**
 * LLM调用结果
 */
export interface LLMResult {
  success: boolean
  response?: LLMResponse
  error?: string
  executionTime: string
}

// ============================================
// 工作流相关类型定义
// ============================================

/**
 * 工作流节点
 */
export interface WorkflowNode {
  id?: number
  nodeId: string
  nodeType: 'start' | 'end' | 'requirement' | 'test_case' | 'report' | 'approval' | 'custom'
  nodeName: string
  description?: string
  config?: Record<string, unknown>
  nextNodes?: string[]
  previousNodes?: string[]
}

/**
 * 工作流定义
 */
export interface WorkflowDefinition {
  id?: number
  workflowName: string
  workflowType?: string
  description?: string
  nodes: WorkflowNode[]
  edges: Array<{
    id?: string
    source: string
    target: string
    condition?: string
  }>
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 工作流执行参数
 */
export interface WorkflowExecutionParams {
  workflowId: number
  startNodeId?: string
  inputData?: Record<string, unknown>
  variables?: Record<string, unknown>
}

/**
 * 工作流执行结果
 */
export interface WorkflowExecutionResult {
  executionId: string
  status: 'running' | 'completed' | 'failed' | 'cancelled'
  startTime: string
  endTime?: string
  duration?: string
  result?: Record<string, unknown>
  errors?: string[]
}

// ============================================
// 监控相关类型定义
// ============================================

/**
 * 监控指标
 */
export interface MonitoringMetric {
  metricName: string
  metricValue: number
  timestamp: string
  unit?: string
  tags?: Record<string, string>
}

/**
 * 监控数据
 */
export interface MonitoringData {
  timestamp: string
  metrics: MonitoringMetric[]
  systemInfo?: {
    cpuUsage: number
    memoryUsage: number
    diskUsage: number
    networkIn: number
    networkOut: number
  }
}

/**
 * 监控查询参数
 */
export interface MonitoringQueryParams {
  startTime?: string
  endTime?: string
  metricNames?: string[]
  interval?: '1m' | '5m' | '15m' | '30m' | '1h' | '6h' | '12h' | '24h'
}

// ============================================
// 模型配置相关类型定义
// ============================================

/**
 * 模型配置信息
 */
export interface ModelConfig {
  id?: number
  modelName: string
  modelType?: string
  modelProvider?: string
  apiKey?: string
  baseUrl?: string
  maxTokens?: number
  temperature?: number
  isActive?: boolean
  isDefault?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 模型配置查询参数
 */
export interface ModelConfigListParams {
  page?: number
  size?: number
  modelName?: string
  modelType?: string
  isActive?: boolean
}

// ============================================
// 规范检查相关类型定义
// ============================================

/**
 * 规范检查结果
 */
export interface SpecificationCheckResult {
  checkId: string
  documentId: number
  specificationId: number
  totalChecks: number
  passedChecks: number
  failedChecks: number
  checkDetails: Array<{
    ruleId: string
    ruleName: string
    ruleDescription: string
    passed: boolean
    message?: string
    location?: string
  }>
  overallScore: number
  executionTime: string
}

/**
 * 规范检查参数
 */
export interface SpecificationCheckParams {
  documentId: number
  specificationIds?: number[]
  checkLevel?: 'basic' | 'strict' | 'comprehensive'
  includeDetails?: boolean
}

// ============================================
// 知识库相关类型定义
// ============================================

/**
 * 知识条目
 */
export interface KnowledgeBaseItem {
  id?: number
  title: string
  content: string
  category?: string
  tags?: string[]
  relevanceScore?: number
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 知识库查询参数
 */
export interface KnowledgeBaseQueryParams {
  query?: string
  category?: string
  tags?: string[]
  minRelevance?: number
  limit?: number
}

/**
 * 知识库搜索结果
 */
export interface KnowledgeSearchResult {
  items: KnowledgeBaseItem[]
  total: number
  query: string
  searchTime: string
}

// ============================================
// 智能模型相关类型定义
// ============================================

/**
 * 智能模型信息
 */
export interface IntelligentModel {
  id?: number
  modelName: string
  modelType?: string
  description?: string
  capabilities?: string[]
  version?: string
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 智能模型查询参数
 */
export interface IntelligentModelListParams {
  page?: number
  size?: number
  modelName?: string
  modelType?: string
  isActive?: boolean
}

/**
 * 智能模型调用结果
 */
export interface IntelligentModelResult {
  modelId: number
  input: string
  output: string
  confidence: number
  processingTime: string
  metadata?: Record<string, unknown>
}

// ============================================
// UI脚本相关类型定义
// ============================================

/**
 * UI脚本模板
 */
export interface UIScriptTemplate {
  id?: number
  templateName: string
  templateContent: string
  templateType?: string
  browserType?: string
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * UI脚本模板查询参数
 */
export interface UIScriptTemplateListParams {
  page?: number
  size?: number
  templateName?: string
  templateType?: string
  browserType?: string
}

/**
 * UI脚本生成参数
 */
export interface UIScriptGenerationParams {
  requirementId: number
  templateId?: number
  browserType?: string
  includeAssertions?: boolean
  generateComments?: boolean
}

/**
 * UI脚本生成结果
 */
export interface UIScriptGenerationResult {
  scriptContent: string
  scriptType: string
  browserType: string
  testCases: TestCase[]
  executionTime: string
  suggestions?: string[]
}

/**
 * UI脚本修复参数
 */
export interface UIScriptRepairParams {
  scriptId: number
  scriptContent: string
  issues?: string[]
  browserType?: string
}

/**
 * UI脚本修复结果
 */
export interface UIScriptRepairResult {
  repairedContent: string
  fixedIssues: string[]
  newIssues?: string[]
  repairTime: string
}

// ============================================
// 页面元素相关类型定义
// ============================================

/**
 * 页面元素信息
 */
export interface PageElement {
  id?: number
  elementName: string
  selector: string
  elementType?: string
  pageUrl?: string
  elementPath?: string
  attributes?: Record<string, string>
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 页面元素查询参数
 */
export interface PageElementListParams {
  page?: number
  size?: number
  elementName?: string
  elementType?: string
  pageUrl?: string
}

// ============================================
// 流程文档相关类型定义
// ============================================

/**
 * 流程文档信息
 */
export interface FlowDocument {
  id?: number
  documentName: string
  documentType?: string
  processFlow: string
  description?: string
  isActive?: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 流程文档查询参数
 */
export interface FlowDocumentListParams {
  page?: number
  size?: number
  documentName?: string
  documentType?: string
  isActive?: boolean
}

// ============================================
// 测试用例质量相关类型定义
// ============================================

/**
 * 测试用例质量评估结果
 */
export interface TestCaseQualityResult {
  caseId: number
  qualityScore: number
  qualityLevel: 'excellent' | 'good' | 'fair' | 'poor'
  assessmentDetails: Array<{
    criterion: string
    score: number
    weight: number
    description: string
  }>
  suggestions: string[]
  assessmentTime: string
}

/**
 * 测试用例质量评估参数
 */
export interface TestCaseQualityParams {
  caseIds?: number[]
  assessmentCriteria?: string[]
  includeDetails?: boolean
}

