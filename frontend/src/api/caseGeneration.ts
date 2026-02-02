import request from './request'
import type { TestCase } from './testCase'

// 用例生成相关类型定义
export interface CaseGenerationRequest {
  requirementId: number
  layerCode?: string
  methodCode?: string
  templateId?: number
  modelCode?: string
  creatorId?: number
}

export interface CaseGenerationResult {
  taskId: number
  status: string
  message?: string
}

export interface BatchCaseGenerationResult {
  taskIds: number[]
  totalTasks: number
  message?: string
}

export interface GenerationTask {
  id: number
  requirementId: number
  status: string
  progress?: number
  message?: string
  result?: any
  createTime?: string
  updateTime?: string
}

// 任务列表查询参数
export interface TaskListQuery {
  page?: number
  size?: number
  taskStatus?: string
  requirementId?: number
  taskCode?: string
}

// 任务列表项
export interface TaskListItem {
  id: number
  taskCode: string
  requirementId: number
  requirementName: string
  requirementCode: string
  layerId: number
  layerName: string
  layerCode: string
  methodId: number
  methodName: string
  methodCode: string
  modelCode: string
  taskStatus: string
  progress: number
  totalCases: number
  successCases: number
  failCases: number
  errorMessage?: string
  createTime: string
  completeTime?: string
}

// 任务详情
export interface TaskDetail {
  id: number
  taskCode: string
  requirementId: number
  requirementName: string
  requirementCode: string
  requirementDescription?: string
  layerId: number
  layerName: string
  layerCode: string
  methodId: number
  methodName: string
  methodCode: string
  templateId?: number
  modelCode: string
  taskStatus: string
  progress: number
  totalCases: number
  successCases: number
  failCases: number
  errorMessage?: string
  createTime: string
  updateTime?: string
  completeTime?: string
  cases: TestCase[]
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 用例生成API
export const caseGenerationApi = {
  // 生成用例
  generateTestCases(data: CaseGenerationRequest) {
    return request.post<CaseGenerationResult>('/v1/case-generation/generate', data)
  },

  // 批量生成用例
  batchGenerateTestCases(data: CaseGenerationRequest[]) {
    return request.post<BatchCaseGenerationResult>('/v1/case-generation/batch-generate', {
      requirementIds: data.map(item => item.requirementId),
      layerCode: data[0]?.layerCode,
      methodCode: data[0]?.methodCode,
      templateId: data[0]?.templateId,
      modelCode: data[0]?.modelCode,
      creatorId: data[0]?.creatorId
    })
  },

  // 查询生成任务（旧接口，保留兼容）
  getGenerationTask(id: number) {
    return request.get<GenerationTask>(`/v1/case-generation/${id}`)
  },

  // 查询任务列表
  getTaskList(query: TaskListQuery) {
    return request.post<PageResult<TaskListItem>>('/v1/case-generation/tasks/list', query)
  },

  // 查询任务详情
  getTaskDetail(taskId: number) {
    return request.get<TaskDetail>(`/v1/case-generation/tasks/${taskId}`)
  },

  // 导出任务用例到Excel
  exportTaskToExcel(taskId: number) {
    return request.get(`/v1/case-generation/tasks/${taskId}/export-excel`, {
      responseType: 'blob'
    })
  }
}

