import request from './request'
import type { PageResult } from './testReport'

export interface TestCoverageAnalysisRequestDTO {
    analysisName: string
    requirementId?: number
    coverageType: 'REQUIREMENT' | 'FUNCTION' | 'SCENARIO' | 'CODE'
    analyzerId?: number
}

export interface TestCoverageAnalysisResponseDTO {
    id: number
    analysisCode: string
    analysisName: string
    requirementId?: number
    coverageType: string
    totalItems?: number
    coveredItems?: number
    coverageRate?: number
    uncoveredItems?: string
    coverageDetails?: string
    analysisTime?: string
    analyzerId?: number
}

export const testCoverageApi = {
    // Analyze coverage (general)
    analyzeCoverage(data: TestCoverageAnalysisRequestDTO) {
        return request.post<any, TestCoverageAnalysisResponseDTO>('/v1/test-coverage/analyze', data)
    },

    // Analyze requirement coverage
    analyzeRequirementCoverage(requirementId: number) {
        return request.post<any, TestCoverageAnalysisResponseDTO>(`/v1/test-coverage/analyze/requirement/${requirementId}`)
    },

    // Analyze function coverage
    analyzeFunctionCoverage(requirementId: number) {
        return request.post<any, TestCoverageAnalysisResponseDTO>(`/v1/test-coverage/analyze/function/${requirementId}`)
    },

    // Analyze scenario coverage
    analyzeScenarioCoverage(requirementId: number) {
        return request.post<any, TestCoverageAnalysisResponseDTO>(`/v1/test-coverage/analyze/scenario/${requirementId}`)
    },

    // Analyze code coverage
    analyzeCodeCoverage(requirementId: number | undefined, coverageData: string) {
        return request.post<any, TestCoverageAnalysisResponseDTO>('/v1/test-coverage/analyze/code', coverageData, {
            params: { requirementId }
        })
    },

    // Get analysis list
    getAnalysisList(page: number = 0, size: number = 10) {
        return request.get<any, PageResult<TestCoverageAnalysisResponseDTO>>('/v1/test-coverage', {
            params: { page, size }
        })
    },

    // Get analysis by ID
    getAnalysisById(id: number) {
        return request.get<any, TestCoverageAnalysisResponseDTO>(`/v1/test-coverage/${id}`)
    },

    // Get analysis by requirement ID
    getAnalysisByRequirementId(requirementId: number) {
        return request.get<any, TestCoverageAnalysisResponseDTO[]>(`/v1/test-coverage/requirement/${requirementId}`)
    },

    // Get analysis by code
    getAnalysisByCode(analysisCode: string) {
        return request.get<any, TestCoverageAnalysisResponseDTO>(`/v1/test-coverage/code/${analysisCode}`)
    },

    // Get analysis by coverage type
    getAnalysisByCoverageType(coverageType: string) {
        return request.get<any, TestCoverageAnalysisResponseDTO[]>(`/v1/test-coverage/type/${coverageType}`)
    },

    // Get coverage trend
    getCoverageTrend(requirementId?: number, coverageType?: string, days: number = 7) {
        return request.get<any, string>('/v1/test-coverage/trend', {
            params: { requirementId, coverageType, days }
        })
    },

    // Check coverage insufficiency
    checkCoverageInsufficiency(requirementId?: number, threshold: number = 80.0) {
        return request.get<any, string>('/v1/test-coverage/insufficiency', {
            params: { requirementId, threshold }
        })
    },

    // Generate coverage report
    generateCoverageReport(requirementId?: number, coverageType?: string) {
        return request.get<any, string>('/v1/test-coverage/report', {
            params: { requirementId, coverageType }
        })
    }
}
