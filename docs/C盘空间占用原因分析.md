# C盘空间占用原因分析

## 问题描述

运行项目后，C盘空间减少了约5GB。本文档分析可能的原因。

## 原因分析（按可能性排序）

### 1. Maven本地仓库（最可能的原因）⭐

**位置**：`C:\Users\{您的用户名}\.m2\repository`

**为什么占用空间**：
- Maven在首次构建项目时会自动下载所有依赖到本地仓库
- 您的项目是Spring Boot 3.x项目，包含大量依赖：
  - Spring Boot核心框架（~200MB）
  - Spring Data JPA（~50MB）
  - PostgreSQL驱动（~10MB）
  - Lombok、MapStruct等工具（~50MB）
  - 测试框架（JUnit、Mockito等，~100MB）
  - 其他第三方库（可能几百MB到几GB）
- **一个完整的Spring Boot项目首次构建可能下载2-5GB的依赖**

**空间占用特点**：
- 依赖下载是一次性的，后续构建会复用
- 不同项目的依赖会累积在同一个仓库中
- 如果之前没有运行过Maven项目，首次下载会占用较多空间

**验证方法**：
```powershell
# 检查Maven仓库大小
Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(GB)";Expression={[math]::Round($_.Sum / 1GB, 2)}}
```

---

### 2. Docker数据（如果使用Docker运行项目）

**位置**：
- `C:\ProgramData\Docker\`（Docker Desktop默认位置）
- 或 `C:\Users\{用户名}\AppData\Local\Docker\`

**为什么占用空间**：
- 如果使用`docker-compose.yml`运行项目，Docker会下载以下镜像：
  - PostgreSQL镜像（~300MB）
  - Redis镜像（~50MB）
  - Maven构建镜像（~1GB+）- 用于构建Java后端
  - Python基础镜像（~500MB）- 用于Python服务
  - 最终应用镜像（可能几百MB到1GB）
- **总计可能占用2-4GB空间**

**空间占用特点**：
- 镜像下载是一次性的，后续运行会复用
- 如果之前没有使用过Docker，首次下载会占用较多空间
- Docker还会创建数据卷存储数据库数据

**验证方法**：
```powershell
# 检查Docker磁盘使用
docker system df

# 检查Docker数据目录大小
Get-ChildItem -Path "C:\ProgramData\Docker" -Recurse -ErrorAction SilentlyContinue | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(GB)";Expression={[math]::Round($_.Sum / 1GB, 2)}}
```

---

### 3. 项目日志文件（可能性较低，除非运行时间很长）

**位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\logs\`

**为什么占用空间**：
- 根据配置，日志文件会滚动保存：
  - `test-design-assistant.log`：最大100MB，保留30天历史
  - `slow-query.log`：最大50MB，保留7天历史
- **理论上最多占用约3GB（30个100MB文件 + 7个50MB文件）**
- 但这是累积的，需要运行很长时间才会达到

**空间占用特点**：
- 日志是逐步累积的，不会一次性占用5GB
- 除非项目运行了很长时间，否则不太可能是主要原因

**验证方法**：
```powershell
# 检查日志目录大小
Get-ChildItem -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs" -Recurse | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(MB)";Expression={[math]::Round($_.Sum / 1MB, 2)}}
```

---

### 4. 其他可能的原因（可能性较低）

#### 4.1 Maven编译产物（target目录）
- **位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\target\`
- **占用**：通常几百MB，不会达到5GB
- **说明**：在项目目录下，不在C盘

#### 4.2 前端node_modules
- **位置**：`D:\Demo\test-genius\frontend\node_modules\`
- **占用**：通常几百MB到1GB
- **说明**：在项目目录下，不在C盘

#### 4.3 文件上传目录
- **位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads\`
- **占用**：取决于上传的文件数量
- **说明**：在项目目录下，不在C盘

#### 4.4 Python虚拟环境
- **位置**：可能在项目目录或用户目录
- **占用**：通常几百MB到1GB
- **说明**：如果存在，可能在C盘用户目录

---

## 结论

**最可能的原因排序**：

1. **Maven本地仓库**（90%可能性）
   - 首次构建Spring Boot项目会下载大量依赖
   - 可能占用2-5GB空间
   - 位置：`C:\Users\{用户名}\.m2\repository`

2. **Docker镜像和数据**（如果使用Docker，80%可能性）
   - 首次运行docker-compose会下载多个镜像
   - 可能占用2-4GB空间
   - 位置：`C:\ProgramData\Docker\`

3. **项目日志文件**（5%可能性）
   - 需要运行很长时间才会累积到5GB
   - 位置：项目目录下的logs文件夹（不在C盘）

**建议验证方法**：
运行检查脚本快速定位：
```powershell
cd D:\Demo\test-genius
.\scripts\check-disk-usage.ps1
```

---

## 技术说明

### Maven依赖下载机制

1. **首次构建**：
   - Maven检查`pom.xml`中声明的依赖
   - 从中央仓库（或配置的镜像）下载依赖到本地仓库
   - 下载的依赖包括：
     - 直接依赖（pom.xml中声明的）
     - 传递依赖（依赖的依赖）
     - 依赖的源代码和文档（可选）

2. **依赖大小**：
   - Spring Boot Starter Web：~50MB（包含所有传递依赖）
   - Spring Data JPA：~30MB
   - PostgreSQL驱动：~1MB
   - 测试框架：~100MB
   - 其他工具库：可能几百MB

3. **累积效应**：
   - 如果之前运行过其他Maven项目，依赖会累积
   - 不同版本的同一依赖会分别存储
   - 本地仓库会持续增长

### Docker镜像下载机制

1. **镜像分层**：
   - Docker镜像采用分层存储
   - 基础镜像（如PostgreSQL、Redis）会被多个项目共享
   - 但首次下载会占用完整空间

2. **构建镜像**：
   - Maven构建镜像（`maven:3.9-eclipse-temurin-17`）通常很大（~1GB+）
   - 包含完整的Maven和JDK环境

3. **数据卷**：
   - PostgreSQL数据会存储在Docker数据卷中
   - 可能占用几百MB到几GB（取决于数据量）

---

## 总结

**C盘减少5GB的最可能原因**：
- **Maven本地仓库首次下载依赖**（最可能）
- **Docker镜像首次下载**（如果使用Docker）

这些都是**一次性下载**，后续运行项目不会继续增加空间占用（除非添加新的依赖或镜像）。

























