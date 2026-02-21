import request from './request'
import type { ApiResult } from './types'

export interface QualityScore {
  totalScore: number
  completenessScore: number
  standardizationScore: number
  executabilityScore: number
  qualityLevel: string
  details: QualityScoreDetails
  suggestions?: string[]
}

export interface CompletenessScore {
  totalScore: number
  preConditionScore: number
  testStepScore: number
  expectedResultScore: number
  basicInfoScore: number
}

export interface StandardizationScore {
  totalScore: number
  namingScore: number
  formatScore: number
  contentScore: number
}

export interface ExecutabilityScore {
  totalScore: number
  stepClarityScore: number
  dataPreparationScore: number
  environmentDependencyScore: number
}

export interface QualityScoreDetails {
  completeness: CompletenessScore
  standardization: StandardizationScore
  executability: ExecutabilityScore
}

export const testCaseQualityApi = {
// Assess Quality
  assessQuality(caseId: number) {
    return request.get<ApiResult<QualityScore>>(`/v1/test-case-quality/assess/${caseId}`)
  },

  // Check Completeness
  checkCompleteness(caseId: number) {
    return request.get<ApiResult<CompletenessScore>>(`/v1/test-case-quality/completeness/${caseId}`)
  },

  // Check Standardization
  checkStandardization(caseId: number) {
    return request.get<ApiResult<StandardizationScore>>(`/v1/test-case-quality/standardization/${caseId}`)
  }
}
