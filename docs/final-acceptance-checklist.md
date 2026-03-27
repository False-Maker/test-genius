# 项目级最终验收清单

更新日期：2026-03-27

本文档记录当前工作区在开发环境中的最终验收状态，重点覆盖本轮实际收口的前端、Java、Python、知识库回填与手动运维入口。

自动化入口：
- `./scripts/run-final-acceptance.sh`
- GitLab manual job: `acceptance:final`
- GitHub manual workflow: `Final Acceptance`
- 平台 runbook: `docs/platform-manual-runbook.md`

## 1. 运行环境

- [x] `docker compose --profile dev` 核心容器已启动
  - `frontend-dev` `http://127.0.0.1:3000`
  - `backend-java-dev` `http://127.0.0.1:8081`
  - `backend-python-dev` `http://127.0.0.1:8001`
  - `postgres`
  - `redis`
- [x] Java liveness 通过
  - `curl -fsS http://127.0.0.1:8081/api/actuator/health/liveness`
  - 返回 `{"status":"UP"}`
- [x] Python health 通过
  - `curl -fsS http://127.0.0.1:8001/health`
  - 返回 `{"status":"healthy"}`
- [x] 前端登录页可访问
  - `curl -fsS http://127.0.0.1:3000/login`
  - 返回 Vite 页面壳和 `测试设计助手系统` 标题

## 2. 前端验收

- [x] ESLint 检查通过
  - `cd frontend && npm run lint:check`
  - 结果：0 error，存在 9 条 warning
- [x] TypeScript 类型检查通过
  - `cd frontend && npm run type-check`
- [x] Vitest 全量单测通过
  - `cd frontend && npm run test:run`
  - 结果：`13 files / 192 tests passed`
- [x] 前端生产构建通过
  - `cd frontend && npm run build`
  - 结果：构建成功，存在 Sass deprecation 与 chunk size warning
- [x] Playwright UI smoke 通过
  - `cd frontend && npm run test:smoke`
  - 覆盖项：
    - 登录进入主应用壳
    - 用例复用语义检索、更新向量、创建测试套件
    - 知识库上传文档并做语义检索
    - 知识库 `Add New` 打开弹窗、真实提交、列表出现新文档

## 3. Java 验收

- [x] Java 全量测试套件通过
  - `docker exec test-design-backend-java-dev mvn -f /workspace/test-design-assistant-core/pom.xml test`
  - 结果：`Tests run: 412, Failures: 0, Errors: 0, Skipped: 0`
- [x] Java Controller 测试批量通过
  - `docker exec test-design-backend-java-dev mvn -f /workspace/test-design-assistant-core/pom.xml -Dtest='*ControllerTest' test`
  - 结果：`Tests run: 111, Failures: 0, Errors: 0, Skipped: 0`
- [x] Java Repository 测试批量通过
  - `docker exec test-design-backend-java-dev mvn -f /workspace/test-design-assistant-core/pom.xml -Dtest='*RepositoryTest' test`
  - 结果：`Tests run: 24, Failures: 0, Errors: 0, Skipped: 0`
- [x] 知识库 Controller 定向回归通过
  - `docker exec test-design-backend-java-dev mvn -f /workspace/test-design-assistant-core/pom.xml -Dtest=KnowledgeBaseControllerTest test`
  - 结果：`Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`
- [x] Java 测试环境基建问题已部分修复
  - 修复项：
    - test profile 强制使用 H2
    - 关闭 test profile Flyway
    - `maven-surefire-plugin` 注入测试 datasource 参数
  - 结果：之前大批 controller 测试的 `Flyway/H2 datasource mismatch` 已消除
- [x] 当前知识库 `Add Document` JSON 契约已覆盖 `kbId` 场景
  - 位置：
    - `backend-java/test-design-assistant-core/src/main/java/com/sinosoft/testdesign/controller/KnowledgeBaseController.java`
    - `backend-java/test-design-assistant-core/src/test/java/com/sinosoft/testdesign/controller/KnowledgeBaseControllerTest.java`

