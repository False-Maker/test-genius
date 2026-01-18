import request from './request'
import type { PageResult } from './testReport'

export interface TestReportTemplateRequestDTO {
    templateName: string
    templateType: 'EXECUTION' | 'COVERAGE' | 'QUALITY' | 'RISK'
    templateContent: string
    templateVariables?: string
    fileFormat?: 'WORD' | 'PDF' | 'EXCEL'
    templateDescription?: string
    isDefault?: '0' | '1'
    isActive?: '0' | '1'
    creatorId?: number
}

export interface TestReportTemplateResponseDTO {
    id: number
    templateCode: string
    templateName: string
    templateType: string
    templateContent: string
    templateVariables?: string
    fileFormat?: string
    templateDescription?: string
    isDefault: '0' | '1'
    isActive: '0' | '1'
    version?: number
    creatorId?: number
    createTime?: string
    updateTime?: string
}

export const testReportTemplateApi = {
    // Create template
    createTemplate(data: TestReportTemplateRequestDTO) {
        return request.post<any, TestReportTemplateResponseDTO>('/v1/test-report-templates', data)
    },

    // Get template list
    getTemplateList(page: number = 0, size: number = 10) {
        return request.get<any, PageResult<TestReportTemplateResponseDTO>>('/v1/test-report-templates', {
            params: { page, size }
        })
    },

    // Get template detail
    getTemplateById(id: number) {
        return request.get<any, TestReportTemplateResponseDTO>(`/v1/test-report-templates/${id}`)
    },

    // Get template by code
    getTemplateByCode(templateCode: string) {
        return request.get<any, TestReportTemplateResponseDTO>(`/v1/test-report-templates/code/${templateCode}`)
    },

    // Update template
    updateTemplate(id: number, data: TestReportTemplateRequestDTO) {
        return request.put<any, TestReportTemplateResponseDTO>(`/v1/test-report-templates/${id}`, data)
    },

    // Delete template
    deleteTemplate(id: number) {
        return request.delete<any, void>(`/v1/test-report-templates/${id}`)
    },

    // Toggle template status
    toggleTemplateStatus(id: number, isActive: '0' | '1') {
        return request.put<any, TestReportTemplateResponseDTO>(`/v1/test-report-templates/${id}/status`, null, {
            params: { isActive }
        })
    },

    // Set default template
    setDefaultTemplate(id: number, templateType: string) {
        return request.put<any, TestReportTemplateResponseDTO>(`/v1/test-report-templates/${id}/default`, null, {
            params: { templateType }
        })
    },

    // Get active templates by type
    getActiveTemplatesByType(templateType: string) {
        return request.get<any, TestReportTemplateResponseDTO[]>('/v1/test-report-templates/active', {
            params: { templateType }
        })
    },

    // Get default template by type
    getDefaultTemplateByType(templateType: string) {
        return request.get<any, TestReportTemplateResponseDTO>('/v1/test-report-templates/default', {
            params: { templateType }
        })
    }
}
