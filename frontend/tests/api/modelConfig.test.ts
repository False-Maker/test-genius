import { describe, it, expect, vi, beforeEach } from 'vitest'
import { modelConfigApi } from '@/api/modelConfig'
import request from '@/api/request'

vi.mock('@/api/request')

describe('ModelConfigAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createModelConfig', () => {
    it('should create a new model configuration', async () => {
      const modelData = {
        modelName: 'DeepSeek',
        modelCode: 'DEEPSEEK-001',
        modelType: 'OPENAI',
        apiEndpoint: 'https://api.deepseek.com',
        maxTokens: 4096,
        temperature: 0.7
      }
      
      const mockResponse = { code: 200, message: 'Created', data: { ...modelData, id: 1 } }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.createModelConfig(modelData)
      
      expect(request.post).toHaveBeenCalledWith('/v1/model-configs', modelData)
      expect(result.data).toEqual({ ...modelData, id: 1 })
    })

    it('should handle error response', async () => {
      const mockErrorResponse = { code: 400, message: 'Invalid model configuration', data: null }
      vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
      
      const result = await modelConfigApi.createModelConfig({
        modelName: 'Test',
        modelCode: 'TEST-001'
      })
      
      expect(result.code).toBe(400)
      expect(result.message).toBe('Invalid model configuration')
    })
  })

  describe('getModelConfigList', () => {
    it('should fetch model config list with pagination', async () => {
      const mockData = {
        content: [
          {
            id: 1,
            modelCode: 'DEEPSEEK-001',
            modelName: 'DeepSeek',
            modelType: 'OPENAI',
            maxTokens: 4096,
            temperature: 0.7,
            isActive: '1'
          }
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0
      }
      
      const mockResponse = { code: 200, message: 'Success', data: mockData }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.getModelConfigList({ page: 0, size: 10 })
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs', { 
        params: { page: 0, size: 10 } 
      })
      expect(result.data).toEqual(mockData)
    })

    it('should fetch list with filter parameters', async () => {
      const mockData = { content: [], totalElements: 0 }
      const mockResponse = { code: 200, message: 'Success', data: mockData }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.getModelConfigList({
        modelName: 'DeepSeek',
        modelType: 'OPENAI',
        isActive: '1'
      })
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs', { 
        params: { modelName: 'DeepSeek', modelType: 'OPENAI', isActive: '1' } 
      })
      expect(result.data).toEqual(mockData)
    })

    it('should handle default parameters', async () => {
      const mockResponse = { code: 200, message: 'Success', data: { content: [], totalElements: 0 } }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.getModelConfigList()
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs', { 
        params: {} 
      })
      expect(result.data).toEqual({ content: [], totalElements: 0 })
    })
  })

  describe('getModelConfigById', () => {
    it('should fetch model config by id', async () => {
      const mockModel = {
        id: 1,
        modelCode: 'DEEPSEEK-001',
        modelName: 'DeepSeek',
        modelType: 'OPENAI',
        apiEndpoint: 'https://api.deepseek.com',
        maxTokens: 4096,
        temperature: 0.7,
        isActive: '1',
        priority: 1
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockModel 
      })
      
      const result = await modelConfigApi.getModelConfigById(1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/1')
      expect(result.data).toEqual(mockModel)
    })

    it('should handle model not found', async () => {
      const mockErrorResponse = { code: 404, message: 'Model not found', data: null }
      vi.mocked(request.get).mockResolvedValue(mockErrorResponse)
      
      const result = await modelConfigApi.getModelConfigById(999)
      
      expect(result.code).toBe(404)
      expect(result.message).toBe('Model not found')
    })
  })

  describe('getModelConfigByCode', () => {
    it('should fetch model config by code', async () => {
      const mockModel = {
        id: 1,
        modelCode: 'DEEPSEEK-001',
        modelName: 'DeepSeek',
        modelType: 'OPENAI',
        maxTokens: 4096,
        temperature: 0.7,
        isActive: '1'
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockModel 
      })
      
      const result = await modelConfigApi.getModelConfigByCode('DEEPSEEK-001')
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/code/DEEPSEEK-001')
      expect(result.data).toEqual(mockModel)
    })
  })

  describe('updateModelConfig', () => {
    it('should update model configuration', async () => {
      const updateData = {
        modelName: 'Updated DeepSeek',
        temperature: 0.8
      }
      const mockResponse = { 
        code: 200, 
        message: 'Updated', 
        data: { id: 1, ...updateData } 
      }
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.updateModelConfig(1, updateData)
      
      expect(request.put).toHaveBeenCalledWith('/v1/model-configs/1', updateData)
      expect(result.data).toEqual({ id: 1, ...updateData })
    })
  })

  describe('deleteModelConfig', () => {
    it('should delete model configuration', async () => {
      const mockResponse = { code: 200, message: 'Deleted', data: null }
      vi.mocked(request.delete).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.deleteModelConfig(1)
      
      expect(request.delete).toHaveBeenCalledWith('/v1/model-configs/1')
      expect(result.message).toBe('Deleted')
    })

    it('should handle delete error', async () => {
      const mockErrorResponse = { code: 404, message: 'Model not found', data: null }
      vi.mocked(request.delete).mockResolvedValue(mockErrorResponse)
      
      const result = await modelConfigApi.deleteModelConfig(999)
      
      expect(result.code).toBe(404)
      expect(result.message).toBe('Model not found')
    })
  })

  describe('toggleModelConfigStatus', () => {
    it('should enable model config', async () => {
      const mockResponse = { 
        code: 200, 
        message: 'Status updated', 
        data: { id: 1, isActive: '1' } 
      }
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.toggleModelConfigStatus(1, '1')
      
      expect(request.put).toHaveBeenCalledWith('/v1/model-configs/1/status', null, {
        params: { isActive: '1' }
      })
      expect(result.data.isActive).toBe('1')
    })

    it('should disable model config', async () => {
      const mockResponse = { 
        code: 200, 
        message: 'Status updated', 
        data: { id: 1, isActive: '0' } 
      }
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.toggleModelConfigStatus(1, '0')
      
      expect(request.put).toHaveBeenCalledWith('/v1/model-configs/1/status', null, {
        params: { isActive: '0' }
      })
      expect(result.data.isActive).toBe('0')
    })
  })

  describe('getActiveModelConfigs', () => {
    it('should fetch all active model configs', async () => {
      const mockConfigs = [
        {
          id: 1,
          modelCode: 'DEEPSEEK-001',
          modelName: 'DeepSeek',
          modelType: 'OPENAI',
          isActive: '1',
          priority: 1
        },
        {
          id: 2,
          modelCode: 'DOUBAO-001',
          modelName: '豆包',
          modelType: 'OPENAI',
          isActive: '1',
          priority: 2
        }
      ]
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockConfigs 
      })
      
      const result = await modelConfigApi.getActiveModelConfigs()
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/active')
      expect(result.data).toEqual(mockConfigs)
    })

    it('should handle empty active configs', async () => {
      const mockResponse = { code: 200, message: 'Success', data: [] }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.getActiveModelConfigs()
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/active')
      expect(result.data).toEqual([])
    })
  })

  describe('getActiveModelConfigsByType', () => {
    it('should fetch active configs by type', async () => {
      const mockConfigs = [
        {
          id: 1,
          modelCode: 'DEEPSEEK-001',
          modelName: 'DeepSeek',
          modelType: 'OPENAI',
          isActive: '1',
          priority: 1
        }
      ]
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockConfigs 
      })
      
      const result = await modelConfigApi.getActiveModelConfigsByType('OPENAI')
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/active/type/OPENAI')
      expect(result.data).toEqual(mockConfigs)
    })

    it('should handle no active configs for type', async () => {
      const mockResponse = { code: 200, message: 'Success', data: [] }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await modelConfigApi.getActiveModelConfigsByType('CLAUDE')
      
      expect(request.get).toHaveBeenCalledWith('/v1/model-configs/active/type/CLAUDE')
      expect(result.data).toEqual([])
    })
  })
})