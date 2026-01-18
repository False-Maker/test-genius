# 测试设计助手系统 API 接口文档

## 一、概述

本文档描述了测试设计助手系统的所有API接口，包括请求参数、响应格式、错误码等信息。

## 二、通用说明

### 2.1 基础信息

- **基础URL**：`http://localhost:8080/api`
- **API版本**：v1
- **数据格式**：JSON
- **字符编码**：UTF-8

### 2.2 统一响应格式

所有接口返回统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**响应字段说明**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |

### 2.3 响应码说明

| 响应码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 600 | 业务异常 |

### 2.4 分页参数

列表接口支持分页，使用以下参数：

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，从1开始 |
| size | Integer | 否 | 10 | 每页数量 |
| sort | String | 否 | - | 排序字段，格式：field,asc/desc |

### 2.5 搜索参数

列表接口支持搜索，使用以下参数：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 关键词搜索 |
| status | String | 否 | 状态筛选 |

## 三、需求管理接口

### 3.1 创建需求

**接口地址**：`POST /v1/requirements`

**请求参数**：

```json
{
  "requirementName": "需求名称",
  "requirementDescription": "需求描述",
  "requirementType": "功能需求",
  "priority": "高",
  "creatorId": 1
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "requirementCode": "REQ-20240101-001",
    "requirementName": "需求名称",
    "requirementDescription": "需求描述",
    "requirementType": "功能需求",
    "priority": "高",
    "status": "DRAFT",
    "createTime": "2024-01-01T10:00:00"
  }
}
```

### 3.2 更新需求

**接口地址**：`PUT /v1/requirements/{id}`

**请求参数**：同创建需求

### 3.3 删除需求

**接口地址**：`DELETE /v1/requirements/{id}`

### 3.4 查询需求详情

**接口地址**：`GET /v1/requirements/{id}`

### 3.5 查询需求列表

**接口地址**：`GET /v1/requirements`

**查询参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |
| requirementName | String | 否 | 需求名称（模糊搜索） |
| requirementStatus | String | 否 | 需求状态（精确匹配） |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "requirementCode": "REQ-20240101-001",
        "requirementName": "需求名称",
        "requirementDescription": "需求描述",
        "requirementStatus": "DRAFT",
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
}
```

### 3.6 分析需求

**接口地址**：`POST /v1/requirements/{id}/analyze`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 需求ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "requirementId": 1,
    "testPoints": [
      {
        "point": "测试要点1",
        "priority": "高"
      }
    ],
    "businessRules": [
      {
        "rule": "业务规则1",
        "description": "规则描述"
      }
    ],
    "keywords": ["关键词1", "关键词2"],
    "summary": "需求分析摘要"
  }
}
```

### 3.7 提取测试要点

**接口地址**：`GET /v1/requirements/{id}/test-points`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 需求ID |

### 3.8 提取业务规则

**接口地址**：`GET /v1/requirements/{id}/business-rules`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 需求ID |

## 四、用例管理接口

### 4.1 创建用例

**接口地址**：`POST /v1/test-cases`

**请求参数**：

```json
{
  "requirementId": 1,
  "layerId": 1,
  "methodId": 1,
  "caseName": "用例名称",
  "caseType": "正常",
  "casePriority": "高",
  "preCondition": "前置条件",
  "testStep": "测试步骤",
  "expectedResult": "预期结果",
  "creatorId": 1
}
```

### 4.2 更新用例

**接口地址**：`PUT /v1/test-cases/{id}`

### 4.3 删除用例

**接口地址**：`DELETE /v1/test-cases/{id}`

### 4.4 查询用例详情

**接口地址**：`GET /v1/test-cases/{id}`

### 4.5 查询用例列表

**接口地址**：`GET /v1/test-cases`

### 4.6 更新用例状态

**接口地址**：`PUT /v1/test-cases/{id}/status`

**请求参数**：

```json
{
  "status": "REVIEWING"
}
```

### 4.7 审核用例

**接口地址**：`POST /v1/test-cases/{id}/review`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用例ID |

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reviewResult | String | 是 | 审核结果：PASS（通过）/REJECT（不通过） |
| reviewComment | String | 否 | 审核意见 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "caseCode": "CASE-20240101-001",
    "caseName": "用例名称",
    "caseStatus": "REVIEWED",
    "reviewComment": "审核通过"
  }
}
```

### 4.8 导出用例

**接口地址**：`GET /v1/test-cases/export`

**查询参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| caseName | String | 否 | 用例名称（模糊搜索） |
| caseStatus | String | 否 | 用例状态（精确匹配） |
| requirementId | Long | 否 | 需求ID |

**说明**：返回Excel文件流，文件名格式：`测试用例_{时间戳}.xlsx`

### 4.9 导出用例模板

**接口地址**：`GET /v1/test-cases/export-template`

**说明**：返回Excel模板文件流，文件名：`测试用例导入模板.xlsx`

### 4.10 导入用例

**接口地址**：`POST /v1/test-cases/import`

**请求参数**（Form-data）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | Excel文件 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCount": 100,
    "successCount": 95,
    "failCount": 5,
    "errors": [
      {
        "row": 3,
        "message": "用例编码不能为空"
      }
    ]
  }
}
```

