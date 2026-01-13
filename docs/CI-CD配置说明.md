# CI/CD配置说明文档

## 概述

本文档说明测试设计助手系统的CI/CD配置，包括GitLab CI和GitHub Actions两种CI/CD方案的配置和使用方法。

## 一、CI/CD配置文件

### 1.1 GitLab CI配置

**文件位置**: `.gitlab-ci.yml`

**主要功能**:
- 自动化构建（Java、Python、前端）
- 自动化测试（单元测试、集成测试）
- 代码质量检查（SonarQube、ESLint、Pylint）
- Docker镜像构建和推送
- 多环境部署（开发、测试、生产）

### 1.2 GitHub Actions配置

**文件位置**: `.github/workflows/ci.yml`

**主要功能**:
- 自动化构建（Java、Python、前端）
- 自动化测试（单元测试、集成测试）
- 代码质量检查（SonarQube、ESLint、Pylint）
- Docker镜像构建和推送到GitHub Container Registry
- 多环境部署（开发、测试、生产）

### 1.3 Dockerfile文件

- **Java后端**: `backend-java/test-design-assistant-core/Dockerfile`
- **Python后端**: `backend-python/ai-service/Dockerfile`（已存在）
- **前端**: `frontend/Dockerfile`

## 二、GitLab CI配置说明

### 2.1 环境变量配置

在GitLab项目的 **Settings > CI/CD > Variables** 中配置以下变量：

| 变量名 | 说明 | 是否必需 |
|--------|------|---------|
| `SONARQUBE_URL` | SonarQube服务器地址 | 否（代码质量检查需要） |
| `SONARQUBE_TOKEN` | SonarQube认证Token | 否（代码质量检查需要） |
| `CI_REGISTRY` | Docker镜像仓库地址 | 是（Docker镜像推送需要） |
| `CI_REGISTRY_USER` | Docker镜像仓库用户名 | 是（Docker镜像推送需要） |
| `CI_REGISTRY_PASSWORD` | Docker镜像仓库密码 | 是（Docker镜像推送需要） |

### 2.2 Pipeline阶段说明

#### 构建阶段（build）
- `build:java` - Java后端编译
- `build:python` - Python后端依赖安装和代码检查
- `build:frontend` - 前端构建

#### 测试阶段（test）
- `test:java` - Java单元测试和代码覆盖率
- `test:python` - Python单元测试和代码覆盖率
- `test:frontend` - 前端Lint检查和单元测试（Vitest）

#### 代码质量检查阶段（quality）
- `quality:java` - SonarQube代码质量分析（手动触发）
- `quality:python` - Pylint、Black、Flake8代码质量检查

#### 打包阶段（package）
- `package:java` - 构建Java后端Docker镜像并推送
- `package:python` - 构建Python后端Docker镜像并推送
- `package:frontend` - 构建前端Docker镜像并推送

#### 部署阶段（deploy）
- `deploy:dev` - 部署到开发环境（手动触发）
- `deploy:test` - 部署到测试环境（手动触发）
- `deploy:prod` - 部署到生产环境（仅main/master分支，手动触发）

### 2.3 使用说明

1. **自动触发**: 当代码推送到 `develop`、`main`、`master` 分支或创建Merge Request时，会自动触发Pipeline
2. **手动触发**: 代码质量检查和部署任务需要手动触发
3. **查看结果**: 在GitLab项目的 **CI/CD > Pipelines** 中查看Pipeline执行结果

### 2.4 构建Java Docker镜像

**注意**: Java Dockerfile需要在 `backend-java` 目录下执行构建：

```bash
cd backend-java
docker build -f test-design-assistant-core/Dockerfile -t backend-java:latest .
```

## 三、GitHub Actions配置说明

### 3.1 Secrets配置

在GitHub仓库的 **Settings > Secrets and variables > Actions** 中配置以下Secrets：

| Secret名称 | 说明 | 是否必需 |
|-----------|------|---------|
| `SONAR_TOKEN` | SonarQube认证Token | 否（代码质量检查需要） |
| `SONAR_HOST_URL` | SonarQube服务器地址 | 否（代码质量检查需要） |
| `GITHUB_TOKEN` | GitHub Token（自动生成） | 是（Docker镜像推送需要） |

### 3.2 Workflow触发条件

- **Push事件**: 推送到 `main`、`master`、`develop` 分支
- **Pull Request**: 创建或更新PR到 `main`、`master`、`develop` 分支
- **手动触发**: 通过GitHub Actions界面手动触发

### 3.3 工作流说明

#### 构建作业
- `build-java` - Java后端编译
- `build-python` - Python后端依赖安装
- `build-frontend` - 前端构建

#### 测试作业
- `test-java` - Java单元测试和代码覆盖率（使用PostgreSQL和Redis服务）
- `test-python` - Python单元测试和代码覆盖率
- `test-frontend` - 前端Lint检查和单元测试（Vitest）

