# Docker 镜像加速配置指南

## 问题说明

如果遇到以下错误：
```
ERROR: failed to do request: Head "https://registry-1.docker.io/v2/library/node/manifests/18-alpine": EOF
```

这通常是因为：
1. 网络连接不稳定
2. Docker Hub 访问受限
3. 需要配置镜像加速

## 解决方案

### 方法1：配置 Docker 镜像加速（推荐）

#### 步骤1：打开 Docker Desktop 设置

1. 右键点击系统托盘中的 Docker 图标
2. 选择 **Settings**（设置）

#### 步骤2：配置镜像加速

1. 在左侧菜单选择 **Docker Engine**
2. 在右侧的 JSON 配置中添加以下内容：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
```

3. 点击 **Apply & Restart**（应用并重启）

#### 步骤3：验证配置

重启后，运行以下命令验证：

```powershell
docker info | Select-String -Pattern "Registry Mirrors"
```

应该能看到配置的镜像地址。

### 方法2：使用国内镜像源

如果方法1不行，可以尝试其他镜像源：

**阿里云镜像**（需要登录阿里云获取专属地址）：
1. 访问：https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors
2. 登录后获取专属加速地址
3. 添加到 Docker Engine 配置中

**腾讯云镜像**：
```json
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

### 方法3：手动拉取镜像

如果配置镜像加速后仍然失败，可以尝试：

```powershell
# 使用镜像加速地址手动拉取
docker pull docker.mirrors.ustc.edu.cn/library/node:18-alpine
docker pull docker.mirrors.ustc.edu.cn/library/maven:3.9-eclipse-temurin-17
docker pull docker.mirrors.ustc.edu.cn/library/eclipse-temurin:17-jre-alpine
docker pull docker.mirrors.ustc.edu.cn/library/python:3.10-slim
docker pull docker.mirrors.ustc.edu.cn/library/nginx:alpine
```

然后重新标记镜像：

```powershell
docker tag docker.mirrors.ustc.edu.cn/library/node:18-alpine node:18-alpine
docker tag docker.mirrors.ustc.edu.cn/library/maven:3.9-eclipse-temurin-17 maven:3.9-eclipse-temurin-17
docker tag docker.mirrors.ustc.edu.cn/library/eclipse-temurin:17-jre-alpine eclipse-temurin:17-jre-alpine
docker tag docker.mirrors.ustc.edu.cn/library/python:3.10-slim python:3.10-slim
docker tag docker.mirrors.ustc.edu.cn/library/nginx:alpine nginx:alpine
```

### 方法4：使用代理

如果有代理，可以配置 Docker 使用代理：

1. 打开 Docker Desktop → Settings → Resources → Proxies
2. 配置代理服务器地址和端口
3. 点击 Apply & Restart

## 验证配置

配置完成后，重新尝试启动服务：

```powershell
docker compose up -d
```

## 临时解决方案

如果暂时无法解决网络问题，可以：

1. **稍后重试**：网络问题可能是暂时的
2. **使用本地开发模式**：不通过 Docker，直接在本地运行服务
3. **使用 VPN 或代理**：改善网络连接

## 常见问题

### Q: 配置镜像加速后仍然失败？

A: 尝试：
1. 检查镜像地址是否正确
2. 尝试其他镜像源
3. 检查网络连接
4. 重启 Docker Desktop

### Q: 如何查看当前配置的镜像源？

A: 运行：
```powershell
docker info | Select-String -Pattern "Registry Mirrors"
```

### Q: 配置后需要重启 Docker 吗？

A: 是的，配置镜像加速后需要重启 Docker Desktop 才能生效。