## 五、用例生成接口

### 5.1 生成用例

**接口地址**：`POST /v1/case-generation/generate`

**请求参数**：

```json
{
  "requirementId": 1,
  "layerCode": "FUNCTIONAL",
  "methodCode": "SCENARIO",
  "modelCode": "DEEPSEEK-001",
  "templateId": 1,
  "creatorId": 1
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "status": "PROCESSING",
    "message": "用例生成任务已提交，正在处理中..."
  }
}
```

### 5.2 查询生成任务

**接口地址**：`GET /v1/case-generation/{id}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 任务ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "taskCode": "TASK-20240101-001",
    "requirementId": 1,
    "status": "SUCCESS",
    "progress": 100,
    "totalCases": 5,
    "successCases": 5,
    "failCases": 0,
    "errorMessage": null,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:01:30",
    "completeTime": "2024-01-01T10:01:30",
    "result": {}
  }
}
```

### 5.3 批量生成用例

**接口地址**：`POST /v1/case-generation/batch-generate`

**请求参数**：

```json
{
  "requirementIds": [1, 2, 3],
  "layerCode": "FUNCTIONAL",
  "methodCode": "SCENARIO",
  "modelCode": "DEEPSEEK-001",
  "templateId": 1,
  "creatorId": 1
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "batchTaskId": 1,
    "batchTaskCode": "BATCH-TASK-20240101-001",
    "totalTasks": 3,
    "successTasks": 3,
    "failTasks": 0,
    "taskIds": [1, 2, 3],
    "status": "PROCESSING",
    "message": "批量任务已提交，正在处理中..."
  }
}
```

### 5.4 批量查询生成任务

**接口地址**：`POST /v1/case-generation/batch-query`

**请求参数**：

```json
[1, 2, 3]
```

**说明**：请求体为任务ID数组

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "taskCode": "TASK-20240101-001",
      "requirementId": 1,
      "status": "SUCCESS",
      "progress": 100
    },
    {
      "id": 2,
      "taskCode": "TASK-20240101-002",
      "requirementId": 2,
      "status": "PROCESSING",
      "progress": 50
    }
  ]
}
```

## 六、提示词模板接口

### 6.1 创建模板

**接口地址**：`POST /v1/prompt-templates`

**请求参数**：

```json
{
  "templateName": "模板名称",
  "templateContent": "模板内容 {变量名}",
  "variableDefinition": "{\"变量名\": \"变量说明\"}",
  "layerCode": "FUNCTIONAL",
  "methodCode": "SCENARIO",
  "isActive": true,
  "creatorId": 1
}
```

### 6.2 更新模板

**接口地址**：`PUT /v1/prompt-templates/{id}`

### 6.3 删除模板

**接口地址**：`DELETE /v1/prompt-templates/{id}`

### 6.4 查询模板详情

**接口地址**：`GET /v1/prompt-templates/{id}`

### 6.5 查询模板列表

**接口地址**：`GET /v1/prompt-templates`

### 6.6 切换模板状态

**接口地址**：`PUT /v1/prompt-templates/{id}/status`

**请求参数**：

```json
{
  "isActive": true
}
```

### 6.7 生成提示词（根据模板ID）

**接口地址**：`POST /v1/prompt-templates/{id}/generate`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 模板ID |

**请求参数**：

```json
{
  "requirement_text": "需求描述",
  "layer_code": "FUNCTIONAL",
  "method_code": "SCENARIO"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "生成的提示词内容"
}
```

### 6.8 生成提示词（自定义模板）

**接口地址**：`POST /v1/prompt-templates/generate`

**请求参数**（Query参数 + Request Body）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| templateContent | String | 是 | 模板内容 |

**请求体**：

```json
{
  "变量名1": "变量值1",
  "变量名2": "变量值2"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "生成的提示词内容"
}
```

## 七、模型配置接口

### 7.1 创建模型配置

**接口地址**：`POST /v1/model-configs`

**请求参数**：

```json
{
  "modelName": "DeepSeek",
  "modelType": "DEEPSEEK",
  "modelVersion": "v1",
  "apiKey": "your-api-key",
  "apiEndpoint": "https://api.deepseek.com",
  "maxTokens": 2000,
  "temperature": 0.7,
  "priority": 1,
  "isActive": true,
  "creatorId": 1
}
```

### 7.2 更新模型配置

**接口地址**：`PUT /v1/model-configs/{id}`

### 7.3 删除模型配置

**接口地址**：`DELETE /v1/model-configs/{id}`

### 7.4 查询模型配置详情

**接口地址**：`GET /v1/model-configs/{id}`

### 7.5 查询模型配置列表

**接口地址**：`GET /v1/model-configs`

**查询参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |
| modelName | String | 否 | 模型名称（模糊搜索） |
| modelType | String | 否 | 模型类型（精确匹配） |
| isActive | String | 否 | 是否启用：1/0 |

### 7.6 根据模型编码获取配置

**接口地址**：`GET /v1/model-configs/code/{modelCode}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| modelCode | String | 模型编码 |