#### 代码质量检查作业
- `quality-java` - SonarQube代码质量分析（仅develop/main/master分支）
- `quality-python` - Pylint、Black、Flake8代码质量检查

#### 打包作业
- `package-java` - 构建Java后端Docker镜像并推送到GitHub Container Registry
- `package-python` - 构建Python后端Docker镜像并推送到GitHub Container Registry
- `package-frontend` - 构建前端Docker镜像并推送到GitHub Container Registry

#### 部署作业
- `deploy-dev` - 部署到开发环境（develop分支，手动触发）
- `deploy-prod` - 部署到生产环境（仅tag版本，手动触发）

### 3.4 使用说明

1. **自动触发**: 当代码推送到指定分支或创建PR时，会自动触发Workflow
2. **查看结果**: 在GitHub仓库的 **Actions** 标签页查看Workflow执行结果
3. **Docker镜像**: 构建的Docker镜像会推送到GitHub Container Registry，格式为：
   - `ghcr.io/<owner>/<repo>/backend-java:<tag>`
   - `ghcr.io/<owner>/<repo>/backend-python:<tag>`
   - `ghcr.io/<owner>/<repo>/frontend:<tag>`

## 四、Docker镜像构建说明

### 4.1 Java后端Docker镜像

**构建命令**:
```bash
cd backend-java
docker build -f test-design-assistant-core/Dockerfile -t backend-java:latest .
```

**特点**:
- 多阶段构建，减小镜像体积
- 使用JRE运行时镜像
- 包含健康检查
- 非root用户运行

### 4.2 Python后端Docker镜像

**构建命令**:
```bash
cd backend-python/ai-service
docker build -t backend-python:latest .
```

**特点**:
- 基于Python 3.10-slim镜像
- 最小化依赖安装
- 暴露8000端口

### 4.3 前端Docker镜像

**构建命令**:
```bash
cd frontend
docker build -t frontend:latest .
```

**特点**:
- 多阶段构建，使用Nginx提供静态文件服务
- 构建产物优化（代码分割、压缩）
- 包含健康检查

## 五、部署配置

### 5.1 环境配置

CI/CD配置支持多环境部署，需要创建对应的docker-compose配置文件：

- `docker-compose.dev.yml` - 开发环境配置
- `docker-compose.test.yml` - 测试环境配置
- `docker-compose.prod.yml` - 生产环境配置

### 5.2 部署脚本

部署阶段需要根据实际环境配置部署脚本，可以使用以下方式：

1. **Docker Compose**: 适用于单机部署
2. **Kubernetes**: 适用于容器编排部署
3. **Ansible**: 适用于多服务器部署
4. **自定义脚本**: 根据实际需求编写

### 5.3 部署策略

- **开发环境**: 自动部署（develop分支）
- **测试环境**: 手动部署（develop分支）
- **生产环境**: 手动部署（仅main/master分支的tag版本）

## 六、注意事项

### 6.1 配置要求

1. **GitLab CI**:
   - 需要配置Docker镜像仓库地址和认证信息
   - 需要配置SonarQube服务器信息（如使用代码质量检查）
   - 需要配置Runner支持Docker-in-Docker

2. **GitHub Actions**:
   - 需要配置GitHub Container Registry访问权限
   - 需要配置SonarQube服务器信息（如使用代码质量检查）
   - 需要配置部署服务器的访问凭证（如使用自动部署）

### 6.2 性能优化

1. **缓存配置**: CI/CD配置已包含Maven、npm、pip缓存配置
2. **并行执行**: 构建、测试、打包作业可以并行执行
3. **条件执行**: 部分作业仅在特定分支或条件下执行

### 6.3 安全建议

1. **敏感信息**: 所有敏感信息（密码、Token等）必须使用Secrets/Variables配置
2. **镜像安全**: 定期更新基础镜像，扫描安全漏洞
3. **访问控制**: 限制生产环境部署权限

## 七、故障排查

### 7.1 常见问题

1. **构建失败**: 检查依赖版本、网络连接、构建环境
2. **测试失败**: 检查测试环境配置、数据库连接、服务依赖
3. **镜像推送失败**: 检查镜像仓库认证信息、网络连接
4. **部署失败**: 检查部署脚本、服务器连接、环境配置

### 7.2 日志查看

- **GitLab CI**: 在Pipeline详情页查看各阶段的日志
- **GitHub Actions**: 在Workflow运行详情页查看各作业的日志

## 八、后续优化建议

1. **自动化测试**: 增加集成测试、端到端测试
2. **性能测试**: 添加性能测试阶段
3. **安全扫描**: 添加依赖安全扫描、镜像安全扫描
4. **通知机制**: 配置构建失败通知、部署成功通知
5. **回滚机制**: 实现自动回滚功能

---

**文档版本**: v1.0  
**最后更新**: 2024-01-13  
**维护人**: 开发团队

