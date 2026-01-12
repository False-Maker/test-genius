import request from './request'
import type { PageResult } from './types'

// 需求相关类型定义
export interface TestRequirement {
  id?: number
  requirementCode?: string
  requirementName: string
  requirementType?: string
  requirementDescription?: string
  requirementDocUrl?: string
  requirementStatus?: string
  businessModule?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  version?: number
}

export interface RequirementListParams {
  page?: number
  size?: number
  requirementName?: string
  requirementStatus?: string
}

// 需求管理API
export const requirementApi = {
  // 创建需求
  createRequirement(data: TestRequirement) {
    return request.post<TestRequirement>('/v1/requirements', data)
  },

  // 查询需求列表
  getRequirementList(params: RequirementListParams = {}) {
    return request.get<PageResult<TestRequirement>>('/v1/requirements', { params })
  },

  // 获取需求详情
  getRequirementById(id: number) {
    return request.get<TestRequirement>(`/v1/requirements/${id}`)
  },

  // 更新需求
  updateRequirement(id: number, data: TestRequirement) {
    return request.put<TestRequirement>(`/v1/requirements/${id}`, data)
  },

  // 删除需求
  deleteRequirement(id: number) {
    return request.delete(`/v1/requirements/${id}`)
  },

  // 更新需求状态
  updateRequirementStatus(id: number, status: string) {
    return request.put<TestRequirement>(`/v1/requirements/${id}/status`, null, {
      params: { status }
    })
  }
}