### 7.7 切换模型配置状态

**接口地址**：`PUT /v1/model-configs/{id}/status`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 模型配置ID |

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isActive | String | 是 | 是否启用：1/0 |

### 7.8 获取所有启用的模型配置

**接口地址**：`GET /v1/model-configs/active`

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "modelCode": "DEEPSEEK-001",
      "modelName": "DeepSeek",
      "modelType": "DEEPSEEK",
      "priority": 1,
      "isActive": "1"
    }
  ]
}
```

### 7.9 根据类型获取启用的模型配置

**接口地址**：`GET /v1/model-configs/active/type/{modelType}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| modelType | String | 模型类型 |

## 八、文件上传接口

### 8.1 上传文件

**接口地址**：`POST /v1/files/upload`

**请求参数**（Form-data）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 文件（支持Word、PDF） |

**文件限制**：
- 支持格式：.doc, .docx, .pdf
- 最大文件大小：100MB

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "filePath": "2024/01/01/uuid-file.docx",
    "fileUrl": "http://localhost:8080/files/2024/01/01/uuid-file.docx",
    "fileName": "原文件名.docx",
    "fileSize": "1024000"
  }
}
```

### 8.2 删除文件

**接口地址**：`DELETE /v1/files/{filePath}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| filePath | String | 文件路径（支持通配符路径） |

**说明**：filePath可以是完整路径或路径的一部分，支持路径通配符

## 九、用例质量评估接口

### 9.1 评估用例质量

**接口地址**：`GET /v1/test-case-quality/assess/{caseId}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | Long | 用例ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "overallScore": 85,
    "completenessScore": 90,
    "standardizationScore": 80,
    "executabilityScore": 85,
    "grade": "良好",
    "suggestions": ["建议优化测试步骤描述"]
  }
}
```

### 9.2 检查用例完整性

**接口地址**：`GET /v1/test-case-quality/completeness/{caseId}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | Long | 用例ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "score": 90,
    "preConditionScore": 20,
    "testStepScore": 40,
    "expectedResultScore": 30,
    "basicInfoScore": 10
  }
}
```

### 9.3 检查用例规范性

**接口地址**：`GET /v1/test-case-quality/standardization/{caseId}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | Long | 用例ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "score": 80,
    "namingScore": 30,
    "formatScore": 40,
    "contentScore": 30
  }
}
```

## 十、用例复用接口

### 10.1 初始化用例向量表

**接口地址**：`POST /v1/case-reuse/init`

**说明**：初始化用例向量表结构（用于语义检索）

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 10.2 更新用例向量

**接口地址**：`POST /v1/case-reuse/cases/{caseId}/embedding`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | Long | 用例ID |

**说明**：更新指定用例的向量表示（用于语义检索）

### 10.3 搜索相似用例

**接口地址**：`POST /v1/case-reuse/cases/search/similar`

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| caseText | String | 是 | 用例文本（用于相似度匹配） |
| layerId | Long | 否 | 测试分层ID（筛选条件） |
| methodId | Long | 否 | 测试方法ID（筛选条件） |
| topK | Integer | 否 | 返回结果数量，默认10 |
| similarityThreshold | Double | 否 | 相似度阈值，默认0.7 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "caseCode": "CASE-20240101-001",
      "caseName": "用例名称",
      "similarity": 0.95
    }
  ]
}
```

### 10.4 关键词检索用例

**接口地址**：`GET /v1/case-reuse/cases/search/keyword/{keyword}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | String | 关键词 |

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| layerId | Long | 否 | 测试分层ID（筛选条件） |
| methodId | Long | 否 | 测试方法ID（筛选条件） |
| topK | Integer | 否 | 返回结果数量，默认10 |

### 10.5 推荐相似用例

**接口地址**：`GET /v1/case-reuse/cases/{caseId}/recommend`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| caseId | Long | 用例ID |

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| topK | Integer | 否 | 返回结果数量，默认5 |

### 10.6 创建用例组合（测试套件）

