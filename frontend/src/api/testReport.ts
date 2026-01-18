import request from './request'

export interface TestReportRequestDTO {
    reportName: string
    reportType: 'EXECUTION' | 'COVERAGE' | 'QUALITY' | 'RISK'
    templateId?: number
    requirementId?: number
    executionTaskId?: number
    generateConfig?: string
    creatorId?: number
    creatorName?: string
}

export interface TestReportResponseDTO {
    id: number
    reportCode: string
    reportName: string
    reportType: string
    templateId?: number
    requirementId?: number
    executionTaskId?: number
    reportContent?: string
    reportSummary?: string
    reportStatus: 'DRAFT' | 'PUBLISHED'
    generateConfig?: string
    fileUrl?: string
    fileFormat?: string
    creatorId?: number
    creatorName?: string
    createTime?: string
    updateTime?: string
    publishTime?: string
}

export interface PageResult<T> {
    content: T[]
    totalElements: number
    totalPages: number
    size: number
    number: number
}

export const testReportApi = {
    // Generate report
    generateReport(data: TestReportRequestDTO) {
        return request.post<any, TestReportResponseDTO>('/v1/test-reports', data)
    },

    // Get report list
    getReportList(page: number = 0, size: number = 10) {
        return request.get<any, PageResult<TestReportResponseDTO>>('/v1/test-reports', {
            params: { page, size }
        })
    },

    // Get report detail
    getReportById(id: number) {
        return request.get<any, TestReportResponseDTO>(`/v1/test-reports/${id}`)
    },

    // Get report by code
    getReportByCode(reportCode: string) {
        return request.get<any, TestReportResponseDTO>(`/v1/test-reports/code/${reportCode}`)
    },

    // Get reports by requirement ID
    getReportsByRequirementId(requirementId: number) {
        return request.get<any, TestReportResponseDTO[]>(`/v1/test-reports/requirement/${requirementId}`)
    },

    // Get reports by execution task ID
    getReportsByExecutionTaskId(executionTaskId: number) {
        return request.get<any, TestReportResponseDTO[]>(`/v1/test-reports/execution-task/${executionTaskId}`)
    },

    // Update report
    updateReport(id: number, data: TestReportRequestDTO) {
        return request.put<any, TestReportResponseDTO>(`/v1/test-reports/${id}`, data)
    },

    // Publish report
    publishReport(id: number) {
        return request.put<any, TestReportResponseDTO>(`/v1/test-reports/${id}/publish`)
    },

    // Delete report
    deleteReport(id: number) {
        return request.delete<any, void>(`/v1/test-reports/${id}`)
    },

    // Export report
    exportReport(reportCode: string, format: 'WORD' | 'PDF' | 'EXCEL') {
        return request.get<any, string>(`/v1/test-reports/${reportCode}/export`, {
            params: { format }
        })
    },

    // Summarize execution results
    summarizeExecutionResults(requirementId?: number, executionTaskId?: number) {
        return request.get<any, string>('/v1/test-reports/summarize', {
            params: { requirementId, executionTaskId }
        })
    }
}
