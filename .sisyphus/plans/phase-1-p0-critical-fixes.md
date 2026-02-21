# 阶段1: P0严重问题修复计划

> **计划类型**: 紧急修复
> **预计工时**: 3-5天
> **依赖**: 无
> **优先级**: P0 - 必须立即修复

---

## TL;DR

> **目标**: 修复阻止系统正常运行和构建的4个严重问题
>
> **问题清单**:
> 1. 前端 `requirementAnalysis.ts` 语法错误 - 阻止TypeScript编译
> 2. Python后端LLM服务空实现 - 核心AI功能不可用
> 3. Python后端其他核心服务空实现 - 工作流、Agent等功能缺失
> 4. Python CORS配置安全风险 - 允许任意来源访问
>
> **预计成果**: 系统能够正常构建运行，核心AI功能可用
>
> **执行方式**: 4个任务可独立并行执行

---

## Context

### 原始需求来源

基于项目检查报告 (`.sisyphus/drafts/项目检查报告.md`)，发现了12个严重问题，其中4个P0问题需要立即修复：

1. **前端构建失败**: `requirementAnalysis.ts` 文件在第46行被截断，导致TypeScript编译错误
2. **Python AI服务空实现**: 多个核心服务只有 `pass` 占位符
3. **CORS安全风险**: Python服务配置 `allow_origins=["*"]`
4. **测试依赖问题**: SQLAlchemy未安装导致Python测试无法运行

### 技术背景

- **前端**: Vue 3 + TypeScript，使用vue-tsc进行类型检查
- **Python后端**: FastAPI + LangChain，提供AI能力服务
- **问题影响**:
  - 前端构建失败导致无法部署
  - Python核心服务缺失导致AI功能不可用
  - CORS配置存在安全风险

---

## Work Objectives

### Core Objective

修复4个P0严重问题，使系统能够正常构建、运行并提供核心AI功能。

### Concrete Deliverables

1. 修复后的 `frontend/src/api/requirementAnalysis.ts` 文件
2. 实现完整的Python服务文件：
   - `backend-python/ai-service/app/services/llm_service.py` (已有框架，需验证)
   - `backend-python/ai-service/app/services/agent_engine.py`
   - `backend-python/ai-service/app/services/agent_tool_manager.py`
   - `backend-python/ai-service/app/services/document_parser_service.py`
   - `backend-python/ai-service/app/services/workflow_engine.py`
   - `backend-python/ai-service/app/services/page_parser_service.py`
   - `backend-python/ai-service/app/services/agent_tool_registry.py`
   - `backend-python/ai-service/app/utils/model_adapter.py`
3. 更新后的 `backend-python/ai-service/app/main.py` CORS配置
4. 验证Python依赖可正常安装

### Definition of Done

- [ ] `cd frontend && npm run type-check` 通过，无编译错误
- [ ] Python所有服务文件无 `pass` 占位符
- [ ] CORS配置限制允许的来源
- [ ] `cd backend-python/ai-service && pip install -r requirements.txt` 成功
- [ ] `cd backend-python/ai-service && pytest` 能运行（不要求全部通过）

### Must Have

- 必须保持现有API接口不变
- 不能破坏已有的Java后端功能
- 所有修改必须向后兼容

### Must NOT Have

- 不能修改API接口签名
- 不能重构现有业务逻辑（只修复问题）
- 不能添加新功能（留到后续阶段）

---

## Verification Strategy

### Test Decision

- **Infrastructure exists**: YES (前端有vitest，Python有pytest)
- **Automated tests**: Tests-after (修复后添加回归测试)
- **Framework**: vitest (前端), pytest (Python)

### QA Policy

每个任务完成后，执行以下验证场景：

#### 场景1: 前端类型检查
```bash
cd frontend
npm run type-check
# 期望: 无错误输出
```

#### 场景2: Python依赖安装
```bash
cd backend-python/ai-service
pip install -r requirements.txt
# 期望: 所有依赖安装成功，无ModuleNotFoundError
```

#### 场景3: Python服务导入检查
```bash
cd backend-python/ai-service
python -c "from app.services.llm_service import LLMService; print('LLM服务导入成功')"
python -c "from app.services.agent_engine import AgentEngine; print('Agent引擎导入成功')"
# 期望: 所有服务能正常导入
```

