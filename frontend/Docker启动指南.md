# 前端项目 Docker 启动指南

## 📋 概述

前端项目使用 Docker 多阶段构建，最终通过 Nginx 提供静态文件服务。

## 🚀 快速启动

### 方式一：使用 Docker 命令直接构建和运行

```bash
# 1. 进入前端目录
cd frontend

# 2. 构建 Docker 镜像
docker build -t test-genius-frontend:latest .

# 3. 运行容器
docker run -d \
  --name test-genius-frontend \
  -p 80:80 \
  test-genius-frontend:latest
```

### 方式二：使用 Docker Compose（推荐）

如果项目根目录有 `docker-compose.yml`，可以使用：

```bash
# 从项目根目录启动所有服务（包括前端、后端等）
docker-compose up -d

# 或者只启动前端服务
docker-compose up -d frontend
```

### 方式三：仅启动前端服务（独立容器）

```bash
cd frontend

# 构建镜像（带标签）
docker build -t test-genius-frontend:latest .

# 运行容器（映射端口）
docker run -d \
  --name test-genius-frontend \
  -p 8080:80 \
  --restart unless-stopped \
  test-genius-frontend:latest
```

## 📝 详细说明

### Docker 构建命令参数

```bash
docker build -t <镜像名称>:<标签> <构建上下文路径>
```

- `-t, --tag`: 指定镜像名称和标签
- `.`: 当前目录作为构建上下文

### Docker 运行命令参数

```bash
docker run [选项] <镜像名称>
```

常用选项：
- `-d, --detach`: 后台运行容器
- `-p, --publish`: 端口映射 `主机端口:容器端口`
- `--name`: 指定容器名称
- `--restart`: 重启策略（`unless-stopped` 表示除非手动停止，否则总是重启）

### 访问应用

容器启动后，可以通过以下地址访问：

- **HTTP**: http://localhost:80（如果使用默认端口）
- **HTTP**: http://localhost:8080（如果映射到 8080 端口）

### 检查容器状态

```bash
# 查看运行中的容器
docker ps

# 查看所有容器（包括已停止的）
docker ps -a

# 查看容器日志
docker logs test-genius-frontend

# 查看实时日志
docker logs -f test-genius-frontend
```

### 停止和删除容器

```bash
# 停止容器
docker stop test-genius-frontend

# 删除容器
docker rm test-genius-frontend

# 停止并删除容器（一条命令）
docker rm -f test-genius-frontend
```

### 删除镜像

```bash
# 查看镜像
docker images

# 删除镜像
docker rmi test-genius-frontend:latest

# 强制删除镜像（即使有容器在使用）
docker rmi -f test-genius-frontend:latest
```

## 🔧 配置说明

### Nginx 配置

Nginx 配置文件位于 `frontend/nginx.conf`，主要功能：

- **静态文件服务**: 提供前端构建产物
- **API 代理**: 将 `/api` 请求代理到后端服务 `backend-java:8080`
- **SPA 路由支持**: 使用 `try_files` 支持 Vue Router 的 history 模式
- **健康检查**: 提供 `/health` 端点

### 环境变量（可选）

如果需要修改 API 后端地址，可以在运行容器时传递环境变量：

```bash
docker run -d \
  --name test-genius-frontend \
  -p 80:80 \
  -e API_BASE_URL=http://your-backend:8080 \
  test-genius-frontend:latest
```

注意：当前 Nginx 配置中后端地址是硬编码的，如需动态配置需要修改 `nginx.conf`。

## 🐛 故障排查

### 容器无法启动

```bash
# 查看容器日志
docker logs test-genius-frontend

# 查看容器详细信息
docker inspect test-genius-frontend
```

### 端口被占用

如果 80 端口被占用，可以映射到其他端口：

```bash
docker run -d \
  --name test-genius-frontend \
  -p 3000:80 \
  test-genius-frontend:latest
```

然后访问 http://localhost:3000

### 构建失败

检查构建日志：

```bash
# 查看详细构建日志
docker build -t test-genius-frontend:latest . --progress=plain

# 不缓存构建（强制重新构建所有层）
docker build -t test-genius-frontend:latest . --no-cache
```

### 健康检查失败

容器健康检查配置在 Dockerfile 中：

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1/health || exit 1
```

检查健康状态：

```bash
# 查看容器健康状态
docker inspect --format='{{.State.Health.Status}}' test-genius-frontend
```

## 🔄 重新构建和部署

### 更新代码后重新部署

```bash
# 1. 停止并删除旧容器
docker rm -f test-genius-frontend

# 2. 重新构建镜像
docker build -t test-genius-frontend:latest .

# 3. 启动新容器
docker run -d \
  --name test-genius-frontend \
  -p 80:80 \
  test-genius-frontend:latest
```

### 使用 Docker Compose

```bash
# 重新构建并启动
docker-compose up -d --build frontend
```

## 📚 相关文件

- `Dockerfile`: Docker 构建文件
- `nginx.conf`: Nginx 配置文件
- `package.json`: 前端依赖配置
- `vite.config.ts`: Vite 构建配置

## 💡 最佳实践

1. **使用版本标签**: 为镜像打上版本标签，便于管理
   ```bash
   docker build -t test-genius-frontend:v1.0.0 .
   ```

2. **使用 Docker Compose**: 在开发和生产环境中使用 Docker Compose 管理多个服务

3. **挂载配置文件**: 如果需要动态修改 Nginx 配置，可以挂载配置文件
   ```bash
   docker run -d \
     --name test-genius-frontend \
     -p 80:80 \
     -v $(pwd)/nginx.conf:/etc/nginx/conf.d/default.conf \
     test-genius-frontend:latest
   ```

4. **使用 .dockerignore**: 创建 `.dockerignore` 文件排除不需要的文件，减小构建上下文

## 🔗 相关文档

- [Docker 官方文档](https://docs.docker.com/)
- [Nginx 官方文档](https://nginx.org/en/docs/)
- 项目根目录: `Docker安装指南.md`
- 项目根目录: `docs/Docker操作指南.md`

