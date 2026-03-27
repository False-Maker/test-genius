# 测试设计助手系统

测试设计助手系统是为保险行业个险核心系统测试团队开发的智能化测试设计工具，旨在提升测试设计效率和质量，实现测试设计的标准化、智能化和自动化。

## 系统概述

系统包含两个核心模块：
1. **标准化测试设计流程模块**：建设标准化的测试设计工作台，实现测试设计的全面线上化
2. **提示词工程用例生成模块**：基于大语言模型和提示词工程，自动生成高质量测试用例

## 技术架构

- **后端技术栈**：
  - Java 17 + Spring Boot 3.x（核心业务服务）
  - Python 3.10+ + FastAPI（AI能力服务）
- **前端技术栈**：Vue 3 + Element Plus + TypeScript
- **数据库**：
  - PostgreSQL 14+（主数据库）
  - Milvus/PGVector（向量数据库，用于语义检索）
  - Neo4j（图数据库，用于知识图谱）
- **AI能力**：LangChain + 多模型支持（DeepSeek、豆包、Kimi、千问等）
- **中间件**：Redis（缓存）、RocketMQ（消息队列）、MinIO（对象存储）

## 项目结构

```
测试设计助手系统/
├── backend-java/                    # Java后端服务（核心业务）
│   └── test-design-assistant-core/  # 核心业务服务
├── backend-python/                 # Python后端服务（AI能力）
│   └── ai-service/                 # AI服务
├── frontend/                        # 前端项目
├── database/                        # 数据库脚本
└── docs/                           # 文档目录
```

## 快速开始

### 环境要求

- Java 17+
- Python 3.10+
- Node.js 18+
- PostgreSQL 14+
- Redis 7+

### 启动步骤

1. **启动数据库和中间件**
   ```bash
   docker-compose up -d postgres redis
   ```

2. **初始化数据库**
   ```bash
   # 执行数据库初始化脚本
   psql -U postgres -d test_design_assistant -f database/init/01_init_tables.sql
   psql -U postgres -d test_design_assistant -f database/init/02_init_data.sql
   ```

3. **启动Java后端服务**
   ```bash
   cd backend-java/test-design-assistant-core
   mvn spring-boot:run
   ```

4. **启动Python AI服务**
   ```bash
   cd backend-python/ai-service
   pip install -r requirements.txt
   python -m app.main
   ```

5. **启动前端服务**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### Docker启动命令速查

**完整启动（包含监控组件）**

```bash
docker compose --profile monitoring up -d
```

**快速启动（只启动前端/后端/AI + 基础依赖，不启动监控）**

```bash
docker compose up -d postgres redis backend-java backend-python frontend
```

**开发模式（自动重载，不重建镜像）**

```bash
docker compose --profile dev up postgres redis backend-java-dev backend-python-dev frontend-dev
```

**开发模式访问端口**

- 前端（dev）：http://localhost:3000
- Java 后端（dev）：http://localhost:8081
- AI 服务（dev）：http://localhost:8001

**查看日志**

```bash
docker compose logs -f backend-java backend-python frontend
```

**单独启动某一端**

```bash
docker compose up -d backend-java
docker compose up -d backend-python
docker compose up -d frontend
```

**进入容器调试**

```bash
docker exec -it test-design-backend-java-dev sh
docker exec -it test-design-backend-python-dev sh
docker exec -it test-design-frontend-dev sh
```

**停止与清理**

```bash
docker compose down
```

**清理卷（会清空数据库与缓存）**

```bash
docker compose down -v
```

> 注意：执行 `down -v` 会删除命名卷（含 `uploads_data`）。之后需求分析若报「文档不存在或不是文件」，是因为历史上传文件已清空，请重新上传需求文档或在该需求中填写需求描述后再分析。

**重新构建镜像（仅当 Dockerfile 或依赖变化时需要）**

```bash
docker compose build
```

### 项目级最终验收

可直接运行根脚本串行执行当前项目级关键验收项：

```bash
./scripts/run-final-acceptance.sh
```

