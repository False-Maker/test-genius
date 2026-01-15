# C盘空间占用检查脚本
# 用于检查项目运行后可能占用C盘空间的文件和目录

Write-Host "=== C盘空间占用检查 ===" -ForegroundColor Green
Write-Host "正在检查可能占用C盘空间的位置...`n" -ForegroundColor Yellow

$totalSize = 0
$results = @()

# 1. 检查Maven仓库
Write-Host "1. 检查Maven本地仓库..." -ForegroundColor Yellow
$mavenRepo = "$env:USERPROFILE\.m2\repository"
if (Test-Path $mavenRepo) {
    try {
        $size = (Get-ChildItem -Path $mavenRepo -Recurse -ErrorAction SilentlyContinue | 
            Measure-Object -Property Length -Sum).Sum
        $sizeGB = [math]::Round($size / 1GB, 2)
        $totalSize += $size
        $results += [PSCustomObject]@{
            Location = "Maven本地仓库"
            Path = $mavenRepo
            Size = "$sizeGB GB"
            CanDelete = "否（项目依赖）"
        }
        Write-Host "   ✓ Maven仓库大小: $sizeGB GB" -ForegroundColor Cyan
    } catch {
        Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   - Maven仓库不存在" -ForegroundColor Gray
}

# 2. 检查项目日志
Write-Host "`n2. 检查项目日志..." -ForegroundColor Yellow
$logPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\logs"
if (Test-Path $logPath) {
    try {
        $size = (Get-ChildItem -Path $logPath -Recurse -ErrorAction SilentlyContinue | 
            Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        $totalSize += $size
        $results += [PSCustomObject]@{
            Location = "项目日志文件"
            Path = $logPath
            Size = "$sizeMB MB"
            CanDelete = "是（可清理旧日志）"
        }
        Write-Host "   ✓ 日志文件大小: $sizeMB MB" -ForegroundColor Cyan
    } catch {
        Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   - 日志目录不存在" -ForegroundColor Gray
}

# 3. 检查文件上传目录
Write-Host "`n3. 检查文件上传目录..." -ForegroundColor Yellow
$uploadPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\uploads"
if (Test-Path $uploadPath) {
    try {
        $size = (Get-ChildItem -Path $uploadPath -Recurse -ErrorAction SilentlyContinue | 
            Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        $totalSize += $size
        $results += [PSCustomObject]@{
            Location = "文件上传目录"
            Path = $uploadPath
            Size = "$sizeMB MB"
            CanDelete = "谨慎（确认不需要后）"
        }
        Write-Host "   ✓ 上传文件大小: $sizeMB MB" -ForegroundColor Cyan
    } catch {
        Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   - 上传目录不存在" -ForegroundColor Gray
}

# 4. 检查Maven编译产物
Write-Host "`n4. 检查Maven编译产物..." -ForegroundColor Yellow
$targetPath = "D:\Demo\test-genius\backend-java\test-design-assistant-core\target"
if (Test-Path $targetPath) {
    try {
        $size = (Get-ChildItem -Path $targetPath -Recurse -ErrorAction SilentlyContinue | 
            Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        $totalSize += $size
        $results += [PSCustomObject]@{
            Location = "Maven编译产物"
            Path = $targetPath
            Size = "$sizeMB MB"
            CanDelete = "是（运行 mvn clean）"
        }
        Write-Host "   ✓ target目录大小: $sizeMB MB" -ForegroundColor Cyan
    } catch {
        Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   - target目录不存在" -ForegroundColor Gray
}

# 5. 检查前端node_modules
Write-Host "`n5. 检查前端node_modules..." -ForegroundColor Yellow
$nodeModulesPath = "D:\Demo\test-genius\frontend\node_modules"
if (Test-Path $nodeModulesPath) {
    try {
        $size = (Get-ChildItem -Path $nodeModulesPath -Recurse -ErrorAction SilentlyContinue | 
            Measure-Object -Property Length -Sum).Sum
        $sizeMB = [math]::Round($size / 1MB, 2)
        $totalSize += $size
        $results += [PSCustomObject]@{
            Location = "前端node_modules"
            Path = $nodeModulesPath
            Size = "$sizeMB MB"
            CanDelete = "是（运行 npm install 重新安装）"
        }
        Write-Host "   ✓ node_modules大小: $sizeMB MB" -ForegroundColor Cyan
    } catch {
        Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "   - node_modules不存在" -ForegroundColor Gray
}

# 6. 检查Docker数据（如果存在）
Write-Host "`n6. 检查Docker数据..." -ForegroundColor Yellow
$dockerPaths = @(
    "C:\ProgramData\Docker",
    "$env:USERPROFILE\AppData\Local\Docker"
)

foreach ($dockerPath in $dockerPaths) {
    if (Test-Path $dockerPath) {
        try {
            $size = (Get-ChildItem -Path $dockerPath -Recurse -ErrorAction SilentlyContinue | 
                Measure-Object -Property Length -Sum).Sum
            $sizeGB = [math]::Round($size / 1GB, 2)
            $totalSize += $size
            $results += [PSCustomObject]@{
                Location = "Docker数据"
                Path = $dockerPath
                Size = "$sizeGB GB"
                CanDelete = "是（运行 docker system prune）"
            }
            Write-Host "   ✓ Docker数据大小: $sizeGB GB (路径: $dockerPath)" -ForegroundColor Cyan
        } catch {
            Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
        }
    }
}

if (-not (Test-Path "C:\ProgramData\Docker") -and -not (Test-Path "$env:USERPROFILE\AppData\Local\Docker")) {
    Write-Host "   - Docker数据目录不存在" -ForegroundColor Gray
}

# 7. 检查Python虚拟环境
Write-Host "`n7. 检查Python虚拟环境..." -ForegroundColor Yellow
$venvPaths = @(
    "D:\Demo\test-genius\backend-python\ai-service\venv",
    "D:\Demo\test-genius\backend-python\ai-service\.venv",
    "D:\Demo\test-genius\venv",
    "D:\Demo\test-genius\.venv"
)

$venvFound = $false
foreach ($venvPath in $venvPaths) {
    if (Test-Path $venvPath) {
        try {
            $size = (Get-ChildItem -Path $venvPath -Recurse -ErrorAction SilentlyContinue | 
                Measure-Object -Property Length -Sum).Sum
            $sizeMB = [math]::Round($size / 1MB, 2)
            $totalSize += $size
            $results += [PSCustomObject]@{
                Location = "Python虚拟环境"
                Path = $venvPath
                Size = "$sizeMB MB"
                CanDelete = "是（可重新创建）"
            }
            Write-Host "   ✓ Python虚拟环境大小: $sizeMB MB (路径: $venvPath)" -ForegroundColor Cyan
            $venvFound = $true
        } catch {
            Write-Host "   ✗ 检查失败: $_" -ForegroundColor Red
        }
    }
}

if (-not $venvFound) {
    Write-Host "   - Python虚拟环境不存在" -ForegroundColor Gray
}

# 显示汇总结果
Write-Host "`n=== 检查结果汇总 ===" -ForegroundColor Green
$totalGB = [math]::Round($totalSize / 1GB, 2)
Write-Host "总占用空间: $totalGB GB`n" -ForegroundColor Yellow

if ($results.Count -gt 0) {
    $results | Format-Table -AutoSize
    
    Write-Host "`n=== 清理建议 ===" -ForegroundColor Green
    
    # 找出占用最大的项目
    $maxItem = $results | Sort-Object {[double]($_.Size -replace '[^\d.]')} -Descending | Select-Object -First 1
    if ($maxItem) {
        Write-Host "占用最大的位置: $($maxItem.Location) - $($maxItem.Size)" -ForegroundColor Yellow
        Write-Host "路径: $($maxItem.Path)" -ForegroundColor Cyan
    }
    
    Write-Host "`n推荐清理步骤:" -ForegroundColor Yellow
    Write-Host "1. 清理Maven编译产物: cd backend-java/test-design-assistant-core && mvn clean" -ForegroundColor White
    Write-Host "2. 清理日志文件: 删除 logs 目录下的旧日志文件" -ForegroundColor White
    Write-Host "3. 如果Maven仓库占用大，考虑迁移到其他盘（见文档）" -ForegroundColor White
    Write-Host "4. 如果使用Docker，运行: docker system prune -a" -ForegroundColor White
} else {
    Write-Host "未发现明显的空间占用。" -ForegroundColor Gray
}

Write-Host "`n详细说明请查看: docs/C盘空间占用检查与清理指南.md" -ForegroundColor Cyan



