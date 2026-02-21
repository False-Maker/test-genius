import request from './request'
import type { ApiResult } from './types'
import type { PageResult } from './testReport'

export interface UIScriptTemplateRequestDTO {
    templateName: string
    templateType: 'SELENIUM' | 'PLAYWRIGHT' | 'PUPPETEER' | string
    scriptLanguage: 'PYTHON' | 'JAVA' | 'JAVASCRIPT' | string
    templateContent: string
    templateVariables?: string
    applicableScenarios?: string
    templateDescription?: string
    isActive?: string
    creatorId?: number
}

export interface UIScriptTemplateResponseDTO {
    id: number
    templateCode: string
    templateName: string
    templateType: string
    scriptLanguage: string
    templateContent: string
    templateVariables: string
    applicableScenarios: string
    templateDescription: string
    version: number
    isActive: string
    creatorId: number
    createTime: string
    updateTime: string
}

export const uiScriptTemplateApi = {
    // Create template
    createTemplate(data: UIScriptTemplateRequestDTO) {
        return request.post<UIScriptTemplateResponseDTO>('/v1/ui-script-templates', data)
    },

    // Get template list
    getTemplateList(page: number = 0, size: number = 10, templateName?: string, templateType?: string, scriptLanguage?: string, isActive?: string) {
        return request.get<ApiResult<PageResult<UIScriptTemplateResponseDTO>>>('/v1/ui-script-templates', {
            params: { page, size, templateName, templateType, scriptLanguage, isActive }
        })
    },

    // Get template by ID
    getTemplateById(id: number) {
        return request.get<UIScriptTemplateResponseDTO>(`/v1/ui-script-templates/${id}`)
    },

    // Get template by code
    getTemplateByCode(templateCode: string) {
        return request.get<UIScriptTemplateResponseDTO>(`/v1/ui-script-templates/code/${templateCode}`)
    },

    // Update template
    updateTemplate(id: number, data: UIScriptTemplateRequestDTO) {
        return request.put<UIScriptTemplateResponseDTO>(`/v1/ui-script-templates/${id}`, data)
    },

    // Delete template
    deleteTemplate(id: number) {
        return request.delete<void>(`/v1/ui-script-templates/${id}`)
    },

    // Get active templates by type and language
    getActiveTemplates(templateType?: string, scriptLanguage?: string) {
        return request.get<UIScriptTemplateResponseDTO[]>('/v1/ui-script-templates/active', {
            params: { templateType, scriptLanguage }
        })
    },

    // Update template status
    updateTemplateStatus(id: number, isActive: string) {
        return request.put<UIScriptTemplateResponseDTO>(`/v1/ui-script-templates/${id}/status`, null, {
            params: { isActive }
        })
    }
}
