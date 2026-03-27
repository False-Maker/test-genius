# test-genius 重构计划

## 0. 校正前提

本计划以当前仓库代码现状为准，不以 README 的历史描述为准。

需要先明确 5 个事实：

1. 当前问题的核心不只是“前端直连 Python”，更准确地说，是前端残留了大量 Python 风格路径、Python 风格响应结构、以及未收口的跨服务协议；其中部分接口在 `baseURL=/api` 前提下还会形成双 `/api` 前缀，直接返回 404。
2. Java 侧并非完全没有网关雏形，已经存在 `AIServiceClient`；问题在于没有形成统一的 `integration/python` 边界，很多业务 service 仍在各自直接 `RestTemplate` 调 Python。
3. Python 当前仍直接写 `workflow_execution`、`agent_session`、`agent_message` 等 Java 业务表，其中 `workflow_execution` 还存在 Java 与 Python 双写并存的问题。
4. Python 不仅是内部执行层，还暴露了 `/java/*` 反向代理入口，并在代码中写死了 Java 地址；这既是边界问题，也是运维与安全问题。
5. README、目录说明与代码实现存在明显漂移；重构过程需要同步修正文档，而不是继续以旧文档指导开发。

## 1. 重构目标与非目标

### 目标

- 收拢混乱的跨服务调用链，建立清晰的边界与职责。
- 修复主链路中的伪异步、路径失配、协议冲突和硬编码问题。
- 把平台能力（Agent、Workflow、Monitoring）从主业务闭环中解耦，避免继续侵入核心链路。

### 非目标

- 首月不做完整安全平台重写。
- 不全面拆服务。
- 不在现有错误边界上继续叠加新功能。

## 2. 三层边界与主调用链

### Frontend 边界

- 只负责页面状态、交互和展示。
- 浏览器不直接访问 Python 服务，也不直接依赖 Python 的 URL、DTO、响应格式。
- 不承载业务编排，不拥有任务状态。
- 所有对外 HTTP 统一经 `frontend/src/api` 调用 Java 公开 API。
- 对外 API 约定建议统一收敛到 Java 的公开前缀；如采用 `/api/v1/**`，则前端不得再保留 Python 风格路径。

### Java 边界

- 唯一公开 API。
- 业务状态拥有者。
- 任务生命周期拥有者。
- 审计、权限、监控入口。
- 对 Python 的访问统一收敛到 `integration/python` 或 `AiGateway/PythonGateway`。
- 统一适配 `Result<T>`、plain JSON、文件流等对前端可感知的响应协议。

### Python 边界

- 内部 AI、RAG、Workflow、Agent 执行层。
- 只接收 Java 定义的内部契约 DTO/Schema。
- 不再直接写 Java 拥有的 `workflow_execution`、`agent_session`、`agent_message` 以及任务/业务状态表。
- 不再承担 Java 的反向代理入口职责。

### 主调用链

- CRUD 查询链：Frontend → Java Controller/Service/Repository → DB
- AI 同步链：Frontend → Java Facade → Python Internal API → Java 归一化响应 → Frontend
- AI 异步链：Frontend 提交 → Java 建任务/执行记录 → 异步执行器调 Python → Java 更新状态 → Frontend 轮询 Java
- Agent 链：Frontend → Java Agent API → Python Agent Engine → Java 持久化消息、工具调用和会话状态

## 3. 当前问题分级

### P0：修主链路断点

- 前端绕过 Java 契约，直接依赖 Python 风格路径或 Python 风格响应结构；部分请求还因双 `/api` 前缀直接 404。
- 请求层协议冲突：当前 `request.ts` 默认只适配 `Result<T>`，但项目中同时存在 plain JSON 和 `blob`。
- 伪异步：多个 `@Async` 方法存在同类内直接调用问题。
- 工作流执行路径错配、内部链路前后缀拼接错误。
- 硬编码地址、反向代理与 dev 容器访问路径不一致。
- 生产链路下代理超时配置与 AI 请求时长不一致。
- 假登录、死接口、半成品接口仍暴露在主导航链路中。
- CI 与本地开发模式漂移。