**接口地址**：`POST /v1/case-reuse/suites`

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| suiteName | String | 是 | 套件名称 |
| caseIds | List<Long> | 是 | 用例ID列表 |
| creatorId | Long | 否 | 创建人ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

## 十一、知识库接口

### 11.1 初始化知识库

**接口地址**：`POST /v1/knowledge/init`

**说明**：初始化知识库表结构（用于向量存储）

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 11.2 添加知识库文档

**接口地址**：`POST /v1/knowledge/documents`

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| docCode | String | 是 | 文档编码 |
| docName | String | 是 | 文档名称 |
| docType | String | 是 | 文档类型 |
| docContent | String | 是 | 文档内容 |
| docCategory | String | 否 | 文档分类 |
| docUrl | String | 否 | 文档URL |
| creatorId | Long | 否 | 创建人ID |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

### 11.3 语义检索知识库文档

**接口地址**：`POST /v1/knowledge/documents/search`

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| queryText | String | 是 | 查询文本 |
| docType | String | 否 | 文档类型（筛选条件） |
| topK | Integer | 否 | 返回结果数量，默认10 |
| similarityThreshold | Double | 否 | 相似度阈值，默认0.7 |

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "docCode": "DOC-001",
      "docName": "文档名称",
      "docContent": "文档内容",
      "similarity": 0.95
    }
  ]
}
```

### 11.4 关键词检索知识库文档

**接口地址**：`GET /v1/knowledge/documents/keyword/{keyword}`

**路径参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | String | 关键词 |

**请求参数**（Query参数）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| docType | String | 否 | 文档类型（筛选条件） |
| topK | Integer | 否 | 返回结果数量，默认10 |

## 十二、通用接口

### 8.1 查询测试分层列表

**接口地址**：`GET /v1/common/test-layers`

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "layerCode": "PERSONAL",
      "layerName": "个人级测试"
    },
    {
      "id": 2,
      "layerCode": "BUSINESS",
      "layerName": "业务案例测试"
    }
  ]
}
```

### 8.2 查询测试方法列表

**接口地址**：`GET /v1/common/test-methods`

### 12.1 查询测试分层列表

**接口地址**：`GET /v1/common/test-layers`

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "layerCode": "PERSONAL",
      "layerName": "个人级测试"
    },
    {
      "id": 2,
      "layerCode": "BUSINESS",
      "layerName": "业务案例测试"
    }
  ]
}
```

### 12.2 查询测试方法列表

**接口地址**：`GET /v1/common/test-design-methods`

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "methodCode": "SCENARIO",
      "methodName": "场景法"
    },
    {
      "id": 2,
      "methodCode": "BOUNDARY",
      "methodName": "边界值分析法"
    }
  ]
}
```

### 12.3 查询启用的模型配置列表

**接口地址**：`GET /v1/common/model-configs`

**响应示例**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "modelCode": "DEEPSEEK-001",
      "modelName": "DeepSeek",
      "modelType": "DEEPSEEK",
      "priority": 1
    }
  ]
}
```

## 十三、Python AI服务接口

**基础URL**：`http://localhost:8000`

### 9.1 调用大模型

**接口地址**：`POST /api/llm/call`

**请求参数**：

```json
{
  "model_code": "DEEPSEEK-001",
  "prompt": "提示词内容",
  "max_tokens": 2000,
  "temperature": 0.7
}
```

**响应示例**：

```json
{
  "content": "模型响应内容",
  "model_code": "DEEPSEEK-001",
  "tokens_used": 150,
  "response_time": 2000
}
```

### 9.2 批量调用大模型

**接口地址**：`POST /api/llm/batch-call`

**请求参数**：

```json
{
  "requests": [
    {
      "model_code": "DEEPSEEK-001",
      "prompt": "提示词1",
      "max_tokens": 2000,
      "temperature": 0.7
    },
    {
      "model_code": "DEEPSEEK-001",
      "prompt": "提示词2",
      "max_tokens": 2000,
      "temperature": 0.7
    }
  ]
}
```

### 9.3 生成用例

**接口地址**：`POST /api/case/generate`

**请求参数**：

```json
{
  "requirement_id": 1,
  "requirement_text": "需求描述",
  "layer_code": "FUNCTIONAL",
  "method_code": "SCENARIO",
  "model_code": "DEEPSEEK-001",
  "template_id": 1
}
```

**响应示例**：

```json
{
  "cases": [
    {
      "case_name": "用例名称",
      "case_type": "正常",
      "case_priority": "高",
      "pre_condition": "前置条件",
      "test_step": "测试步骤",
      "expected_result": "预期结果"
    }
  ],
  "request_id": "uuid",
  "status": "success",
  "message": "成功生成 5 个用例"
}
```

