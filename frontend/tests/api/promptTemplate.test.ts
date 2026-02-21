import { describe, it, expect, vi, beforeEach } from 'vitest'
import { promptTemplateApi, promptTemplateVersionApi, promptTemplateAbTestApi } from '@/api/promptTemplate'
import request from '@/api/request'

vi.mock('@/api/request')

describe('PromptTemplateAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createTemplate', () => {
    it('should create a new template', async () => {
      const mockTemplate = {
        id: 1,
        templateName: 'Test Template',
        templateContent: 'Test content',
        templateCode: 'TEMPLATE-001'
      }
      
      const mockResponse = { code: 200, message: 'Created', data: mockTemplate }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.createTemplate(mockTemplate)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates', mockTemplate)
      expect(result).toEqual(mockResponse)
      expect(result.data).toEqual(mockTemplate)
    })

    it('should handle error response', async () => {
      const mockErrorResponse = { code: 400, message: 'Invalid data', data: null }
      vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
      
      const result = await promptTemplateApi.createTemplate({
        templateName: 'Test',
        templateContent: 'Content'
      })
      
      expect(result.code).toBe(400)
      expect(result.message).toBe('Invalid data')
    })
  })

  describe('getTemplateList', () => {
    it('should fetch template list with pagination', async () => {
      const mockData = {
        content: [{ id: 1, templateName: 'Template 1' }],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0
      }
      
      const mockResponse = { code: 200, message: 'Success', data: mockData }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.getTemplateList({ page: 0, size: 10 })
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates', { 
        params: { page: 0, size: 10 } 
      })
      expect(result.data).toEqual(mockData)
    })

    it('should handle default parameters', async () => {
      const mockResponse = { code: 200, message: 'Success', data: { content: [], totalElements: 0 } }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.getTemplateList()
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates', { 
        params: {} 
      })
      expect(result.data).toEqual({ content: [], totalElements: 0 })
    })
  })

  describe('getTemplateById', () => {
    it('should fetch template by id', async () => {
      const mockTemplate = {
        id: 1,
        templateName: 'Test Template',
        templateContent: 'Test content'
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockTemplate 
      })
      
      const result = await promptTemplateApi.getTemplateById(1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1')
      expect(result.data).toEqual(mockTemplate)
    })
  })

  describe('updateTemplate', () => {
    it('should update template', async () => {
      const updateData = { templateName: 'Updated Template' }
      const mockResponse = { 
        code: 200, 
        message: 'Updated', 
        data: { id: 1, ...updateData } 
      }
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.updateTemplate(1, updateData)
      
      expect(request.put).toHaveBeenCalledWith('/v1/prompt-templates/1', updateData)
      expect(result.data).toEqual({ id: 1, ...updateData })
    })
  })

  describe('deleteTemplate', () => {
    it('should delete template', async () => {
      const mockResponse = { code: 200, message: 'Deleted', data: null }
      vi.mocked(request.delete).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.deleteTemplate(1)
      
      expect(request.delete).toHaveBeenCalledWith('/v1/prompt-templates/1')
      expect(result.message).toBe('Deleted')
    })
  })

  describe('toggleTemplateStatus', () => {
    it('should toggle template status', async () => {
      const mockResponse = { 
        code: 200, 
        message: 'Status updated', 
        data: { id: 1, isActive: '0' } 
      }
      vi.mocked(request.put).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.toggleTemplateStatus(1, '0')
      
      expect(request.put).toHaveBeenCalledWith('/v1/prompt-templates/1/status', null, {
        params: { isActive: '0' }
      })
      expect(result.data.isActive).toBe('0')
    })
  })

  describe('generatePrompt', () => {
    it('should generate prompt from template', async () => {
      const variables = { variable1: 'test' }
      const mockResponse = { code: 200, message: 'Generated', data: 'Generated prompt content' }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.generatePrompt(1, variables)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates/1/generate', variables)
      expect(result.data).toBe('Generated prompt content')
    })
  })

  describe('generatePromptWithContent', () => {
    it('should generate prompt from custom content', async () => {
      const templateContent = 'Custom template {{variable}}'
      const variables = { variable: 'value' }
      const mockResponse = { code: 200, message: 'Generated', data: 'Custom prompt content' }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateApi.generatePromptWithContent(templateContent, variables)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates/generate', variables, {
        params: { templateContent }
      })
      expect(result.data).toBe('Custom prompt content')
    })
  })
})

