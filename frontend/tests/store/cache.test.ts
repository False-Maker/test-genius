import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCacheStore } from '@/store/cache'
import { requirementApi } from '@/api/requirement'
import { commonApi } from '@/api/common'
import { promptTemplateApi } from '@/api/promptTemplate'

// Mock API模块
vi.mock('@/api/requirement', () => ({
  requirementApi: {
    getRequirementList: vi.fn()
  }
}))

vi.mock('@/api/common', () => ({
  commonApi: {
    getTestLayerList: vi.fn(),
    getTestDesignMethodList: vi.fn(),
    getModelConfigList: vi.fn()
  }
}))

vi.mock('@/api/promptTemplate', () => ({
  promptTemplateApi: {
    getTemplateList: vi.fn()
  }
}))

describe('useCacheStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('初始化', () => {
    it('应该正确初始化空状态', () => {
      const store = useCacheStore()

      expect(store.requirementList).toEqual([])
      expect(store.layerList).toEqual([])
      expect(store.methodList).toEqual([])
      expect(store.templateList).toEqual([])
      expect(store.modelList).toEqual([])
    })
  })

  describe('loadLayerList', () => {
    it('应该成功加载测试分层列表', async () => {
      const store = useCacheStore()
      const mockLayers = [
        { id: 1, layerCode: 'LAYER-001', layerName: '个人级', isActive: '1' },
        { id: 2, layerCode: 'LAYER-002', layerName: '业务案例', isActive: '1' }
      ]

      vi.mocked(commonApi.getTestLayerList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: mockLayers
      })

      const result = await store.loadLayerList(true)

      expect(commonApi.getTestLayerList).toHaveBeenCalled()
      expect(store.layerList).toEqual(mockLayers)
      expect(result).toEqual(mockLayers)
      expect(store.loading.layerList).toBe(false)
    })

    it('应该使用缓存当缓存未过期时', async () => {
      const store = useCacheStore()
      const mockLayers = [
        { id: 1, layerCode: 'LAYER-001', layerName: '个人级', isActive: '1' }
      ]

      // 第一次加载
      vi.mocked(commonApi.getTestLayerList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: mockLayers
      })

      await store.loadLayerList(true)
      expect(commonApi.getTestLayerList).toHaveBeenCalledTimes(1)

      // 第二次加载（不强制刷新）
      const result = await store.loadLayerList(false)
      expect(commonApi.getTestLayerList).toHaveBeenCalledTimes(1) // 应该使用缓存
      expect(result).toEqual(mockLayers)
    })

    it('应该处理加载错误', async () => {
      const store = useCacheStore()
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      vi.mocked(commonApi.getTestLayerList).mockRejectedValue(new Error('网络错误'))

      await expect(store.loadLayerList(true)).rejects.toThrow('网络错误')
      expect(store.loading.layerList).toBe(false)

      consoleErrorSpy.mockRestore()
    })
  })

  describe('loadMethodList', () => {
    it('应该成功加载测试方法列表', async () => {
      const store = useCacheStore()
      const mockMethods = [
        { id: 1, methodCode: 'METHOD-001', methodName: '等价类划分', isActive: '1' }
      ]

      vi.mocked(commonApi.getTestDesignMethodList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: mockMethods
      })

      const result = await store.loadMethodList(true)

      expect(commonApi.getTestDesignMethodList).toHaveBeenCalled()
      expect(store.methodList).toEqual(mockMethods)
      expect(result).toEqual(mockMethods)
    })
  })

  describe('loadModelList', () => {
    it('应该成功加载模型配置列表', async () => {
      const store = useCacheStore()
      const mockModels = [
        { id: 1, modelCode: 'MODEL-001', modelName: 'DeepSeek', isActive: '1' }
      ]

      vi.mocked(commonApi.getModelConfigList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: mockModels
      })

      const result = await store.loadModelList(true)

      expect(commonApi.getModelConfigList).toHaveBeenCalled()
      expect(store.modelList).toEqual(mockModels)
      expect(result).toEqual(mockModels)
    })
  })

  describe('loadTemplateList', () => {
    it('应该成功加载模板列表并过滤启用状态', async () => {
      const store = useCacheStore()
      const mockTemplates = [
        { id: 1, templateCode: 'TMP-001', templateName: '模板1', isActive: '1' },
        { id: 2, templateCode: 'TMP-002', templateName: '模板2', isActive: '0' }
      ]

      vi.mocked(promptTemplateApi.getTemplateList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: {
          content: mockTemplates,
          totalElements: 2,
          totalPages: 1
        }
      })

      const result = await store.loadTemplateList(true)

      expect(promptTemplateApi.getTemplateList).toHaveBeenCalled()
      // 应该只缓存启用的模板
      expect(store.templateList).toEqual([mockTemplates[0]])
      expect(result).toEqual([mockTemplates[0]])
    })
  })

  describe('loadRequirementList', () => {
    it('应该成功加载需求列表', async () => {
      const store = useCacheStore()
      const mockRequirements = [
        { id: 1, requirementCode: 'REQ-001', requirementName: '需求1' }
      ]

      vi.mocked(requirementApi.getRequirementList).mockResolvedValue({
        code: 200,
        message: '操作成功',
        data: {
          content: mockRequirements,
          totalElements: 1,
          totalPages: 1
        }
      })

      const result = await store.loadRequirementList(true)

      expect(requirementApi.getRequirementList).toHaveBeenCalled()
      expect(store.requirementList).toEqual(mockRequirements)
      expect(result).toEqual(mockRequirements)
    })
  })

  describe('计算属性', () => {
    it('activeLayers应该只返回启用的测试分层', () => {
      const store = useCacheStore()
      store.layerList = [
        { id: 1, layerCode: 'LAYER-001', layerName: '个人级', isActive: '1' } as any,
        { id: 2, layerCode: 'LAYER-002', layerName: '业务案例', isActive: '0' } as any
      ]

      expect(store.activeLayers).toHaveLength(1)
      expect(store.activeLayers[0].layerCode).toBe('LAYER-001')
    })

    it('activeMethods应该只返回启用的测试方法', () => {
      const store = useCacheStore()
      store.methodList = [
        { id: 1, methodCode: 'METHOD-001', methodName: '等价类划分', isActive: '1' } as any,
        { id: 2, methodCode: 'METHOD-002', methodName: '边界值分析', isActive: '0' } as any
      ]

      expect(store.activeMethods).toHaveLength(1)
      expect(store.activeMethods[0].methodCode).toBe('METHOD-001')
    })

    it('activeModels应该只返回启用的模型配置', () => {
      const store = useCacheStore()
      store.modelList = [
        { id: 1, modelCode: 'MODEL-001', modelName: 'DeepSeek', isActive: '1' } as any,
        { id: 2, modelCode: 'MODEL-002', modelName: '豆包', isActive: '0' } as any
      ]

      expect(store.activeModels).toHaveLength(1)
      expect(store.activeModels[0].modelCode).toBe('MODEL-001')
    })
  })

  describe('清除缓存', () => {
    it('应该清除所有缓存', () => {
      const store = useCacheStore()
      store.requirementList = [{ id: 1 }] as any
      store.layerList = [{ id: 1 }] as any
      store.methodList = [{ id: 1 }] as any
      store.templateList = [{ id: 1 }] as any
      store.modelList = [{ id: 1 }] as any

      store.clearAllCache()

      expect(store.requirementList).toEqual([])
      expect(store.layerList).toEqual([])
      expect(store.methodList).toEqual([])
      expect(store.templateList).toEqual([])
      expect(store.modelList).toEqual([])
    })

    it('应该清除指定类型的缓存', () => {
      const store = useCacheStore()
      store.requirementList = [{ id: 1 }] as any
      store.layerList = [{ id: 1 }] as any

      store.clearCache('requirementList')

      expect(store.requirementList).toEqual([])
      expect(store.layerList).toEqual([{ id: 1 }])
    })
  })
})

