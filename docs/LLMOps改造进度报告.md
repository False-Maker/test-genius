# LLMOps 改造进度报告

## 一、改造概述

参考 Dify 框架，对测试设计助手系统进行 LLMOps 和监控能力升级，建立完整的日志记录、性能监控和成本统计能力。

## 二、已完成工作

### 2.1 数据库设计 ✅

**文件**: `database/init/05_llmops_tables.sql`

创建了以下表结构：

1. **app_log** - 应用日志表
   - 记录所有模型调用和应用操作的详细信息
   - 包含：请求ID、用户信息、应用类型、模型信息、提示词、响应、token使用量、响应时间、成本等
   - 建立了完善的索引以支持高效查询

2. **user_feedback** - 用户反馈表
   - 记录用户对模型响应质量的反馈
   - 包含：评分、评论、反馈类型、标签等

3. **model_cost_config** - 模型成本配置表
   - 配置各模型的token价格
   - 支持输入和输出token分别定价

4. **alert_rule** - 告警规则表
   - 定义各种告警规则
   - 支持多种告警条件和通知渠道

5. **alert_record** - 告警记录表
   - 记录触发的告警信息
   - 支持告警解决和通知状态跟踪

### 2.2 Java后端实现 ✅

#### 实体类
- `AppLog.java` - 应用日志实体
- `UserFeedback.java` - 用户反馈实体
- `ModelCostConfig.java` - 模型成本配置实体
- `AlertRule.java` - 告警规则实体
- `AlertRecord.java` - 告警记录实体

#### Repository层
- `AppLogRepository.java` - 应用日志Repository，包含丰富的查询方法
- `UserFeedbackRepository.java` - 用户反馈Repository
- `ModelCostConfigRepository.java` - 模型成本配置Repository
- `AlertRuleRepository.java` - 告警规则Repository
- `AlertRecordRepository.java` - 告警记录Repository

#### Service层
- `AppLogService.java` - 应用日志服务接口
- `AppLogServiceImpl.java` - 应用日志服务实现
  - 支持同步和异步日志记录
  - 提供性能统计、成本统计、模型使用统计等功能

#### 日志记录中间件
- `@AppLog` 注解 - 用于标记需要记录日志的方法
- `AppLogAspect.java` - AOP切面，自动拦截标记的方法并记录日志
  - 自动提取模型信息、提示词、响应等
  - 支持记录请求参数和响应结果
  - 异步记录，不影响主业务流程

### 2.3 Python AI服务实现 ✅

#### 日志记录装饰器
- `app_log_decorator.py` - 应用日志装饰器
  - `@app_log` 装饰器，用于标记需要记录日志的函数
  - 支持同步和异步函数
  - 自动提取模型信息、token使用量等
  - 自动计算成本（基于模型成本配置）
  - 异步记录到数据库

## 三、已完成工作（续）

### 3.1 性能监控统计API ✅

**文件位置**:
- Controller: `backend-java/.../controller/MonitoringController.java`
- Service: `backend-java/.../service/MonitoringService.java`
- Service实现: `backend-java/.../service/impl/MonitoringServiceImpl.java`

**已实现功能**:
- ✅ 响应时间统计API（P50、P95、P99）
- ✅ 成功率统计API
- ✅ Token使用量统计API
- ✅ 成本统计API
- ✅ 支持时间范围查询
- ✅ 支持按模型、按应用、按用户统计
- ✅ 时间序列数据API（用于图表展示）

### 3.2 成本配置和统计功能 ✅

**文件位置**:
- Controller: `backend-java/.../controller/ModelCostConfigController.java`
- Service: `backend-java/.../service/ModelCostConfigService.java`
- Service实现: `backend-java/.../service/impl/ModelCostConfigServiceImpl.java`

**已实现功能**:
- ✅ 成本配置管理API（CRUD）
- ✅ 成本计算逻辑（已在装饰器中实现）
- ✅ 成本统计API（按模型、按应用、按用户）
- ✅ 成本配置启用/禁用功能

### 3.3 告警机制 ✅

**文件位置**:
- Controller: `backend-java/.../controller/AlertController.java`
- Service: `backend-java/.../service/AlertService.java`
- Service实现: `backend-java/.../service/impl/AlertServiceImpl.java`
- 定时任务: `backend-java/.../scheduled/AlertScheduler.java`

