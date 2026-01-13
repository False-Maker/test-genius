import { vi } from 'vitest'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'

/**
 * 创建Mock Axios响应
 */
export const createMockAxiosResponse = <T = any>(
  data: T,
  status = 200,
  statusText = 'OK',
  headers: Record<string, string> = {}
): AxiosResponse<T> => {
  return {
    data,
    status,
    statusText,
    headers,
    config: {} as AxiosRequestConfig
  }
}

/**
 * Mock Axios实例
 */
export const mockAxios = {
  create: vi.fn(() => mockAxios),
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  patch: vi.fn(),
  interceptors: {
    request: {
      use: vi.fn(),
      eject: vi.fn()
    },
    response: {
      use: vi.fn(),
      eject: vi.fn()
    }
  }
}

/**
 * 重置所有Mock
 */
export const resetAxiosMocks = () => {
  vi.clearAllMocks()
  mockAxios.get.mockClear()
  mockAxios.post.mockClear()
  mockAxios.put.mockClear()
  mockAxios.delete.mockClear()
  mockAxios.patch.mockClear()
}

