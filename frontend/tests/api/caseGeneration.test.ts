import { describe, it, expect, vi, beforeEach } from 'vitest'
import { caseGenerationApi } from '@/api/caseGeneration'
import request from '@/api/request'

vi.mock('@/api/request')

describe('CaseGenerationAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('generateTestCases', () => {
    it('应该生成测试用例', async () => {
      const mockRequest = { requirementId: 1, layerCode: 'L1' }
      const mockResult = { taskId: 1, status: 'PENDING' }
      vi.mocked(request.post).mockResolvedValue({ data: mockResult })

      const result = await caseGenerationApi.generateTestCases(mockRequest as any)

      expect(result.data).toEqual(mockResult)
      expect(request.post).toHaveBeenCalledWith('/v1/case-generation/generate', mockRequest)
    })
  })

  describe('batchGenerateTestCases', () => {
    it('应该批量生成测试用例', async () => {
      const mockRequests = [
        { requirementId: 1, layerCode: 'L1' },
        { requirementId: 2, layerCode: 'L1' }
      ]
      const mockResult = { taskIds: [1, 2], totalTasks: 2 }
      vi.mocked(request.post).mockResolvedValue({ data: mockResult })

      const result = await caseGenerationApi.batchGenerateTestCases(mockRequests as any)

      expect(result.data).toEqual(mockResult)
      expect(request.post).toHaveBeenCalledWith('/v1/case-generation/batch-generate', {
        requirementIds: [1, 2],
        layerCode: 'L1',
        methodCode: undefined,
        templateId: undefined,
        modelCode: undefined,
        creatorId: undefined
      })
    })

    it('应该处理空数组', async () => {
      vi.mocked(request.post).mockResolvedValue({ data: { taskIds: [], totalTasks: 0 } })

      const result = await caseGenerationApi.batchGenerateTestCases([])

      expect(result.data).toEqual({ taskIds: [], totalTasks: 0 })
    })
  })

  describe('getGenerationTask', () => {
    it('应该获取生成任务', async () => {
      const mockTask = {
        id: 1,
        requirementId: 1,
        status: 'COMPLETED',
        progress: 100,
        totalCases: 10,
        successCases: 10,
        failCases: 0
      }
      vi.mocked(request.get).mockResolvedValue({ data: mockTask })

      const result = await caseGenerationApi.getGenerationTask(1)

      expect(result.data).toEqual(mockTask)
      expect(request.get).toHaveBeenCalledWith('/v1/case-generation/1')
    })
  })

  describe('getTaskList', () => {
    it('应该获取任务列表', async () => {
      const mockData = {
        list: [
          { id: 1, taskCode: 'TASK-001', requirementId: 1, taskStatus: 'PENDING' }
        ],
        total: 1,
        page: 0,
        size: 10
      }
      vi.mocked(request.post).mockResolvedValue({ data: mockData })

      const result = await caseGenerationApi.getTaskList({ page: 0, size: 10 })

      expect(result.data).toEqual(mockData)
      expect(request.post).toHaveBeenCalledWith('/v1/case-generation/tasks/list', {
        page: 0,
        size: 10
      })
    })
  })

  describe('getTaskDetail', () => {
    it('应该获取任务详情', async () => {
      const mockDetail = {
        id: 1,
        taskCode: 'TASK-001',
        requirementId: 1,
        taskStatus: 'COMPLETED',
        cases: [
          { id: 1, caseName: '测试用例1' }
        ]
      }
      vi.mocked(request.get).mockResolvedValue({ data: mockDetail })

      const result = await caseGenerationApi.getTaskDetail(1)

      expect(result.data).toEqual(mockDetail)
      expect(request.get).toHaveBeenCalledWith('/v1/case-generation/tasks/1')
    })
  })

  describe('exportTaskToExcel', () => {
    it('应该导出任务到Excel', async () => {
      const mockBlob = new Blob(['excel data'])
      vi.mocked(request.get).mockResolvedValue({ data: mockBlob })

      const result = await caseGenerationApi.exportTaskToExcel(1)

      expect(result.data).toEqual(mockBlob)
      expect(request.get).toHaveBeenCalledWith('/v1/case-generation/tasks/1/export-excel', {
        responseType: 'blob',
        responseMode: 'blob'
      })
    })
  })
})
