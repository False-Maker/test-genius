import request from './request'
import type { PageResult, ApiResult } from './types'

// 模型配置类型
export interface ModelConfig {
  id?: number
  modelCode?: string
  modelName: string
  modelType?: string
  apiEndpoint?: string
  modelVersion?: string
  maxTokens?: number
  temperature?: number
  isActive?: string
  priority?: number
  dailyLimit?: number
  createTime?: string
  updateTime?: string
}

export interface ModelConfigListParams {
  page?: number
  size?: number
  modelName?: string
  modelType?: string
  isActive?: string
}

// 模型配置API
export const modelConfigApi = {
  // 创建模型配置
  createModelConfig(data: ModelConfig) {
    return request.post<ModelConfig>('/v1/model-configs', data)
  },

  // 查询模型配置列表
  getModelConfigList(params: ModelConfigListParams = {}) {
    return request.get<PageResult<ModelConfig>>('/v1/model-configs', { params })
  },

  // 获取模型配置详情
  getModelConfigById(id: number) {
    return request.get<ModelConfig>(`/v1/model-configs/${id}`)
  },

  // 根据模型编码获取配置
  getModelConfigByCode(modelCode: string) {
    return request.get<ModelConfig>(`/v1/model-configs/code/${modelCode}`)
  },

  // 更新模型配置
  updateModelConfig(id: number, data: ModelConfig) {
    return request.put<ModelConfig>(`/v1/model-configs/${id}`, data)
  },

  // 删除模型配置
  deleteModelConfig(id: number) {
    return request.delete(`/v1/model-configs/${id}`)
  },

  // 启用/禁用模型配置
  toggleModelConfigStatus(id: number, isActive: string) {
    return request.put<ModelConfig>(`/v1/model-configs/${id}/status`, null, {
      params: { isActive }
    })
  },

  // 获取所有启用的模型配置
  getActiveModelConfigs() {
    return request.get<ModelConfig[]>('/v1/model-configs/active')
  },

  // 根据类型获取启用的模型配置
  getActiveModelConfigsByType(modelType: string) {
    return request.get<ModelConfig[]>(`/v1/model-configs/active/type/${modelType}`)
  }
}

