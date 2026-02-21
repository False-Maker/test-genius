import { describe, it, expect, vi, beforeEach } from 'vitest'
import { workflowApi } from '@/api/workflow'
import request from '@/api/request'

vi.mock('@/api/request')

describe('WorkflowAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Workflow Definition Management', () => {
    describe('createWorkflow', () => {
      it('should create a new workflow definition', async () => {
        const workflowData = {
          workflowCode: 'TEST-WORKFLOW-001',
          workflowName: 'Test Workflow',
          workflowDescription: 'A test workflow',
          workflowType: 'TEST_GENERATION',
          workflowConfig: '{"nodes": [], "edges": []}',
          version: 1,
          isActive: true,
          isDefault: false
        }
        
        const mockResponse = { 
          code: 200, 
          message: 'Created', 
          data: { ...workflowData, id: 1, executionCount: 0 } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.createWorkflow(workflowData)
        
        expect(request.post).toHaveBeenCalledWith('/v1/workflows', workflowData)
        expect(result.data).toEqual({ ...workflowData, id: 1, executionCount: 0 })
      })

      it('should handle workflow creation error', async () => {
        const mockErrorResponse = { code: 400, message: 'Invalid workflow data', data: null }
        vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
        
        const result = await workflowApi.createWorkflow({
          workflowCode: 'TEST-001',
          workflowName: 'Test Workflow',
          workflowConfig: '{}',
          version: 1,
          isActive: true,
          isDefault: false
        })
        
        expect(result.code).toBe(400)
        expect(result.message).toBe('Invalid workflow data')
      })
    })

    describe('updateWorkflow', () => {
      it('should update workflow definition', async () => {
        const updateData = {
          workflowName: 'Updated Test Workflow',
          workflowConfig: '{"nodes": [{"id": "1", "type": "start", "name": "Start"}]}'
        }
        const mockResponse = { 
          code: 200, 
          message: 'Updated', 
          data: { ...updateData, id: 1 } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.updateWorkflow(1, updateData)
        
        expect(request.put).toHaveBeenCalledWith('/v1/workflows/1', updateData)
        expect(result.data).toEqual({ ...updateData, id: 1 })
      })
    })

    describe('getWorkflow', () => {
      it('should fetch workflow by id', async () => {
        const mockWorkflow = {
          id: 1,
          workflowCode: 'TEST-WORKFLOW-001',
          workflowName: 'Test Workflow',
          workflowType: 'TEST_GENERATION',
          workflowConfig: '{"nodes": [], "edges": []}',
          version: 1,
          isActive: true,
          isDefault: false,
          executionCount: 5
        }
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockWorkflow 
        })
        
        const result = await workflowApi.getWorkflow(1)
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows/1')
        expect(result.data).toEqual(mockWorkflow)
      })

      it('should handle workflow not found', async () => {
        const mockErrorResponse = { code: 404, message: 'Workflow not found', data: null }
        vi.mocked(request.get).mockResolvedValue(mockErrorResponse)
        
        const result = await workflowApi.getWorkflow(999)
        
        expect(result.code).toBe(404)
        expect(result.message).toBe('Workflow not found')
      })
    })

    describe('getWorkflowByCode', () => {
      it('should fetch workflow by code', async () => {
        const mockWorkflow = {
          id: 1,
          workflowCode: 'TEST-WORKFLOW-001',
          workflowName: 'Test Workflow',
          workflowConfig: '{"nodes": [], "edges": []}',
          isActive: true
        }
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockWorkflow 
        })
        
        const result = await workflowApi.getWorkflowByCode('TEST-WORKFLOW-001')
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows/code/TEST-WORKFLOW-001')
        expect(result.data).toEqual(mockWorkflow)
      })
    })

    describe('getAllWorkflows', () => {
      it('should fetch all workflows', async () => {
        const mockWorkflows = [
          {
            id: 1,
            workflowCode: 'WORKFLOW-001',
            workflowName: 'Test Generation Workflow',
            workflowType: 'TEST_GENERATION',
            isActive: true
          },
          {
            id: 2,
            workflowCode: 'WORKFLOW-002',
            workflowName: 'Case Optimization Workflow',
            workflowType: 'CASE_OPTIMIZATION',
            isActive: true
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockWorkflows 
        })
        
        const result = await workflowApi.getAllWorkflows()
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows')
        expect(result.data).toEqual(mockWorkflows)
      })
    })

    describe('getWorkflowsByType', () => {
      it('should fetch workflows by type', async () => {
        const mockWorkflows = [
          {
            id: 1,
            workflowCode: 'TEST-WORKFLOW-001',
            workflowName: 'Test Workflow',
            workflowType: 'TEST_GENERATION',
            isActive: true
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockWorkflows 
        })
        
        const result = await workflowApi.getWorkflowsByType('TEST_GENERATION')
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows/type/TEST_GENERATION')
        expect(result.data).toEqual(mockWorkflows)
      })
    })

    describe('getActiveWorkflows', () => {
      it('should fetch all active workflows', async () => {
        const mockWorkflows = [
          {
            id: 1,
            workflowCode: 'ACTIVE-WORKFLOW-001',
            workflowName: 'Active Workflow',
            isActive: true
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockWorkflows 
        })
        
        const result = await workflowApi.getActiveWorkflows()
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows/active')
        expect(result.data).toEqual(mockWorkflows)
      })
    })

    describe('toggleActive', () => {
      it('should activate workflow', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Activated', 
          data: { id: 1, isActive: true } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.toggleActive(1, true)
        
        expect(request.put).toHaveBeenCalledWith('/v1/workflows/1/toggle-active', null, {
          params: { isActive: true }
        })
        expect(result.data.isActive).toBe(true)
      })

      it('should deactivate workflow', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Deactivated', 
          data: { id: 1, isActive: false } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.toggleActive(1, false)
        
        expect(request.put).toHaveBeenCalledWith('/v1/workflows/1/toggle-active', null, {
          params: { isActive: false }
        })
        expect(result.data.isActive).toBe(false)
      })
    })

    describe('setDefault', () => {
      it('should set workflow as default', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Set as default', 
          data: { id: 1, isDefault: true } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.setDefault(1, true)
        
        expect(request.put).toHaveBeenCalledWith('/v1/workflows/1/set-default', null, {
          params: { isDefault: true }
        })
        expect(result.data.isDefault).toBe(true)
      })
    })
  })

  describe('Workflow Version Management', () => {
    describe('createVersion', () => {
      it('should create workflow version', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Version created', 
          data: { 
            id: 1, 
            workflowId: 1, 
            version: 2,
            workflowConfig: '{"nodes": [], "edges": []}'
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.createVersion(1, 'Version 2 description')
        
        expect(request.post).toHaveBeenCalledWith('/v1/workflows/1/versions', null, {
          params: { versionDescription: 'Version 2 description' }
        })
        expect(result.data.version).toBe(2)
      })
    })

    describe('getVersions', () => {
      it('should fetch workflow versions', async () => {
        const mockVersions = [
          {
            id: 1,
            workflowId: 1,
            version: 1,
            workflowConfig: '{"nodes": [], "edges": []}',
            isCurrent: '1'
          },
          {
            id: 2,
            workflowId: 1,
            version: 2,
            workflowConfig: '{"nodes": [{"id": "1", "type": "start"}], "edges": []}',
            isCurrent: '0'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockVersions 
        })
        
        const result = await workflowApi.getVersions(1)
        
        expect(request.get).toHaveBeenCalledWith('/v1/workflows/1/versions')
        expect(result.data).toEqual(mockVersions)
      })
    })

    describe('rollbackToVersion', () => {
      it('should rollback to specified version', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Rolled back', 
          data: { 
            id: 1, 
            version: 1,
            workflowConfig: '{"nodes": [], "edges": []}'
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.rollbackToVersion(1, 1)
        
        expect(request.post).toHaveBeenCalledWith('/v1/workflows/1/rollback', null, {
          params: { version: 1 }
        })
        expect(result.data.version).toBe(1)
      })
    })
  })

  describe('Workflow Execution', () => {
    describe('executeWorkflow', () => {
      it('should execute workflow successfully', async () => {
        const workflowConfig = '{"nodes": [{"id": "1", "type": "start"}], "edges": []}'
        const inputData = { requirementId: 1 }
        
        const mockResponse = { 
          code: 200, 
          message: 'Execution started', 
          data: {
            execution_id: 'exec-001',
            status: 'RUNNING',
            output: null,
            error: null
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.executeWorkflow(workflowConfig, inputData, 1)
        
        expect(request.post).toHaveBeenCalledWith('/api/v1/workflow/execute', {
          workflow_config: workflowConfig,
          input_data: inputData,
          workflow_id: 1,
          workflow_code: undefined,
          workflow_version: undefined
        })
        expect(result.data.execution_id).toBe('exec-001')
        expect(result.data.status).toBe('RUNNING')
      })

      it('should execute workflow with all parameters', async () => {
        const workflowConfig = '{"nodes": [], "edges": []}'
        const inputData = { test: 'data' }
        
        const mockResponse = { 
          code: 200, 
          message: 'Execution started', 
          data: {
            execution_id: 'exec-002',
            status: 'COMPLETED',
            output: { result: 'success' }
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.executeWorkflow(
          workflowConfig, 
          inputData, 
          1, 
          'TEST-WORKFLOW-001', 
          2
        )
        
        expect(request.post).toHaveBeenCalledWith('/api/v1/workflow/execute', {
          workflow_config: workflowConfig,
          input_data: inputData,
          workflow_id: 1,
          workflow_code: 'TEST-WORKFLOW-001',
          workflow_version: 2
        })
        expect(result.data.status).toBe('COMPLETED')
      })

      it('should handle workflow execution error', async () => {
        const mockResponse = { 
          code: 400, 
          message: 'Invalid workflow config', 
          data: {
            execution_id: null,
            status: 'FAILED',
            error: 'Invalid workflow configuration'
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.executeWorkflow(
          'invalid-config', 
          { test: 'data' }
        )
        
        expect(result.data.status).toBe('FAILED')
        expect(result.data.error).toBe('Invalid workflow configuration')
      })
    })

    describe('validateWorkflowConfig', () => {
      it('should validate workflow config successfully', async () => {
        const workflowConfig = '{"nodes": [{"id": "1", "type": "start"}], "edges": []}'
        
        const mockResponse = { 
          code: 200, 
          message: 'Validation successful', 
          data: {
            valid: true,
            errors: [],
            warnings: []
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.validateWorkflowConfig(workflowConfig)
        
        expect(request.post).toHaveBeenCalledWith('/v1/workflows/validate', {
          workflowConfig
        })
        expect(result.data.valid).toBe(true)
        expect(result.data.errors).toEqual([])
        expect(result.data.warnings).toEqual([])
      })

      it('should handle invalid workflow config', async () => {
        const mockResponse = { 
          code: 400, 
          message: 'Validation failed', 
          data: {
            valid: false,
            errors: ['Missing required node: start'],
            warnings: ['Unused node detected']
          } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await workflowApi.validateWorkflowConfig('invalid-config')
        
        expect(result.data.valid).toBe(false)
        expect(result.data.errors).toContain('Missing required node: start')
        expect(result.data.warnings).toContain('Unused node detected')
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle various API errors consistently', async () => {
      const errorTests = [
        { 
          method: () => workflowApi.deleteWorkflow(999), 
          expectedCode: 404,
          mockMethod: 'delete'
        },
        { 
          method: () => workflowApi.getWorkflow(999), 
          expectedCode: 404,
          mockMethod: 'get'
        },
        { 
          method: () => workflowApi.getWorkflowByCode('INVALID-CODE'), 
          expectedCode: 404,
          mockMethod: 'get'
        }
      ]

      for (const test of errorTests) {
        const mockErrorResponse = { code: 404, message: 'Not found', data: null }
        
        if (test.mockMethod === 'delete') {
          vi.mocked(request.delete).mockResolvedValue(mockErrorResponse)
        } else {
          vi.mocked(request.get).mockResolvedValue(mockErrorResponse)
        }
        
        const result = await test.method()
        expect(result.code).toBe(test.expectedCode)
        expect(result.message).toBe('Not found')
      }
    })

    it('should handle network errors gracefully', async () => {
      vi.mocked(request.post).mockRejectedValue(new Error('Network error'))
      
      await expect(workflowApi.createWorkflow({
        workflowCode: 'TEST',
        workflowName: 'Test',
        workflowConfig: '{}',
        version: 1,
        isActive: true,
        isDefault: false
      })).rejects.toThrow('Network error')
    })

    it('should handle empty workflow list', async () => {
      const mockResponse = { code: 200, message: 'Success', data: [] }
      vi.mocked(request.get).mockResolvedValue(mockResponse)
      
      const result = await workflowApi.getAllWorkflows()
      
      expect(result.data).toEqual([])
      expect(Array.isArray(result.data)).toBe(true)
    })
  })
})