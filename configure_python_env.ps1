# Python 3.10 环境变量配置脚本
# 需要以管理员权限运行

Write-Host "=== Python 3.10 环境变量配置 ===" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告: 需要管理员权限来修改系统环境变量" -ForegroundColor Yellow
    Write-Host "将以用户环境变量方式配置..." -ForegroundColor Yellow
    Write-Host ""
}

# Python 3.10 路径
$python310Path = "D:\Develop\Python"
$python310Scripts = "D:\Develop\Python\Scripts"

# 验证路径存在
if (-not (Test-Path $python310Path)) {
    Write-Host "错误: Python 3.10 路径不存在: $python310Path" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "$python310Path\python.exe")) {
    Write-Host "错误: 未找到 python.exe: $python310Path\python.exe" -ForegroundColor Red
    exit 1
}

Write-Host "Python 3.10 路径: $python310Path" -ForegroundColor Green
Write-Host "Scripts 路径: $python310Scripts" -ForegroundColor Green
Write-Host ""

# 获取当前 PATH
$scope = if ($isAdmin) { "Machine" } else { "User" }
$currentPath = [Environment]::GetEnvironmentVariable("Path", $scope)

Write-Host "当前 PATH 中的 Python 路径:" -ForegroundColor Yellow
$pythonPaths = $currentPath -split ';' | Where-Object { $_ -like '*Python*' -and $_ -ne '' }
if ($pythonPaths) {
    $pythonPaths | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "  (无)" -ForegroundColor Gray
}
Write-Host ""

# 移除旧的 Python 路径（如果存在）
$pathArray = $currentPath -split ';' | Where-Object { 
    $_ -ne '' -and 
    $_ -notlike '*Python38*' -and 
    $_ -ne $python310Path -and 
    $_ -ne $python310Scripts 
}

# 添加 Python 3.10 路径到最前面（优先级最高）
$newPathArray = @($python310Path, $python310Scripts) + $pathArray
$newPath = $newPathArray -join ';'

# 更新环境变量
Write-Host "正在更新环境变量..." -ForegroundColor Cyan
try {
    [Environment]::SetEnvironmentVariable("Path", $newPath, $scope)
    Write-Host "✓ 环境变量已更新 ($scope 级别)" -ForegroundColor Green
} catch {
    Write-Host "✗ 更新失败: $_" -ForegroundColor Red
    exit 1
}

# 刷新当前会话的 PATH
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

Write-Host ""
Write-Host "验证配置..." -ForegroundColor Cyan

# 等待一下让系统更新
Start-Sleep -Seconds 2

# 验证
try {
    $version = python --version 2>&1
    if ($version -like "*3.10*") {
        Write-Host "✓ 配置成功!" -ForegroundColor Green
        Write-Host "  Python 版本: $version" -ForegroundColor Green
        
        $pythonExe = python -c "import sys; print(sys.executable)" 2>&1
        Write-Host "  Python 路径: $pythonExe" -ForegroundColor Green
        
        Write-Host ""
        Write-Host "新的 PATH 中的 Python 路径:" -ForegroundColor Yellow
        $newPythonPaths = $env:Path -split ';' | Where-Object { $_ -like '*Python*' -and $_ -ne '' } | Select-Object -First 5
        $newPythonPaths | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    } else {
        Write-Host "⚠ 检测到版本: $version" -ForegroundColor Yellow
        Write-Host "  可能需要重新打开 PowerShell 终端" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ 无法验证，请重新打开 PowerShell 后运行: python --version" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== 配置完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示:" -ForegroundColor Yellow
Write-Host "  - 请重新打开 PowerShell 终端以确保环境变量生效" -ForegroundColor White
Write-Host "  - 运行 'python --version' 应该显示 Python 3.10.11" -ForegroundColor White
Write-Host "  - 运行 'pip --version' 应该显示 pip (python 3.10)" -ForegroundColor White

