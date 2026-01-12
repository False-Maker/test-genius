// 统一响应结果类型
export interface ApiResult<T = any> {
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

