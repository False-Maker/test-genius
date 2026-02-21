import { describe, it, expect, vi, beforeEach } from 'vitest'
import { agentApi } from '@/api/agent'
import request from '@/api/request'

vi.mock('@/api/request')

describe('AgentAPI', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Agent Management', () => {
    describe('createAgent', () => {
      it('should create a new agent', async () => {
        const agentData = {
          agentCode: 'TEST-AGENT-001',
          agentName: 'Test Agent',
          agentType: 'TEST_DESIGN_ASSISTANT',
          agentDescription: 'A test agent for testing',
          systemPrompt: 'You are a helpful test assistant',
          maxIterations: 10,
          maxTokens: 4096,
          temperature: 0.7
        }
        
        const mockResponse = { 
          code: 200, 
          message: 'Created', 
          data: { ...agentData, id: 1 } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await agentApi.createAgent(agentData)
        
        expect(request.post).toHaveBeenCalledWith('/v1/agents', agentData)
        expect(result.data).toEqual({ ...agentData, id: 1 })
      })

      it('should handle agent creation error', async () => {
        const mockErrorResponse = { code: 400, message: 'Invalid agent data', data: null }
        vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
        
        const result = await agentApi.createAgent({
          agentCode: 'TEST-001',
          agentName: 'Test Agent',
          agentType: 'TEST_DESIGN_ASSISTANT'
        })
        
        expect(result.code).toBe(400)
        expect(result.message).toBe('Invalid agent data')
      })
    })

    describe('updateAgent', () => {
      it('should update an agent', async () => {
        const updateData = {
          agentName: 'Updated Test Agent',
          systemPrompt: 'Updated system prompt'
        }
        const mockResponse = { 
          code: 200, 
          message: 'Updated', 
          data: { id: 1, ...updateData } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await agentApi.updateAgent(1, updateData)
        
        expect(request.put).toHaveBeenCalledWith('/v1/agents/1', updateData)
        expect(result.data).toEqual({ id: 1, ...updateData })
      })
    })

    describe('getAgentById', () => {
      it('should fetch agent by id', async () => {
        const mockAgent = {
          id: 1,
          agentCode: 'TEST-AGENT-001',
          agentName: 'Test Agent',
          agentType: 'TEST_DESIGN_ASSISTANT',
          isActive: '1',
          systemPrompt: 'You are a test assistant'
        }
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockAgent 
        })
        
        const result = await agentApi.getAgentById(1)
        
        expect(request.get).toHaveBeenCalledWith('/v1/agents/1')
        expect(result.data).toEqual(mockAgent)
      })

      it('should handle agent not found', async () => {
        const mockErrorResponse = { code: 404, message: 'Agent not found', data: null }
        vi.mocked(request.get).mockResolvedValue(mockErrorResponse)
        
        const result = await agentApi.getAgentById(999)
        
        expect(result.code).toBe(404)
        expect(result.message).toBe('Agent not found')
      })
    })

    describe('getAgentByCode', () => {
      it('should fetch agent by code', async () => {
        const mockAgent = {
          id: 1,
          agentCode: 'TEST-AGENT-001',
          agentName: 'Test Agent',
          agentType: 'TEST_DESIGN_ASSISTANT'
        }
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockAgent 
        })
        
        const result = await agentApi.getAgentByCode('TEST-AGENT-001')
        
        expect(request.get).toHaveBeenCalledWith('/v1/agents/code/TEST-AGENT-001')
        expect(result.data).toEqual(mockAgent)
      })
    })

    describe('getAllActiveAgents', () => {
      it('should fetch all active agents', async () => {
        const mockAgents = [
          {
            id: 1,
            agentCode: 'AGENT-001',
            agentName: 'Test Design Assistant',
            agentType: 'TEST_DESIGN_ASSISTANT',
            isActive: '1'
          },
          {
            id: 2,
            agentCode: 'AGENT-002',
            agentName: 'Case Optimizer',
            agentType: 'CASE_OPTIMIZATION',
            isActive: '1'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockAgents 
        })
        
        const result = await agentApi.getAllActiveAgents()
        
        expect(request.get).toHaveBeenCalledWith('/v1/agents/active')
        expect(result.data).toEqual(mockAgents)
      })

      it('should handle empty active agents', async () => {
        const mockResponse = { code: 200, message: 'Success', data: [] }
        vi.mocked(request.get).mockResolvedValue(mockResponse)
        
        const result = await agentApi.getAllActiveAgents()
        
        expect(result.data).toEqual([])
      })
    })

    describe('getAgentsByType', () => {
      it('should fetch agents by type', async () => {
        const mockAgents = [
          {
            id: 1,
            agentCode: 'TEST-AGENT-001',
            agentName: 'Test Assistant',
            agentType: 'TEST_DESIGN_ASSISTANT'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockAgents 
        })
        
        const result = await agentApi.getAgentsByType('TEST_DESIGN_ASSISTANT')
        
        expect(request.get).toHaveBeenCalledWith('/v1/agents/type/TEST_DESIGN_ASSISTANT')
        expect(result.data).toEqual(mockAgents)
      })
    })

    describe('toggleAgentActive', () => {
      it('should activate agent', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Activated', 
          data: { id: 1, isActive: '1' } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await agentApi.toggleAgentActive(1, true)
        
        expect(request.put).toHaveBeenCalledWith('/v1/agents/1/active', null, {
          params: { active: true }
        })
        expect(result.data.isActive).toBe('1')
      })

      it('should deactivate agent', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Deactivated', 
          data: { id: 1, isActive: '0' } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await agentApi.toggleAgentActive(1, false)
        
        expect(request.put).toHaveBeenCalledWith('/v1/agents/1/active', null, {
          params: { active: false }
        })
        expect(result.data.isActive).toBe('0')
      })
    })
  })

  describe('Agent Session Management', () => {
    describe('createSession', () => {
      it('should create a new session', async () => {
        const sessionData = {
          sessionCode: 'SESSION-001',
          agentId: 1,
          sessionTitle: 'Test Session',
          contextData: { initialData: 'test' }
        }
        
        const mockResponse = { 
          code: 200, 
          message: 'Created', 
          data: { ...sessionData, id: 1 } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await agentApi.createSession(sessionData)
        
        expect(request.post).toHaveBeenCalledWith('/v1/agent-sessions', sessionData)
        expect(result.data).toEqual({ ...sessionData, id: 1 })
      })
    })

    describe('getSessionById', () => {
      it('should fetch session by id', async () => {
        const mockSession = {
          id: 1,
          sessionCode: 'SESSION-001',
          agentId: 1,
          status: 'ACTIVE',
          totalTokens: 1500,
          totalIterations: 5
        }
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockSession 
        })
        
        const result = await agentApi.getSessionById(1)
        
        expect(request.get).toHaveBeenCalledWith('/v1/agent-sessions/1')
        expect(result.data).toEqual(mockSession)
      })
    })

    describe('closeSession', () => {
      it('should close a session', async () => {
        const mockResponse = { 
          code: 200, 
          message: 'Closed', 
          data: { id: 1, status: 'CLOSED' } 
        }
        vi.mocked(request.put).mockResolvedValue(mockResponse)
        
        const result = await agentApi.closeSession(1)
        
        expect(request.put).toHaveBeenCalledWith('/v1/agent-sessions/1/close')
        expect(result.data.status).toBe('CLOSED')
      })
    })
  })

  describe('Agent Chat and Messages', () => {
    describe('chatWithAgent', () => {
      it('should send message to agent', async () => {
        const chatData = {
          session_id: 1,
          message: 'Hello, how are you?'
        }
        
        const mockResponse = { 
          code: 200, 
          message: 'Message sent', 
          data: { reply: 'I am doing well, thank you!' } 
        }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await agentApi.chatWithAgent(1, 'Hello, how are you?')
        
        expect(request.post).toHaveBeenCalledWith('/api/v1/agent/chat', chatData)
        expect(result.data).toEqual({ reply: 'I am doing well, thank you!' })
      })

      it('should handle chat error', async () => {
        const mockErrorResponse = { code: 400, message: 'Invalid session', data: null }
        vi.mocked(request.post).mockResolvedValue(mockErrorResponse)
        
        const result = await agentApi.chatWithAgent(999, 'Hello')
        
        expect(result.code).toBe(400)
        expect(result.message).toBe('Invalid session')
      })
    })

    describe('getSessionHistory', () => {
      it('should fetch session history', async () => {
        const mockHistory = [
          {
            id: 1,
            sessionId: 1,
            messageType: 'USER',
            role: 'user',
            content: 'Hello',
            createTime: '2024-01-01T10:00:00'
          },
          {
            id: 2,
            sessionId: 1,
            messageType: 'ASSISTANT',
            role: 'assistant',
            content: 'Hello there!',
            createTime: '2024-01-01T10:00:05'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockHistory 
        })
        
        const result = await agentApi.getSessionHistory(1)
        
        expect(request.get).toHaveBeenCalledWith('/api/v1/agent/sessions/1/history')
        expect(result.data).toEqual(mockHistory)
      })
    })
  })

  describe('Agent Tool Management', () => {
    describe('getAllTools', () => {
      it('should fetch all available tools', async () => {
        const mockTools = [
          {
            id: 1,
            toolCode: 'TEST-TOOL-001',
            toolName: 'Test Tool',
            toolType: 'TEST_RELATED',
            toolDescription: 'A test tool'
          },
          {
            id: 2,
            toolCode: 'GENERAL-TOOL-001',
            toolName: 'General Tool',
            toolType: 'GENERAL',
            toolDescription: 'A general tool'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockTools 
        })
        
        const result = await agentApi.getAllTools()
        
        expect(request.get).toHaveBeenCalledWith('/api/v1/agent/tools')
        expect(result.data).toEqual(mockTools)
      })
    })

    describe('getAgentTools', () => {
      it('should fetch agent tools', async () => {
        const mockTools = [
          {
            id: 1,
            toolCode: 'AGENT-TOOL-001',
            toolName: 'Agent Specific Tool',
            toolType: 'TEST_RELATED'
          }
        ]
        
        vi.mocked(request.get).mockResolvedValue({ 
          code: 200, 
          message: 'Success', 
          data: mockTools 
        })
        
        const result = await agentApi.getAgentTools(1)
        
        expect(request.get).toHaveBeenCalledWith('/v1/agent-tools/1')
        expect(result.data).toEqual(mockTools)
      })
    })

    describe('addAgentTool', () => {
      it('should add tool to agent', async () => {
        const mockResponse = { code: 200, message: 'Tool added', data: null }
        vi.mocked(request.post).mockResolvedValue(mockResponse)
        
        const result = await agentApi.addAgentTool(1, 2)
        
        expect(request.post).toHaveBeenCalledWith('/v1/agent-tools/1/2')
        expect(result.message).toBe('Tool added')
      })
    })

    describe('removeAgentTool', () => {
      it('should remove tool from agent', async () => {
        const mockResponse = { code: 200, message: 'Tool removed', data: null }
        vi.mocked(request.delete).mockResolvedValue(mockResponse)
        
        const result = await agentApi.removeAgentTool(1, 2)
        
        expect(request.delete).toHaveBeenCalledWith('/v1/agent-tools/1/2')
        expect(result.message).toBe('Tool removed')
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle various API errors consistently', async () => {
      const errorTests = [
        { method: () => agentApi.deleteAgent(999), expectedCode: 404 },
        { method: () => agentApi.deleteSession(999), expectedCode: 404 },
        { method: () => agentApi.toggleAgentActive(999, true), expectedCode: 404 }
      ]

      for (const test of errorTests) {
        const mockErrorResponse = { code: 404, message: 'Not found', data: null }
        vi.mocked(request.delete).mockResolvedValue(mockErrorResponse)
        vi.mocked(request.put).mockResolvedValue(mockErrorResponse)
        
        const result = await test.method()
        expect(result.code).toBe(404)
      }
    })

    it('should handle network errors gracefully', async () => {
      vi.mocked(request.post).mockRejectedValue(new Error('Network error'))
      
      await expect(agentApi.createAgent({
        agentCode: 'TEST',
        agentName: 'Test',
        agentType: 'TEST_DESIGN_ASSISTANT'
      })).rejects.toThrow('Network error')
    })
  })
})