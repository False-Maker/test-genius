import request from './request'

// LLM相关类型定义
export interface LLMRequest {
  model_code: string
  prompt: string
  max_tokens?: number
  temperature?: number
}

export interface LLMResponse {
  content: string
  model_code: string
  tokens_used?: number
  response_time?: number
  error?: string
}

export interface ParallelCallRequest {
  prompt: string
  model_codes: string[]
  max_tokens?: number
  temperature?: number
  max_workers?: number
}

export interface ParallelCallResponse {
  results: LLMResponse[]
  total_time: number
  success_count: number
  fail_count: number
}

// LLM API
export const llmApi = {
  // 调用单个模型
  callModel(data: LLMRequest) {
    return request.post<LLMResponse>('/api/v1/llm/call', data)
  },

  // 并行调用多个模型（用于性能对比）
  parallelCall(data: ParallelCallRequest) {
    return request.post<ParallelCallResponse>('/api/v1/llm/parallel-call', data)
  }
}
