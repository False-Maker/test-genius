# Docker 操作指南

> **快速开始**: 使用 Docker Compose 一键启动所有服务

## 📋 目录

1. [启动所有服务](#1-启动所有服务)
2. [分步启动服务](#2-分步启动服务)
3. [查看服务状态](#3-查看服务状态)
4. [查看服务日志](#4-查看服务日志)
5. [停止服务](#5-停止服务)
6. [重启服务](#6-重启服务)
7. [清理服务](#7-清理服务)
8. [常见问题](#8-常见问题)

---

## 1. 启动所有服务

### 方式一：启动所有服务（推荐）

```powershell
# 在项目根目录执行
docker compose up -d
```

**说明**:
- `-d` 参数表示后台运行（detached mode）
- 会自动启动所有服务：PostgreSQL、Redis、Java后端、Python服务、前端、监控服务等

### 方式二：只启动核心服务（数据库和Redis）

如果只想启动数据库和Redis，可以先启动基础服务：

```powershell
# 启动数据库和Redis
docker compose up -d postgres redis

# 等待服务就绪后，再启动其他服务
docker compose up -d
```

---

## 2. 分步启动服务

### 步骤1：启动基础服务（数据库、Redis）

```powershell
docker compose up -d postgres redis
```

**等待服务就绪**（约30秒）:
```powershell
# 检查服务状态
docker compose ps

# 查看日志确认服务启动成功
docker compose logs postgres
docker compose logs redis
```

### 步骤2：启动应用服务

```powershell
# 启动Java后端和Python服务
docker compose up -d backend-java backend-python

# 等待服务就绪后，启动前端
docker compose up -d frontend
```

### 步骤3：启动监控服务（可选）

```powershell
# 启动监控服务
docker compose up -d prometheus grafana alertmanager zipkin
```

---

## 3. 查看服务状态

### 查看所有服务状态

```powershell
# 查看服务列表和状态
docker compose ps

# 或者使用 Docker 命令
docker ps
```

**预期输出**:
```
NAME                        STATUS              PORTS
test-design-postgres        Up (healthy)        0.0.0.0:5432->5432/tcp
test-design-redis          Up (healthy)         0.0.0.0:6379->6379/tcp
test-design-backend-java    Up (healthy)        0.0.0.0:8080->8080/tcp
test-design-backend-python Up (healthy)        0.0.0.0:8000->8000/tcp
test-design-frontend        Up                  0.0.0.0:3000->3000/tcp
```

### 检查服务健康状态

```powershell
# Java后端健康检查
curl http://localhost:8080/actuator/health

# Python服务健康检查
curl http://localhost:8000/health

# 前端访问
# 浏览器打开: http://localhost:3000
```

---

## 4. 查看服务日志

### 查看所有服务日志

```powershell
# 查看所有服务日志
docker compose logs

# 实时查看所有服务日志（类似 tail -f）
docker compose logs -f
```

### 查看特定服务日志

```powershell
# 查看Java后端日志
docker compose logs -f backend-java

# 查看Python服务日志
docker compose logs -f backend-python

# 查看数据库日志
docker compose logs -f postgres

# 查看Redis日志
docker compose logs -f redis
```

### 查看最近100行日志

```powershell
docker compose logs --tail=100 backend-java
```

---

## 5. 停止服务

### 停止所有服务

```powershell
# 停止所有服务（保留容器和数据）
docker compose stop

# 停止并删除容器（保留数据卷）
docker compose down
```

### 停止特定服务

```powershell
# 停止Java后端
docker compose stop backend-java

# 停止前端
docker compose stop frontend
```

---

## 6. 重启服务

### 重启所有服务

```powershell
docker compose restart
```

### 重启特定服务

```powershell
# 重启Java后端
docker compose restart backend-java

# 重启Python服务
docker compose restart backend-python
```

---

## 7. 清理服务

### 清理容器和网络（保留数据卷）

```powershell
# 停止并删除容器、网络（数据卷保留）
docker compose down
```

### 完全清理（包括数据卷）

⚠️ **警告**: 这会删除所有数据，包括数据库数据！

```powershell
# 停止并删除容器、网络、数据卷
docker compose down -v
```

### 清理未使用的资源

```powershell
# 清理未使用的镜像、容器、网络
docker system prune

# 清理所有未使用的资源（包括未使用的镜像）
docker system prune -a
```

---

## 8. 常见问题

### 问题1：端口被占用

**错误信息**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**解决方案**:
```powershell
# 检查端口占用
netstat -ano | findstr :8080

# 停止占用端口的进程，或修改 docker-compose.yml 中的端口映射
```

### 问题2：服务启动失败

**解决方案**:
```powershell
# 查看详细错误日志
docker compose logs <service-name>

# 检查服务配置
docker compose config

# 重新构建镜像（如果修改了代码）
docker compose build --no-cache <service-name>
docker compose up -d <service-name>
```

### 问题3：数据库连接失败

**解决方案**:
```powershell
# 检查数据库服务是否运行
docker compose ps postgres

# 检查数据库日志
docker compose logs postgres

# 进入数据库容器检查
docker exec -it test-design-postgres psql -U postgres -d test_design_assistant
```

### 问题4：服务健康检查失败

**解决方案**:
```powershell
# 查看服务详细日志
docker compose logs -f <service-name>

# 检查服务是否真的在运行
docker exec -it <container-name> /bin/sh

# 手动测试健康检查端点
curl http://localhost:8080/actuator/health
```

### 问题5：需要重新构建镜像

**解决方案**:
```powershell
# 重新构建所有镜像
docker compose build --no-cache

# 重新构建特定服务
docker compose build --no-cache backend-java

# 构建并启动
docker compose up -d --build
```

---

## 9. 常用命令速查

| 操作 | 命令 |
|------|------|
| 启动所有服务 | `docker compose up -d` |
| 停止所有服务 | `docker compose stop` |
| 停止并删除容器 | `docker compose down` |
| 查看服务状态 | `docker compose ps` |
| 查看日志 | `docker compose logs -f <service>` |
| 重启服务 | `docker compose restart <service>` |
| 重新构建 | `docker compose build --no-cache` |
| 进入容器 | `docker exec -it <container-name> /bin/sh` |
| 查看资源使用 | `docker stats` |

---

## 10. 服务访问地址

启动成功后，可以通过以下地址访问服务：

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:3000 | 主应用界面 |
| Java后端API | http://localhost:8080 | REST API |
| Java后端健康检查 | http://localhost:8080/actuator/health | 健康检查 |
| Python AI服务 | http://localhost:8000 | AI服务API |
| Python健康检查 | http://localhost:8000/health | 健康检查 |
| Prometheus | http://localhost:9090 | 监控指标 |
| Grafana | http://localhost:3001 | 监控面板（admin/admin） |
| AlertManager | http://localhost:9093 | 告警管理 |
| Zipkin | http://localhost:9411 | 链路追踪 |

---

## 下一步

服务启动成功后，可以：
1. 访问前端界面：http://localhost:3000
2. 查看API文档：http://localhost:8080/swagger-ui.html
3. 查看监控面板：http://localhost:3001
4. 开始使用系统功能

