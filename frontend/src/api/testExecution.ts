import request from './request'
import type { PageResult } from './types'

// 测试执行任务相关类型
export interface TestExecutionTaskRequest {
  taskName: string
  taskType: string // AUTO_SCRIPT_GENERATION/AUTO_SCRIPT_REPAIR/MANUAL_EXECUTION
  requirementId?: number
  caseId?: number
  caseSuiteId?: number
  scriptType?: string // SELENIUM/PLAYWRIGHT/PUPPETEER
  scriptContent?: string
  scriptLanguage?: string // PYTHON/JAVA/JAVASCRIPT
  pageCodeUrl?: string
  naturalLanguageDesc?: string
  errorLog?: string
  executionConfig?: string
  creatorId?: number
  creatorName?: string
}

export interface TestExecutionTaskResponse {
  id?: number
  taskCode?: string
  taskName?: string
  taskType?: string
  requirementId?: number
  caseId?: number
  caseSuiteId?: number
  scriptType?: string
  scriptContent?: string
  scriptLanguage?: string
  pageCodeUrl?: string
  naturalLanguageDesc?: string
  taskStatus?: string // PENDING/PROCESSING/SUCCESS/FAILED
  progress?: number // 0-100
  successCount?: number
  failCount?: number
  errorMessage?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  finishTime?: string
}

// 测试执行记录相关类型
export interface TestExecutionRecordRequest {
  taskId: number
  caseId?: number
  executionType: string // MANUAL/AUTOMATED
  executionStatus: string // PENDING/RUNNING/SUCCESS/FAILED/SKIPPED
  executionResult?: string
  executionLog?: string
  errorMessage?: string
  executionDuration?: number
  executedBy?: number
  executedByName?: string
  screenshotUrl?: string
  videoUrl?: string
}

export interface TestExecutionRecordResponse {
  id?: number
  recordCode?: string
  taskId?: number
  caseId?: number
  executionType?: string
  executionStatus?: string
  executionResult?: string
  executionLog?: string
  errorMessage?: string
  executionDuration?: number
  executedBy?: number
  executedByName?: string
  executionTime?: string
  finishTime?: string
  screenshotUrl?: string
  videoUrl?: string
}

// 执行统计类型
export interface TestExecutionStatistics {
  totalTasks?: number
  pendingTasks?: number
  processingTasks?: number
  successTasks?: number
  failedTasks?: number
  totalRecords?: number
  successRecords?: number
  failedRecords?: number
  skippedRecords?: number
  successRate?: number
  avgExecutionDuration?: number
  trendData?: Array<Record<string, any>>
}

// 测试执行任务列表查询参数
export interface TestExecutionTaskListParams {
  page?: number
  size?: number
  taskName?: string
  taskStatus?: string
  taskType?: string
}

// 测试执行记录列表查询参数
export interface TestExecutionRecordListParams {
  page?: number
  size?: number
  taskId?: number
  caseId?: number
  executionStatus?: string
}

// 测试执行API
export const testExecutionApi = {
  // ========== 执行任务相关接口 ==========

  // 创建执行任务
  createExecutionTask(data: TestExecutionTaskRequest) {
    return request.post<TestExecutionTaskResponse>('/v1/test-execution/tasks', data)
  },

  // 查询执行任务列表
  getExecutionTaskList(params?: TestExecutionTaskListParams) {
    return request.get<PageResult<TestExecutionTaskResponse>>('/v1/test-execution/tasks', { params })
  },

  // 获取执行任务详情（根据ID）
  getExecutionTaskById(id: number) {
    return request.get<TestExecutionTaskResponse>(`/v1/test-execution/tasks/${id}`)
  },

  // 获取执行任务详情（根据任务编码）
  getExecutionTaskByCode(taskCode: string) {
    return request.get<TestExecutionTaskResponse>(`/v1/test-execution/tasks/code/${taskCode}`)
  },

  // 更新执行任务
  updateExecutionTask(id: number, data: TestExecutionTaskRequest) {
    return request.put<TestExecutionTaskResponse>(`/v1/test-execution/tasks/${id}`, data)
  },

  // 删除执行任务
  deleteExecutionTask(id: number) {
    return request.delete(`/v1/test-execution/tasks/${id}`)
  },

  // 更新任务状态
  updateTaskStatus(taskCode: string, status: string) {
    return request.put<TestExecutionTaskResponse>(`/v1/test-execution/tasks/${taskCode}/status`, null, {
      params: { status }
    })
  },

  // 更新任务进度
  updateTaskProgress(taskCode: string, progress: number) {
    return request.put<TestExecutionTaskResponse>(`/v1/test-execution/tasks/${taskCode}/progress`, null, {
      params: { progress }
    })
  },

  // ========== 执行记录相关接口 ==========

  // 创建执行记录
  createExecutionRecord(data: TestExecutionRecordRequest) {
    return request.post<TestExecutionRecordResponse>('/v1/test-execution/records', data)
  },

  // 查询执行记录列表
  getExecutionRecordList(params?: TestExecutionRecordListParams) {
    return request.get<PageResult<TestExecutionRecordResponse>>('/v1/test-execution/records', { params })
  },

  // 获取执行记录详情（根据ID）
  getExecutionRecordById(id: number) {
    return request.get<TestExecutionRecordResponse>(`/v1/test-execution/records/${id}`)
  },

  // 获取执行记录详情（根据记录编码）
  getExecutionRecordByCode(recordCode: string) {
    return request.get<TestExecutionRecordResponse>(`/v1/test-execution/records/code/${recordCode}`)
  },

  // 获取任务的执行记录列表
  getExecutionRecordsByTaskId(taskId: number) {
    return request.get<TestExecutionRecordResponse[]>(`/v1/test-execution/records/task/${taskId}`)
  },

  // 更新执行记录状态
  updateExecutionRecordStatus(recordCode: string, status: string) {
    return request.put<TestExecutionRecordResponse>(`/v1/test-execution/records/${recordCode}/status`, null, {
      params: { status }
    })
  },

  // ========== 统计分析相关接口 ==========

  // 获取执行统计信息
  getExecutionStatistics(params?: { requirementId?: number; caseId?: number; startTime?: string; endTime?: string }) {
    return request.get<TestExecutionStatistics>('/v1/test-execution/statistics', { params })
  }
}