### P1：收边界

- Java 调 Python 未统一收口。
- Java/Python 内部契约缺失。
- Python 仍越层落库。
- Agent、Workflow 对外链路未归一。

### P2：整理平台

- 真实异步底座。
- 安全鉴权。
- 模块收敛。
- 可观测性和发布治理。

## 4. 阶段一：2 周止血

### 涉及模块

- `frontend/src/api`
- `frontend/src/views/{case-generation,test-execution,agent,login,workflow}`
- `frontend/vite.config.ts`
- `frontend/nginx.conf`
- `backend-java/.../service/impl`
- `backend-java/.../controller`
- `backend-java/.../resources/application.yml`
- `backend-python/ai-service/app/{main.py,api,config.py}`
- `docker-compose.yml`
- `.github/workflows/ci.yml`

### 主要改动

- 下线或改造所有 Python 风格接口，不再让页面依赖 Python 的 path 或 response shape。
- 修复 Agent、LLM、Workflow 等接口中的双 `/api` 前缀问题，先把当前硬 404 链路恢复到“可到达”状态。
- 工作流执行统一改走 Java，对外只暴露 Java 的执行/轮询接口。
- `request.ts` 增加 `Result`、plain JSON、`blob` 三种响应模式。
- 清理前端 API 中硬编码的双前缀路径，禁止在 `baseURL=/api` 前提下继续书写 `/api/...`。
- 修复 `@Async` 自调用，至少保证当前“提交即返回任务号”的主链路是成立的。
- 修复 Case Reuse 内部链路前后缀错误，重点是 Java 调 Python 的路径与 Python 实际挂载路径不一致，而不是前端页面路由错误。
- 修复 Python 访问 Java 的硬编码问题；这不仅是改环境变量，还包括 `main.py` 中写死地址、Agent 配置中的默认值，以及 `docker-compose` 中缺失的环境注入。
- 下线 Python 侧 `/java/*` 反向代理入口，不继续暴露兼容代理。
- 修正生产链路下 `nginx` / Java / 前端的超时配置，使长 AI 请求不被 60 秒代理超时提前切断。
- 补充并收紧 `workflow_execution` 的主写方约定，阶段一先停止继续扩大双写入口。
- 移除、隐藏或打功能开关处理以下无后端实现或契约不一致接口：
  - `/auth/*`
  - `/v1/requirements/analyze`
  - `/v1/requirements/{id}/analysis-history`
  - `/api/v1/agent/tools`
  - `/v1/agent-tools/**`
- 对 README、开发命令、dev 代理做最小纠偏，避免继续误导开发。
- 修正 CI 中 Python 构建检查和测试环境配置，避免出现“绿灯但关键链路未被验证”的假象。

### 依赖关系

先冻结“Java 是唯一对外入口” → 再改前端 API → 再修 Java 异步 → 最后补 Python 与环境配置。

### 测试与验收

- 增加 5 条 smoke 链路：需求分析、用例生成提交/轮询、工作流执行、UI 脚本生成、Case Reuse 搜索。
- 前端请求层补 `blob` / plain JSON 单测。
- 开发模式容器链路可通。
- 生产链路或前端容器链路下，长时间 AI 请求不会被 60 秒代理超时提前切断。

### 验收标准

- 主导航功能不再依赖死接口、双 `/api` 路径或 Python 风格接口。
- 长任务提交后立即返回任务号，可轮询到终态。
- `workflow`、`case-generation`、`test-execution` 三条主链路统一回到 Java 公开 API。
- Python 不再对外暴露 `/java/*` 代理入口。

## 5. 阶段二：1 个月收边界

### 涉及模块

- `backend-java/.../controller`
- `backend-java/.../service`
- 新增 `backend-java/.../integration/python`
- `backend-python/ai-service/app/api`
- `backend-python/ai-service/app/services/{workflow_engine,agent_service,...}`
- `frontend/src/api`

### 主要改动