默认会依次检查：
- 若 dev 栈未启动，会先执行 `docker compose --profile dev up -d`
- dev 容器状态
- Java / Python 健康接口
- 前端登录页可访问
- 前端 `lint:check`
- 前端 `type-check`
- 前端 `vitest`
- 前端生产构建
- Playwright smoke
- Java 全量 `mvn test`
- Python 全量 `pytest tests/ -q --no-cov --capture=no`
- Python 知识库回填与服务定向回归
- 知识库回填 dry-run
- `knowledge_document.embedding IS NULL` 计数

脚本位置：
- `scripts/run-final-acceptance.sh`

平台手动入口：
- GitLab manual job：`acceptance:final`
- GitHub workflow：`Final Acceptance`

这两个入口都会执行同一份根脚本，并保留：
- `final-acceptance.log`
- `docker-compose-final-acceptance.log`
- `final-acceptance-summary.json`

平台变量校验脚本：
- `bash scripts/check-required-env.sh final-acceptance`
- `bash scripts/check-required-env.sh kb-backfill`
- `bash scripts/check-required-env.sh list final-acceptance`
- `bash scripts/check-required-env.sh list kb-backfill`

平台手动执行说明：
- `docs/platform-manual-runbook.md`

### 知识库历史文档 embedding 回填

当历史知识库文档是在旧逻辑下写入、`knowledge_document.embedding` 为空时，可以先 dry-run 统计，再执行一次性回填：

```bash
./scripts/run-kb-embedding-backfill.sh --dry-run --kb-id 2
```

```bash
./scripts/run-kb-embedding-backfill.sh --kb-id 2 --batch-size 100
```

如果希望把它接到发布后步骤里，在仍有未回填文档时返回非 0 退出码：

```bash
./scripts/run-kb-embedding-backfill.sh --kb-id 2 --fail-on-remaining
```

脚本位置：
- `scripts/run-kb-embedding-backfill.sh`
- `backend-python/ai-service/scripts/backfill_knowledge_document_embeddings.py`

CI / 工作流入口：
- GitLab manual job: `maintenance:kb-embedding-backfill`
- GitHub Actions workflow: `Knowledge Base Embedding Backfill`

默认行为：
- GitLab job 默认使用 `KB_BACKFILL_ARGS="--dry-run --fail-on-remaining"`，需要实际回填时可在运行前覆盖这个变量
- GitHub workflow 默认 `dry_run=true`、`fail_on_remaining=true`
- 两个入口都会产出一份 JSON 结果；GitLab 作为 job artifact 保存，GitHub 会同时写入 workflow summary 并上传 artifact `kb-embedding-backfill-result`

手动工作流需要预先配置运行环境变量或 Secrets，至少包括：
- `DATABASE_URL`
- `EMBEDDING_PROVIDER`

按 provider 区分：
- `local`：通常不需要额外 Secret，未显式配置时默认使用 `EMBEDDING_MODEL_NAME=BAAI/bge-m3`
- `openai`：至少需要 `EMBEDDING_API_KEY`、`OPENAI_EMBEDDING_MODEL`，如有自建兼容网关再补 `EMBEDDING_API_BASE`

## API文档

启动服务后，访问以下地址查看API文档：
- Java后端：http://localhost:8080/api/swagger-ui.html
- Python AI服务：http://localhost:8000/docs

## 开发说明

### 代码规范

- Java代码遵循Google Java Style Guide
- Python代码遵循PEP 8规范
- TypeScript代码遵循ESLint规则

### 数据库迁移

使用Flyway进行数据库版本管理，迁移脚本位于：
`backend-java/test-design-assistant-core/src/main/resources/db/migration/`

## 后续开发

当前版本为基础框架，以下功能待后续开发：
- 需求文档解析功能
- 智能用例生成功能
- 模型调用功能
- 质量评估功能
- 知识库集成

## 参考文档

详细设计文档请参考 `docs/` 目录下的文档：
- 系统架构设计方案
- 核心模块详细设计
- 技术选型建议
- 实施路线图

## 许可证

内部项目，仅供公司内部使用。
