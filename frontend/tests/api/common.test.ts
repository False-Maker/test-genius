import { describe, it, expect, vi, beforeEach } from 'vitest'
import { commonApi, type TestLayer, type TestDesignMethod, type ModelConfig } from '@/api/common'
import request from '@/api/request'

// Mock request模块
vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn()
  }
}))

describe('commonApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getTestLayerList', () => {
    it('应该成功获取测试分层列表', async () => {
      const mockLayers: TestLayer[] = [
        {
          id: 1,
          layerCode: 'LAYER-001',
          layerName: '个人级',
          layerDescription: '个人级测试',
          layerOrder: 1,
          isActive: '1'
        },
        {
          id: 2,
          layerCode: 'LAYER-002',
          layerName: '业务案例',
          layerDescription: '业务案例测试',
          layerOrder: 2,
          isActive: '1'
        }
      ]

      const mockResponse = {
        code: 200,
        message: '操作成功',
        data: mockLayers
      }

      vi.mocked(request.get).mockResolvedValue(mockResponse)

      const result = await commonApi.getTestLayerList()

      expect(request.get).toHaveBeenCalledWith('/v1/common/test-layers')
      expect(result).toEqual(mockResponse)
      expect(result.data).toEqual(mockLayers)
    })

    it('应该处理API错误响应', async () => {
      const mockErrorResponse = {
        code: 500,
        message: '服务器错误',
        data: null
      }

      vi.mocked(request.get).mockResolvedValue(mockErrorResponse)

      const result = await commonApi.getTestLayerList()

      expect(result).toEqual(mockErrorResponse)
      expect(result.code).toBe(500)
      expect(result.message).toBe('服务器错误')
    })
  })

  describe('getTestDesignMethodList', () => {
    it('应该成功获取测试设计方法列表', async () => {
      const mockMethods: TestDesignMethod[] = [
        {
          id: 1,
          methodCode: 'METHOD-001',
          methodName: '等价类划分',
          methodDescription: '等价类划分法',
          applicableLayers: 'LAYER-001,LAYER-002',
          isActive: '1'
        },
        {
          id: 2,
          methodCode: 'METHOD-002',
          methodName: '边界值分析',
          methodDescription: '边界值分析法',
          applicableLayers: 'LAYER-001,LAYER-002',
          isActive: '1'
        }
      ]

      const mockResponse = {
        code: 200,
        message: '操作成功',
        data: mockMethods
      }

      vi.mocked(request.get).mockResolvedValue(mockResponse)

      const result = await commonApi.getTestDesignMethodList()

      expect(request.get).toHaveBeenCalledWith('/v1/common/test-design-methods')
      expect(result).toEqual(mockResponse)
      expect(result.data).toEqual(mockMethods)
    })
  })

  describe('getModelConfigList', () => {
    it('应该成功获取模型配置列表', async () => {
      const mockModels: ModelConfig[] = [
        {
          id: 1,
          modelCode: 'MODEL-001',
          modelName: 'DeepSeek',
          modelType: 'OPENAI',
          apiEndpoint: 'https://api.deepseek.com',
          maxTokens: 4096,
          temperature: 0.7,
          isActive: '1',
          priority: 1
        },
        {
          id: 2,
          modelCode: 'MODEL-002',
          modelName: '豆包',
          modelType: 'OPENAI',
          apiEndpoint: 'https://api.doubao.com',
          maxTokens: 4096,
          temperature: 0.7,
          isActive: '1',
          priority: 2
        }
      ]

      const mockResponse = {
        code: 200,
        message: '操作成功',
        data: mockModels
      }

      vi.mocked(request.get).mockResolvedValue(mockResponse)

      const result = await commonApi.getModelConfigList()

      expect(request.get).toHaveBeenCalledWith('/v1/common/model-configs')
      expect(result).toEqual(mockResponse)
      expect(result.data).toEqual(mockModels)
    })

    it('应该处理空列表响应', async () => {
      const mockResponse = {
        code: 200,
        message: '操作成功',
        data: []
      }

      vi.mocked(request.get).mockResolvedValue(mockResponse)

      const result = await commonApi.getModelConfigList()

      expect(result.data).toEqual([])
      expect(Array.isArray(result.data)).toBe(true)
    })
  })
})

