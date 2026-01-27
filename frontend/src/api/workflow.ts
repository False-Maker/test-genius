import request from './request'

// 工作流定义类型
export interface WorkflowDefinition {
  id?: number
  workflowCode: string
  workflowName: string
  workflowDescription?: string
  workflowType?: string
  workflowConfig: string  // JSON字符串
  version: number
  isActive: boolean
  isDefault: boolean
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
  lastExecutionTime?: string
  executionCount: number
}

// 工作流版本类型
export interface WorkflowVersion {
  id: number
  workflowId: number
  workflowCode: string
  version: number
  workflowConfig: string
  versionDescription?: string
  creatorId?: number
  createTime: string
}

// 工作流执行记录类型
export interface WorkflowExecution {
  id: number
  executionId: string
  workflowId: number
  workflowCode: string
  workflowVersion: number
  executionType: string
  inputData?: string
  outputData?: string
  status: string
  progress: number
  currentNodeId?: string
  errorMessage?: string
  errorNodeId?: string
  executionLog?: string
  startTime?: string
  endTime?: string
  duration?: number
  creatorId?: number
  creatorName?: string
  createTime: string
}

// 工作流节点配置类型
export interface WorkflowNode {
  id: string
  type: string
  name: string
  config: Record<string, any>
}

// 工作流边配置类型
export interface WorkflowEdge {
  source: string
  target: string
}

// 工作流配置类型
export interface WorkflowConfig {
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
}

// 工作流API
export const workflowApi = {
  // 创建工作流定义
  createWorkflow(workflow: WorkflowDefinition) {
    return request.post<WorkflowDefinition>('/v1/workflows', workflow)
  },

  // 更新工作流定义
  updateWorkflow(id: number, workflow: WorkflowDefinition) {
    return request.put<WorkflowDefinition>(`/v1/workflows/${id}`, workflow)
  },

  // 查询工作流定义
  getWorkflow(id: number) {
    return request.get<WorkflowDefinition>(`/v1/workflows/${id}`)
  },

  // 根据代码查询工作流定义
  getWorkflowByCode(workflowCode: string) {
    return request.get<WorkflowDefinition>(`/v1/workflows/code/${workflowCode}`)
  },

  // 查询所有工作流定义
  getAllWorkflows() {
    return request.get<WorkflowDefinition[]>('/v1/workflows')
  },

  // 根据类型查询工作流定义
  getWorkflowsByType(workflowType: string) {
    return request.get<WorkflowDefinition[]>(`/v1/workflows/type/${workflowType}`)
  },

  // 查询所有启用的工作流定义
  getActiveWorkflows() {
    return request.get<WorkflowDefinition[]>('/v1/workflows/active')
  },

  // 删除工作流定义
  deleteWorkflow(id: number) {
    return request.delete<void>(`/v1/workflows/${id}`)
  },

  // 启用/禁用工作流
  toggleActive(id: number, isActive: boolean) {
    return request.put<WorkflowDefinition>(`/v1/workflows/${id}/toggle-active`, null, {
      params: { isActive }
    })
  },

  // 设置默认工作流
  setDefault(id: number, isDefault: boolean) {
    return request.put<WorkflowDefinition>(`/v1/workflows/${id}/set-default`, null, {
      params: { isDefault }
    })
  },

  // 验证工作流配置
  validateWorkflowConfig(workflowConfig: string) {
    return request.post<{ valid: boolean; errors: string[]; warnings: string[] }>(
      '/v1/workflows/validate',
      { workflowConfig }
    )
  },

  // 创建工作流版本
  createVersion(id: number, versionDescription?: string) {
    return request.post<WorkflowVersion>(`/v1/workflows/${id}/versions`, null, {
      params: versionDescription ? { versionDescription } : {}
    })
  },

  // 查询工作流版本列表
  getVersions(id: number) {
    return request.get<WorkflowVersion[]>(`/v1/workflows/${id}/versions`)
  },

  // 回滚到指定版本
  rollbackToVersion(id: number, version: number) {
    return request.post<WorkflowDefinition>(`/v1/workflows/${id}/rollback`, null, {
      params: { version }
    })
  },

  // 执行工作流
  executeWorkflow(
    workflowConfig: string,
    inputData: Record<string, any>,
    workflowId?: number,
    workflowCode?: string,
    workflowVersion?: number
  ) {
    return request.post<{
      execution_id: string
      status: string
      output?: any
      error?: string
      error_node?: string
    }>('/api/v1/workflow/execute', {
      workflow_config: workflowConfig,
      input_data: inputData,
      workflow_id: workflowId,
      workflow_code: workflowCode,
      workflow_version: workflowVersion
    })
  }
}
