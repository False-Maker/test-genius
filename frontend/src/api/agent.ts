import request from './request'

// Agent相关类型定义
export interface Agent {
  id?: number
  agentCode: string
  agentName: string
  agentType: string // TEST_DESIGN_ASSISTANT, CASE_OPTIMIZATION, CUSTOM
  agentDescription?: string
  agentConfig?: Record<string, unknown>
  systemPrompt?: string
  maxIterations?: number
  maxTokens?: number
  temperature?: number
  isActive?: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

export interface AgentSession {
  id?: number
  sessionCode: string
  agentId: number
  userId?: number
  userName?: string
  sessionTitle?: string
  contextData?: Record<string, unknown>
  status?: string // ACTIVE, CLOSED, EXPIRED
  totalTokens?: number
  totalIterations?: number
  createTime?: string
  updateTime?: string
  lastActiveTime?: string
}

export interface AgentMessage {
  id?: number
  sessionId: number
  messageType: string // USER, ASSISTANT, TOOL, SYSTEM
  role: string // user, assistant, tool, system
  content: string
  toolCalls?: AgentToolCall[]
  toolResults?: Record<string, unknown>[]
  tokensUsed?: number
  responseTime?: number
  modelCode?: string
  iterationNumber?: number
  createTime?: string
}

export interface AgentToolCallFunction {
  name?: string
  arguments?: unknown
}

export interface AgentToolCall {
  tool?: string
  arguments?: Record<string, unknown>
  result?: Record<string, unknown>
  function?: AgentToolCallFunction
}

export interface AgentChatResponse {
  content: string
  response?: string
  tool_calls?: AgentToolCall[]
  tool_results?: Record<string, unknown>[]
  tokens_used?: number
  response_time?: number
  model_code?: string
  iteration_number?: number
  iterations?: number
}

// Agent API
export const agentApi = {
  // 创建Agent
  createAgent(data: Agent) {
    return request.post<Agent>('/v1/agents', data)
  },

  // 更新Agent
  updateAgent(id: number, data: Agent) {
    return request.put<Agent>(`/v1/agents/${id}`, data)
  },

  // 根据ID查询Agent
  getAgentById(id: number) {
    return request.get<Agent>(`/v1/agents/${id}`)
  },

  // 根据编码查询Agent
  getAgentByCode(code: string) {
    return request.get<Agent>(`/v1/agents/code/${code}`)
  },

  // 查询所有启用的Agent
  getAllActiveAgents() {
    return request.get<Agent[]>('/v1/agents/active')
  },

  // 查询指定类型的Agent
  getAgentsByType(type: string) {
    return request.get<Agent[]>(`/v1/agents/type/${type}`)
  },

  // 删除Agent
  deleteAgent(id: number) {
    return request.delete<void>(`/v1/agents/${id}`)
  },

  // 启用/禁用Agent
  toggleAgentActive(id: number, active: boolean) {
    return request.put<Agent>(`/v1/agents/${id}/active`, null, {
      params: { active }
    })
  },

  // Agent Session API
  // 创建会话
  createSession(data: AgentSession) {
    return request.post<AgentSession>('/v1/agent-sessions', data)
  },

  // 根据ID查询会话
  getSessionById(id: number) {
    return request.get<AgentSession>(`/v1/agent-sessions/${id}`)
  },

  // 根据编码查询会话
  getSessionByCode(code: string) {
    return request.get<AgentSession>(`/v1/agent-sessions/code/${code}`)
  },

  // 查询Agent的所有会话
  getSessionsByAgentId(agentId: number) {
    return request.get<AgentSession[]>(`/v1/agent-sessions/agent/${agentId}`)
  },

  // 查询用户的所有会话
  getSessionsByUserId(userId: number) {
    return request.get<AgentSession[]>(`/v1/agent-sessions/user/${userId}`)
  },

  // 查询Agent和用户的所有会话
  getSessionsByAgentIdAndUserId(agentId: number, userId: number) {
    return request.get<AgentSession[]>(
      `/v1/agent-sessions/agent/${agentId}/user/${userId}`
    )
  },

  // 关闭会话
  closeSession(id: number) {
    return request.put<AgentSession>(`/v1/agent-sessions/${id}/close`)
  },

  // 删除会话
  deleteSession(id: number) {
    return request.delete<void>(`/v1/agent-sessions/${id}`)
  },

  // Agent Runtime API（统一经Java公开接口）
  // Agent对话
  chatWithAgent(sessionId: number, message: string) {
    return request.post<AgentChatResponse>('/v1/agent/chat', {
      session_id: sessionId,
      message: message
    })
  },

  // 获取会话历史
  getSessionHistory(sessionId: number) {
    return request.get<AgentMessage[]>(`/v1/agent/sessions/${sessionId}/history`)
  }
}

export default agentApi
