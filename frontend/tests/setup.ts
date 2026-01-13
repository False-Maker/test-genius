import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// 全局Mock配置
global.console = {
  ...console,
  // 测试时静默console.error，避免干扰测试输出
  error: vi.fn(),
  warn: vi.fn()
}

// Mock Element Plus组件
config.global.mocks = {
  $t: (key: string) => key,
  $tc: (key: string) => key
}

// Mock Element Plus的ElMessage
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    },
    ElLoading: {
      service: vi.fn(() => ({
        close: vi.fn()
      }))
    }
  }
})

// Mock环境变量
vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080')

