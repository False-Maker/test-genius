export const mockRequirement = {
  id: 1,
  requirementCode: 'REQ-001',
  requirementName: '测试需求',
  isActive: '1'
}

export const mockTestCase = {
  id: 1,
  testCaseCode: 'TC-001',
  testCaseTitle: '测试用例',
  requirementId: 1
}

export const createMockResponse = <T>(data: T) => ({
  code: 200,
  message: 'success',
  data,
  timestamp: Date.now()
})