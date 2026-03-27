import { describe, it, expect, vi, beforeEach } from 'vitest'
import { testCaseApi } from '@/api/testCase'
import request from '@/api/request'

vi.mock('@/api/request')

describe('TestCaseAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createTestCase', () => {
    it('应该创建测试用例', async () => {
      const mockData = { id: 1, caseName: '测试用例' }
      vi.mocked(request.post).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.createTestCase(mockData as any)

      expect(result.data).toEqual(mockData)
      expect(request.post).toHaveBeenCalledWith('/v1/test-cases', mockData)
    })
  })

  describe('getTestCaseList', () => {
    it('应该获取用例列表', async () => {
      const mockData = {
        content: [{ id: 1, caseCode: 'TC-001' }],
        totalElements: 1
      }
      vi.mocked(request.get).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.getTestCaseList({ page: 0, size: 10 })

      expect(result.data).toEqual(mockData)
      expect(request.get).toHaveBeenCalledWith('/v1/test-cases', { params: { page: 0, size: 10 } })
    })
  })

  describe('getTestCaseById', () => {
    it('应该获取用例详情', async () => {
      const mockData = { id: 1, caseCode: 'TC-001', caseName: '测试用例' }
      vi.mocked(request.get).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.getTestCaseById(1)

      expect(result.data).toEqual(mockData)
      expect(request.get).toHaveBeenCalledWith('/v1/test-cases/1')
    })
  })

  describe('updateTestCase', () => {
    it('应该更新用例', async () => {
      const mockData = { id: 1, caseName: '更新后的用例' }
      vi.mocked(request.put).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.updateTestCase(1, mockData as any)

      expect(result.data).toEqual(mockData)
      expect(request.put).toHaveBeenCalledWith('/v1/test-cases/1', mockData)
    })
  })

  describe('deleteTestCase', () => {
    it('应该删除用例', async () => {
      vi.mocked(request.delete).mockResolvedValue({ data: null })

      await testCaseApi.deleteTestCase(1)

      expect(request.delete).toHaveBeenCalledWith('/v1/test-cases/1')
    })
  })

  describe('updateCaseStatus', () => {
    it('应该更新用例状态', async () => {
      const mockData = { id: 1, caseStatus: 'ACTIVE' }
      vi.mocked(request.put).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.updateCaseStatus(1, 'ACTIVE')

      expect(result.data).toEqual(mockData)
      expect(request.put).toHaveBeenCalledWith('/v1/test-cases/1/status', null, {
        params: { status: 'ACTIVE' }
      })
    })
  })

  describe('reviewTestCase', () => {
    it('应该审核用例', async () => {
      const mockData = { id: 1, reviewResult: 'APPROVED' }
      vi.mocked(request.post).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.reviewTestCase(1, 'APPROVED', '审核通过')

      expect(result.data).toEqual(mockData)
      expect(request.post).toHaveBeenCalledWith('/v1/test-cases/1/review', null, {
        params: { reviewResult: 'APPROVED', reviewComment: '审核通过' }
      })
    })
  })

  describe('exportTestCases', () => {
    it('应该导出用例', async () => {
      const mockBlob = new Blob(['test data'])
      vi.mocked(request.get).mockResolvedValue({ data: mockBlob })

      const result = await testCaseApi.exportTestCases({ caseName: '测试' })

      expect(result.data).toEqual(mockBlob)
      expect(request.get).toHaveBeenCalledWith('/v1/test-cases/export', {
        params: { caseName: '测试' },
        responseType: 'blob',
        responseMode: 'blob'
      })
    })
  })

  describe('exportTemplate', () => {
    it('应该导出模板', async () => {
      const mockBlob = new Blob(['template data'])
      vi.mocked(request.get).mockResolvedValue({ data: mockBlob })

      const result = await testCaseApi.exportTemplate()

      expect(result.data).toEqual(mockBlob)
      expect(request.get).toHaveBeenCalledWith('/v1/test-cases/export-template', {
        responseType: 'blob',
        responseMode: 'blob'
      })
    })
  })

  describe('importTestCases', () => {
    it('应该导入用例', async () => {
      const mockData = { successCount: 10, failCount: 0 }
      const mockFile = new File(['test'], 'test.xlsx')
      vi.mocked(request.post).mockResolvedValue({ data: mockData })

      const result = await testCaseApi.importTestCases(mockFile)

      expect(result.data).toEqual(mockData)
      expect(request.post).toHaveBeenCalledWith('/v1/test-cases/import', expect.any(FormData), {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    })
  })
})
