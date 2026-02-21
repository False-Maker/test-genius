import request from './request'
import type { ApiResult } from './types'

export interface SpecificationCheckRequestDTO {
    caseId: number
    specificationIds?: number[]
}

export interface SpecificationSummaryDTO {
    id: number
    specCode: string
    specName: string
    specType: string
    currentVersion: string
}

export interface ComplianceIssueDTO {
    specCode: string
    specName: string
    issueType: string
    issueDescription: string
    severity: string
    suggestion: string
}

export interface SpecificationCheckResponseDTO {
    isCompliant: boolean
    complianceScore: number
    totalChecks: number
    passedChecks: number
    failedChecks: number
    issues: ComplianceIssueDTO[]
    matchedSpecifications: SpecificationSummaryDTO[]
}

export interface EnhancedTestCaseDTO {
    caseCode: string
    caseName: string
    preCondition: string
    testStep: string
    expectedResult: string
}

export interface SpecificationInjectionResponseDTO {
    enhancedTestCase: EnhancedTestCaseDTO
    injectedContents: Record<string, unknown>
    appliedSpecs: SpecificationSummaryDTO[]
}

export interface SpecificationComplianceReportDTO {
    reportCode: string
    caseId: number
    caseCode: string
    caseName: string
    checkTime: string
    isCompliant: boolean
    complianceScore: number
    totalChecks: number
    passedChecks: number
    failedChecks: number
    issues: ComplianceIssueDTO[]
    matchedSpecifications: SpecificationSummaryDTO[]
    summary: string
}

export const specificationCheckApi = {
    // Match specifications
    matchSpecifications(caseId: number) {
        return request.get<ApiResult<SpecificationSummaryDTO[]>>(`/v1/specification-check/match/${caseId}`)
    },

    // Check compliance
    checkCompliance(data: SpecificationCheckRequestDTO) {
        return request.post<ApiResult<SpecificationCheckResponseDTO>>('/v1/specification-check/check', data)
    },

    // Inject specification
    injectSpecification(data: SpecificationCheckRequestDTO) {
        return request.post<ApiResult<SpecificationInjectionResponseDTO>>('/v1/specification-check/inject', data)
    },

    // Generate compliance report
    generateComplianceReport(data: SpecificationCheckRequestDTO) {
        return request.post<ApiResult<SpecificationComplianceReportDTO>>('/v1/specification-check/report', data)
    }
}