### 9.4 解析用例

**接口地址**：`POST /api/case/parse`

**请求参数**：

```json
{
  "content": "用例文本内容"
}
```

**响应示例**：

```json
{
  "cases": [
    {
      "case_name": "用例名称",
      "test_step": "测试步骤",
      "expected_result": "预期结果"
    }
  ],
  "count": 1
}
```

## 十四、错误处理

### 14.1 错误响应格式

```json
{
  "code": 400,
  "message": "参数错误：缺少必需参数 requirementId"
}
```

### 14.2 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 参数错误 | 检查请求参数是否正确 |
| 401 | 未授权 | 检查认证信息 |
| 403 | 禁止访问 | 检查权限配置 |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 500 | 服务器错误 | 查看服务器日志 |
| 600 | 业务异常 | 查看具体错误信息 |

### 14.3 状态枚举值说明

#### 需求状态（RequirementStatus）
- `DRAFT`：草稿
- `REVIEWING`：审核中
- `APPROVED`：已通过
- `CLOSED`：已关闭

#### 用例状态（CaseStatus）
- `DRAFT`：草稿
- `REVIEWING`：待审核
- `REVIEWED`：已审核
- `ABANDONED`：已废弃

#### 任务状态（TaskStatus）
- `PENDING`：待处理
- `PROCESSING`：处理中
- `SUCCESS`：成功
- `FAILED`：失败

#### 审核结果（ReviewResult）
- `PASS`：通过
- `REJECT`：不通过

#### 质量等级（QualityGrade）
- `优秀`：≥90分
- `良好`：75-89分
- `一般`：60-74分
- `需改进`：<60分

## 十五、测试执行接口

### 15.1 执行任务管理

#### 15.1.1 创建执行任务

**接口地址**：`POST /v1/test-execution/tasks`

**请求参数**：
```json
{
  "taskName": "任务名称",
  "taskType": "AUTO_SCRIPT_GENERATION",
  "requirementId": 1,
  "caseId": 1,
  "scriptType": "SELENIUM",
  "scriptLanguage": "PYTHON",
  "naturalLanguageDesc": "自然语言描述",
  "executionConfig": "{}"
}
```

#### 15.1.2 查询执行任务列表

**接口地址**：`GET /v1/test-execution/tasks`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |
| taskName | String | 否 | 任务名称（模糊搜索） |
| taskStatus | String | 否 | 任务状态（精确匹配） |
| taskType | String | 否 | 任务类型（精确匹配） |

#### 15.1.3 获取执行任务详情

**接口地址**：`GET /v1/test-execution/tasks/{id}`

#### 15.1.4 根据任务编码获取执行任务详情

**接口地址**：`GET /v1/test-execution/tasks/code/{taskCode}`

#### 15.1.5 更新执行任务

**接口地址**：`PUT /v1/test-execution/tasks/{id}`

#### 15.1.6 删除执行任务

**接口地址**：`DELETE /v1/test-execution/tasks/{id}`

#### 15.1.7 更新任务状态

**接口地址**：`PUT /v1/test-execution/tasks/{taskCode}/status`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 任务状态：PENDING/PROCESSING/SUCCESS/FAILED |

#### 15.1.8 更新任务进度

**接口地址**：`PUT /v1/test-execution/tasks/{taskCode}/progress`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| progress | Integer | 是 | 任务进度（0-100） |

### 15.2 执行记录管理

#### 15.2.1 创建执行记录

**接口地址**：`POST /v1/test-execution/records`

**请求参数**：
```json
{
  "taskId": 1,
  "caseId": 1,
  "executionType": "AUTOMATED",
  "executionStatus": "PENDING",
  "executionResult": "执行结果详情",
  "executionLog": "执行日志"
}
```

#### 15.2.2 查询执行记录列表

**接口地址**：`GET /v1/test-execution/records`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |
| taskId | Long | 否 | 任务ID |
| caseId | Long | 否 | 用例ID |
| executionStatus | String | 否 | 执行状态 |

#### 15.2.3 获取执行记录详情

**接口地址**：`GET /v1/test-execution/records/{id}`

#### 15.2.4 根据记录编码获取执行记录详情

**接口地址**：`GET /v1/test-execution/records/code/{recordCode}`

#### 15.2.5 根据任务ID查询执行记录列表

**接口地址**：`GET /v1/test-execution/tasks/{taskId}/records`

#### 15.2.6 更新执行记录状态

**接口地址**：`PUT /v1/test-execution/records/{recordCode}/status`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 执行状态：PENDING/RUNNING/SUCCESS/FAILED/SKIPPED |

### 15.3 UI脚本修复

#### 15.3.1 分析错误日志

**接口地址**：`POST /v1/test-execution/ui-script/analyze-error`