#### 场景4: CORS配置验证
```bash
cd backend-python/ai-service
grep -A 5 "CORSMiddleware" app/main.py
# 期望: allow_origins 不为 ["*"]
```

---

## Execution Strategy

### Parallel Execution Waves

所有4个任务可以完全并行执行：

```
Wave 1 (全部并行 - 无依赖):
├── Task 1: 修复前端requirementAnalysis.ts语法错误 [quick]
├── Task 2: 实现LLM服务相关模块 [unspecified-high]
├── Task 3: 实现Agent和工作流服务 [unspecified-high]
├── Task 4: 修复CORS配置和测试依赖 [quick]

Wave 2 (验证 - 所有任务完成后):
├── Task 5: 前端类型检查验证 [quick]
├── Task 6: Python服务导入验证 [quick]
├── Task 7: 依赖安装验证 [quick]
└── Task 8: 整体构建验证 [quick]

Critical Path: 无 (全部并行)
Parallel Speedup: 75% (4个任务并行)
Max Concurrent: 4
```

### Dependency Matrix

- **1-4**: — — 5-8
- **5-8**: 1-4 — (依赖任务1-4完成)

### Agent Dispatch Summary

- **Wave 1**: 4个并行任务
  - Task 1 → `quick`
  - Task 2 → `unspecified-high`
  - Task 3 → `unspecified-high`
  - Task 4 → `quick`
- **Wave 2**: 4个并行验证任务
  - Task 5-8 → `quick`

---

## TODOs

### Wave 1: 并行修复 (4个任务)

- [x] 1. **修复前端 requirementAnalysis.ts 语法错误** ✅ VERIFIED

  **What to do**:
  - 打开 `frontend/src/api/requirementAnalysis.ts`
  - 补充第46行 `getBusinessRules` 函数的完整实现
  - 检查文件末尾是否缺少 `}` 括号
  - 确保所有导出的函数都有完整实现

  **Expected Implementation**:
  ```typescript
  // 提取业务规则
  getBusinessRules(requirementId: number) {
    return request.get<BusinessRule[]>(`/v1/requirements/${requirementId}/business-rules`)
  }
  ```

  **Must NOT do**:
  - 不能修改其他API函数
  - 不能改变类型定义

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > - **Reason**: 简单的语法修复，单文件操作

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (Tasks 1, 2, 3, 4)
  - **Blocked By**: None

  **References**:
  - `frontend/src/api/requirementAnalysis.ts:1-46` - 当前文件内容
  - `frontend/src/api/*.ts` - 其他API文件的实现模式参考

  **Acceptance Criteria**:
  - [ ] 文件末尾有正确的闭合括号
  - [ ] `getBusinessRules` 函数有完整实现
  - [ ] 文件无TypeScript语法错误

  **QA Scenarios**:
  ```bash
  Scenario: 前端类型检查通过
    Tool: Bash
    Preconditions: None
    Steps:
      1. cd frontend
      2. npm run type-check
    Expected Result: 命令退出码为0，无TS1005错误
    Failure Indicators: 输出包含 "error TS1005"
    Evidence: .sisyphus/evidence/task-1-type-check.log
  ```

  **Commit**: YES
  - Message: `fix(frontend): repair requirementAnalysis.ts syntax error`

---

