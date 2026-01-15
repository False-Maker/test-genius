# C盘空间占用检查与清理指南

## 问题描述

运行项目后，C盘空间减少了约5GB。本文档帮助您定位和清理这些占用空间的文件。

## 可能占用C盘空间的位置

### 1. Maven本地仓库（最可能的原因）⭐

**位置**：`C:\Users\{您的用户名}\.m2\repository`

**说明**：
- Maven会自动下载项目依赖到本地仓库
- 一个Spring Boot项目可能下载几百MB到几GB的依赖
- 这是**最可能占用5GB空间**的原因

**检查方法**：
```powershell
# 检查Maven仓库大小
Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(GB)";Expression={[math]::Round($_.Sum / 1GB, 2)}}
```

**清理方法**：
```powershell
# 方法1：清理Maven本地仓库（删除未使用的依赖）
# 注意：这会删除所有Maven依赖，下次运行项目需要重新下载
Remove-Item -Path "$env:USERPROFILE\.m2\repository" -Recurse -Force

# 方法2：只清理特定项目的依赖（推荐）
# 找到项目使用的依赖，只删除不需要的
```

**建议**：
- 如果C盘空间紧张，可以将Maven仓库迁移到其他盘
- 编辑 `C:\Users\{您的用户名}\.m2\settings.xml`，添加：
```xml
<settings>
  <localRepository>D:\MavenRepository</localRepository>
</settings>
```

---

### 2. 项目日志文件

**位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\logs\`

**说明**：
- 日志文件配置：
  - `test-design-assistant.log`：最大100MB，保留30天历史
  - `slow-query.log`：最大50MB，保留7天历史
- 如果运行时间较长，可能累积几GB的日志文件

**检查方法**：
```powershell
# 检查日志目录大小
Get-ChildItem -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs" -Recurse | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(MB)";Expression={[math]::Round($_.Sum / 1MB, 2)}}
```

**清理方法**：
```powershell
# 删除日志文件（保留最新的）
Remove-Item -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs\*.log.*" -Force
```

**建议**：
- 定期清理旧日志文件
- 可以修改 `application.yml` 中的日志配置，减少保留天数

---

### 3. 文件上传目录

**位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads\`

**说明**：
- 上传的文件存储在 `./uploads` 目录
- 如果上传了大量文件，可能占用较多空间

**检查方法**：
```powershell
# 检查上传目录大小
if (Test-Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads") {
    Get-ChildItem -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads" -Recurse | 
        Measure-Object -Property Length -Sum | 
        Select-Object @{Name="Size(MB)";Expression={[math]::Round($_.Sum / 1MB, 2)}}
}
```

**清理方法**：
```powershell
# 删除上传的文件（谨慎操作，确认不需要后再删除）
Remove-Item -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads" -Recurse -Force
```

---

### 4. Maven编译产物（target目录）

**位置**：`D:\Demo\test-genius\backend-java\test-design-assistant-core\target\`

**说明**：
- Maven编译后的class文件、jar包等
- 通常占用几百MB到1GB空间
- 可以安全删除，下次编译会重新生成

**检查方法**：
```powershell
# 检查target目录大小
Get-ChildItem -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\target" -Recurse -ErrorAction SilentlyContinue | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(MB)";Expression={[math]::Round($_.Sum / 1MB, 2)}}
```

**清理方法**：
```powershell
# 清理Maven编译产物（推荐）
cd D:\Demo\test-genius\backend-java\test-design-assistant-core
mvn clean

# 或者手动删除
Remove-Item -Path "D:\Demo\test-genius\backend-java\test-design-assistant-core\target" -Recurse -Force
```

---

### 5. 前端node_modules目录

**位置**：`D:\Demo\test-genius\frontend\node_modules\`

**说明**：
- 前端依赖包，通常占用几百MB到1GB
- 可以安全删除，使用 `npm install` 重新安装

**检查方法**：
```powershell
# 检查node_modules目录大小
Get-ChildItem -Path "D:\Demo\test-genius\frontend\node_modules" -Recurse -ErrorAction SilentlyContinue | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(MB)";Expression={[math]::Round($_.Sum / 1MB, 2)}}
```

**清理方法**：
```powershell
# 删除node_modules（可以重新安装）
Remove-Item -Path "D:\Demo\test-genius\frontend\node_modules" -Recurse -Force

# 重新安装依赖
cd D:\Demo\test-genius\frontend
npm install
```

---

### 6. Docker数据（如果使用Docker）

**位置**：
- Docker镜像：`C:\ProgramData\Docker\` 或 `C:\Users\{用户名}\AppData\Local\Docker\`
- Docker数据卷：根据docker-compose.yml配置

**说明**：
- Docker镜像和容器可能占用几GB空间
- 如果使用Docker运行项目，这是可能的原因

**检查方法**：
```powershell
# 检查Docker磁盘使用情况
docker system df

# 检查Docker数据目录大小
Get-ChildItem -Path "C:\ProgramData\Docker" -Recurse -ErrorAction SilentlyContinue | 
    Measure-Object -Property Length -Sum | 
    Select-Object @{Name="Size(GB)";Expression={[math]::Round($_.Sum / 1GB, 2)}}
```

**清理方法**：
```powershell
# 清理未使用的Docker资源
docker system prune -a --volumes

