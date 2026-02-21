import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useConfigStore } from '@/store/config'

describe('useConfigStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    // 重置环境变量
    delete process.env.VITE_API_BASE_URL
  })

  describe('初始化', () => {
    it('应该正确初始化默认配置', () => {
      const store = useConfigStore()

      expect(store.config.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(store.config.timeout).toBe(30000)
      expect(store.config.pageSize).toBe(10)
      expect(store.config.pageSizes).toEqual([10, 20, 50, 100])
      expect(store.config.theme).toBe('light')
      expect(store.config.language).toBe('zh-CN')
      expect(store.config.selectedModel).toBeUndefined()
    })

    it('应该从localStorage恢复配置', () => {
      const savedConfig = {
        apiBaseUrl: 'http://api.example.com',
        timeout: 60000,
        pageSize: 20,
        pageSizes: [20, 50, 100],
        theme: 'dark' as const,
        language: 'en-US' as const
      }

      localStorage.setItem('systemConfig', JSON.stringify(savedConfig))

      const store = useConfigStore()
      store.initFromStorage()

      expect(store.config.apiBaseUrl).toBe('http://api.example.com')
      expect(store.config.timeout).toBe(60000)
      expect(store.config.pageSize).toBe(20)
      expect(store.config.theme).toBe('dark')
      expect(store.config.language).toBe('en-US')
    })

    it('应该处理无效的localStorage配置', () => {
      localStorage.setItem('systemConfig', 'invalid-json')

      const store = useConfigStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      store.initFromStorage()

      expect(store.config.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(localStorage.getItem('systemConfig')).toBeNull()

      consoleErrorSpy.mockRestore()
    })

    it('应该合并部分配置与默认配置', () => {
      const partialConfig = {
        theme: 'dark' as const,
        pageSize: 50
      }

      localStorage.setItem('systemConfig', JSON.stringify(partialConfig))

      const store = useConfigStore()
      store.initFromStorage()

      expect(store.config.theme).toBe('dark')
      expect(store.config.pageSize).toBe(50)
      expect(store.config.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(store.config.language).toBe('zh-CN')
    })
  })

  describe('计算属性', () => {
    it('apiBaseUrl计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.apiBaseUrl).toBe('http://localhost:8080/api')

      store.config.apiBaseUrl = 'http://custom.api.com'
      expect(store.apiBaseUrl).toBe('http://custom.api.com')
    })

    it('timeout计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.timeout).toBe(30000)

      store.config.timeout = 60000
      expect(store.timeout).toBe(60000)
    })

    it('pageSize计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.pageSize).toBe(10)

      store.config.pageSize = 20
      expect(store.pageSize).toBe(20)
    })

    it('pageSizes计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.pageSizes).toEqual([10, 20, 50, 100])
    })

    it('theme计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.theme).toBe('light')

      store.config.theme = 'dark'
      expect(store.theme).toBe('dark')
    })

    it('language计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.language).toBe('zh-CN')

      store.config.language = 'en-US'
      expect(store.language).toBe('en-US')
    })

    it('selectedModel计算属性应该返回正确的值', () => {
      const store = useConfigStore()

      expect(store.selectedModel).toBeUndefined()

      store.config.selectedModel = {
        modelCode: 'MODEL-001',
        modelName: 'DeepSeek',
        provider: 'deepseek'
      }
      expect(store.selectedModel).toEqual({
        modelCode: 'MODEL-001',
        modelName: 'DeepSeek',
        provider: 'deepseek'
      })
    })
  })

  describe('配置管理', () => {
    it('应该正确更新配置', () => {
      const store = useConfigStore()

      store.updateConfig({
        theme: 'dark',
        pageSize: 50
      })

      expect(store.config.theme).toBe('dark')
      expect(store.config.pageSize).toBe(50)
      expect(store.config.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(localStorage.getItem('systemConfig')).toBeTruthy()
    })

    it('应该正确重置配置', () => {
      const store = useConfigStore()

      store.config.theme = 'dark'
      store.config.pageSize = 50
      store.config.apiBaseUrl = 'http://custom.api.com'

      store.resetConfig()

      expect(store.config.theme).toBe('light')
      expect(store.config.pageSize).toBe(10)
      expect(store.config.apiBaseUrl).toBe('http://localhost:8080/api')
    })

    it('重置配置应该保存到localStorage', () => {
      const store = useConfigStore()

      store.config.theme = 'dark'
      store.resetConfig()

      const saved = localStorage.getItem('systemConfig')
      expect(saved).toBeTruthy()

      const parsed = JSON.parse(saved!)
      expect(parsed.theme).toBe('light')
    })
  })

  describe('主题设置', () => {
    it('应该正确设置主题', () => {
      const store = useConfigStore()

      store.setTheme('dark')

      expect(store.config.theme).toBe('dark')
      expect(localStorage.getItem('systemConfig')).toBeTruthy()
    })

    it('应该支持切换主题', () => {
      const store = useConfigStore()

      expect(store.config.theme).toBe('light')

      store.setTheme('dark')
      expect(store.config.theme).toBe('dark')

      store.setTheme('light')
      expect(store.config.theme).toBe('light')
    })
  })

  describe('语言设置', () => {
    it('应该正确设置语言', () => {
      const store = useConfigStore()

      store.setLanguage('en-US')

      expect(store.config.language).toBe('en-US')
      expect(localStorage.getItem('systemConfig')).toBeTruthy()
    })

    it('应该支持切换语言', () => {
      const store = useConfigStore()

      expect(store.config.language).toBe('zh-CN')

      store.setLanguage('en-US')
      expect(store.config.language).toBe('en-US')

      store.setLanguage('zh-CN')
      expect(store.config.language).toBe('zh-CN')
    })
  })

  describe('分页设置', () => {
    it('应该正确设置分页大小', () => {
      const store = useConfigStore()

      store.setPageSize(20)

      expect(store.config.pageSize).toBe(20)
      expect(localStorage.getItem('systemConfig')).toBeTruthy()
    })

    it('应该支持不同的分页大小', () => {
      const store = useConfigStore()

      store.setPageSize(10)
      expect(store.config.pageSize).toBe(10)

      store.setPageSize(50)
      expect(store.config.pageSize).toBe(50)

      store.setPageSize(100)
      expect(store.config.pageSize).toBe(100)
    })
  })

  describe('状态持久化', () => {
    it('配置更改应该持久化到localStorage', () => {
      const store = useConfigStore()

      store.setTheme('dark')
      store.setLanguage('en-US')
      store.setPageSize(20)

      const saved = localStorage.getItem('systemConfig')
      expect(saved).toBeTruthy()

      const parsed = JSON.parse(saved!)
      expect(parsed.theme).toBe('dark')
      expect(parsed.language).toBe('en-US')
      expect(parsed.pageSize).toBe(20)
    })

    it('updateConfig应该持久化到localStorage', () => {
      const store = useConfigStore()

      store.updateConfig({
        theme: 'dark',
        selectedModel: {
          modelCode: 'MODEL-001',
          modelName: 'DeepSeek',
          provider: 'deepseek'
        }
      })

      const saved = localStorage.getItem('systemConfig')
      expect(saved).toBeTruthy()

      const parsed = JSON.parse(saved!)
      expect(parsed.theme).toBe('dark')
      expect(parsed.selectedModel).toEqual({
        modelCode: 'MODEL-001',
        modelName: 'DeepSeek',
        provider: 'deepseek'
      })
    })
  })
})
