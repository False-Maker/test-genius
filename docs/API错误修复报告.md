# API错误修复报告

## 修复日期
2026-01-17

## 修复的问题

### 1. test-report-templates API 500错误 ✅

**问题描述**：
- API: `GET /api/v1/test-report-templates?page=0&size=10`
- 错误：500 Internal Server Error
- 原因：数据库表 `test_report_template` 不存在

**修复方案**：
1. 修复了SQL脚本中的MySQL语法问题（COMMENT语法）
2. 执行SQL脚本创建了 `test_report_template` 表

**状态**：✅ 已修复

---

### 2. test-reports API 500错误 ✅

**问题描述**：
- API: `GET /api/v1/test-reports?page=0&size=10`
- 错误：500 Internal Server Error
- 原因：数据库表 `test_report` 不存在

**修复方案**：
1. 修复了SQL脚本中的MySQL语法问题（COMMENT语法）
2. 执行SQL脚本创建了 `test_report` 表

**状态**：✅ 已修复

---

### 3. specifications API 500错误 ✅

**问题描述**：
- API: `GET /api/v1/specifications?page=0&size=10`
- 错误：500 Internal Server Error
- 原因：Repository查询使用 `SELECT *` 导致字段映射问题

**修复方案**：
修改了 `TestSpecificationRepository.findWithFilters()` 方法，将 `SELECT *` 改为显式指定所有字段名，确保PostgreSQL原生查询能正确映射到实体类。

**修改文件**：
- `backend-java/test-design-assistant-core/src/main/java/com/sinosoft/testdesign/repository/TestSpecificationRepository.java`

**修改内容**：
```java
// 修改前
@Query(value = "SELECT * FROM test_specification ts WHERE ...", nativeQuery = true)

// 修改后
@Query(value = "SELECT ts.id, ts.spec_code, ts.spec_name, ts.spec_type, ... FROM test_specification ts WHERE ...", nativeQuery = true)
```

**状态**：✅ 已修复（需要重启后端服务）

---

## 修复总结

### SQL语法修复
- 修复了 `database/init/01_init_tables.sql` 中所有MySQL特有的语法
- 移除了 `COMMENT '注释'` 语法（PostgreSQL不支持）
- 移除了 `ON UPDATE CURRENT_TIMESTAMP` 语法（PostgreSQL不支持）

### 数据库表创建
已成功创建以下表：
- ✅ `test_report_template` - 测试报告模板表
- ✅ `test_report` - 测试报告表
- ✅ `test_coverage_analysis` - 测试覆盖分析表
- ✅ `test_specification` - 测试规约表
- ✅ `spec_version` - 规约版本管理表
- ✅ `field_test_point` - 字段测试要点表
- ✅ `logic_test_point` - 逻辑测试要点表
- ✅ `test_execution_task` - 测试执行任务表
- ✅ `test_execution_record` - 测试执行记录表
- ✅ `test_risk_assessment` - 风险评估表

### Repository查询修复
- ✅ 修复了 `TestSpecificationRepository.findWithFilters()` 的字段映射问题

## 下一步操作

1. **重启后端服务**
   - 修复后的代码需要重新编译和启动才能生效

2. **测试API**
   - 测试 `/api/v1/test-report-templates` - 应返回200状态码
   - 测试 `/api/v1/test-reports` - 应返回200状态码
   - 测试 `/api/v1/specifications` - 应返回200状态码

3. **验证前端页面**
   - 测试报告模板管理页面
   - 测试报告管理页面
   - 测试规约管理页面

## 注意事项

1. **字段映射**：使用PostgreSQL原生SQL查询时，建议显式指定字段名而不是使用 `SELECT *`，以确保字段正确映射到实体类。

2. **SQL语法兼容性**：确保SQL脚本与目标数据库（PostgreSQL）兼容，避免使用MySQL特有的语法。

3. **表依赖关系**：某些表有外键依赖，创建表时需要注意顺序。

## 相关文件

- `database/init/01_init_tables.sql` - 数据库初始化脚本（已修复）
- `database/init/04_test_execution_tables.sql` - 测试执行模块表脚本
- `scripts/execute_sql.py` - SQL执行工具
- `backend-java/test-design-assistant-core/src/main/java/com/sinosoft/testdesign/repository/TestSpecificationRepository.java` - Repository查询修复



