# 清理特定镜像
docker image prune -a
```

---

### 7. Python虚拟环境（如果使用）

**位置**：可能在项目目录或用户目录下的虚拟环境

**说明**：
- Python虚拟环境可能占用几百MB到1GB

**检查方法**：
```powershell
# 查找Python虚拟环境
Get-ChildItem -Path "D:\Demo\test-genius" -Recurse -Directory -Filter "*venv*" -ErrorAction SilentlyContinue
Get-ChildItem -Path "D:\Demo\test-genius" -Recurse -Directory -Filter ".venv" -ErrorAction SilentlyContinue
```

---

## 一键检查脚本

创建一个PowerShell脚本来检查所有可能的位置：

```powershell
# 保存为 check-disk-usage.ps1

Write-Host "=== C盘空间占用检查 ===" -ForegroundColor Green

# 1. 检查Maven仓库
Write-Host "`n1. 检查Maven本地仓库..." -ForegroundColor Yellow
$mavenRepo = "$env:USERPROFILE\.m2\repository"
if (Test-Path $mavenRepo) {
    $size = (Get-ChildItem -Path $mavenRepo -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   Maven仓库大小: $([math]::Round($size / 1GB, 2)) GB" -ForegroundColor Cyan
} else {
    Write-Host "   Maven仓库不存在" -ForegroundColor Gray
}

# 2. 检查项目日志
Write-Host "`n2. 检查项目日志..." -ForegroundColor Yellow
$logPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs"
if (Test-Path $logPath) {
    $size = (Get-ChildItem -Path $logPath -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   日志文件大小: $([math]::Round($size / 1MB, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "   日志目录不存在" -ForegroundColor Gray
}

# 3. 检查文件上传目录
Write-Host "`n3. 检查文件上传目录..." -ForegroundColor Yellow
$uploadPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads"
if (Test-Path $uploadPath) {
    $size = (Get-ChildItem -Path $uploadPath -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   上传文件大小: $([math]::Round($size / 1MB, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "   上传目录不存在" -ForegroundColor Gray
}

# 4. 检查Maven编译产物
Write-Host "`n4. 检查Maven编译产物..." -ForegroundColor Yellow
$targetPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\target"
if (Test-Path $targetPath) {
    $size = (Get-ChildItem -Path $targetPath -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   target目录大小: $([math]::Round($size / 1MB, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "   target目录不存在" -ForegroundColor Gray
}

# 5. 检查前端node_modules
Write-Host "`n5. 检查前端node_modules..." -ForegroundColor Yellow
$nodeModulesPath = "D:\Demo\test-genius\frontend\node_modules"
if (Test-Path $nodeModulesPath) {
    $size = (Get-ChildItem -Path $nodeModulesPath -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   node_modules大小: $([math]::Round($size / 1MB, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "   node_modules不存在" -ForegroundColor Gray
}

# 6. 检查Docker数据
Write-Host "`n6. 检查Docker数据..." -ForegroundColor Yellow
$dockerPath = "C:\ProgramData\Docker"
if (Test-Path $dockerPath) {
    $size = (Get-ChildItem -Path $dockerPath -Recurse -ErrorAction SilentlyContinue | 
        Measure-Object -Property Length -Sum).Sum
    Write-Host "   Docker数据大小: $([math]::Round($size / 1GB, 2)) GB" -ForegroundColor Cyan
} else {
    Write-Host "   Docker数据目录不存在" -ForegroundColor Gray
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Green
```

**使用方法**：
```powershell
# 运行检查脚本
.\check-disk-usage.ps1
```

---

## 推荐清理步骤

### 步骤1：检查空间占用
运行上面的检查脚本，确定哪些位置占用了空间。

### 步骤2：清理编译产物（安全）
```powershell
cd D:\Demo\test-genius\backend-java\test-design-assistant-core
mvn clean
```

### 步骤3：清理日志文件（谨慎）
```powershell
# 只删除旧日志，保留最新的
Remove-Item "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs\*.log.*" -Force
```

### 步骤4：迁移Maven仓库（如果Maven仓库占用大）
1. 在其他盘创建目录：`D:\MavenRepository`
2. 创建或编辑 `C:\Users\{您的用户名}\.m2\settings.xml`：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <localRepository>D:\MavenRepository</localRepository>
</settings>
```
3. 移动现有仓库（可选）：
```powershell
Move-Item "$env:USERPROFILE\.m2\repository" "D:\MavenRepository" -Force
```

### 步骤5：清理Docker（如果使用Docker）
```powershell
docker system prune -a --volumes
```

---

## 预防措施

1. **配置Maven仓库到其他盘**：避免占用C盘空间
2. **定期清理日志**：设置日志保留天数，定期清理
3. **使用Docker时配置数据目录**：将Docker数据目录配置到其他盘
4. **定期运行清理命令**：
   ```powershell
   # 清理Maven编译产物
   mvn clean
   
   # 清理Docker
   docker system prune -a
   ```

---

## 总结

**最可能占用5GB空间的原因**：
1. ⭐ **Maven本地仓库**（`C:\Users\{用户名}\.m2\repository`）- 最可能
2. Docker镜像和数据
3. 项目日志文件（如果运行时间很长）

**建议操作**：
1. 先运行检查脚本确定占用位置
2. 清理Maven编译产物（`mvn clean`）
3. 如果Maven仓库占用大，迁移到其他盘
4. 定期清理日志文件