- 在现有 `AIServiceClient` 基础上收敛出统一的 Java `AiGateway` / `integration/python`，逐步替换散落的 `RestTemplate`。
- 为需求分析、工作流、Agent、知识库、用例生成定义内部 DTO/Schema。
- Python 路由降为内部接口，不再作为前端可感知的公开契约来源。
- 停止 Python 在工作流执行里重复写 `workflow_execution`，并为双写历史制定迁移与核对策略。
- 停止 Python 直接写 `agent_session`、`agent_message` 等 Java 业务状态表。
- Agent 对外接口统一回到 Java；缺失的工具管理要么补齐，要么从 UI 移除。
- 清理阶段一保留的兼容代码和临时适配层。

### 依赖关系

必须先完成阶段一的 smoke 基线。

### 测试与验收

- Java-Python 契约测试覆盖核心 internal API。
- Java 集成测试 mock Python。
- Python 测试验证 DTO 兼容。

### 验收标准

- Frontend 只依赖 Java 公开 API。
- Python 不再直接拥有 Java 业务表写权限。
- Java 调 Python 的实现路径可枚举、可治理、可监控。

## 6. 阶段三：2 到 3 个月整理平台

### 涉及模块

- 三端全部，重点是：
  - `backend-java` 包结构
  - `backend-python/app/services`
  - `frontend/src/views`
  - `monitoring/`
  - 发布脚本

### 主要改动

- 按业务域重组模块。
- 把长任务从进程内 `@Async` 迁到统一任务执行底座。
- 补完整安全链路。
- 建立稳定/实验功能分层。
- 把部署脚本从占位改成可执行流水线。

### 依赖关系

阶段二完成后再做。

### 测试与验收

- CI 增加真实前端测试，而不是只跑 lint。
- 每个域至少 1 条端到端金路径。
- 发布具备回滚开关和演练记录。

## 7. 必须先修 / 可以延后

### 必须先修

- 前端绕过 Java 契约、双 `/api` 前缀与响应协议冲突。
- 伪异步。
- 工作流执行路径错配与 Case Reuse 内部链路前后缀错误。
- Python 硬编码 Java 地址与 `/java/*` 代理入口。
- 生产链路代理超时配置不一致。
- 开发/CI 配置漂移。
- 主链路上的假登录和死接口。

### 可以延后

- `RestTemplate` → `WebClient` 技术替换。
- 完整 JWT / SSO。
- 消息队列 / 事件总线。
- 模块重命名和高级监控看板。

## 8. 风险与回滚

### 风险

- 改统一入口后，会暴露一批之前被 mock、错误响应适配或死接口掩盖的假成功测试。
- `workflow_execution` 当前存在 Java 与 Python 双写；如果不先明确主写方和迁移策略，后续收边界时会出现历史数据冲突、重复和来源不清的问题。
- 去掉 Python 越层落库后，历史页面可能短期缺数据。
- 如果只修业务代码、不同时修正代理和超时配置，表面上“接口已改通”，生产链路仍可能被 60 秒超时切断。
- 文档和配置同步不及时，会导致“代码已修、开发方式仍错”的二次混乱。

### 回滚策略

- 新旧链路并行一版，Java Gateway 保留兼容适配。
- 所有改造优先加法，不先删表、不先拆库。
- 对 Agent、Workflow、登录页加功能开关，不能在一轮里全量硬切。
- 对 `workflow_execution` 双写链路保留核对窗口，迁移期内需要能回查“谁写入、何时写入、按哪条链路写入”。

## 9. 建议产出物

建议补建 `docs/refactor/`，至少包含：

- `docs/refactor/boundary-map.md`：三层边界、调用链、拥有权
- `docs/refactor/api-inventory.md`：前端 API 到 Java/Python/DB 的映射与废弃清单
- `docs/refactor/contract-registry.md`：Java-Python DTO/Schema 目录
- `docs/refactor/smoke-matrix.md`：核心冒烟链路和验收口径
- `docs/refactor/rollback-playbook.md`：开关、兼容期、回滚步骤
- `docs/refactor/module-ownership.md`：域负责人和目录归属

## 10. 建议执行顺序

先做 P0，不在当前错误边界上继续加功能。

P0 完成后再开 P1，否则只会把错误接口、错误协议和伪异步固化得更深。
