import request from './request'
import type { ApiResult } from './types'

// 测试要点类型
export interface TestPoint {
  point: string
  description?: string
  priority?: string
}

// 业务规则类型
export interface BusinessRule {
  rule: string
  description?: string
  type?: string
}

// 需求分析结果类型
export interface RequirementAnalysisResult {
  requirementId: number
  requirementName: string
  keywords: string[]
  contentLength: number
  sentenceCount: number
  testPoints: TestPoint[]
  businessRules: BusinessRule[]
  analysisTime: string
}

// 需求分析API
export const requirementAnalysisApi = {
  // 分析需求
  analyzeRequirement(requirementId: number) {
    return request.post<RequirementAnalysisResult>(`/v1/requirements/${requirementId}/analyze`)
  },

  // 提取测试要点
  getTestPoints(requirementId: number) {
    return request.get<TestPoint[]>(`/v1/requirements/${requirementId}/test-points`)
  },

  // 提取业务规则
  getBusinessRules(requirementId: number) {
    return request.get<BusinessRule[]>(`/v1/requirements/${requirementId}/business-rules`)
  }
}

