 架构审计报告：test-genius

  ---
  A. 独立审查结论

  这个系统在三个核心链路上全部断路，不是"需要重构"，是现在就不能正常运行。

  架构现状：
  - Java（port 8080）是名义入口，有 context-path /api，有 35 个 Controller，有 11 个 Service 各自独立持有 Python URL
  - Python（port 8000）是实际 AI 执行层，路由前缀是 /api/v1/...，同时直接写 Java 业务表，还有一个 /java/{path} 反向代理入口指向 localhost:8080
  - 前端（port 3000）baseURL=/api，有 6 处路径写了 /api/v1/...，产生 double-prefix（/api/api/v1/...），这些请求既到不了 Java 也到不了 Python，当场
  404
  - 认证：前端路由守卫全部注释掉，Java 没有 AuthController、没有 SecurityConfig，登录 API /auth/login 是死接口

  ---
  B. 最严重问题 Top 10

  1. Double-prefix 导致三条主链路当场 404

  - 问题：request.ts:43 baseURL=/api；agent.ts:162,170,176 路径写 /api/v1/agent/...；llm.ts:38,43 路径写 /api/v1/llm/...；workflow.ts:175 路径写
  /api/v1/workflow/execute
  - 实际请求路径：/api + /api/v1/agent/chat = /api/api/v1/agent/chat
  - 为什么严重：Agent 对话、LLM 调用、Workflow 执行三条链路，在到达任何后端之前就已经 404。这不是"绕过 Java"，是根本没到任何地方
  - 证据：frontend/src/api/request.ts:43、frontend/src/api/agent.ts:162,170,176、frontend/src/api/llm.ts:38,43、frontend/src/api/workflow.ts:175
  - 如果不修：这三个功能模块一个 HTTP 请求都无法成功

  ---
  2. Workflow 执行存在三层路径不一致

  - 问题：
    - 前端调用（含 double-prefix 修正后应为）：POST /api/v1/workflow/execute
    - Python 提供：POST /api/v1/workflow/execute（workflow_router.py:16，挂载在 prefix="/api/v1/workflow"）
    - Java 提供：POST /api/v1/workflow-executions/execute（WorkflowExecutionController.java:26,33）
  - 问题本质：即使修复 double-prefix，前端 workflow 执行请求会打到 Python 而非 Java（因为路径与 Java 不匹配），Java 的 Workflow 执行链路（含 JPA
  状态管理）完全被绕过
  - 证据：frontend/src/api/workflow.ts:175、WorkflowExecutionController.java:26、backend-python/ai-service/app/api/workflow_router.py:16
  - 如果不修：Workflow 执行永远打 Python，Java 里的 WorkflowExecution 生命周期管理形同虚设

  ---
  3. @Async 自调用：两处伪异步，任务提交即同步阻塞

  - 问题：IntelligentCaseGenerationServiceImpl.java:117 和 :215 直接 this.executeGenerationTask(task.getId())，绕过 Spring
  代理，@Async（:242）完全失效；WorkflowExecutionServiceImpl.java:85 同样直接调 executeWorkflowAsync()（:90 有 @Async），也失效
  - 为什么严重：用例生成和工作流执行都是长任务，失效后在 HTTP 线程池上同步执行，直接耗尽线程
  - 证据：IntelligentCaseGenerationServiceImpl.java:117,215,242、WorkflowExecutionServiceImpl.java:85,90
  - 如果不修：高并发时服务线程打满，整个 Java 服务无响应

  ---
  4. Python 直写 Java 业务表（多张，含 raw SQL）

  - 问题：Python 用 raw SQL 直接写 agent_session（agent_service.py:138）、agent_message（agent_context_service.py:102）、agent_tool_call（agent_serv
  ice.py:250）、workflow_execution（workflow_engine.py:615）
  - 为什么严重：Java 的 JPA 实体完全被绕过，事务边界混乱，Java 侧无法感知 Python 写入，字段约束和审计逻辑均失效；workflow_execution 表被 Java 和
  Python 双写
  - 证据：backend-python/ai-service/app/services/agent_service.py:138,250、app/services/agent_context_service.py:102、app/services/workflow_engine.p
  y:615
  - 如果不修：数据一致性无保障，重构期间任何对表结构的改动都会同时破坏 Java 和 Python 两侧

  ---
  5. Python /java/{path} 反向代理指向 localhost:8080，容器内永久失效

  - 问题：main.py:74 java_backend_url = "http://localhost:8080"，没有任何环境变量覆盖（不是 os.getenv）；docker-compose.yml 也没有设置
  JAVA_API_BASE_URL for Python 容器
  - 进一步：config.py:7 JAVA_API_BASE_URL = os.getenv("JAVA_API_BASE_URL", "http://localhost:8080") 虽然读了环境变量，但 docker-compose
  里根本没传这个变量，Python 容器里实际值仍是 localhost
  - 为什么严重：/java/ 反向代理在容器部署下永久 502；且这个接口本身就是高危设计（开放代理），在失效状态下还在注册路由
  - 证据：main.py:74、config.py:7、docker-compose.yml（无 JAVA_API_BASE_URL 字段）
  - 如果不修：任何依赖此代理的调用链全部 502；容器化环境下测试结果无法信任

  ---
  6. 认证系统完全缺失：没有 AuthController，没有 SecurityConfig，有死接口

  - 问题：
    - auth.ts:25 调 /auth/login；Java 没有任何 AuthController（已逐一列出全部 35 个 Controller）
    - Java 没有 SecurityConfig，没有 JWT 过滤器
    - router/index.ts:331-369 所有路由守卫注释掉，next() 直接放行
  - 为什么严重：前端完全无鉴权；任何接口都是裸奔；/auth/login 是真正的死接口，调用后 404
  - 证据：frontend/src/api/auth.ts:25、frontend/src/router/index.ts:368（next() 无条件放行）
  - 如果不修：任何人可以不登录访问任何页面；/auth/login 调用始终失败

  ---
  7. 11 个 Java Service 各自独立持有 Python URL，AIServiceClient 形同虚设

  - 问题：Java 中 11 个 ServiceImpl 各自有 @Value("${app.ai-service.url:http://localhost:8000}") 和独立
  RestTemplate，包括：UIScriptRepairServiceImpl、UIScriptGenerationServiceImpl、WorkflowExecutionServiceImpl、KnowledgeBaseServiceImpl、FlowDocument
  GenerationServiceImpl、CaseReuseServiceImpl、RequirementAnalysisServiceImpl、IntelligentCaseGenerationServiceImpl、KnowledgeBaseManageServiceImpl
  、DataDocumentGenerationServiceImpl、ModelCallServiceImpl
  - AIServiceClient 存在但没人用：service/AIServiceClient.java 和 AIServiceClientImpl.java 确实存在，但这 11 个 Service 完全无视它
  - 证据：11 个 @Value 位置已逐一列出（见上方 grep 结果）
  - 如果不修：无法统一熔断、超时、重试策略；无法统一监控 Python 调用链

  ---
  8. Nginx 超时 60s 是整个链路的瓶颈天花板

  - 问题：nginx.conf:20-22 proxy_read_timeout 60s；Java ai-service.timeout: 180000（application.yml）；Python LLM_REQUEST_TIMEOUT:
  120（config.py:14）；前端超时 180000ms（request.ts:48）
  - 为什么严重：AI 调用通常需要 90s+，Nginx 在 60s 时就断连，Java 的 180s 超时配置完全失效，前端展示的"请求失败"实际上是 Nginx 断掉的，不是 AI 失败
  - 证据：frontend/nginx.conf:20-22、application.yml ai-service timeout
  - 如果不修：任何 >60s 的 AI 请求都会被 Nginx 切断，即使后端还在正常执行

  ---
  9. /v1/requirements/analyze（无 id）和 /v1/requirements/{id}/analysis-history 是真正的死接口，但还有被调用的代码

  - 问题：
    - requirementAnalysis.ts:13 调 POST /v1/requirements/analyze（无 id）
    - requirementAnalysis.ts:28 调 GET /v1/requirements/{id}/analysis-history
    - Java RequirementController.java 只有 POST /v1/requirements/{id}/analyze（有 id），没有 POST /v1/requirements/analyze（无 id），没有
  analysis-history 端点
  - 证据：frontend/src/api/requirementAnalysis.ts:13,28、RequirementController.java（全文已读，无对应方法）
  - 如果不修：这两个接口在页面调用时 404，且没有任何明显提示

  ---
  10. CI Python 构建用的 shell glob 在 GitHub Actions 上可能静默失败

  - 问题：ci.yml:76 python -m py_compile app/**/*.py，在 Ubuntu bash 下 ** 不展开子目录（需要 shopt -s globstar），导致 py_compile
  实际上只编译第一层，大量文件被静默跳过
  - Python 测试无数据库依赖：CI 中 Python test job 没有 postgres service，但 agent_service、workflow_engine 等都直接使用
  SQLAlchemy，测试要么全跳过要么全失败
  - Java CI 环境变量漂移：CI 设 SPRING_REDIS_HOST，但 application.yml 读的是 spring.data.redis.host（Spring Boot 2.x
  旧写法），需要确认是否能正确绑定
  - 证据：.github/workflows/ci.yml:76,163-200

  ---
  C. 对文档的"打脸清单"

  ---
  C1. "前端绕过 Java 契约，直接依赖 Python 风格路径"

  - 文档判定：准确
  - 但表述失真：文档把这个问题描述成"前端直连 Python"，实际情况更糟——由于 double-prefix，这些请求根本连 Python 都到不了，是硬 404。不是"绕过
  Java"，是"废弃请求"
  - 证据：request.ts:43 (baseURL=/api) + agent.ts:162 (/api/v1/agent/chat) = /api/api/v1/agent/chat → nginx 代理到
  http://backend-java:8080/api/api/v1/agent/chat → Java 找不到这个路径，404
  - 更准确的说法：agent chat、LLM call、workflow execute 三个功能在当前代码状态下产生 double-prefix，HTTP 请求 404，功能完全不可用，不是"contract
  mismatch"，是"服务从未收到请求"

  ---
  C2. "Case Reuse 路由错误"（P0 必须先修）

  - 文档判定：不准确（证据不足，且疑似判断错误）
  - 代码事实：caseReuse.ts:24,29,33 全部使用 /v1/case-reuse/...；Java CaseReuseController.java:20
  @RequestMapping("/v1/case-reuse")；路径完全一致，没有 case reuse 路由错误
  - 证据：frontend/src/api/caseReuse.ts（全文已读）、CaseReuseController.java:20
  - 更准确的说法：文档把 Case Reuse 列为 P0 路由错误是错的。Case Reuse 的前端 API 路径指向正确。文档可能混淆了另一个有 double-prefix 的接口

  ---
  C3. "Python 硬编码 Java 地址" / "改成环境变量"

  - 文档判定：基本准确但低估了
  - 被漏掉的关键事实：main.py:74 java_backend_url = "http://localhost:8080" 是普通变量赋值，不读环境变量，改环境变量根本没用；而且
  docker-compose.yml 里 Python 服务的 environment 块完全没有 JAVA_API_BASE_URL，即使 config.py:7 读了环境变量，容器里也是 localhost
  - 双层漏掉：agent_service.py:219 agent_config.get("java_api_base_url", "http://localhost:8080") 从 agent 配置读，完全是另一套机制，与 config.py
  无关
  - 更准确的说法：Python 硬编码问题分三处，只改 config.py 不够；main.py 中的 proxy 函数必须单独修，且 docker-compose 必须补 JAVA_API_BASE_URL:
  http://backend-java:8000 环境变量（注意不是 8080，是 8000 port mapping 到容器内）

  ---
  C4. "Java 统一 Gateway / 已经存在 AIServiceClient"

  - 文档判定：基本准确但说轻了
  - 真实情况：AIServiceClient 存在，但被 11 个 ServiceImpl 无视，这 11 个各自有独立的 @Value + RestTemplate。不是"散落"，是"压根没人用
  AIServiceClient"
  - 证据：@Value("${app.ai-service.url}") 出现在 11 个不同的 ServiceImpl 里（已逐一列出）
  - 更准确的说法：AIServiceClient 是孤立的，主线业务都绕过了它，收口工作量远比"在现有基础上扩展"更大

  ---
  C5. "主链路上的假登录和死接口"

  - 文档判定：准确，但范围说窄了
  - 漏掉的内容：
    - requirementAnalysis.ts:13（POST /v1/requirements/analyze 无 id 版本）是死接口
    - requirementAnalysis.ts:28（GET /v1/requirements/{id}/analysis-history）是死接口
    - 这两个接口不在文档的下线列表里，但确实无对应 Java 实现
  - 证据：RequirementController.java（全文已读，无这两个端点）

  ---
  C6. "CI 与本地开发模式漂移"

  - 文档判定：基本准确但描述模糊
  - 具体问题被漏：
    - ci.yml:76 Python py_compile 的 ** glob 静默失败
    - Python CI 无数据库 service，测试实际无法覆盖任何涉及 DB 的代码
    - dev 容器（backend-java-dev、backend-python-dev）都在 profiles: dev 下，且 Python dev 容器也没有 JAVA_API_BASE_URL
  - 更准确的说法：CI 的 Python "构建"检查是虚假的安全感；Python 的 DB 相关逻辑在 CI 中完全未被测试

  ---
  C7. "阶段一范围 / 建议执行顺序"

  - 文档判定：优先级判断有误
  - 具体问题：文档把 Case Reuse 路由错误列为 P0，但这个问题不存在（见 C2）；而 double-prefix 的本质（404，而非"绕过"）更需要立即修复；Nginx 60s
  超时瓶颈完全不在阶段一范围里，但会导致 AI 功能上线后立即全部超时
  - 文档漏掉的阶段一项目：
    a. 修 Nginx proxy_read_timeout（最快，改一行，影响所有 AI 功能）
    b. main.py:74 hardcode 无环境变量覆盖（改一行即可，但漏掉了）
    c. docker-compose 补 Python 容器的 JAVA_API_BASE_URL

  ---
  C8. "去掉 Python 越层落库后，历史页面可能短期缺数据"（风险一节）

  - 文档判定：把问题描述得太轻
  - 真实风险：Python 直写 workflow_execution、agent_session、agent_message，而 Java 也在写
  workflow_execution（WorkflowExecutionServiceImpl）。这是双写，不只是"Python 在写"。迁移时必须处理数据来源混乱的问题，不是简单地"停止 Python 写"
  - 证据：WorkflowExecutionServiceImpl.java:68（Java JPA 写 workflow_execution）+ workflow_engine.py:615（Python raw SQL 写同一张表）

  ---
  D. 文档成立的部分

  以下内容源码可以验证：

  1. "前端 baseURL=/api，统一走 Nginx/Vite 代理"：正确，request.ts:43 确实是 /api，Vite proxy 和 Nginx 都配置了 /api → Java:8080
  2. "Java 有 AIServiceClient 雏形但未统一"：正确，AIServiceClient.java 存在，但被大量绕过
  3. "Python 仍直接写 workflow_execution、agent_session、agent_message"：正确，三张表均有 raw SQL INSERT
  4. "@Async 存在同类内自调用"：正确，IntelligentCaseGenerationServiceImpl 和 WorkflowExecutionServiceImpl 都有此问题
  5. "无统一内部 DTO/Schema"：正确，Python 和 Java 之间的调用都是 raw Map，没有定义的契约
  6. "Python CORS 默认允许 localhost 三端"：正确，main.py:34-37
  7. "P0 先于 P1，P1 先于 P2 的执行顺序总原则"：正确

  ---
  E. 文档遗漏项（必须进入计划）

  E1. Nginx proxy_read_timeout 60s 必须列入阶段一
  - 改一行，效果覆盖所有 AI 功能。不改，阶段一所有 AI 接口上线即超时
  - 文件：frontend/nginx.conf:20-22

  E2. main.py:74 hardcode 无法通过环境变量覆盖
  - 不是 os.getenv，不读任何环境变量，必须单独改代码
  - 文件：backend-python/ai-service/app/main.py:74

  E3. docker-compose Python 容器缺 JAVA_API_BASE_URL 环境变量
  - 即使 config.py 改了，容器不传环境变量，Python 还是 localhost
  - 文件：docker-compose.yml（backend-python 的 environment 块）

  E4. /java/{path} 反向代理是安全隐患，应列为 P0 下线
  - 文档把这个放在阶段二"评估"，但这是一个暴露在 Python 服务上的开放代理，任何人调用 Python 的 /java/xxx 就能转发任意请求到 Java。即使现在 localhost
   失效，容器环境修好后立即变成高危
  - 文件：backend-python/ai-service/app/main.py:77-121

  E5. WorkflowExecution 双写问题
  - Java JPA 写 workflow_execution + Python raw SQL 也写同一张表，迁移前必须明确数据所有权，不能只说"停止 Python 写"

  E6. CI Python 构建虚假安全感
  - py_compile app/**/*.py 的 glob 问题
  - Python CI 测试无数据库，覆盖率虚高
  - 阶段一就应该修，否则 CI 一直是误导性的绿灯

  E7. requirementAnalysis.ts:13,28 的两个死接口未列入下线清单
  - 文档的下线列表漏了这两个

  E8. agent_service.py:219 的第三处 hardcode
  - 来源是 agent_config 配置，与 config.py 完全独立，需要单独处理

  ---
  F. 如果由我重写这份计划

  保留：
  - 三层边界划分（Frontend/Java/Python）的总体方向正确
  - P0/P1/P2 分级框架正确
  - "先止血再收边界"的执行策略正确
  - 阶段二对 AIServiceClient 收口的方向正确

  删掉：
  - "修复 Case Reuse 路由前后缀错误"——代码证明这个问题不存在，不要浪费时间
  - "评估并下线 Python 侧 /java/* 反向代理入口"里的"评估"二字——直接下线，不需要评估

  重排：
  - Nginx proxy_read_timeout 从"未列出"提到阶段一第一条，改一行，解锁所有 AI 功能
  - main.py:74 hardcode 从"环境变量改造"提到阶段一独立条目
  - /java/{path} 反向代理下线从阶段二提到阶段一

  必须补充：
  - 补充 double-prefix 的精确技术描述（不是"绕过 Java"，是"404 废弃请求"），防止工程师改错方向
  - 补充 WorkflowExecution 双写的迁移策略（谁是 master，历史数据怎么处理）
  - 补充 CI 修复的具体动作（Python glob 问题、数据库 service 补充）
  - 在阶段一验收标准里加入"Nginx 超时不触发"的具体测试用例
  - docker-compose.yml 需要补 JAVA_API_BASE_URL 的修复必须列入阶段一环境配置项