- [x] 2. **实现LLM服务相关模块** ✅ VERIFIED

  **What to do**:
  - 补充 `backend-python/ai-service/app/utils/model_adapter.py` 的空实现
  - 验证 `llm_service.py` 的完整性（已有大部分实现）
  - 确保ModelAdapterFactory能正确创建各类型LLM实例

  **Details for model_adapter.py**:
  该文件需要实现以下功能：
  - `ModelAdapterFactory.create_llm()` - 根据模型类型创建LLM实例
  - 支持的模型类型：DEEPSEEK, OPENAI, KIMI, QIANWEN, DOUBAO
  - 返回兼容LangChain 0.3.x的LLM实例

  **Must NOT do**:
  - 不能修改 `llm_service.py` 的现有实现
  - 不能改变已有的函数签名

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high`
  > - **Reason**: 需要理解LangChain和多个LLM API
  > **Skills**: N/A (无特定技能要求)

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (Tasks 1, 2, 3, 4)
  - **Blocked By**: None

  **References**:
  - `backend-python/ai-service/app/utils/model_adapter.py` - 需要实现的目标文件
  - `backend-python/ai-service/app/services/llm_service.py:69-76` - LLM创建调用示例
  - `backend-python/ai-service/requirements.txt:10-15` - LangChain依赖版本
  - 官方文档: https://python.langchain.com/docs/introduction

  **Acceptance Criteria**:
  - [ ] `model_adapter.py` 无 `pass` 占位符
  - [ ] `create_llm()` 函数返回有效的LLM实例
  - [ ] 支持至少DEEPSEEK和OPENAI两种模型类型

  **QA Scenarios**:
  ```bash
  Scenario: LLM服务模块导入成功
    Tool: Bash
    Preconditions: Python依赖已安装
    Steps:
      1. cd backend-python/ai-service
      2. python -c "from app.utils.model_adapter import ModelAdapterFactory; print('OK')"
    Expected Result: 输出 "OK"，无ImportError
    Failure Indicators: ModuleNotFoundError, IndentationError
    Evidence: .sisyphus/evidence/task-2-import-test.log
  ```

  **Commit**: YES
  - Message: `feat(python): implement ModelAdapterFactory for LLM creation`

---

- [x] 3. **实现Agent和工作流服务模块** ✅ VERIFIED

  **What to do**:
  补充以下文件的空实现：
  
  **3.1 Agent引擎** (`agent_engine.py`):
  - 实现 `AgentEngine` 类
  - 方法：`run_session()`, `process_message()`, `get_history()`
  
  **3.2 工具管理器** (`agent_tool_manager.py`):
  - 实现 `AgentToolManager` 类
  - 方法：`register_tool()`, `get_tool()`, `execute_tool()`
  
  **3.3 工具注册表** (`agent_tool_registry.py`):
  - 实现工具注册机制
  - 预定义工具：知识库查询、用例生成、需求分析
  
  **3.4 文档解析服务** (`document_parser_service.py`):
  - 实现 `DocumentParserService` 类
  - 方法：`parse_word()`, `parse_pdf()`, `parse_text()`
  
  **3.5 工作流引擎** (`workflow_engine.py`):
  - 实现 `WorkflowEngine` 类
  - 方法：`execute()`, `validate()`, `get_status()`
  
  **3.6 页面解析服务** (`page_parser_service.py`):
  - 实现 `PageParserService` 类
  - 方法：`parse_ui_elements()`, `extract_selectors()`

  **Must NOT do**:
  - 不能修改已有的API路由接口
  - 不能改变数据库表结构

  **Recommended Agent Profile**:
  > **Category**: `unspecified-high`
  > - **Reason**: 复杂的业务逻辑实现，涉及多个模块

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (Tasks 1, 2, 3, 4)
  - **Blocked By**: None

  **References**:
  - `backend-python/ai-service/app/api/agent_router.py` - Agent路由定义
  - `backend-python/ai-service/app/api/workflow_router.py` - 工作流路由定义
  - `backend-python/ai-service/app/api/document_router.py` - 文档路由定义
  - `backend-python/ai-service/app/models/` - 数据模型定义

  **Acceptance Criteria**:
  - [ ] 所有6个服务文件无 `pass` 占位符
  - [ ] 每个类都有完整的 `__init__` 方法
  - [ ] 每个公共方法都有基本实现（可抛出NotImplementedError表示未完成）

  **QA Scenarios**:
  ```bash
  Scenario: Agent和工作流服务导入成功
    Tool: Bash
    Preconditions: Python依赖已安装
    Steps:
      1. cd backend-python/ai-service
      2. python -c "from app.services.agent_engine import AgentEngine; print('AgentEngine OK')"
      3. python -c "from app.services.workflow_engine import WorkflowEngine; print('WorkflowEngine OK')"
      4. python -c "from app.services.document_parser_service import DocumentParserService; print('DocumentParser OK')"
    Expected Result: 所有服务都能成功导入
    Failure Indicators: ImportError, AttributeError
    Evidence: .sisyphus/evidence/task-3-import-test.log
  ```

  **Commit**: YES
  - Message: `feat(python): implement Agent and Workflow service modules`

---

- [x] 4. **修复CORS配置和测试依赖** ✅ VERIFIED

  **What to do**:
  
  **4.1 修复CORS配置**:
  - 修改 `backend-python/ai-service/app/main.py`
  - 将 `allow_origins=["*"]` 改为配置化的来源列表
  - 从环境变量读取允许的来源
  
  **4.2 验证测试依赖**:
  - 检查 `backend-python/ai-service/requirements.txt`
  - 确认sqlalchemy已列出
  - 如果缺失，添加到依赖列表

  **Expected CORS Implementation**:
  ```python
  import os
  from fastapi.middleware.cors import CORSMiddleware
  
  # 从环境变量获取允许的来源，默认为开发环境
  allowed_origins = os.getenv(
      "ALLOWED_ORIGINS",
      "http://localhost:3000,http://localhost:8080"
  ).split(",")
  
  app.add_middleware(
      CORSMiddleware,
      allow_origins=allowed_origins,
      allow_credentials=True,
      allow_methods=["*"],
      allow_headers=["*"],
  )
  ```

  **Must NOT do**:
  - 不能影响开发环境的本地测试
  - 不能改变其他中间件配置

  **Recommended Agent Profile**:
  > **Category**: `quick`
  > - **Reason**: 配置修改，简单直接

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (Tasks 1, 2, 3, 4)
  - **Blocked By**: None

  **References**:
  - `backend-python/ai-service/app/main.py:15-22` - 当前CORS配置
  - `backend-python/ai-service/requirements.txt` - Python依赖列表
  - `backend-python/ai-service/.env` - 环境变量示例

  **Acceptance Criteria**:
  - [ ] CORS配置不使用 `["*"]`
  - [ ] 支持通过环境变量配置允许的来源
  - [ ] requirements.txt包含sqlalchemy

  **QA Scenarios**:
  ```bash
  Scenario: CORS配置验证
    Tool: Bash
    Preconditions: None
    Steps:
      1. cd backend-python/ai-service
      2. grep -A 3 "allow_origins" app/main.py
    Expected Result: 输出不包含 '["*"]'
    Failure Indicators: allow_origins=["*"]
    Evidence: .sisyphus/evidence/task-4-cors-check.log
  
  Scenario: Python依赖安装测试
    Tool: Bash
    Preconditions: Python虚拟环境已激活
    Steps:
      1. cd backend-python/ai-service
      2. pip install -r requirements.txt
    Expected Result: 安装成功，无ModuleNotFoundError: sqlalchemy
    Failure Indicators: Could not find a version that satisfies the requirement
    Evidence: .sisyphus/evidence/task-4-dependency-install.log
  ```

  **Commit**: YES
  - Message: `fix(python): secure CORS config and ensure test dependencies`

---

### Wave 2: 验证 (4个任务)

- [ ] 5. **前端类型检查验证**

  **What to do**:
  - 运行 `cd frontend && npm run type-check`
  - 捕获输出到证据文件
  - 确认无TS1005错误

  **Acceptance Criteria**:
  - [ ] type-check命令退出码为0
  - [ ] 证据文件存在且无错误

  **QA Scenarios**:
  ```bash
  Scenario: 完整前端类型检查
    Tool: Bash
    Steps:
      1. cd frontend
      2. npm run type-check 2>&1 | tee .sisyphus/evidence/task-5-full-type-check.log
    Expected Result: 无错误输出
    Evidence: .sisyphus/evidence/task-5-full-type-check.log
  ```

  **Commit**: NO (验证任务)

---

- [ ] 6. **Python服务导入验证**

  **What to do**:
  - 测试所有修改的Python服务能否正常导入
  - 验证无语法错误

  **Acceptance Criteria**:
  - [ ] 所有服务模块能成功导入
  - [ ] 无IndentationError或SyntaxError

  **QA Scenarios**:
  ```bash
  Scenario: 所有Python服务导入测试
    Tool: Bash
    Steps:
      1. cd backend-python/ai-service
      2. python -c "
