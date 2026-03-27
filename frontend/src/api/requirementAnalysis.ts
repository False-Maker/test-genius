import request from './request'
import type { TestPoint, BusinessRule, RequirementAnalysisResult } from './types'

export type { RequirementAnalysisResult } from './types'

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
