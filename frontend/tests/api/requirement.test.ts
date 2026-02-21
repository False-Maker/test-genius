import { describe, it, expect, vi, beforeEach } from 'vitest'
import { requirementApi } from '@/api/requirement'
import request from '@/api/request'

vi.mock('@/api/request')

describe('RequirementAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createRequirement', () => {
    it('应该创建需求', async () => {
      const mockData = { id: 1, requirementName: '测试需求' }
      vi.mocked(request.post).mockResolvedValue({ data: mockData })

      const result = await requirementApi.createRequirement(mockData as any)

      expect(result.data).toEqual(mockData)
      expect(request.post).toHaveBeenCalledWith('/v1/requirements', mockData)
    })

    it('应该处理创建失败', async () => {
      vi.mocked(request.post).mockRejectedValue(new Error('创建失败'))

      await expect(requirementApi.createRequirement({} as any)).rejects.toThrow('创建失败')
    })
  })

  describe('getRequirementList', () => {
    it('应该获取需求列表', async () => {
      const mockData = {
        content: [{ id: 1, requirementCode: 'REQ-001' }],
        totalElements: 1
      }
      vi.mocked(request.get).mockResolvedValue({ data: mockData })

      const result = await requirementApi.getRequirementList({ page: 0, size: 10 })

      expect(result.data).toEqual(mockData)
      expect(request.get).toHaveBeenCalledWith('/v1/requirements', { params: { page: 0, size: 10 } })
    })

    it('应该使用默认参数', async () => {
      vi.mocked(request.get).mockResolvedValue({ data: { content: [], totalElements: 0 } })

      await requirementApi.getRequirementList()

      expect(request.get).toHaveBeenCalledWith('/v1/requirements', { params: {} })
    })
  })

  describe('getRequirementById', () => {
    it('应该获取需求详情', async () => {
      const mockData = { id: 1, requirementCode: 'REQ-001', requirementName: '测试需求' }
      vi.mocked(request.get).mockResolvedValue({ data: mockData })

      const result = await requirementApi.getRequirementById(1)

      expect(result.data).toEqual(mockData)
      expect(request.get).toHaveBeenCalledWith('/v1/requirements/1')
    })
  })

  describe('updateRequirement', () => {
    it('应该更新需求', async () => {
      const mockData = { id: 1, requirementName: '更新后的需求' }
      vi.mocked(request.put).mockResolvedValue({ data: mockData })

      const result = await requirementApi.updateRequirement(1, mockData as any)

      expect(result.data).toEqual(mockData)
      expect(request.put).toHaveBeenCalledWith('/v1/requirements/1', mockData)
    })
  })

  describe('deleteRequirement', () => {
    it('应该删除需求', async () => {
      vi.mocked(request.delete).mockResolvedValue({ data: null })

      await requirementApi.deleteRequirement(1)

      expect(request.delete).toHaveBeenCalledWith('/v1/requirements/1')
    })
  })

  describe('updateRequirementStatus', () => {
    it('应该更新需求状态', async () => {
      const mockData = { id: 1, status: 'ACTIVE' }
      vi.mocked(request.put).mockResolvedValue({ data: mockData })

      const result = await requirementApi.updateRequirementStatus(1, 'ACTIVE')

      expect(result.data).toEqual(mockData)
      expect(request.put).toHaveBeenCalledWith('/v1/requirements/1/status', null, {
        params: { status: 'ACTIVE' }
      })
    })
  })
})
