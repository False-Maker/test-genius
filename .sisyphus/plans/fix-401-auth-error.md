# 修复需求分析401身份验证错误

## TL;DR

> **问题**：Java后端发送带有 `model_config` 对象的请求，但Python端无法正确处理，导致使用空/无效的API密钥调用大模型，返回401错误。
>
> **解决方案**：修改Python端 `requirement_router.py`，使其能够接收并使用Java端传递的 `model_config` 对象，同时修改 `llm_service.py` 添加支持动态配置的方法。
>
> **影响范围**：仅修改Python AI服务的2个文件
>
> **工作量**：Short（约30分钟）

---

## Context

### 错误日志
```
2026-02-22 13:33:35.530 [ERROR] 调用AI分析服务失败，使用降级策略: 500 Internal Server Error:
"{"detail":"需求分析失败: Error code: 401 - {'error': {'code': '1000', 'message': '身份验证失败。'}}}"
```

### 根本原因分析

1. **Java后端** (`ModelCallServiceImpl.java:78-91`) 发送请求时：
   - 在请求体中注入 `model_config` 对象，包含 `api_key`、`api_endpoint`、`model_code` 等
   - 但**没有**设置顶层的 `model_code` 字段

2. **Python端** (`requirement_router.py:17-22`) 的 `RequirementAnalyzeRequest` 模型：
   - **没有定义** `model_config` 字段（Pydantic会忽略额外字段）
   - 只定义了顶层的 `model_code` 字段（Java端没传这个值）

3. **调用流程**：
   - Python端收到请求后，`request.model_code` 为 `None`
   - 调用 `llm_service.call_model(model_code=None, ...)`
   - LLM服务从数据库查询默认模型配置
   - 如果数据库中没有配置或API key无效 → 401错误

---

## Work Objectives

### Core Objective
修复Python端需求分析接口，使其能够正确接收和使用Java端传递的 `model_config` 动态配置，避免401身份验证错误。

### Concrete Deliverables
- 修改 `backend-python/ai-service/app/api/requirement_router.py`
  - 添加 `ModelConfigData` 模型定义
  - 在 `RequirementAnalyzeRequest` 中添加 `model_config` 字段
  - 修改请求处理逻辑，优先使用 `model_config` 中的参数

- 修改 `backend-python/ai-service/app/services/llm_service.py`
  - 添加 `call_model_with_config` 方法，支持动态传入模型配置
  - 当提供 `model_config` 时，使用传入的配置而不是从数据库查询

### Definition of Done
- [ ] 修改后需求分析功能正常工作，无401错误
- [ ] 日志显示正确使用了传入的 `model_config`
- [ ] 降级机制仍然有效（数据库配置兜底）

### Must Have
- Python端能接收 `model_config` 对象
- 优先使用 `model_config` 中的 `api_key`、`api_endpoint` 等配置
- 当 `model_config` 不存在或 `model_code` 为空时，仍能从数据库查询

### Must NOT Have (Guardrails)
- 不修改Java后端代码
- 不破坏现有的数据库配置查询逻辑
- 不影响其他使用 `llm_service` 的模块

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES
- **Automated tests**: Tests-after（修改后添加测试用例）
- **Framework**: pytest

### QA Policy
每个任务包含详细的Agent-Executed QA场景。

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: 修改 requirement_router.py 请求模型定义 [quick]
├── Task 2: 修改 llm_service.py 添加动态配置支持 [quick]
└── Task 3: 添加单元测试验证修复 [quick]

Wave 2 (After Wave 1):
├── Task 4: 手动测试验证 [unspecified-high]
└── Task 5: 日志验证和调试 [quick]

