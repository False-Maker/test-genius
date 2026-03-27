# 平台手动执行 Runbook

更新日期：2026-03-27

本文档只覆盖当前仓库内已经落地的两类平台手动入口：
- 项目级最终验收
- 知识库历史文档 embedding 回填

## 1. 先确认所需变量

本地可直接查看所需变量清单：

```bash
bash ./scripts/check-required-env.sh list final-acceptance
bash ./scripts/check-required-env.sh list kb-backfill
```

`final-acceptance` 需要：
- `BIGMODEL_API_KEY`
- `BIGMODEL_API_BASE`
- `BIGMODEL_EMBEDDING_MODEL`
- `BIGMODEL_EMBEDDING_DIMENSION`

`kb-backfill` 需要：
- `DATABASE_URL`
- 当 `EMBEDDING_PROVIDER=openai` 时，还需要：
  `EMBEDDING_API_KEY`
  `OPENAI_EMBEDDING_MODEL`

## 2. GitHub 手动入口

项目级最终验收：
- workflow 名称：`Final Acceptance`
- 运行后应看到 artifact：
  `final-acceptance-artifacts`
- artifact 内应包含：
  `final-acceptance.log`
  `docker-compose-final-acceptance.log`
  `final-acceptance-summary.json`
- 成功判定：
  workflow 绿灯
  `final-acceptance-summary.json` 中 `final_status = "passed"`

知识库回填：
- workflow 名称：`Knowledge Base Embedding Backfill`
- 默认参数：
  `dry_run=true`
  `fail_on_remaining=true`
- 成功判定：
  workflow 绿灯
  summary / artifact 中 `remaining_count = 0`，或 dry-run 时 `missing_count = 0`

## 3. GitLab 手动入口

项目级最终验收：
- job 名称：`acceptance:final`
- artifact 应包含：
  `final-acceptance.log`
  `docker-compose-final-acceptance.log`
  `final-acceptance-summary.json`
- 成功判定：
  job 成功
  `final-acceptance-summary.json` 中 `final_status = "passed"`

知识库回填：
- job 名称：`maintenance:kb-embedding-backfill`
- 默认变量：
  `KB_BACKFILL_ARGS="--dry-run --fail-on-remaining"`
- artifact 应包含：
  `backend-python/ai-service/kb-embedding-backfill-result.json`
- 成功判定：
  job 成功
  结果 JSON 中 `remaining_count = 0`，或 dry-run 时 `missing_count = 0`

## 4. 执行后最少检查项

项目级最终验收执行后至少检查：
- 最终状态是否成功
- 前端 smoke 是否为 `4 passed`
- Java 全量是否为 `412` 通过
- Python 全量是否为 `272` 通过
- `knowledge_document.embedding IS NULL` 是否为 `0`

知识库回填执行后至少检查：
- JSON 结果文件是否存在
- `embedding_provider` 是否符合目标环境
- dry-run 时是否仍有缺口
- 实跑时 `remaining_count` 是否为 `0`
