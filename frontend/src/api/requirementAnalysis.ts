import request from './request'
import type { ApiResult, TestPoint, BusinessRule, KeyInfo, RequirementAnalysisResult, RequirementAnalysisRequest } from './types'

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