Wave FINAL (After ALL tasks):
├── F1: 功能验证 [unspecified-high]
└── F2: 回归测试 [quick]
```

---

## TODOs

- [ ] 1. 修改 requirement_router.py 添加 model_config 支持

  **What to do**:
  - 在 `requirement_router.py` 中添加 `ModelConfigData` Pydantic 模型
  - 修改 `RequirementAnalyzeRequest` 添加 `model_config: Optional[ModelConfigData] = None` 字段
  - 修改 `analyze_requirement` 函数，优先使用 `model_config.model_code` 作为 `effective_model_code`
  - 优先使用 `model_config.max_tokens` 和 `model_config.temperature`

  **Must NOT do**:
  - 不修改Java后端代码
  - 不破坏现有的降级机制

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的模型定义和逻辑修改
  - **Skills**: N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 2
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `backend-python/ai-service/app/api/requirement_router.py:17-22` - 现有的 RequestAnalyzeRequest 定义

  **API/Type References**:
  - Pydantic BaseModel: https://docs.pydantic.dev/latest/concepts/models/

  **Test References**:
  - `backend-python/ai-service/tests/test_api_requirement.py` - 现有测试文件（如果存在）

  **External References**:
  - FastAPI Request Body: https://fastapi.tiangolo.com/tutorial/body/

  **WHY Each Reference Matters**:
  - 现有的RequestAnalyzeRequest定义展示了如何使用Pydantic模型
  - 需要添加类似的嵌套模型来接收model_config对象

  **Acceptance Criteria**:
  - [ ] `ModelConfigData` 模型已定义，包含所有必要字段（model_code, api_key, api_endpoint等）
  - [ ] `RequirementAnalyzeRequest` 已添加 `model_config` 字段
  - [ ] `analyze_requirement` 函数正确提取 `effective_model_code`

  **QA Scenarios (MANDATORY)**:

  ```
  Scenario: 验证 model_config 字段正确接收
    Tool: Bash (curl)
    Preconditions: Python AI服务已启动
    Steps:
      1. curl -X POST http://localhost:8000/api/v1/requirement/analyze \
         -H "Content-Type: application/json" \
         -d '{"requirement_text": "测试需求", "model_config": {"model_code": "TEST", "api_key": "test-key"}}'
      2. 检查返回状态码应为500（因为model_code不存在）或200（如果数据库有配置）
      3. 检查Python日志，确认model_config被正确接收（不是被忽略）
    Expected Result: 日志中显示接收到了model_config对象，没有"extra fields ignored"警告
    Failure Indicators: Pydantic报错"extra fields not permitted"
    Evidence: .sisyphus/evidence/task-1-receive-model-config.log

  Scenario: 验证有效参数提取
    Tool: Bash (curl)
    Preconditions: Python AI服务已启动
    Steps:
      1. 发送带有model_config的请求
      2. 在Python代码中添加临时日志打印effective_model_code
      3. 检查日志确认使用了model_config.model_code
    Expected Result: effective_model_code = model_config.model_code（不是None）
    Failure Indicators: effective_model_code为None
    Evidence: .sisyphus/evidence/task-1-effective-model-code.log
  ```

  **Evidence to Capture**:
  - [ ] 日志文件确认model_config被接收
  - [ ] 日志文件确认effective_model_code正确

  **Commit**: NO

---

- [ ] 2. 修改 llm_service.py 添加 call_model_with_config 方法

  **What to do**:
  - 在 `LLMService` 类中添加新方法 `call_model_with_config`
  - 方法签名：`call_model_with_config(model_code, prompt, max_tokens, temperature, model_config: Optional[Dict[str, Any]])`
  - 当 `model_config` 不为 None 且包含必要字段时：
    - 使用传入的 `model_config` 创建临时配置对象
    - 跳过数据库查询，直接使用传入的配置
  - 当 `model_config` 为 None 时，回退到现有的数据库查询逻辑

  **Must NOT do**:
  - 不修改现有的 `call_model` 方法（保持向后兼容）
  - 不破坏其他模块对 `call_model` 的调用

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 在现有服务中添加新方法，逻辑清晰
  - **Skills**: N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 3
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `backend-python/ai-service/app/services/llm_service.py:156-228` - 现有的 call_model 方法实现
  - `backend-python/ai-service/app/services/llm_service.py:33-85` - _get_llm_instance 方法实现

  **API/Type References**:
  - 同上

  **Test References**:
  - `backend-python/ai-service/tests/test_llm_service.py` - 现有LLM服务测试

  **WHY Each Reference Matters**:
  - 需要理解现有的call_model方法实现，以便创建类似的call_model_with_config方法
  - 需要了解_get_llm_instance如何使用ModelConfig对象

  **Acceptance Criteria**:
  - [ ] `call_model_with_config` 方法已添加
  - [ ] 方法能正确处理传入的model_config字典
  - [ ] 当model_config为None时，回退到数据库查询
  - [ ] 现有的call_model方法不受影响

  **QA Scenarios (MANDATORY)**:

  ```
  Scenario: 验证 call_model_with_config 使用动态配置
    Tool: Bash (python -c)
    Preconditions: Python环境已配置
    Steps:
      1. 创建测试脚本，模拟调用call_model_with_config
      2. 传入model_config包含有效的api_key和api_endpoint
      3. 验证方法使用了传入的配置而不是查询数据库
      4. 添加mock验证_get_llm_instance被调用时使用了正确的参数
    Expected Result: 方法正确使用动态配置，不需要从数据库查询
    Failure Indicators: 仍然从数据库查询，或抛出"model_config不存在"错误
    Evidence: .sisyphus/evidence/task-2-dynamic-config-test.log

  Scenario: 验证降级到数据库查询
    Tool: Bash (python -c)
    Preconditions: Python环境已配置，数据库有模型配置
    Steps:
      1. 调用call_model_with_config时传入model_config=None
      2. 验证方法回退到数据库查询
      3. 验证查询成功使用了数据库中的配置
    Expected Result: 正常回退，使用数据库配置
    Failure Indicators: 抛出异常，没有回退
    Evidence: .sisyphus/evidence/task-2-fallback-test.log
  ```

  **Evidence to Capture**:
  - [ ] 动态配置测试日志
  - [ ] 降级测试日志

  **Commit**: NO

---

- [ ] 3. 更新 requirement_router.py 使用新的 call_model_with_config 方法

  **What to do**:
  - 修改 `analyze_requirement` 函数中的调用
  - 将 `llm_service.call_model(...)` 改为 `llm_service.call_model_with_config(...)`
  - 传递 `model_config=request.model_config.dict() if request.model_config else None`

  **Must NOT do**:
  - 不修改其他接口的逻辑

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 简单的方法调用替换
  - **Skills**: N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 4
  - **Blocked By**: Task 1, Task 2

  **References**:

  **Pattern References**:
  - `backend-python/ai-service/app/api/requirement_router.py:38-45` - 现有的llm_service.call_model调用

  **Acceptance Criteria**:
  - [ ] analyze_requirement 使用 call_model_with_config
  - [ ] 正确传递 model_config 参数

  **QA Scenarios (MANDATORY)**:

  ```
  Scenario: 端到端验证修复
    Tool: Bash (curl)
    Preconditions: 所有服务已启动，数据库有模型配置
    Steps:
      1. 从Java后端触发需求分析（或模拟Java请求）
      2. 检查Python日志，确认使用了model_config中的配置
      3. 验证返回成功，无401错误
    Expected Result: 需求分析成功返回
    Failure Indicators: 仍然返回401错误
    Evidence: .sisyphus/evidence/task-3-e2e-test.log
  ```

  **Evidence to Capture**:
  - [ ] 端到端测试日志

  **Commit**: NO

---

- [ ] 4. 添加单元测试

  **What to do**:
  - 在 `backend-python/ai-service/tests/` 目录下添加或更新测试文件
  - 测试 `ModelConfigData` 模型正确解析
  - 测试 `call_model_with_config` 方法
  - 测试完整的analyze_requirement流程

  **Must NOT do**:
  - 不修改生产代码

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 标准单元测试编写
  - **Skills**: N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: Task 5
  - **Blocked By**: Task 1, Task 2, Task 3

  **References**:

  **Test References**:
  - `backend-python/ai-service/tests/test_api_llm.py` - 参考现有测试结构

  **Acceptance Criteria**:
  - [ ] 新增测试文件或更新现有测试
  - [ ] 所有测试通过
  - [ ] 测试覆盖率不降低

  **QA Scenarios (MANDATORY)**:

  ```
  Scenario: 运行单元测试
    Tool: Bash (pytest)
    Preconditions: Python环境已配置
    Steps:
      1. cd backend-python/ai-service
      2. pytest tests/ -v --tb=short
      3. 检查所有测试通过
    Expected Result: 所有测试PASS
    Failure Indicators: 有测试FAIL
    Evidence: .sisyphus/evidence/task-4-unit-test.log
  ```

  **Evidence to Capture**:
  - [ ] 测试运行结果日志

  **Commit**: NO

---

- [ ] 5. 手动验证和日志检查

  **What to do**:
  - 重启Python AI服务
  - 从前端触发需求分析功能
  - 检查日志确认：
    - 接收到model_config对象
    - 使用了正确的model_code
    - API调用成功（无401错误）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要完整验证修复效果
  - **Skills**: N/A

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential
  - **Blocks**: F1, F2
  - **Blocked By**: Task 1, Task 2, Task 3, Task 4

  **Acceptance Criteria**:
  - [ ] 需求分析功能正常工作
  - [ ] 日志显示使用了model_config
  - [ ] 无401错误

  **QA Scenarios (MANDATORY)**:

  ```
  Scenario: 完整功能验证
    Tool: Bash (docker logs)
    Preconditions: Docker容器已启动
    Steps:
      1. docker compose logs -f backend-python --tail=100 > .sisyphus/evidence/task-5-python.log &
      2. 从前端触发需求分析
      3. 等待10秒
      4. 检查日志中的关键信息：
         - "接收到的model_config" 或类似日志
         - "使用模型配置: modelCode=xxx"
         - 无"身份验证失败"错误
      5. 验证前端显示分析结果
    Expected Result: 需求分析成功，日志显示正确使用了配置
    Failure Indicators: 日志中有401或"身份验证失败"
    Evidence: .sisyphus/evidence/task-5-python.log
  ```

  **Evidence to Capture**:
  - [ ] Python服务日志
  - [ ] 前端成功截图（如果适用）

  **Commit**: NO

---

## Final Verification Wave (MANDATORY)

- [ ] F1. **功能验证** — `unspecified-high`
  验证需求分析功能从触发到返回结果全流程正常。
  - [ ] 触发需求分析，返回成功结果
  - [ ] 无401或500错误
  - [ ] 日志显示使用了正确的model_config
  Output: `功能 [PASS/FAIL] | 错误 [NONE/401/500]`

- [ ] F2. **回归测试** — `quick`
  验证其他依赖llm_service的功能不受影响。
  - [ ] 运行现有单元测试
  - [ ] 检查其他API接口（如用例生成）正常工作
  Output: `测试 [PASS/FAIL] | 其他功能 [正常/异常]`

---

## Commit Strategy

- **1**: `fix(ai-service): support model_config from Java backend to fix 401 auth error` — requirement_router.py, llm_service.py

---

## Success Criteria

### Verification Commands
```bash
# 重启服务
docker compose restart backend-python backend-java

# 检查日志
docker compose logs -f backend-python | grep -E "model_config|身份验证|401"

# 运行测试
cd backend-python/ai-service && pytest tests/ -v
```

### Final Checklist
- [ ] Python端能接收model_config对象
- [ ] 优先使用model_config中的配置
- [ ] 无401身份验证错误
- [ ] 现有功能不受影响
