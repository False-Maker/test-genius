import request from './request'
import type { PageResult } from './types'

// 用例相关类型定义
export interface TestCase {
  id?: number
  caseCode?: string
  caseName: string
  requirementId?: number
  layerId?: number
  methodId?: number
  caseType?: string
  casePriority?: string
  preCondition?: string
  testStep?: string
  expectedResult?: string
  caseStatus?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  version?: number
}

export interface TestCaseListParams {
  page?: number
  size?: number
  caseName?: string
  caseStatus?: string
  requirementId?: number
}

// 用例管理API
export const testCaseApi = {
  // 创建用例
  createTestCase(data: TestCase) {
    return request.post<TestCase>('/v1/test-cases', data)
  },

  // 查询用例列表
  getTestCaseList(params: TestCaseListParams = {}) {
    return request.get<PageResult<TestCase>>('/v1/test-cases', { params })
  },

  // 获取用例详情
  getTestCaseById(id: number) {
    return request.get<TestCase>(`/v1/test-cases/${id}`)
  },

  // 更新用例
  updateTestCase(id: number, data: TestCase) {
    return request.put<TestCase>(`/v1/test-cases/${id}`, data)
  },

  // 删除用例
  deleteTestCase(id: number) {
    return request.delete(`/v1/test-cases/${id}`)
  },

  // 更新用例状态
  updateCaseStatus(id: number, status: string) {
    return request.put<TestCase>(`/v1/test-cases/${id}/status`, null, {
      params: { status }
    })
  },

  // 审核用例
  reviewTestCase(id: number, reviewResult: string, reviewComment?: string) {
    return request.post<TestCase>(`/v1/test-cases/${id}/review`, null, {
      params: { reviewResult, reviewComment }
    })
  },

  // 导出用例
  exportTestCases(params?: {
    caseName?: string
    caseStatus?: string
    requirementId?: number
  }) {
    return request.get('/v1/test-cases/export', {
      params,
      responseType: 'blob'
    })
  },

  // 导出用例模板
  exportTemplate() {
    return request.get('/v1/test-cases/export-template', {
      responseType: 'blob'
    })
  },

  // 导入用例
  importTestCases(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<{
      successCount: number
      failCount: number
      errors: Array<{ row: number; message: string }>
    }>('/v1/test-cases/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