**已实现功能**:
- ✅ 告警规则管理API（CRUD）
- ✅ 告警引擎（定时检查告警规则，每60秒执行一次）
- ✅ 告警触发逻辑（支持失败率、响应时间、成本等规则类型）
- ✅ 告警记录查询和管理
- ✅ 告警解决功能
- ⏳ 告警通知发送（邮件/站内信/Webhook）- 待实现

## 四、已完成工作（续）

### 4.1 前端监控Dashboard ✅

**文件位置**:
- 页面: `frontend/src/views/monitoring/MonitoringDashboard.vue`
- API: `frontend/src/api/monitoring.ts`

**已实现功能**:
- ✅ 响应时间趋势图（使用ECharts）
- ✅ 成功率饼图
- ✅ Token使用量柱状图
- ✅ 成本趋势图
- ✅ 模型使用情况对比
- ✅ 应用使用情况对比
- ✅ 支持时间范围选择
- ✅ 统计卡片展示（总请求数、成功率、平均响应时间、总成本）

## 五、总结

### 5.1 完成情况

第一阶段（LLMOps 和监控）的所有核心功能已经完成：

1. ✅ **数据库设计** - 完整的表结构设计
2. ✅ **Java后端实现** - 实体类、Repository、Service、Controller
3. ✅ **Python AI服务实现** - 日志记录装饰器
4. ✅ **性能监控统计API** - 完整的统计API
5. ✅ **成本配置和统计功能** - 成本配置管理和统计
6. ✅ **告警机制** - 告警规则管理和定时检查
7. ✅ **前端监控Dashboard** - 可视化监控界面

### 5.2 已完成功能（补充）

#### 5.2.1 用户反馈功能 ✅

**文件位置**:
- Service: `backend-java/.../service/UserFeedbackService.java`
- Service实现: `backend-java/.../service/impl/UserFeedbackServiceImpl.java`
- Controller: `backend-java/.../controller/UserFeedbackController.java`

**已实现功能**:
- ✅ 反馈收集 API（创建反馈）
- ✅ 反馈查询 API（按请求ID、日志ID、用户ID、时间范围查询）
- ✅ 反馈解决功能
- ✅ 反馈统计分析（平均评分、评分分布、解决率等）
- ⏳ 前端用户反馈界面（待实现）

### 5.3 待完善功能

1. ⏳ **告警通知发送** - 邮件/站内信/Webhook通知（已预留接口）
2. ⏳ **前端用户反馈界面** - 用户反馈展示和提交页面
3. ⏳ **数据归档策略** - 历史数据归档和清理

### 5.3 使用说明

1. **数据库初始化**: 执行 `database/init/05_llmops_tables.sql`
2. **配置模型成本**: 通过 `/v1/model-cost-configs` API配置各模型的token价格
3. **配置告警规则**: 通过 `/v1/alerts/rules` API配置告警规则
4. **查看监控数据**: 访问前端监控Dashboard页面
5. **查看告警记录**: 通过 `/v1/alerts/records` API查询告警记录

## 四、使用说明

### 4.1 Java后端使用

在需要记录日志的方法上添加 `@AppLog` 注解：

```java
@AppLog(appType = "CASE_GENERATION", logRequest = true, logResponse = true)
public Result<CaseGenerationResult> generateCases(CaseGenerationRequest request) {
    // 业务逻辑
}
```

### 4.2 Python AI服务使用

在需要记录日志的函数上添加 `@app_log` 装饰器：

```python
from app.utils.app_log_decorator import app_log

@app_log(app_type="MODEL_CALL", log_request=True, log_response=True)
def call_model(self, model_code: str, prompt: str, db: Session):
    # 业务逻辑
    return result
```

## 五、下一步计划

1. **Week 2**: 实现性能监控统计API
2. **Week 2**: 实现前端监控Dashboard
3. **Week 3**: 实现成本配置和统计功能
4. **Week 4**: 实现告警机制和用户反馈功能

## 六、注意事项

1. **性能考虑**: 日志记录使用异步方式，避免影响主业务流程
2. **数据量**: 日志数据量可能很大，需要考虑数据归档和清理策略
3. **成本计算**: 需要配置各模型的token价格才能准确计算成本
4. **告警规则**: 告警规则需要合理配置，避免告警风暴

---

**文档版本**: v1.0  
**创建时间**: 2026-01-26  
**最后更新**: 2026-01-26