## 4. Python 验收

- [x] Python 全量测试通过
  - `docker exec test-design-backend-python-dev sh -lc 'cd /app && pytest tests/ -q --no-cov --capture=no'`
  - 结果：`272 passed, 7 warnings`
- [x] 知识库服务与回填脚本定向单测通过
  - `docker exec test-design-backend-python-dev sh -lc 'cd /app && pytest tests/scripts/test_backfill_knowledge_document_embeddings.py tests/services/test_knowledge_base_service.py -q --no-cov'`
  - 结果：`11 passed`
- [x] 回填脚本可执行
  - `docker exec test-design-backend-python-dev sh -lc 'cd /app && python scripts/backfill_knowledge_document_embeddings.py --dry-run --kb-id 2'`
  - 返回示例：`{"kb_id": 2, "missing_count": 0, "embedding_provider": "openai"}`
- [x] 回填脚本支持结果文件落盘
  - `--output-file /tmp/kb-backfill-result.json`
- [x] 回填脚本支持失败退出码
  - `--fail-on-remaining`
- [x] 回填脚本环境校验有效
  - 缺 `DATABASE_URL` 时直接失败并返回非 0

## 5. 知识库数据验收

- [x] 当前活跃知识库文档无 `embedding IS NULL`
  - SQL：
    - `SELECT COUNT(*) FROM knowledge_document WHERE is_active = '1' AND embedding IS NULL;`
  - 结果：`0`
- [x] 历史文档懒回填链路有效
  - 已通过“插入旧文档 -> 触发语义检索 -> 查询 embedding 回填成功 -> 清理测试数据”验证
- [x] 一次性回填脚本有效
  - 已通过“插入旧文档 -> 执行 backfill 脚本 -> 查询 embedding 回填成功 -> 清理测试数据”验证

## 6. 运维入口验收

- [x] 本地统一入口脚本存在
  - `scripts/run-kb-embedding-backfill.sh`
- [x] 项目级最终验收脚本存在
  - `scripts/run-final-acceptance.sh`
- [x] GitLab manual job 已配置
  - `maintenance:kb-embedding-backfill`
- [x] GitLab manual final acceptance job 已配置
  - `acceptance:final`
- [x] GitHub manual workflow 已配置
  - `Knowledge Base Embedding Backfill`
- [x] GitHub manual final acceptance workflow 已配置
  - `Final Acceptance`
- [x] GitLab / GitHub 工作流配置可解析
- [x] 两个手动入口都具备前置环境校验
- [x] 平台变量校验已统一到脚本
  - `scripts/check-required-env.sh`
- [x] 两个手动入口都具备 JSON 结果产物
  - GitLab：artifact `backend-python/ai-service/kb-embedding-backfill-result.json`
  - GitHub：workflow summary + artifact `kb-embedding-backfill-result`
- [x] 最终验收手动入口具备日志产物
  - GitLab：`final-acceptance.log`、`docker-compose-final-acceptance.log`
  - GitHub：artifact `final-acceptance-artifacts`
- [x] 最终验收手动入口具备结构化 summary 产物
  - `final-acceptance-summary.json`

## 7. 当前未完成项

- [ ] GitLab 项目侧实际配置并验证 `DATABASE_URL` 与 embedding 相关变量
- [ ] GitHub 仓库侧实际配置并验证 `DATABASE_URL` 与 embedding 相关 Secrets
- [ ] 在真实平台上手动触发一次 GitLab / GitHub backfill 入口并保留结果
- [ ] 在真实平台上手动触发一次 GitLab / GitHub final acceptance 入口并保留结果
- [ ] 对仓库里与本轮无关的其他既有改动做独立最终验收

## 8. 当前最高剩余风险

- 平台级 Secrets / CI 变量尚未在本地代码层验证；如果未配置，GitLab / GitHub manual backfill 入口会立即失败。
- 本文档覆盖的是当前 dev 环境和当前工作树中的主要验收入口，但仓库里与本轮无关的其他既有改动还没有做独立发布级验收。
