import request from './request'

export interface QualityScore {
  totalScore: number
  completenessScore: number
  standardizationScore: number
  executabilityScore: number
  qualityLevel: string
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

export const testCaseQualityApi = {
  // Assess Quality
  assessQuality(caseId: number) {
    return request.get<any, QualityScore>(`/v1/test-case-quality/assess/${caseId}`)
  },

  // Check Completeness
  checkCompleteness(caseId: number) {
    return request.get<any, CompletenessScore>(`/v1/test-case-quality/completeness/${caseId}`)
  },

  // Check Standardization
  checkStandardization(caseId: number) {
    return request.get<any, StandardizationScore>(`/v1/test-case-quality/standardization/${caseId}`)
  }
}
