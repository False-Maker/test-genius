import request from './request'

// UI脚本生成相关类型定义
export interface UIScriptGenerationRequest {
  naturalLanguageDesc: string
  pageCodeUrl?: string
  pageElements?: Array<Record<string, any>>
  scriptType?: string // SELENIUM/PLAYWRIGHT
  scriptLanguage?: string // PYTHON/JAVA/JAVASCRIPT
  pageUrl?: string
  useLlm?: boolean
  requirementId?: number
  caseId?: number
  creatorId?: number
  creatorName?: string
}

export interface UIScriptGenerationResult {
  taskCode: string
  taskStatus: string // PENDING/PROCESSING/SUCCESS/FAILED
  progress?: number // 0-100
  scriptContent?: string
  scriptType?: string
  scriptLanguage?: string
  elementsUsed?: Array<Record<string, any>>
  steps?: Array<Record<string, any>>
  pageUrl?: string
  errorMessage?: string
  createTime?: string
  finishTime?: string
}

export interface PageElementInfo {
  id?: number
  elementCode?: string
  pageUrl?: string
  elementType?: string
  elementLocatorType?: string
  elementLocatorValue?: string
  elementText?: string
  elementAttributes?: Record<string, any>
  pageStructure?: Record<string, any>
  screenshotUrl?: string
}

// UI脚本生成API
export const uiScriptGenerationApi = {
  // 生成UI脚本
  generateScript(data: UIScriptGenerationRequest) {
    return request.post<UIScriptGenerationResult>('/v1/ui-script/generate', data)
  },

  // 查询生成任务状态
  getTaskStatus(taskCode: string) {
    return request.get<UIScriptGenerationResult>(`/v1/ui-script/tasks/${taskCode}`)
  },

  // 解析页面代码
  parsePageCode(pageCodeUrl: string) {
    return request.post<PageElementInfo[]>('/v1/ui-script/parse-page', null, {
      params: { pageCodeUrl }
    })
  }
}

