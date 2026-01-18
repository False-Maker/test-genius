import request from './request'

// UI脚本修复请求类型
export interface UIScriptRepairRequest {
  scriptContent: string
  errorLog: string
  errorAnalysis?: Record<string, any>
  pageChanges?: Record<string, any>
  newPageCodeUrl?: string
  newPageElements?: Array<Record<string, any>>
  scriptType?: string // SELENIUM/PLAYWRIGHT
  scriptLanguage?: string // PYTHON/JAVA/JAVASCRIPT
  useLlm?: boolean
}

// UI脚本修复结果类型
export interface UIScriptRepairResult {
  repairedScript?: string
  errorAnalysis?: Record<string, any>
  pageChanges?: Record<string, any>
  changes?: Array<Record<string, any>>
  suggestions?: Array<string>
  errorMessage?: string
}

// 错误分析结果类型
export interface ErrorAnalysisResult {
  errorType?: string
  errorMessage?: string
  errorLocation?: string
  suggestions?: Array<string>
  affectedElements?: Array<Record<string, any>>
}

// 页面变化检测结果类型
export interface PageChangesResult {
  hasChanges?: boolean
  changes?: Array<Record<string, any>>
  affectedElements?: Array<Record<string, any>>
}

// UI脚本修复API
export const uiScriptRepairApi = {
  // 分析错误
  analyzeError(scriptContent: string, errorLog: string) {
    return request.post<ErrorAnalysisResult>('/v1/test-execution/ui-script/analyze-error', {
      scriptContent,
      errorLog
    })
  },

  // 检测页面变化
  detectPageChanges(
    oldPageCodeUrl: string,
    oldPageElements: Array<Record<string, any>>,
    newPageCodeUrl: string,
    newPageElements?: Array<Record<string, any>>,
    scriptLocators?: Array<Record<string, any>>
  ) {
    return request.post<PageChangesResult>('/v1/test-execution/ui-script/detect-page-changes', {
      oldPageCodeUrl,
      oldPageElements,
      newPageCodeUrl,
      newPageElements,
      scriptLocators
    })
  },

  // 修复UI脚本
  repairScript(data: UIScriptRepairRequest) {
    return request.post<UIScriptRepairResult>('/v1/test-execution/ui-script/repair', data)
  }
}

