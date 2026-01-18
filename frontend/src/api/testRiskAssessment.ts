import request from './request'
import type { PageResult } from './testReport'

export interface TestRiskAssessmentRequestDTO {
    assessmentName: string
    requirementId?: number
    executionTaskId?: number
    assessorId?: number
}

export interface TestRiskAssessmentResponseDTO {
    id: number
    assessmentCode: string
    assessmentName: string
    requirementId?: number
    executionTaskId?: number
    riskLevel: 'HIGH' | 'MEDIUM' | 'LOW'
    riskScore: number
    riskItems: string
    feasibilityScore: number
    feasibilityRecommendation: string
    assessmentDetails: string
    assessmentTime: string
    assessorId?: number
}

export const testRiskAssessmentApi = {
    // Assess risk (general)
    assessRisk(data: TestRiskAssessmentRequestDTO) {
        return request.post<any, TestRiskAssessmentResponseDTO>('/v1/test-risk-assessment/assess', data)
    },

    // Assess requirement risk
    assessRequirementRisk(requirementId: number) {
        return request.post<any, TestRiskAssessmentResponseDTO>(`/v1/test-risk-assessment/assess/requirement/${requirementId}`)
    },

    // Assess execution task risk
    assessExecutionTaskRisk(executionTaskId: number) {
        return request.post<any, TestRiskAssessmentResponseDTO>(`/v1/test-risk-assessment/assess/execution-task/${executionTaskId}`)
    },

    // Assess risk level
    assessRiskLevel(riskScore: number) {
        return request.get<any, string>('/v1/test-risk-assessment/assess/level', {
            params: { riskScore }
        })
    },

    // Assess feasibility
    assessFeasibility(requirementId?: number, executionTaskId?: number) {
        return request.get<any, number>('/v1/test-risk-assessment/assess/feasibility', {
            params: { requirementId, executionTaskId }
        })
    },

    // Identify risk items
    identifyRiskItems(requirementId?: number, executionTaskId?: number) {
        return request.get<any, string>('/v1/test-risk-assessment/identify/risk-items', {
            params: { requirementId, executionTaskId }
        })
    },

    // Get assessment list
    getAssessmentList(page: number = 0, size: number = 10) {
        return request.get<any, PageResult<TestRiskAssessmentResponseDTO>>('/v1/test-risk-assessment', {
            params: { page, size }
        })
    },

    // Get assessment details
    getAssessmentById(id: number) {
        return request.get<any, TestRiskAssessmentResponseDTO>(`/v1/test-risk-assessment/${id}`)
    },

    // Get assessment by code
    getAssessmentByCode(assessmentCode: string) {
        return request.get<any, TestRiskAssessmentResponseDTO>(`/v1/test-risk-assessment/code/${assessmentCode}`)
    },

    // Get assessments by requirement ID
    getAssessmentByRequirementId(requirementId: number) {
        return request.get<any, TestRiskAssessmentResponseDTO[]>(`/v1/test-risk-assessment/requirement/${requirementId}`)
    },

    // Get assessments by execution task ID
    getAssessmentByExecutionTaskId(executionTaskId: number) {
        return request.get<any, TestRiskAssessmentResponseDTO[]>(`/v1/test-risk-assessment/execution-task/${executionTaskId}`)
    },

    // Get assessments by risk level
    getAssessmentByRiskLevel(riskLevel: string) {
        return request.get<any, TestRiskAssessmentResponseDTO[]>(`/v1/test-risk-assessment/level/${riskLevel}`)
    }
}
