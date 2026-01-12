import request from './request'
import type { PageResult } from './types'

// 提示词模板相关类型定义
export interface PromptTemplate {
  id?: number
  templateCode?: string
  templateName: string
  templateCategory?: string
  templateType?: string
  templateContent: string
  templateVariables?: string
  applicableLayers?: string
  applicableMethods?: string
  applicableModules?: string
  templateDescription?: string
  version?: number
  isActive?: string
  creatorId?: number
  createTime?: string
  updateTime?: string
}

export interface PromptTemplateListParams {
  page?: number
  size?: number
}

export interface GeneratePromptRequest {
  [key: string]: any
}

// 提示词模板管理API
export const promptTemplateApi = {
  // 创建模板
  createTemplate(data: PromptTemplate) {
    return request.post<PromptTemplate>('/v1/prompt-templates', data)
  },

  // 查询模板列表
  getTemplateList(params: PromptTemplateListParams = {}) {
    return request.get<PageResult<PromptTemplate>>('/v1/prompt-templates', { params })
  },

  // 获取模板详情
  getTemplateById(id: number) {
    return request.get<PromptTemplate>(`/v1/prompt-templates/${id}`)
  },

  // 更新模板
  updateTemplate(id: number, data: PromptTemplate) {
    return request.put<PromptTemplate>(`/v1/prompt-templates/${id}`, data)
  },

  // 删除模板
  deleteTemplate(id: number) {
    return request.delete(`/v1/prompt-templates/${id}`)
  },

  // 启用/禁用模板
  toggleTemplateStatus(id: number, isActive: string) {
    return request.put<PromptTemplate>(`/v1/prompt-templates/${id}/status`, null, {
      params: { isActive }
    })
  },

  // 生成提示词
  generatePrompt(id: number, variables: GeneratePromptRequest) {
    return request.post<string>(`/v1/prompt-templates/${id}/generate`, variables)
  },

  // 生成提示词（自定义模板）
  generatePromptWithContent(templateContent: string, variables: GeneratePromptRequest) {
    return request.post<string>('/v1/prompt-templates/generate', variables, {
      params: { templateContent }
    })
  }
}