**请求参数**：
```json
{
  "errorLog": "错误日志内容",
  "scriptContent": "脚本内容（可选）",
  "useLlm": true
}
```

#### 15.3.2 检测页面变化

**接口地址**：`POST /v1/test-execution/ui-script/detect-page-changes`

**请求参数**：
```json
{
  "oldPageCodeUrl": "旧页面代码URL",
  "newPageCodeUrl": "新页面代码URL",
  "oldPageElements": [],
  "newPageElements": [],
  "scriptLocators": []
}
```

#### 15.3.3 修复UI脚本

**接口地址**：`POST /v1/test-execution/ui-script/repair`

**请求参数**：
```json
{
  "scriptContent": "脚本内容",
  "errorLog": "错误日志",
  "errorAnalysis": {},
  "pageChanges": {},
  "newPageCodeUrl": "新页面代码URL",
  "newPageElements": [],
  "scriptType": "SELENIUM",
  "scriptLanguage": "PYTHON",
  "useLlm": true
}
```

### 15.4 统计分析

#### 15.4.1 获取执行统计信息

**接口地址**：`GET /v1/test-execution/statistics`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| caseId | Long | 否 | 用例ID |
| startDate | String | 否 | 开始日期（YYYY-MM-DD） |
| endDate | String | 否 | 结束日期（YYYY-MM-DD） |

## 十六、测试报告管理接口

### 16.1 报告生成

#### 16.1.1 生成测试报告

**接口地址**：`POST /v1/test-reports`

**请求参数**：
```json
{
  "reportName": "报告名称",
  "reportType": "EXECUTION",
  "templateId": 1,
  "requirementId": 1,
  "executionTaskId": 1,
  "generateConfig": "{}"
}
```

#### 16.1.2 查询报告列表

**接口地址**：`GET /v1/test-reports`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

#### 16.1.3 获取报告详情

**接口地址**：`GET /v1/test-reports/{id}`

#### 16.1.4 根据编码获取报告详情

**接口地址**：`GET /v1/test-reports/code/{reportCode}`

#### 16.1.5 根据需求ID查询报告列表

**接口地址**：`GET /v1/test-reports/requirement/{requirementId}`

#### 16.1.6 根据执行任务ID查询报告列表

**接口地址**：`GET /v1/test-reports/execution-task/{executionTaskId}`

#### 16.1.7 更新报告

**接口地址**：`PUT /v1/test-reports/{id}`

#### 16.1.8 发布报告

**接口地址**：`PUT /v1/test-reports/{id}/publish`

#### 16.1.9 删除报告

**接口地址**：`DELETE /v1/test-reports/{id}`

#### 16.1.10 导出报告文件

**接口地址**：`GET /v1/test-reports/{reportCode}/export`

**查询参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| format | String | 是 | 导出格式：WORD/PDF/EXCEL |

**响应说明**：
- 返回文件URL（相对路径），格式：`/api/v1/files/reports/{日期}/{报告编码}.{扩展名}`
- 支持格式：
  - **WORD**：导出为Word文档（.docx格式），使用Apache POI生成
  - **PDF**：导出为PDF文档（.pdf格式），使用iText7生成，包含完整的报告内容
  - **EXCEL**：导出为Excel文档（.xlsx格式），使用EasyExcel生成

**PDF导出功能说明**：
- PDF文档包含完整的测试报告内容，包括：
  - 报告标题和基本信息（报告编码、类型、状态、创建时间、创建人等）
  - 报告摘要
  - 统计信息表格（总执行记录数、成功率、平均耗时、总任务数、状态统计等）
  - 执行记录详情表格（记录编码、执行状态、执行类型、执行耗时、执行时间等）
