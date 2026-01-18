# SQL语法问题修复报告

## 问题概述

在检查数据库初始化脚本时，发现多个SQL文件使用了MySQL特有的语法，而应用实际使用的是PostgreSQL数据库，导致表创建失败，进而引发前端API调用500错误。

## 发现的问题

### 1. MySQL COMMENT 语法问题

**问题描述**：MySQL支持在CREATE TABLE语句中直接使用`COMMENT '注释内容'`语法，但PostgreSQL不支持这种语法。

**影响范围**：
- `database/init/01_init_tables.sql` 中的多个表定义

**受影响的表**：
1. `test_report_template` - 测试报告模板表
2. `test_report` - 测试报告表
3. `test_coverage_analysis` - 测试覆盖分析表
4. `test_specification` - 测试规约表
5. `spec_version` - 规约版本管理表
6. `field_test_point` - 字段测试要点表
7. `logic_test_point` - 逻辑测试要点表

### 2. MySQL ON UPDATE 语法问题

**问题描述**：MySQL支持`ON UPDATE CURRENT_TIMESTAMP`语法，但PostgreSQL不支持。

**影响范围**：
- `database/init/01_init_tables.sql` 中的多个表定义

**受影响的表**：
1. `test_specification` - 测试规约表
2. `field_test_point` - 字段测试要点表
3. `logic_test_point` - 逻辑测试要点表

## 修复内容

### 已修复的文件

1. **database/init/01_init_tables.sql**
   - 移除了所有MySQL的`COMMENT '注释'`语法
   - 移除了所有`ON UPDATE CURRENT_TIMESTAMP`语法
   - 保留了PostgreSQL兼容的语法

### 修复详情

#### test_report_template 表
- 移除了所有字段的COMMENT语法
- 保留了表结构和索引定义

#### test_report 表
- 移除了所有字段的COMMENT语法
- 保留了表结构和索引定义

#### test_coverage_analysis 表
- 移除了所有字段的COMMENT语法
- 保留了表结构和索引定义

#### test_specification 表
- 移除了所有字段的COMMENT语法
- 移除了`update_time`字段的`ON UPDATE CURRENT_TIMESTAMP`语法
- 保留了表结构和索引定义

#### spec_version 表
- 移除了所有字段的COMMENT语法
- 保留了表结构和索引定义

#### field_test_point 表
- 移除了所有字段的COMMENT语法
- 移除了`update_time`字段的`ON UPDATE CURRENT_TIMESTAMP`语法
- 保留了表结构和索引定义

#### logic_test_point 表
- 移除了所有字段的COMMENT语法
- 移除了`update_time`字段的`ON UPDATE CURRENT_TIMESTAMP`语法
- 保留了表结构和索引定义

## 前端页面影响

以下前端页面可能因为表不存在而出现500错误：

1. **测试报告模板管理** (`/test-report-template`)
   - API: `GET /api/v1/test-report-templates`
   - 表: `test_report_template`

2. **测试报告管理** (`/test-report`)
   - API: `GET /api/v1/test-reports`
   - 表: `test_report`

3. **测试覆盖分析** (`/test-coverage`)
   - API: `GET /api/v1/test-coverage`
   - 表: `test_coverage_analysis`

4. **测试风险评估** (`/test-risk-assessment`)
   - API: `GET /api/v1/test-risk-assessment`
   - 表: `test_risk_assessment` (在04_test_execution_tables.sql中，无语法问题)

5. **测试执行管理** (`/test-execution`)
   - API: `GET /api/v1/test-execution/tasks`
   - 表: `test_execution_task`, `test_execution_record` (在04_test_execution_tables.sql中，无语法问题)

## 解决方案

### 步骤1：重新执行SQL脚本

修复后的SQL脚本现在兼容PostgreSQL，需要重新执行以创建表：

```sql
-- 连接到PostgreSQL数据库
psql -U postgres -d test_design_assistant

-- 执行修复后的初始化脚本
\i database/init/01_init_tables.sql
\i database/init/04_test_execution_tables.sql
```

### 步骤2：验证表是否创建成功

```sql
-- 检查关键表是否存在
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN (
    'test_report_template',
    'test_report',
    'test_coverage_analysis',
    'test_specification',
    'spec_version',
    'field_test_point',
    'logic_test_point',
    'test_execution_task',
    'test_execution_record',
    'test_risk_assessment'
  );
```

### 步骤3：重启后端服务

修复表结构后，需要重启后端服务以使更改生效。

## 注意事项

1. **数据备份**：如果数据库中已有数据，执行SQL脚本前请先备份。

2. **外键依赖**：某些表有外键依赖关系，确保依赖的表已存在：
   - `test_report` 依赖 `test_report_template`, `test_requirement`, `test_execution_task`
   - `test_coverage_analysis` 依赖 `test_requirement`
   - `test_risk_assessment` 依赖 `test_requirement`, `test_execution_task`

3. **索引创建**：所有索引定义已保留，会在表创建后自动创建。

4. **时间戳字段**：移除了`ON UPDATE CURRENT_TIMESTAMP`后，`update_time`字段不会自动更新，需要在应用层通过`@PreUpdate`注解或触发器来处理。

## 验证清单

- [x] 修复了所有MySQL COMMENT语法
- [x] 修复了所有ON UPDATE CURRENT_TIMESTAMP语法
- [x] 保留了所有表结构和索引定义
- [x] 验证了04_test_execution_tables.sql无语法问题
- [ ] 重新执行SQL脚本创建表
- [ ] 验证表创建成功
- [ ] 测试前端页面API调用
- [ ] 确认500错误已解决

## 后续建议

1. **数据库迁移工具**：建议使用Flyway或Liquibase等数据库迁移工具，可以更好地管理数据库版本和迁移。

2. **SQL语法检查**：在CI/CD流程中添加SQL语法检查，确保SQL脚本与目标数据库兼容。

3. **文档更新**：更新数据库初始化文档，明确说明使用的数据库类型和语法要求。

## 修复日期

2026-01-17

