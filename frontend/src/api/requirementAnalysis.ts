import request from './request'
import { ApiResult } from './types'

// 测试要点类型
export interface TestPoint {
  name: string
  point: string
  description?: string
  priority?: string
}

// 业务规则类型
export interface BusinessRule {
  name: string
  rule: string
  description?: string
  type?: string
}

// 关键信息类型
export interface KeyInfo {
  [key: string]: unknown
}

// 需求分析结果类型
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

// 需求分析请求类型
export interface RequirementAnalysisRequest {
  requirementId: number
  keywords?: string[]
  analyzeDepth?: 'basic' | 'detailed' | 'comprehensive'
  includeTestPoints?: boolean
  includeBusinessRules?: boolean
  includeKeywords?: boolean
}

// 需求分析API
export const requirementAnalysisApi = {
  // 分析需求
  analyzeRequirement(requirementId: number) {
    return request.post<RequirementAnalysisResult>(`/v1/requirements/${requirementId}/analyze`)
  },

  // 分析需求（带请求参数）
  analyzeRequirementWithParams(params: RequirementAnalysisRequest) {
    return request.post<RequirementAnalysisResult>(`/v1/requirements/analyze`, params)
  },

  // 提取测试要点
  getTestPoints(requirementId: number) {
    return request.get<TestPoint[]>(`/v1/requirements/${requirementId}/test-points`)
  },

  // 提取业务规则
  getBusinessRules(requirementId: number) {
    return request.get<BusinessRule[]>(`/v1/requirements/${requirementId}/business-rules`)
  },

  // 获取需求分析历史
  getAnalysisHistory(requirementId: number) {
    return request.get<ApiResult<RequirementAnalysisResult[]>>(`/v1/requirements/${requirementId}/analysis-history`)
  }
}