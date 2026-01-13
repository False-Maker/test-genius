# 监控配置快速启动指南

## 一、快速启动

### 1.1 启动所有服务（包括监控服务）

```bash
# 在项目根目录执行
docker-compose up -d
```

### 1.2 仅启动监控服务

```bash
# 启动Prometheus、Grafana、AlertManager
docker-compose up -d prometheus grafana alertmanager
```

## 二、访问地址

启动成功后，可以通过以下地址访问监控服务：

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001
  - 默认用户名: `admin`
  - 默认密码: `admin` (首次登录后建议修改)
- **AlertManager**: http://localhost:9093

## 三、验证配置

### 3.1 验证Prometheus

1. 访问 http://localhost:9090/targets
2. 检查所有targets状态为 "UP"
3. 访问 http://localhost:9090/graph 可以查询指标

### 3.2 验证Grafana

1. 登录Grafana (http://localhost:3001)
2. 进入 "Configuration" → "Data Sources"
3. 检查Prometheus数据源状态为 "Healthy"
4. 进入 "Dashboards" → "Browse" 查看已加载的Dashboard

### 3.3 验证告警规则

1. 访问 http://localhost:9090/alerts
2. 检查告警规则已加载
3. 访问 http://localhost:9093 查看AlertManager状态

### 3.4 验证健康检查

```bash
# 检查容器健康状态
docker-compose ps

# 应该看到所有服务的健康状态为 "healthy"
```

## 四、Dashboard说明

### 4.1 系统资源监控

- **位置**: Grafana → Dashboards → "系统资源监控"
- **监控内容**: JVM堆内存、非堆内存、GC、线程数、CPU、内存、文件描述符

### 4.2 应用性能监控

- **位置**: Grafana → Dashboards → "应用性能监控"
- **监控内容**: 接口响应时间、QPS、错误率、慢接口、数据库连接池、Redis

### 4.3 业务监控

- **位置**: Grafana → Dashboards → "业务监控"
- **监控内容**: 用例生成任务、成功率、耗时、模型调用统计

**注意**: 业务监控面板中的部分指标需要应用代码中实现自定义指标收集，详见"业务指标监控实现"任务。

## 五、告警配置

### 5.1 告警规则

告警规则文件位于 `monitoring/prometheus/alerts/` 目录：

- `system.rules.yml` - 系统资源告警
- `application.rules.yml` - 应用告警
- `business.rules.yml` - 业务告警
- `health.rules.yml` - 健康检查告警

### 5.2 告警通知

当前配置使用Webhook作为默认通知渠道。实际使用时需要配置真实的通知渠道：

1. 编辑 `monitoring/alertmanager/alertmanager.yml`
2. 配置邮件、企业微信、钉钉等通知渠道
3. 重启AlertManager服务: `docker-compose restart alertmanager`

详细配置说明请参考 `docs/监控配置文档.md`

## 六、常见问题

### 6.1 Prometheus无法抓取指标

**解决方案**:
1. 检查应用服务是否正常运行: `docker-compose ps`
2. 检查网络连接: 确保所有服务在同一个Docker网络
3. 检查Actuator端点: `curl http://localhost:8080/actuator/prometheus`

### 6.2 Grafana无法显示数据

**解决方案**:
1. 检查Prometheus数据源配置
2. 检查Prometheus中是否有数据: 访问 http://localhost:9090/graph
3. 检查时间范围设置

### 6.3 健康检查失败

**解决方案**:
1. 检查应用服务是否正常启动
2. 检查健康检查端点是否可访问
3. 查看容器日志: `docker-compose logs <service-name>`

## 七、目录结构

```
monitoring/
├── grafana/
│   ├── dashboards/              # Dashboard配置文件
│   │   ├── system-resources.json
│   │   ├── application-performance.json
│   │   └── business-metrics.json
│   └── provisioning/
│       ├── datasources/        # 数据源配置
│       │   └── prometheus.yml
│       └── dashboards/         # Dashboard自动加载配置
│           └── dashboards.yml
├── prometheus/
│   ├── prometheus.yml          # Prometheus主配置
│   └── alerts/                 # 告警规则
│       ├── system.rules.yml
│       ├── application.rules.yml
│       ├── business.rules.yml
│       └── health.rules.yml
└── alertmanager/
    └── alertmanager.yml        # AlertManager配置
```

## 八、详细文档

更多详细信息请参考：
- `docs/监控配置文档.md` - 完整的监控配置说明文档

---

**最后更新**: 2024-01-13

