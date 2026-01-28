import request from './request'
import type { ApiResult } from './types'

// 智能模型选择相关类型定义
export interface ModelConfig {
  id?: number
  modelCode: string
  modelName: string
  modelType: string
  apiEndpoint?: string
  apiKey?: string
  modelVersion?: string
  maxTokens?: number
  temperature?: number
  isActive?: string
  priority?: number
  dailyLimit?: number
  performanceScore?: number
  taskTypes?: string
  isRecommended?: string
  lastScoreUpdateTime?: string
  createTime?: string
  updateTime?: string
}

// 智能模型选择API
export const intelligentModelApi = {
  // 根据任务类型选择最优模型
  selectOptimalModel(taskType: string) {
    return request.get<any, ApiResult<ModelConfig>>('/v1/intelligent-model/select-optimal', {
      params: { taskType }
    })
  },

  // 根据任务类型和场景选择最优模型
  selectOptimalModelByScenario(taskType: string, scenario: 'SPEED' | 'RELIABILITY' | 'COST' | 'BALANCED') {
    return request.get<any, ApiResult<ModelConfig>>('/v1/intelligent-model/select-by-scenario', {
      params: { taskType, scenario }
    })
  },

  // 获取任务类型的候选模型列表
  getCandidateModels(taskType: string) {
    return request.get<any, ApiResult<ModelConfig[]>>('/v1/intelligent-model/candidate-models', {
      params: { taskType }
    })
  },

  // 获取任务类型的默认模型
  getDefaultModel(taskType: string) {
    return request.get<any, ApiResult<ModelConfig>>('/v1/intelligent-model/default-model', {
      params: { taskType }
    })
  },

  // 手动刷新模型评分
  refreshScores() {
    return request.post<any, ApiResult<void>>('/v1/intelligent-model/refresh-scores')
  }
}

export default intelligentModelApi

