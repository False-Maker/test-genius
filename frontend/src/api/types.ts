// 统一响应结果类型
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp?: number
}

// 分页结果类型
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 常用响应类型
export interface EmptyResponse {}

export interface IdResponse {
  id: number
}

export interface MessageResponse {
  message: string
}

export interface SuccessResponse {
  success: boolean
}

