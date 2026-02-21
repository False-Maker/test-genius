# 阶段4: 测试覆盖率提升计划

> **计划类型**: 测试增强
> **预计工时**: 1-2周
> **依赖**: 阶段1-3完成（代码修复后）
> **优先级**: P1 - 高优先级

---

## TL;DR

> **目标**: 将测试覆盖率从当前~30%提升到70%以上
>
> **测试范围**:
> 1. **前端测试**: 从3个测试文件扩展到覆盖所有API和Store
> 2. **Python测试**: 从基础测试扩展到覆盖所有服务
> 3. **集成测试**: 添加端到端测试
> 4. **测试基础设施**: 完善测试配置和Mock
>
> **预计成果**: 测试覆盖率达到70%，CI/CD自动化测试

---

## Context

### 当前测试状况

**前端测试** (frontend/tests/):
- 现有: `cache.test.ts`, `user.test.ts`, `common.test.ts`
- 覆盖: 仅Store和API的基础测试
- 缺失: 组件测试、路由测试、工具函数测试

**Python测试** (backend-python/ai-service/tests/):
- 现有: `conftest.py`, 部分API测试
- 问题: 依赖缺失导致无法运行
- 缺失: 服务层测试、集成测试

**测试基础设施**:
- 前端: vitest已配置，但覆盖率报告不完整
- Python: pytest已配置，但依赖和Mock不完整

### 技术背景

- **前端测试**: Vitest + Vue Test Utils + Happy DOM
- **Python测试**: Pytest + pytest-asyncio + pytest-mock
- **覆盖率目标**: 70%
- **测试原则**: 单元测试 + 集成测试 + 少量E2E测试

---

## Work Objectives

### Core Objective

建立完整的测试体系，确保代码质量和回归保护。

### Concrete Deliverables

1. **前端测试**:
   - 完善Store测试 (user, cache, app, config)
   - 添加API测试 (覆盖所有API模块)
   - 添加组件测试 (关键组件)
   - 添加工具函数测试

2. **Python测试**:
   - 完善服务层测试 (所有service文件)
   - 添加API路由测试
   - 添加集成测试
   - 修复测试依赖问题

3. **测试基础设施**:
   - 完善Mock和Fixture
   - 配置覆盖率报告
   - 添加测试脚本

### Definition of Done

- [ ] 前端测试覆盖率 >= 70%
- [ ] Python测试覆盖率 >= 70%
- [ ] 所有测试能正常运行
- [ ] CI/CD能自动运行测试

### Must Have

- 测试必须是独立的（可并行运行）
- 测试必须是确定性的（可重复）
- 关键业务逻辑必须有测试覆盖

### Must NOT Have

- 不测试第三方库
- 不测试私有方法
- 不写无意义的测试（如getter/setter）

---

## Verification Strategy

### Test Decision

- **Infrastructure exists**: YES
- **Automated tests**: TDD (新功能) + Tests-after (现有代码)
- **Framework**: vitest (前端), pytest (Python)

### QA Policy

- 每次提交前运行相关测试
- 合并代码前运行全部测试
- 定期检查覆盖率报告

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (测试基础设施 - 并行):
├── Task 1: 修复Python测试依赖 [quick]
├── Task 2: 完善前端Mock和Fixture [quick]
├── Task 3: 配置测试覆盖率报告 [quick]
└── Task 4: 添加测试工具脚本 [quick]

Wave 2 (前端测试 - 并行):
├── Task 5: 完善Store测试 [unspecified-high]
├── Task 6: 添加API测试 [unspecified-high]
├── Task 7: 添加组件测试 [unspecified-high]
└── Task 8: 添加工具函数测试 [quick]

Wave 3 (Python测试 - 并行):
├── Task 9: 添加服务层测试 [unspecified-high]
├── Task 10: 添加API路由测试 [unspecified-high]
└── Task 11: 添加集成测试 [unspecified-high]

Wave 4 (验证):
├── Task 12: 运行完整测试套件 [quick]
├── Task 13: 生成覆盖率报告 [quick]
└── Task 14: 配置CI/CD自动化 [quick]

