import request from './request'
import type { ApiResult, PageResult, TestRequirement, RequirementListParams } from './types'

// 需求管理API
export const requirementApi = {
  // 创建需求
  createRequirement(data: TestRequirement) {
    return request.post<TestRequirement>('/v1/requirements', data)
  },

// 查询需求列表
  getRequirementList(params: RequirementListParams = {}) {
    return request.get<ApiResult<PageResult<TestRequirement>>>('/v1/requirements', { params })
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

