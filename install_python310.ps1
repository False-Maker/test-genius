# Python 3.10 自动安装脚本
# 需要以管理员权限运行

$ErrorActionPreference = "Stop"

Write-Host "=== Python 3.10 自动安装脚本 ===" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告: 建议以管理员权限运行此脚本以确保正确配置环境变量" -ForegroundColor Yellow
    Write-Host ""
}

# Python 3.10.11 下载信息
$pythonVersion = "3.10.11"
$pythonUrl = "https://www.python.org/ftp/python/$pythonVersion/python-$pythonVersion-amd64.exe"
$installerName = "python-$pythonVersion-amd64.exe"
$installPath = "D:\Develop\Python\Python310"

Write-Host "目标版本: Python $pythonVersion" -ForegroundColor Green
Write-Host "安装路径: $installPath" -ForegroundColor Green
Write-Host ""

# 检查是否已安装
Write-Host "检查当前 Python 版本..." -ForegroundColor Yellow
try {
    $currentVersion = python --version 2>&1
    Write-Host "当前版本: $currentVersion" -ForegroundColor Yellow
} catch {
    Write-Host "未检测到 Python" -ForegroundColor Yellow
}
Write-Host ""

# 下载安装程序
Write-Host "步骤 1: 下载 Python $pythonVersion 安装程序..." -ForegroundColor Cyan
if (Test-Path $installerName) {
    Write-Host "安装程序已存在: $installerName" -ForegroundColor Green
} else {
    Write-Host "正在从官方源下载..." -ForegroundColor Yellow
    try {
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $pythonUrl -OutFile $installerName -UseBasicParsing
        if (Test-Path $installerName) {
            $fileSize = (Get-Item $installerName).Length / 1MB
            Write-Host "下载完成: $installerName ($([math]::Round($fileSize, 2)) MB)" -ForegroundColor Green
        } else {
            throw "下载失败"
        }
    } catch {
        Write-Host "自动下载失败，请手动下载:" -ForegroundColor Red
        Write-Host "  URL: $pythonUrl" -ForegroundColor White
        Write-Host "  或访问: https://www.python.org/downloads/release/python-31011/" -ForegroundColor White
        exit 1
    }
}
Write-Host ""

# 安装 Python
Write-Host "步骤 2: 安装 Python $pythonVersion..." -ForegroundColor Cyan
Write-Host "请在弹出的安装窗口中:" -ForegroundColor Yellow
Write-Host "  1. 勾选 'Add Python $pythonVersion to PATH'" -ForegroundColor White
Write-Host "  2. 选择 'Install Now' 或自定义安装路径为: $installPath" -ForegroundColor White
Write-Host "  3. 等待安装完成" -ForegroundColor White
Write-Host ""

# 启动安装程序（静默安装，自动添加到 PATH）
$installArgs = "/quiet InstallAllUsers=1 PrependPath=1 Include_test=0"
Write-Host "正在启动安装程序..." -ForegroundColor Yellow
Start-Process -FilePath $installerName -ArgumentList $installArgs -Wait

Write-Host ""
Write-Host "步骤 3: 验证安装..." -ForegroundColor Cyan

# 刷新环境变量
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# 等待几秒让系统更新
Start-Sleep -Seconds 3

# 检查安装
try {
    $newVersion = python --version 2>&1
    if ($newVersion -like "*3.10*") {
        Write-Host "✓ 安装成功!" -ForegroundColor Green
        Write-Host "  版本: $newVersion" -ForegroundColor Green
        
        $pythonPath = python -c "import sys; print(sys.executable)" 2>&1
        Write-Host "  路径: $pythonPath" -ForegroundColor Green
        
        Write-Host ""
        Write-Host "步骤 4: 升级 pip..." -ForegroundColor Cyan
        python -m pip install --upgrade pip --quiet
        Write-Host "✓ pip 升级完成" -ForegroundColor Green
    } else {
        Write-Host "⚠ 检测到版本: $newVersion" -ForegroundColor Yellow
        Write-Host "  如果版本不正确，请检查环境变量 PATH 配置" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ 无法验证安装，请手动检查:" -ForegroundColor Yellow
    Write-Host "  1. 重新打开 PowerShell" -ForegroundColor White
    Write-Host "  2. 运行: python --version" -ForegroundColor White
    Write-Host "  3. 如果仍显示旧版本，请检查环境变量 PATH" -ForegroundColor White
}

Write-Host ""
Write-Host "=== 安装完成 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示:" -ForegroundColor Yellow
Write-Host "  - 如果版本未更新，请重新打开 PowerShell 终端" -ForegroundColor White
Write-Host "  - 如需卸载旧版本，可在'控制面板' → '程序和功能'中卸载" -ForegroundColor White
Write-Host "  - 详细说明请查看: docs/Python升级指南.md" -ForegroundColor White

