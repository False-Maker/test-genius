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

// 提示词模板版本相关类型定义
export interface PromptTemplateVersion {
  id?: number
  templateId?: number
  versionNumber: number
  versionName?: string
  versionDescription?: string
  templateContent: string
  templateVariables?: string
  changeLog?: string
  isCurrent?: string
  createdBy?: number
  createdByName?: string
  createTime?: string
}

export interface PromptTemplateVersionRequest {
  versionNumber?: number
  versionName?: string
  versionDescription?: string
  templateContent: string
  templateVariables?: string
  changeLog?: string
  isCurrent?: string
  createdBy?: number
  createdByName?: string
}

export interface VersionCompareResult {
  version1: PromptTemplateVersion
  version2: PromptTemplateVersion
  diff: {
    contentChanged: boolean
    variablesChanged: boolean
  }
}

// 提示词模板版本管理API
export const promptTemplateVersionApi = {
  // 创建版本
  createVersion(templateId: number, data: PromptTemplateVersionRequest) {
    return request.post<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/versions`, data)
  },

  // 查询版本列表
  getVersions(templateId: number) {
    return request.get<PromptTemplateVersion[]>(`/v1/prompt-templates/${templateId}/versions`)
  },

  // 获取版本详情
  getVersionById(templateId: number, id: number) {
    return request.get<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/versions/${id}`)
  },

  // 获取当前版本
  getCurrentVersion(templateId: number) {
    return request.get<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/versions/current`)
  },

  // 根据版本号获取版本
  getVersionByNumber(templateId: number, versionNumber: number) {
    return request.get<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/versions/version/${versionNumber}`)
  },

  // 版本回滚
  rollbackToVersion(templateId: number, versionNumber: number) {
    return request.post<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/versions/rollback/${versionNumber}`)
  },

  // 版本对比
  compareVersions(templateId: number, versionNumber1: number, versionNumber2: number) {
    return request.get<VersionCompareResult>(`/v1/prompt-templates/${templateId}/versions/compare`, {
      params: { versionNumber1, versionNumber2 }
    })
  },

  // 删除版本
  deleteVersion(templateId: number, id: number) {
    return request.delete(`/v1/prompt-templates/${templateId}/versions/${id}`)
  }
}

// A/B测试相关类型定义
export interface PromptTemplateAbTest {
  id?: number
  templateId?: number
  testName: string
  testDescription?: string
  versionAId: number
  versionBId: number
  trafficSplitA?: number
  trafficSplitB?: number
  startTime?: string
  endTime?: string
  status?: string  // draft/running/paused/completed
  autoSelectEnabled?: string
  minSamples?: number
  selectionCriteria?: string  // success_rate/response_time/user_rating
  createdBy?: number
  createdByName?: string
  createTime?: string
  updateTime?: string
}

export interface PromptTemplateAbTestRequest {
  testName: string
  testDescription?: string
  versionAId: number
  versionBId: number
  trafficSplitA?: number
  trafficSplitB?: number
  autoSelectEnabled?: string
  minSamples?: number
  selectionCriteria?: string
}

export interface AbTestStatistics {
  versionA: {
    totalExecutions: number
    successCount: number
    successRate: number
    avgResponseTime?: number
    avgRating?: number
  }
  versionB: {
    totalExecutions: number
    successCount: number
    successRate: number
    avgResponseTime?: number
    avgRating?: number
  }
  totalExecutions: number
  betterVersion: string
}

// A/B测试管理API
export const promptTemplateAbTestApi = {
  // 创建A/B测试
  createAbTest(templateId: number, data: PromptTemplateAbTestRequest) {
    return request.post<PromptTemplateAbTest>(`/v1/prompt-templates/${templateId}/ab-tests`, data)
  },

  // 查询A/B测试列表
  getAbTests(templateId: number) {
    return request.get<PromptTemplateAbTest[]>(`/v1/prompt-templates/${templateId}/ab-tests`)
  },

  // 获取A/B测试详情
  getAbTestById(templateId: number, id: number) {
    return request.get<PromptTemplateAbTest>(`/v1/prompt-templates/${templateId}/ab-tests/${id}`)
  },

  // 启动A/B测试
  startAbTest(templateId: number, id: number) {
    return request.post<PromptTemplateAbTest>(`/v1/prompt-templates/${templateId}/ab-tests/${id}/start`)
  },

  // 暂停A/B测试
  pauseAbTest(templateId: number, id: number) {
    return request.post<PromptTemplateAbTest>(`/v1/prompt-templates/${templateId}/ab-tests/${id}/pause`)
  },

  // 停止A/B测试
  stopAbTest(templateId: number, id: number) {
    return request.post<PromptTemplateAbTest>(`/v1/prompt-templates/${templateId}/ab-tests/${id}/stop`)
  },

  // 获取A/B测试统计
  getAbTestStatistics(templateId: number, id: number) {
    return request.get<AbTestStatistics>(`/v1/prompt-templates/${templateId}/ab-tests/${id}/statistics`)
  },

  // 自动选择最优版本
  autoSelectBestVersion(templateId: number, id: number) {
    return request.post<PromptTemplateVersion>(`/v1/prompt-templates/${templateId}/ab-tests/${id}/auto-select`)
  },

  // 删除A/B测试
  deleteAbTest(templateId: number, id: number) {
    return request.delete(`/v1/prompt-templates/${templateId}/ab-tests/${id}`)
  }
}