from app.services.llm_service import LLMService
from app.services.agent_engine import AgentEngine
from app.services.agent_tool_manager import AgentToolManager
from app.services.document_parser_service import DocumentParserService
from app.services.workflow_engine import WorkflowEngine
from app.services.page_parser_service import PageParserService
from app.utils.model_adapter import ModelAdapterFactory
print('All services imported successfully')
" 2>&1 | tee .sisyphus/evidence/task-6-import-verify.log
    Expected Result: 输出 "All services imported successfully"
    Evidence: .sisyphus/evidence/task-6-import-verify.log
  ```

  **Commit**: NO

---

- [ ] 7. **依赖安装验证**

  **What to do**:
  - 在干净环境中测试Python依赖安装
  - 验证pytest能运行

  **Acceptance Criteria**:
  - [ ] pip install成功
  - [ ] pytest --version能输出版本号

  **QA Scenarios**:
  ```bash
  Scenario: Python测试环境验证
    Tool: Bash
    Steps:
      1. cd backend-python/ai-service
      2. pip install -r requirements.txt 2>&1 | tee .sisyphus/evidence/task-7-install.log
      3. pytest --version 2>&1 | tee -a .sisyphus/evidence/task-7-install.log
    Expected Result: 安装成功，pytest版本正常显示
    Evidence: .sisyphus/evidence/task-7-install.log
  ```

  **Commit**: NO

---

- [ ] 8. **整体构建验证**

  **What to do**:
  - 验证前端能成功构建
  - 验证Python服务能启动

  **Acceptance Criteria**:
  - [ ] 前端构建成功
  - [ ] Python健康检查接口返回200

  **QA Scenarios**:
  ```bash
  Scenario: 前端构建测试
    Tool: Bash
    Steps:
      1. cd frontend
      2. npm run build 2>&1 | tee ../.sisyphus/evidence/task-8-frontend-build.log
    Expected Result: 构建成功，生成dist目录
    Evidence: .sisyphus/evidence/task-8-frontend-build.log
  
  Scenario: Python服务启动测试
    Tool: Bash
    Steps:
      1. cd backend-python/ai-service
      2. timeout 5 python -m uvicorn app.main:app --port 8001 2>&1 | tee ../.sisyphus/evidence/task-8-python-start.log &
      3. sleep 3
      4. curl -f http://localhost:8001/health 2>&1 | tee -a ../.sisyphus/evidence/task-8-python-start.log
      5. pkill -f "uvicorn app.main"
    Expected Result: /health返回{"status": "healthy"}
    Evidence: .sisyphus/evidence/task-8-python-start.log
  ```

  **Commit**: NO

---

## Final Verification Wave

- [ ] F1. **Plan Compliance Audit** — `oracle`
  验证所有P0问题已修复：
  - 检查 `frontend/src/api/requirementAnalysis.ts` 无语法错误
  - 检查所有Python服务文件无 `pass` 占位符
  - 检查CORS配置已更新
  - 检查依赖文件完整
  Output: `P0 Issues [4/4 Fixed] | VERDICT`

- [ ] F2. **Build Verification** — `quick`
  运行完整构建测试：
  - 前端: `cd frontend && npm run build`
  - Python: `cd backend-python/ai-service && python -m pytest --collect-only`
  Output: `Frontend [PASS/FAIL] | Python [PASS/FAIL] | VERDICT`

- [ ] F3. **Service Smoke Test** — `unspecified-high`
  启动服务并执行冒烟测试：
  - Python服务健康检查
  - 前端能访问API
  Output: `Services [UP/DOWN] | VERDICT`

- [ ] F4. **Regression Check** — `quick`
  检查修复是否引入新问题：
  - 运行类型检查
  - 运行现有测试
  Output: `TypeCheck [PASS/FAIL] | Tests [N pass/N fail] | VERDICT`

---

## Commit Strategy

- **Task 1**: `fix(frontend): repair requirementAnalysis.ts syntax error`
- **Task 2**: `feat(python): implement ModelAdapterFactory for LLM creation`
- **Task 3**: `feat(python): implement Agent and Workflow service modules`
- **Task 4**: `fix(python): secure CORS config and ensure test dependencies`
- **Tasks 5-8**: 无commit（验证任务）

---

## Success Criteria

### Verification Commands

```bash
# 前端类型检查
cd frontend && npm run type-check
# Expected: 无错误输出

# Python服务导入
cd backend-python/ai-service
python -c "from app.services.llm_service import LLMService; print('OK')"
# Expected: OK

# CORS配置检查
grep -A 3 "allow_origins" backend-python/ai-service/app/main.py
# Expected: 不包含 ["*"]
```

### Final Checklist

- [ ] 前端TypeScript编译无错误
- [ ] Python所有服务文件无pass占位符
- [ ] CORS配置安全（不使用通配符）
- [ ] Python依赖可正常安装
- [ ] pytest能运行
- [ ] 前端构建成功
- [ ] Python服务能启动
