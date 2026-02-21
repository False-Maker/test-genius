import { describe, it, expect, vi, beforeEach } from 'vitest'
import { llmApi } from '@/api/llm'
import request from '@/api/request'

vi.mock('@/api/request')

describe('LLMApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('callModel', () => {
    it('should successfully call a model', async () => {
      const requestData = {
        model_code: 'DEEPSEEK-001',
        prompt: 'Test prompt',
        max_tokens: 1000,
        temperature: 0.7
      }
      
      const responseData = {
        content: 'This is the AI response',
        model_code: 'DEEPSEEK-001',
        tokens_used: 150,
        response_time: 1250
      }
      
      const mockResponse = { code: 200, message: 'Success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.callModel(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/call', requestData)
      expect(result.data).toEqual(responseData)
      expect(result.data.content).toBe('This is the AI response')
      expect(result.data.model_code).toBe('DEEPSEEK-001')
      expect(result.data.tokens_used).toBe(150)
    })

    it('should call model with default parameters', async () => {
      const requestData = {
        model_code: 'DEEPSEEK-001',
        prompt: 'Simple prompt'
      }
      
      const responseData = {
        content: 'Response without optional params',
        model_code: 'DEEPSEEK-001'
      }
      
      const mockResponse = { code: 200, message: 'Success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.callModel(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/call', requestData)
      expect(result.data).toEqual(responseData)
    })

    it('should handle model error response', async () => {
      const requestData = {
        model_code: 'INVALID-MODEL',
        prompt: 'Test prompt'
      }
      
      const responseData = {
        content: '',
        model_code: 'INVALID-MODEL',
        error: 'Model not found'
      }
      
      const mockResponse = { code: 400, message: 'Model error', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.callModel(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/call', requestData)
      expect(result.code).toBe(400)
      expect(result.data.error).toBe('Model not found')
    })

    it('should handle API server error', async () => {
      const requestData = {
        model_code: 'DEEPSEEK-001',
        prompt: 'Test prompt'
      }
      
      const mockErrorResponse = { code: 500, message: 'Internal server error', data: null }
      vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
      
      const result = await llmApi.callModel(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/call', requestData)
      expect(result.code).toBe(500)
      expect(result.message).toBe('Internal server error')
    })

    it('should handle network error', async () => {
      const requestData = {
        model_code: 'DEEPSEEK-001',
        prompt: 'Test prompt'
      }
      
      vi.mocked(request.post).mockRejectedValue(new Error('Network error'))
      
      await expect(llmApi.callModel(requestData)).rejects.toThrow('Network error')
    })
  })

  describe('parallelCall', () => {
    it('should successfully call multiple models in parallel', async () => {
      const requestData = {
        prompt: 'Compare models response',
        model_codes: ['DEEPSEEK-001', 'DOUBAO-001', 'KIMI-001'],
        max_tokens: 500,
        temperature: 0.7,
        max_workers: 3
      }
      
      const responseData = {
        results: [
          {
            content: 'Response from DeepSeek',
            model_code: 'DEEPSEEK-001',
            tokens_used: 120,
            response_time: 800
          },
          {
            content: 'Response from 豆包',
            model_code: 'DOUBAO-001',
            tokens_used: 135,
            response_time: 950
          },
          {
            content: 'Response from Kimi',
            model_code: 'KIMI-001',
            tokens_used: 110,
            response_time: 750
          }
        ],
        total_time: 1100,
        success_count: 3,
        fail_count: 0
      }
      
      const mockResponse = { code: 200, message: 'Success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/parallel-call', requestData)
      expect(result.data).toEqual(responseData)
      expect(result.data.results).toHaveLength(3)
      expect(result.data.success_count).toBe(3)
      expect(result.data.fail_count).toBe(0)
      expect(result.data.total_time).toBe(1100)
    })

    it('should handle partial failures in parallel call', async () => {
      const requestData = {
        prompt: 'Test parallel call',
        model_codes: ['DEEPSEEK-001', 'INVALID-MODEL', 'DOUBAO-001']
      }
      
      const responseData = {
        results: [
          {
            content: 'DeepSeek response',
            model_code: 'DEEPSEEK-001',
            tokens_used: 100
          },
          {
            content: '',
            model_code: 'INVALID-MODEL',
            error: 'Model not found'
          },
          {
            content: '豆包 response',
            model_code: 'DOUBAO-001',
            tokens_used: 120
          }
        ],
        total_time: 1200,
        success_count: 2,
        fail_count: 1
      }
      
      const mockResponse = { code: 200, message: 'Partial success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(result.data.success_count).toBe(2)
      expect(result.data.fail_count).toBe(1)
      expect(result.data.results).toHaveLength(3)
    })

    it('should handle parallel call with default parameters', async () => {
      const requestData = {
        prompt: 'Simple parallel test',
        model_codes: ['DEEPSEEK-001']
      }
      
      const responseData = {
        results: [
          {
            content: 'Single model response',
            model_code: 'DEEPSEEK-001',
            tokens_used: 80
          }
        ],
        total_time: 500,
        success_count: 1,
        fail_count: 0
      }
      
      const mockResponse = { code: 200, message: 'Success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/parallel-call', requestData)
      expect(result.data.results).toHaveLength(1)
      expect(result.data.success_count).toBe(1)
    })

    it('should handle empty model codes array', async () => {
      const requestData = {
        prompt: 'No models to call',
        model_codes: []
      }
      
      const responseData = {
        results: [],
        total_time: 0,
        success_count: 0,
        fail_count: 0
      }
      
      const mockResponse = { code: 200, message: 'No models to call', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(request.post).toHaveBeenCalledWith('/api/v1/llm/parallel-call', requestData)
      expect(result.data.results).toEqual([])
      expect(result.data.success_count).toBe(0)
      expect(result.data.fail_count).toBe(0)
    })

    it('should handle parallel call API server error', async () => {
      const requestData = {
        prompt: 'Test error handling',
        model_codes: ['DEEPSEEK-001']
      }
      
      const mockErrorResponse = { code: 500, message: 'Parallel processing failed', data: null }
      vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(result.code).toBe(500)
      expect(result.message).toBe('Parallel processing failed')
    })

    it('should handle parallel call network error', async () => {
      const requestData = {
        prompt: 'Network test',
        model_codes: ['DEEPSEEK-001']
      }
      
      vi.mocked(request.post).mockRejectedValue(new Error('Network connection failed'))
      
      await expect(llmApi.parallelCall(requestData)).rejects.toThrow('Network connection failed')
    })

    it('should calculate performance metrics correctly', async () => {
      const requestData = {
        prompt: 'Performance test',
        model_codes: ['MODEL1', 'MODEL2', 'MODEL3']
      }
      
      const responseData = {
        results: [
          { content: 'Response 1', model_code: 'MODEL1', tokens_used: 100, response_time: 500 },
          { content: 'Response 2', model_code: 'MODEL2', tokens_used: 150, response_time: 800 },
          { content: 'Response 3', model_code: 'MODEL3', tokens_used: 120, response_time: 600 }
        ],
        total_time: 900,
        success_count: 3,
        fail_count: 0
      }
      
      const mockResponse = { code: 200, message: 'Success', data: responseData }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await llmApi.parallelCall(requestData)
      
      expect(result.data.total_time).toBe(900)
      expect(result.data.success_count).toBe(3)
      expect(result.data.fail_count).toBe(0)
      
      const totalTokens = result.data.results.reduce((sum, r) => sum + r.tokens_used, 0)
      expect(totalTokens).toBe(370)
    })
  })
})