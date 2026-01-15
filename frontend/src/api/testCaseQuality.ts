import request from './request'
import type { ApiResult } from './types'

// 质量评分类型
export interface QualityScore {
  totalScore: number
  qualityLevel: string
  completenessScore: number
  standardizationScore: number
  executabilityScore: number
  details: {
    completeness: {
      preConditionScore: number
      testStepScore: number
      expectedResultScore: number
      basicInfoScore: number
    }
    standardization: {
      namingScore: number
      formatScore: number
      contentScore: number
    }
    executability: {
      stepClarityScore: number
      dataPreparationScore: number
      environmentDependencyScore: number
    }
  }
  suggestions: string[]
}

export interface CompletenessScore {
  totalScore: number
  preConditionScore: number
  testStepScore: number
  expectedResultScore: number
  basicInfoScore: number
  details: string[]
}

export interface StandardizationScore {
  totalScore: number
  namingScore: number
  formatScore: number
  contentScore: number
  details: string[]
}

// 用例质量评估API
export const testCaseQualityApi = {
  // 评估用例质量
  assessQuality(caseId: number) {
    return request.get<QualityScore>(`/v1/test-case-quality/assess/${caseId}`)
  },

  // 检查用例完整性
  checkCompleteness(caseId: number) {
    return request.get<CompletenessScore>(`/v1/test-case-quality/completeness/${caseId}`)
  },

  // 检查用例规范性
  checkStandardization(caseId: number) {
    return request.get<StandardizationScore>(`/v1/test-case-quality/standardization/${caseId}`)
  }
}