Critical Path: Task 1 → Task 9 → Task 12
Parallel Speedup: ~70%
Max Concurrent: 4
```

### Dependency Matrix

- **1-4**: — — 5-11
- **5-8**: 2, 3 — 12
- **9-11**: 1, 2 — 12
- **12**: 5-11 — 13
- **13**: 12 — 14
- **14**: 13 —

---

## TODOs

### Wave 1: 测试基础设施 (4个任务)

 - [x] 1. **修复Python测试依赖**

  **What to do**:
  
  **1.1 更新requirements.txt**:
  确保包含所有测试依赖：
  ```txt
  # ... existing dependencies
  
  # 测试框架
  pytest==8.3.0
  pytest-asyncio==0.23.7
  pytest-cov==5.0.0
  pytest-mock==3.14.0
  
  # 测试工具
  httpx==0.27.0  # 用于测试API
  freezegun==1.5.0  # 用于时间Mock
  
  # 数据库测试
  sqlalchemy==2.0.23
  
  # 文档解析测试
  python-docx==1.1.2
  PyPDF2==3.0.1
  ```
  
  **1.2 修复conftest.py类型问题**:
  ```python
  # backend-python/ai-service/tests/conftest.py
  from typing import Generator
  from fastapi.testclient import TestClient
  
  @pytest.fixture
  def client(test_db: Session) -> Generator[TestClient, None, None]:
      """创建测试客户端"""
      def override_get_db():
          try:
              yield test_db
          finally:
              pass
      
      app.dependency_overrides[get_db] = override_get_db
      test_client = TestClient(app)
      
      yield test_client
      
      app.dependency_overrides.clear()
  ```

  **Acceptance Criteria**:
  - [ ] `pip install -r requirements.txt` 成功
  - [ ] `pytest --collect-only` 能收集测试

  **Commit**: `test(python): fix test dependencies and conftest`

---

 - [x] 2. **完善前端Mock和Fixture**

  **What to do**:
  
  **2.1 创建API Mock** (`frontend/tests/mocks/api.ts`):
  ```typescript
  import { vi } from 'vitest'
  
  // Mock request模块
  vi.mock('@/api/request', () => ({
    default: {
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
    }
  }))
  
  // Mock Element Plus组件
  vi.mock('element-plus', () => ({
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    }
  }))
  ```
  
  **2.2 创建测试工具** (`frontend/tests/utils/test-helpers.ts`):
  ```typescript
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
  ```

  **Acceptance Criteria**:
  - [ ] Mock文件完整
  - [ ] 测试工具函数可用

  **Commit**: `test(frontend): add API mocks and test helpers`

---

 - [x] 3. **配置测试覆盖率报告**

  **What to do**:
  
  **3.1 更新vitest配置** (`frontend/vitest.config.ts`):
  ```typescript
  import { defineConfig } from 'vitest/config'
  
  export default defineConfig({
    test: {
      globals: true,
      environment: 'happy-dom',
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html', 'lcov'],
        exclude: [
          'node_modules/',
          'tests/',
          '*.config.ts',
          'src/main.ts',
          'src/vite-env.d.ts'
        ],
        thresholds: {
          lines: 70,
          functions: 70,
          branches: 70,
          statements: 70
        }
      }
    }
  })
  ```
  
  **3.2 更新pytest配置** (`backend-python/ai-service/pytest.ini`):
  ```ini
  [pytest]
  testpaths = tests
  python_files = test_*.py
  python_classes = Test*
  python_functions = test_*
  addopts =
      --cov=app
      --cov-report=html
      --cov-report=term-missing
      --cov-report=xml
      --cov-fail-under=70
  ```

  **Acceptance Criteria**:
  - [ ] 覆盖率配置生效
  - [ ] 能生成HTML报告

  **Commit**: `test(configure): add coverage reporting configuration`

---

 - [x] 4. **添加测试工具脚本**

  **What to do**:
  
  **4.1 添加测试脚本** (`package.json`):
  ```json
  {
    "scripts": {
      "test": "vitest",
      "test:run": "vitest run",
      "test:coverage": "vitest run --coverage",
      "test:ui": "vitest --ui"
    }
  }
  ```
  
  **4.2 添加Python测试脚本**:
  创建 `backend-python/ai-service/scripts/test.sh`:
  ```bash
  #!/bin/bash
  set -e
  
  echo "Running Python tests..."
  pytest --cov=app --cov-report=html --cov-report=term
  
  echo "Coverage report generated: htmlcov/index.html"
  ```

  **Acceptance Criteria**:
  - [ ] 测试脚本可执行
  - [ ] 覆盖率报告可生成

  **Commit**: `test(scripts): add test utility scripts`

---

### Wave 2: 前端测试 (4个任务)

 - [x] 5. **完善Store测试**

  **What to do**:
  为所有Store添加测试：
  
  **5.1 完善user store测试** (`frontend/tests/store/user.test.ts`):
  ```typescript
  import { describe, it, expect, beforeEach, vi } from 'vitest'
  import { createPinia, setActivePinia } from 'pinia'
  import { useUserStore } from '@/store/user'
  
  describe('UserStore', () => {
    beforeEach(() => {
      setActivePinia(createPinia())
      localStorage.clear()
    })
    
    describe('login', () => {
      it('should set user info and token', () => {
        const store = useUserStore()
        
        store.login(
          { id: 1, username: 'test' },
          'test-token'
        )
        
        expect(store.userInfo).toEqual({ id: 1, username: 'test' })
        expect(store.token).toBe('test-token')
        expect(store.isLoggedIn).toBe(true)
      })
      
      it('should persist to localStorage', () => {
        const store = useUserStore()
        
        store.login({ id: 1 }, 'token')
        
        expect(localStorage.getItem('token')).toBe('token')
        expect(localStorage.getItem('userInfo')).toBeTruthy()
      })
    })
    
    describe('logout', () => {
      it('should clear user info and token', () => {
        const store = useUserStore()
        store.login({ id: 1 }, 'token')
        store.logout()
        
        expect(store.userInfo).toBeNull()
        expect(store.token).toBe('')
        expect(store.isLoggedIn).toBe(false)
      })
    })
    
    describe('initFromStorage', () => {
      it('should restore user info from localStorage', () => {
        localStorage.setItem('token', 'saved-token')
        localStorage.setItem('userInfo', JSON.stringify({ id: 2 }))
        
        const store = useUserStore()
        store.initFromStorage()
        
        expect(store.token).toBe('saved-token')
        expect(store.userInfo).toEqual({ id: 2 })
      })
    })
  })
  ```
  
  **5.2 添加app store测试**:
  ```typescript
  // frontend/tests/store/app.test.ts
  describe('AppStore', () => {
    it('should manage loading state', () => {
      const store = useAppStore()
      
      store.showLoading('Loading...')
      expect(store.globalLoading).toBe(true)
      expect(store.loadingText).toBe('Loading...')
      
      store.hideLoading()
      expect(store.globalLoading).toBe(false)
    })
    
    it('should manage error state', () => {
      const store = useAppStore()
      
      store.setError('Test error', 'ERR_001')
      expect(store.errorMessage).toBe('Test error')
      expect(store.errorCode).toBe('ERR_001')
      
      store.clearError()
      expect(store.errorMessage).toBeNull()
    })
  })
  ```
  
  **5.3 添加config store测试**:
  ```typescript
  // frontend/tests/store/config.test.ts
  describe('ConfigStore', () => {
    it('should manage configuration', () => {
      const store = useConfigStore()
      
      store.setTheme('dark')
      expect(store.theme).toBe('dark')
      
      store.setPageSize(20)
      expect(store.pageSize).toBe(20)
    })
  })
  ```

  **Acceptance Criteria**:
  - [ ] 4个Store都有测试
  - [ ] 测试覆盖所有公共方法

  **Commit**: `test(frontend): add tests for all stores`

---

 - [x] 6. **添加API测试**

  **What to do**:
  为所有API模块添加测试：
  
  **6.1 API测试模板**:
  ```typescript
  // frontend/tests/api/requirement.test.ts
  import { describe, it, expect, vi, beforeEach } from 'vitest'
  import { requirementApi } from '@/api/requirement'
  import request from '@/api/request'
  
  vi.mock('@/api/request')
  
  describe('RequirementAPI', () => {
    beforeEach(() => {
      vi.clearAllMocks()
    })
    
    describe('getRequirementList', () => {
      it('should fetch requirement list', async () => {
        const mockData = {
          content: [{ id: 1, requirementCode: 'REQ-001' }],
          totalElements: 1
        }
        
        vi.mocked(request.get).mockResolvedValue({
          data: mockData
        })
        
        const result = await requirementApi.getRequirementList({ page: 0, size: 10 })
        
        expect(result.data).toEqual(mockData)
        expect(request.get).toHaveBeenCalledWith(
          '/v1/requirements',
          { params: { page: 0, size: 10 } }
        )
      })
      
      it('should handle errors', async () => {
        vi.mocked(request.get).mockRejectedValue(new Error('Network error'))
        
        await expect(
          requirementApi.getRequirementList({ page: 0, size: 10 })
        ).rejects.toThrow('Network error')
      })
    })
  })
  ```
  
  **需要测试的API模块**:
  - requirement.ts
  - testCase.ts
  - caseGeneration.ts
  - promptTemplate.ts
  - modelConfig.ts
  - knowledgeBase.ts
  - workflow.ts
  - agent.ts
  - monitoring.ts
  - testReport.ts
  - testExecution.ts
  - uiScriptGeneration.ts

  **Acceptance Criteria**:
  - [ ] 每个API模块都有测试
  - [ ] 测试覆盖所有方法

  **Commit**: `test(frontend): add API tests for all modules`

---

- [ ] 7. **添加组件测试**

  **What to do**:
  为关键组件添加测试：
  
  **7.1 组件测试示例**:
  ```typescript
  // frontend/tests/components/FileUpload.test.ts
  import { describe, it, expect, vi } from 'vitest'
  import { mount } from '@vue/test-utils'
  import { createPinia, setActivePinia } from 'pinia'
  import FileUpload from '@/components/FileUpload.vue'
  
  describe('FileUpload Component', () => {
    beforeEach(() => {
      setActivePinia(createPinia())
    })
    
    it('should render upload button', () => {
      const wrapper = mount(FileUpload)
      
      expect(wrapper.find('el-upload').exists()).toBe(true)
    })
    
    it('should emit file-selected event', async () => {
      const wrapper = mount(FileUpload)
      
      // 模拟文件选择
      await wrapper.vm.handleFileSelect(new File([''], 'test.txt'))
      
      expect(wrapper.emitted('file-selected')).toBeTruthy()
    })
  })
  ```
  
  **需要测试的组件**:
  - FileUpload.vue
  - PromptEditor.vue
  - BatchTest.vue
  - VersionHistory.vue
  - ModelComparison.vue

  **Acceptance Criteria**:
  - [ ] 关键组件有测试
  - [ ] 测试用户交互

  **Commit**: `test(frontend): add component tests`

---

- [ ] 8. **添加工具函数测试**

  **What to do**:
  创建 `frontend/tests/utils/` 目录，添加工具函数测试

  **Acceptance Criteria**:
  - [ ] 所有工具函数有测试

  **Commit**: `test(frontend): add utility function tests`

---

### Wave 3: Python测试 (3个任务)

- [ ] 9. **添加服务层测试**

  **What to do**:
  为所有服务添加单元测试：
  
  **9.1 LLM服务测试**:
  ```python
  # backend-python/ai-service/tests/services/test_llm_service.py
  import pytest
  from unittest.mock import Mock, patch
  from app.services.llm_service import LLMService
  
  @pytest.fixture
  def llm_service(db_session):
      return LLMService(db_session)
  
  class TestLLMService:
      def test_call_model_success(self, llm_service, mock_llm_response):
          """测试成功调用模型"""
          with patch.object(llm_service, '_get_llm_instance') as mock_get:
              mock_llm = Mock()
              mock_llm.invoke.return_value = Mock(content="测试响应")
              mock_get.return_value = mock_llm
              
              result = llm_service.call_model(
                  model_code="TEST_MODEL",
                  prompt="测试提示词"
              )
              
              assert result["content"] == "测试响应"
              assert result["model_code"] == "TEST_MODEL"
      
      def test_call_model_with_invalid_model(self, llm_service):
          """测试使用无效模型调用"""
          with pytest.raises(ValueError, match="模型配置不存在"):
              llm_service.call_model(
                  model_code="INVALID_MODEL",
                  prompt="测试"
              )
  ```
  
  **9.2 其他服务测试**:
  - `test_agent_engine.py`
  - `test_agent_tool_manager.py`
  - `test_document_parser_service.py`
  - `test_workflow_engine.py`
  - `test_page_parser_service.py`

  **Acceptance Criteria**:
  - [ ] 所有服务有测试
  - [ ] 测试覆盖正常和异常流程

  **Commit**: `test(python): add service layer tests`

---

- [ ] 10. **添加API路由测试**

  **What to do**:
  为所有API路由添加测试：
  
  ```python
  # backend-python/ai-service/tests/api/test_llm_router.py
  from fastapi.testclient import TestClient
  from app.main import app
  
  client = TestClient(app)
   
  class TestLLMRouter:
      def test_call_model_endpoint(self, client, mock_llm):
          """测试模型调用端点"""
          response = client.post(
              "/api/v1/llm/call",
              json={
                  "model_code": "TEST_MODEL",
                  "prompt": "测试提示词"
              }
          )
          
          assert response.status_code == 200
          data = response.json()
          assert "content" in data
  ```

  **需要测试的API**:
  - LLM路由
  - Agent路由
  - 工作流路由
  - 文档路由
  - 知识库路由

  **Acceptance Criteria**:
  - [ ] 所有API路由有测试
  - [ ] 测试覆盖所有端点

  **Commit**: `test(python): add API route tests`

---

- [ ] 11. **添加集成测试**

  **What to do**:
  添加端到端集成测试：
  
  ```python
  # backend-python/ai-service/tests/integration/test_agent_workflow.py
  class TestAgentWorkflowIntegration:
      def test_full_agent_session(self, client, db_session):
          """测试完整的Agent对话流程"""
          # 1. 创建Agent
          agent_response = client.post(
              "/api/v1/agents",
              json={
                  "agentCode": "TEST_AGENT",
                  "agentName": "测试Agent",
                  "agentType": "CUSTOM",
                  "systemPrompt": "你是一个测试助手"
              }
          )
          assert agent_response.status_code == 200
          agent_id = agent_response.json()["data"]["id"]
          
          # 2. 发送对话消息
          chat_response = client.post(
              f"/api/v1/agents/{agent_id}/chat",
              json={
                  "message": "你好",
                  "session_id": "test-session"
              }
          )
          assert chat_response.status_code == 200
          
          # 3. 验证会话历史
          history_response = client.get(
              f"/api/v1/agents/{agent_id}/sessions"
          )
          assert len(history_response.json()["data"]) > 0
  ```

  **Acceptance Criteria**:
  - [ ] 有完整的集成测试
  - [ ] 测试关键业务流程

  **Commit**: `test(python): add integration tests`

---

### Wave 4: 验证 (3个任务)

- [ ] 12. **运行完整测试套件**

  **QA Scenarios**:
  ```bash
  Scenario: 前端测试
    Tool: Bash
    Steps:
      1. cd frontend
      2. npm run test:run 2>&1 | tee ../.sisyphus/evidence/phase4-task12-frontend-test.log
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/phase4-task12-frontend-test.log
  
  Scenario: Python测试
    Tool: Bash
    Steps:
      1. cd backend-python/ai-service
      2. pytest -v 2>&1 | tee ../.sisyphus/evidence/phase4-task12-python-test.log
    Expected Result: 所有测试通过
    Evidence: .sisyphus/evidence/phase4-task12-python-test.log
  ```

  **Commit**: NO

---

- [ ] 13. **生成覆盖率报告**

  **What to do**:
  生成并检查覆盖率报告：
  
  ```bash
  # 前端覆盖率
  cd frontend && npm run test:coverage
  
  # Python覆盖率
  cd backend-python/ai-service && pytest --cov=app --cov-report=html
  ```

  **Acceptance Criteria**:
  - [ ] 前端覆盖率 >= 70%
  - [ ] Python覆盖率 >= 70%
  - [ ] HTML报告可查看

  **Commit**: `test(report): generate coverage reports`

---

- [ ] 14. **配置CI/CD自动化**

  **What to do**:
  创建CI/CD配置：
  
  **14.1 更新GitLab CI** (`.github/workflows/test.yml` 或 `.gitlab-ci.yml`):
  ```yaml
  test:
    script:
      - cd frontend && npm install && npm run test:run
      - cd backend-python/ai-service && pip install -r requirements.txt
      - cd backend-python/ai-service && pytest
    coverage: '/Coverage: \d+\.\d+%/'
  ```

  **Acceptance Criteria**:
  - [ ] CI/CD配置存在
  - [ ] 测试自动运行

  **Commit**: `ci: add automated testing to CI/CD`

---

## Final Verification Wave

- [ ] F1. **Coverage Report** — `quick`
  检查测试覆盖率是否达到目标
- [ ] F2. **Test Quality** — `quick`
  检查测试质量和可维护性
- [ ] F3. **CI/CD Verification** — `quick`
  验证CI/CD自动化测试
- [ ] F4. **Test Documentation** — `quick`
  添加测试文档说明

---

## Commit Strategy

- **Wave 1**: 基础设施相关
- **Wave 2**: 前端测试
- **Wave 3**: Python测试
- **Wave 4**: 验证和配置

---

## Success Criteria

### Verification Commands

```bash
# 前端测试
cd frontend && npm run test:coverage
# Expected: Coverage >= 70%

# Python测试
cd backend-python/ai-service && pytest --cov=app --cov-report=term-missing
# Expected: Coverage >= 70%
```

### Final Checklist

- [ ] 前端测试覆盖率 >= 70%
- [ ] Python测试覆盖率 >= 70%
- [ ] 所有测试能通过
- [ ] CI/CD自动化测试配置完成
- [ ] 覆盖率报告可查看
