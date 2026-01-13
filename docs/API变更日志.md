# API变更日志

本文档记录测试设计助手系统API的所有变更历史，包括新增、修改、废弃和删除的接口。

## 变更记录格式

- **日期**：变更日期（YYYY-MM-DD）
- **版本**：API版本号
- **类型**：变更类型（新增/修改/废弃/删除）
- **接口**：接口路径和HTTP方法
- **说明**：变更说明和影响
- **迁移指南**：迁移指南链接（如适用）

---

## v1版本变更记录

### 2024-01-01 - v1.0.0 初始版本

**类型**：新增  
**说明**：API v1版本初始发布，包含以下模块：

#### 需求管理模块
- `GET /api/v1/requirements` - 获取需求列表
- `GET /api/v1/requirements/{id}` - 获取需求详情
- `POST /api/v1/requirements` - 创建需求
- `PUT /api/v1/requirements/{id}` - 更新需求
- `DELETE /api/v1/requirements/{id}` - 删除需求
- `POST /api/v1/requirements/{id}/analyze` - 分析需求

#### 测试用例管理模块
- `GET /api/v1/test-cases` - 获取用例列表
- `GET /api/v1/test-cases/{id}` - 获取用例详情
- `POST /api/v1/test-cases` - 创建用例
- `PUT /api/v1/test-cases/{id}` - 更新用例
- `DELETE /api/v1/test-cases/{id}` - 删除用例
- `POST /api/v1/test-cases/import` - 导入用例
- `GET /api/v1/test-cases/export` - 导出用例

#### 用例生成模块
- `POST /api/v1/case-generation/generate` - 生成用例
- `GET /api/v1/case-generation/tasks` - 获取生成任务列表
- `GET /api/v1/case-generation/tasks/{id}` - 获取生成任务详情
- `POST /api/v1/case-generation/tasks/{id}/retry` - 重试生成任务

#### 用例复用模块
- `POST /api/v1/case-reuse/search` - 搜索相似用例
- `POST /api/v1/case-reuse/recommend` - 推荐相似用例
- `POST /api/v1/case-reuse/combine` - 组合用例

#### 知识库模块
- `GET /api/v1/knowledge/documents` - 获取知识库文档列表
- `GET /api/v1/knowledge/documents/{id}` - 获取知识库文档详情
- `POST /api/v1/knowledge/documents` - 添加知识库文档
- `DELETE /api/v1/knowledge/documents/{id}` - 删除知识库文档
- `POST /api/v1/knowledge/search` - 搜索知识库

#### 通用数据模块
- `GET /api/v1/common/test-layers` - 获取测试分层列表
- `GET /api/v1/common/test-methods` - 获取测试方法列表
- `GET /api/v1/common/model-configs` - 获取模型配置列表

#### 模型配置模块
- `GET /api/v1/model-configs` - 获取模型配置列表
- `GET /api/v1/model-configs/{id}` - 获取模型配置详情
- `POST /api/v1/model-configs` - 创建模型配置
- `PUT /api/v1/model-configs/{id}` - 更新模型配置
- `DELETE /api/v1/model-configs/{id}` - 删除模型配置

#### 提示词模板模块
- `GET /api/v1/prompt-templates` - 获取提示词模板列表
- `GET /api/v1/prompt-templates/{id}` - 获取提示词模板详情
- `POST /api/v1/prompt-templates` - 创建提示词模板
- `PUT /api/v1/prompt-templates/{id}` - 更新提示词模板
- `DELETE /api/v1/prompt-templates/{id}` - 删除提示词模板

#### 文件上传模块
- `POST /api/v1/files/upload` - 上传文件
- `GET /api/v1/files/{id}` - 下载文件
- `DELETE /api/v1/files/{id}` - 删除文件

#### 用例质量评估模块
- `POST /api/v1/test-case-quality/evaluate` - 评估用例质量
- `GET /api/v1/test-case-quality/reports/{id}` - 获取质量评估报告

---

## 变更类型说明

### 新增
新增的接口或字段，对现有功能无影响。

### 修改
修改的接口或字段，可能影响现有客户端，需要检查兼容性。

### 废弃
标记为废弃的接口或字段，将在未来版本中删除。建议尽快迁移到新版本。

### 删除
已删除的接口或字段，不再可用。必须在删除前完成迁移。

---

## 版本规划

### v1（当前版本）
- **状态**：稳定期
- **支持期限**：长期支持
- **维护**：持续维护和bug修复

### v2（规划中）
- **状态**：规划中
- **预计发布时间**：待定
- **主要变更**：待定

---

## 如何查看变更

1. **查看本文档**：了解所有API变更历史
2. **查看API文档**：http://localhost:8080/api/swagger-ui.html
3. **查看版本管理策略**：`docs/API版本管理策略.md`
4. **订阅变更通知**：联系开发团队订阅变更通知

---

**文档版本**：v1.0  
**最后更新**：2024-01-01  
**维护人员**：测试设计助手系统开发团队