- PDF格式：A4页面，自动分页，支持表格和格式化文本
- 文档属性：包含标题、作者、创建者等元数据
- 样式：标题居中加粗，表格带表头背景色，内容清晰易读

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "/api/v1/files/reports/2024/01/17/RPT-20240117-001.pdf"
}
```

**PDF导出功能说明**：
- PDF文档包含完整的测试报告内容，包括：
  - 报告标题和基本信息（报告编码、类型、状态、创建时间、创建人等）
  - 报告摘要
  - 统计信息（总执行记录数、成功率、平均耗时、状态统计等）
  - 执行记录详情（记录编码、执行状态、执行类型、执行耗时、执行时间等）
- PDF格式：A4页面，自动分页
- 文档属性：包含标题、作者、创建者等元数据

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| format | String | 是 | 文件格式：WORD/PDF/EXCEL |

#### 16.1.11 汇总测试执行结果

**接口地址**：`GET /v1/test-reports/summarize`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| executionTaskId | Long | 否 | 执行任务ID |

## 十七、测试覆盖分析接口

### 17.1 覆盖分析

#### 17.1.1 分析测试覆盖（通用方法）

**接口地址**：`POST /v1/test-coverage/analyze`

**请求参数**：
```json
{
  "analysisName": "分析名称",
  "requirementId": 1,
  "coverageType": "REQUIREMENT"
}
```

#### 17.1.2 分析需求覆盖

**接口地址**：`POST /v1/test-coverage/analyze/requirement/{requirementId}`

#### 17.1.3 分析功能覆盖

**接口地址**：`POST /v1/test-coverage/analyze/function/{requirementId}`

#### 17.1.4 分析场景覆盖

**接口地址**：`POST /v1/test-coverage/analyze/scenario/{requirementId}`

#### 17.1.5 分析代码覆盖

**接口地址**：`POST /v1/test-coverage/analyze/code`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| coverageData | String | 是 | 代码覆盖数据（JSON格式） |

### 17.2 查询和分析

#### 17.2.1 查询覆盖分析列表

**接口地址**：`GET /v1/test-coverage`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

#### 17.2.2 获取分析详情

**接口地址**：`GET /v1/test-coverage/{id}`

#### 17.2.3 根据编码获取分析详情

**接口地址**：`GET /v1/test-coverage/code/{analysisCode}`

#### 17.2.4 根据需求ID查询分析列表

**接口地址**：`GET /v1/test-coverage/requirement/{requirementId}`

#### 17.2.5 根据覆盖类型查询分析列表

**接口地址**：`GET /v1/test-coverage/type/{coverageType}`

#### 17.2.6 获取覆盖趋势

**接口地址**：`GET /v1/test-coverage/trend`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| coverageType | String | 否 | 覆盖类型 |
| days | Integer | 否 | 天数，默认7 |

#### 17.2.7 检查覆盖不足

**接口地址**：`GET /v1/test-coverage/insufficiency`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| threshold | Double | 否 | 阈值，默认80.0 |

#### 17.2.8 生成覆盖报告

**接口地址**：`GET /v1/test-coverage/report`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| coverageType | String | 否 | 覆盖类型 |

## 十八、风险评估接口

### 18.1 风险评估

#### 18.1.1 执行风险评估（通用方法）

**接口地址**：`POST /v1/test-risk-assessment/assess`

**请求参数**：
```json
{
  "assessmentName": "评估名称",
  "requirementId": 1,
  "executionTaskId": 1
}
```

#### 18.1.2 评估需求风险

**接口地址**：`POST /v1/test-risk-assessment/assess/requirement/{requirementId}`

#### 18.1.3 评估执行任务风险

**接口地址**：`POST /v1/test-risk-assessment/assess/execution-task/{executionTaskId}`

#### 18.1.4 评估风险等级

**接口地址**：`GET /v1/test-risk-assessment/assess/level`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| riskScore | BigDecimal | 是 | 风险评分（0-100） |

#### 18.1.5 评估上线可行性

**接口地址**：`GET /v1/test-risk-assessment/assess/feasibility`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| executionTaskId | Long | 否 | 执行任务ID |

#### 18.1.6 识别风险项

**接口地址**：`GET /v1/test-risk-assessment/identify/risk-items`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requirementId | Long | 否 | 需求ID |
| executionTaskId | Long | 否 | 执行任务ID |

### 18.2 查询评估结果

#### 18.2.1 查询风险评估列表

**接口地址**：`GET /v1/test-risk-assessment`

**查询参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从0开始，默认0 |
| size | Integer | 否 | 每页数量，默认10 |

#### 18.2.2 获取评估详情

**接口地址**：`GET /v1/test-risk-assessment/{id}`

#### 18.2.3 根据编码获取评估详情

**接口地址**：`GET /v1/test-risk-assessment/code/{assessmentCode}`

#### 18.2.4 根据需求ID查询评估列表

**接口地址**：`GET /v1/test-risk-assessment/requirement/{requirementId}`

#### 18.2.5 根据执行任务ID查询评估列表

**接口地址**：`GET /v1/test-risk-assessment/execution-task/{executionTaskId}`

#### 18.2.6 根据风险等级查询评估列表

**接口地址**：`GET /v1/test-risk-assessment/level/{riskLevel}`

**路径参数**：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| riskLevel | String | 风险等级：HIGH/MEDIUM/LOW |

## 十九、流程文档生成接口

### 19.1 场景图生成

#### 19.1.1 生成场景图

**接口地址**：`POST /api/v1/flow-documents/scene-diagrams`

**请求参数**：
```json
{
  "requirementId": 1,
  "caseIds": [1, 2, 3],
  "title": "场景图标题",
  "direction": "LR",
  "includeCaseDetails": true
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "diagramCode": "DIAGRAM-20240117-001",
    "title": "场景图标题",
    "mermaidCode": "graph LR\nA[节点A] --> B[节点B]",
    "nodeCount": 10,
    "edgeCount": 15
  }
}
```

#### 19.1.2 导出场景图文件

**接口地址**：`POST /api/v1/flow-documents/scene-diagrams/export`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| mermaidCode | String | 是 | Mermaid代码 |
| format | String | 是 | 文件格式：PNG/SVG/PDF |
| fileName | String | 否 | 文件名（不含扩展名） |

### 19.2 路径图生成

#### 19.2.1 生成路径图

**接口地址**：`POST /api/v1/flow-documents/path-diagrams`

**请求参数**：
```json
{
  "caseId": 1,
  "caseIds": [1, 2, 3],
  "requirementId": 1,
  "title": "路径图标题",
  "direction": "TB"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "diagramCode": "DIAGRAM-20240117-002",
    "title": "路径图标题",
    "mermaidCode": "graph TB\nA[开始] --> B[步骤1]",
    "pathCount": 5,
    "nodeCount": 8,
    "edgeCount": 12
  }
}
```

#### 19.2.2 导出路径图文件

**接口地址**：`POST /api/v1/flow-documents/path-diagrams/export`

**请求参数**：同场景图导出

## 二十、数据文档生成接口

### 20.1 等价类表生成

#### 20.1.1 生成等价类表

**接口地址**：`POST /api/v1/data-documents/equivalence-tables`

**请求参数**：
```json
{
  "requirementId": 1,
  "caseIds": [1, 2, 3],
  "parameters": [
    {
      "parameterName": "用户名",
      "parameterType": "STRING",
      "validClasses": ["有效长度", "有效字符"],
      "invalidClasses": ["超长", "特殊字符"]
    }
  ],
  "title": "等价类表标题"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "tableCode": "TABLE-20240117-001",
    "title": "等价类表标题",
    "parameters": [...],
    "testCases": [
      {
        "caseNumber": "TC-001",
        "parameterValues": {"用户名": "有效长度"},
        "isValid": true
      }
    ],
    "totalCases": 10,
    "validCases": 6,
    "invalidCases": 4
  }
}
```

#### 20.1.2 导出等价类表到Excel

**接口地址**：`POST /api/v1/data-documents/equivalence-tables/export/excel`

**请求参数**：请求体为等价类表响应数据（EquivalenceTableResponseDTO）

**说明**：返回Excel文件流

#### 20.1.3 导出等价类表到Word

**接口地址**：`POST /api/v1/data-documents/equivalence-tables/export/word`

**请求参数**：同Excel导出

**说明**：返回Word文件流

### 20.2 正交表生成

#### 20.2.1 生成正交表

**接口地址**：`POST /api/v1/data-documents/orthogonal-tables`

**请求参数**：
```json
{
  "factors": [
    {
      "factorName": "浏览器",
      "levels": ["Chrome", "Firefox", "Safari"]
    },
    {
      "factorName": "操作系统",
      "levels": ["Windows", "macOS", "Linux"]
    }
  ],
  "title": "正交表标题"
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "tableCode": "TABLE-20240117-002",
    "title": "正交表标题",
    "factors": [...],
    "orthogonalType": "L9",
    "testCases": [
      {
        "caseNumber": "TC-001",
        "factorValues": {"浏览器": "Chrome", "操作系统": "Windows"}
      }
    ],
    "totalCases": 9,
    "theoreticalMaxCases": 27,
    "reductionRate": 66.67
  }
}
```

#### 20.2.2 导出正交表到Excel

**接口地址**：`POST /api/v1/data-documents/orthogonal-tables/export/excel`

**请求参数**：请求体为正交表响应数据（OrthogonalTableResponseDTO）

**说明**：返回Excel文件流

#### 20.2.3 导出正交表到Word

**接口地址**：`POST /api/v1/data-documents/orthogonal-tables/export/word`

**请求参数**：同Excel导出

**说明**：返回Word文件流

## 二十一、更新记录


| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 1.0 | 2024-01-01 | 初始版本 | 系统 |
| 2.0 | 2024-01-XX | 完善接口文档，补充文件上传、质量评估、批量生成、用例复用、知识库等接口 | 系统 |
| 3.0 | 2024-01-17 | 新增测试执行模块、测试报告生成、测试覆盖分析、风险评估、流程文档生成、数据文档生成等接口 | 系统 |
| 3.1 | 2024-01-XX | 完善测试报告PDF导出功能，支持完整的PDF文档生成（使用iText7），包含报告基本信息、摘要、统计信息和执行记录详情 | 系统 |

---

**文档结束**

*更多详细信息请参考Swagger文档：http://localhost:8080/api/swagger-ui.html*