describe('PromptTemplateVersionAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createVersion', () => {
    it('should create a new version', async () => {
      const versionData = {
        versionNumber: 2,
        templateContent: 'Version 2 content'
      }
      const mockResponse = { code: 200, message: 'Created', data: { ...versionData, id: 1 } }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateVersionApi.createVersion(1, versionData)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates/1/versions', versionData)
      expect(result.data).toEqual({ ...versionData, id: 1 })
    })
  })

  describe('getVersions', () => {
    it('should fetch version list', async () => {
      const mockVersions = [
        { id: 1, versionNumber: 1, templateContent: 'Version 1' },
        { id: 2, versionNumber: 2, templateContent: 'Version 2' }
      ]
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockVersions 
      })
      
      const result = await promptTemplateVersionApi.getVersions(1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1/versions')
      expect(result.data).toEqual(mockVersions)
    })
  })

  describe('getCurrentVersion', () => {
    it('should fetch current version', async () => {
      const mockVersion = { 
        id: 1, 
        versionNumber: 2, 
        isCurrent: '1', 
        templateContent: 'Current version' 
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockVersion 
      })
      
      const result = await promptTemplateVersionApi.getCurrentVersion(1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1/versions/current')
      expect(result.data).toEqual(mockVersion)
    })
  })

  describe('compareVersions', () => {
    it('should compare two versions', async () => {
      const mockCompareResult = {
        version1: { id: 1, templateContent: 'Version 1' },
        version2: { id: 2, templateContent: 'Version 2' },
        diff: { contentChanged: true, variablesChanged: false }
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockCompareResult 
      })
      
      const result = await promptTemplateVersionApi.compareVersions(1, 1, 2)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1/versions/compare', {
        params: { versionNumber1: 1, versionNumber2: 2 }
      })
      expect(result.data).toEqual(mockCompareResult)
    })
  })
})

describe('PromptTemplateAbTestAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createAbTest', () => {
    it('should create A/B test', async () => {
      const testData = {
        testName: 'Performance Test',
        versionAId: 1,
        versionBId: 2
      }
      const mockResponse = { code: 200, message: 'Created', data: { ...testData, id: 1 } }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateAbTestApi.createAbTest(1, testData)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates/1/ab-tests', testData)
      expect(result.data).toEqual({ ...testData, id: 1 })
    })
  })

  describe('getAbTests', () => {
    it('should fetch A/B test list', async () => {
      const mockTests = [
        { id: 1, testName: 'Test 1', status: 'draft' },
        { id: 2, testName: 'Test 2', status: 'running' }
      ]
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockTests 
      })
      
      const result = await promptTemplateAbTestApi.getAbTests(1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1/ab-tests')
      expect(result.data).toEqual(mockTests)
    })
  })

  describe('startAbTest', () => {
    it('should start A/B test', async () => {
      const mockResponse = { 
        code: 200, 
        message: 'Started', 
        data: { id: 1, status: 'running' } 
      }
      vi.mocked(request.post).mockResolvedValue(mockResponse)
      
      const result = await promptTemplateAbTestApi.startAbTest(1, 1)
      
      expect(request.post).toHaveBeenCalledWith('/v1/prompt-templates/1/ab-tests/1/start')
      expect(result.data.status).toBe('running')
    })
  })

  describe('getAbTestStatistics', () => {
    it('should fetch A/B test statistics', async () => {
      const mockStats = {
        versionA: { totalExecutions: 10, successCount: 8, successRate: 0.8 },
        versionB: { totalExecutions: 10, successCount: 7, successRate: 0.7 },
        totalExecutions: 20,
        betterVersion: 'A'
      }
      
      vi.mocked(request.get).mockResolvedValue({ 
        code: 200, 
        message: 'Success', 
        data: mockStats 
      })
      
      const result = await promptTemplateAbTestApi.getAbTestStatistics(1, 1)
      
      expect(request.get).toHaveBeenCalledWith('/v1/prompt-templates/1/ab-tests/1/statistics')
      expect(result.data).toEqual(mockStats)
    })
  })
})