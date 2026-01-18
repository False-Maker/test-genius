import request from './request'

// 用例生成相关类型定义
export interface CaseGenerationRequest {
  requirementId: number
  layerCode?: string
  methodCode?: string
  templateId?: number
  modelCode?: string
  creatorId?: number
}

export interface CaseGenerationResult {
  taskId: number
  status: string
  message?: string
}

export interface GenerationTask {
  id: number
  requirementId: number
  status: string
  progress?: number
  message?: string
  result?: any
  createTime?: string
  updateTime?: string
}

// 用例生成API
export const caseGenerationApi = {
  // 生成用例
  generateTestCases(data: CaseGenerationRequest) {
    return request.post<CaseGenerationResult>('/v1/case-generation/generate', data)
  },

  // 批量生成用例
  batchGenerateTestCases(data: CaseGenerationRequest[]) {
    return request.post<CaseGenerationResult>('/v1/case-generation/batch', data)
  },

  // 查询生成任务
  getGenerationTask(id: number) {
    return request.get<GenerationTask>(`/v1/case-generation/${id}`)
  }
}

