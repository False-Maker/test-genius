# Docker 和 Docker Compose 安装脚本
# 安装路径: D:\Develop\
# Docker version: 20.x.x
# Docker Compose version: v2.x.x

$ErrorActionPreference = "Stop"

# 配置变量
$InstallDir = "D:\Develop"
$DockerDir = Join-Path $InstallDir "Docker"
$DockerComposeDir = Join-Path $InstallDir "Docker\docker-compose"
$DownloadDir = Join-Path $env:TEMP "docker-install"
$DockerDesktopUrl = "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe"
$DockerDesktopInstaller = Join-Path $DownloadDir "DockerDesktopInstaller.exe"

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Docker 和 Docker Compose 安装脚本" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "错误: 需要管理员权限来安装 Docker" -ForegroundColor Red
    Write-Host "请以管理员身份运行 PowerShell，然后重新执行此脚本" -ForegroundColor Yellow
    exit 1
}

# 创建目录
Write-Host "[1/8] 创建安装目录..." -ForegroundColor Green
if (-not (Test-Path $InstallDir)) {
    New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null
}
if (-not (Test-Path $DockerDir)) {
    New-Item -ItemType Directory -Path $DockerDir -Force | Out-Null
}
if (-not (Test-Path $DownloadDir)) {
    New-Item -ItemType Directory -Path $DownloadDir -Force | Out-Null
}
Write-Host "✓ 目录创建完成" -ForegroundColor Green
Write-Host ""

# 检查 WSL2
Write-Host "[2/8] 检查 WSL2 支持..." -ForegroundColor Green
$wslVersion = wsl --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "警告: 未检测到 WSL2，Docker Desktop 需要 WSL2 支持" -ForegroundColor Yellow
    Write-Host "是否启用 WSL2? (Y/N)" -ForegroundColor Yellow
    $response = Read-Host
    if ($response -eq "Y" -or $response -eq "y") {
        Write-Host "正在启用 WSL2..." -ForegroundColor Yellow
        dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
        dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
        Write-Host "✓ WSL2 功能已启用，请重启系统后继续安装" -ForegroundColor Green
        Write-Host "重启后，请再次运行此脚本" -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host "✓ WSL2 已安装" -ForegroundColor Green
}
Write-Host ""

# 下载 Docker Desktop 安装包
Write-Host "[3/8] 下载 Docker Desktop 安装包..." -ForegroundColor Green
if (-not (Test-Path $DockerDesktopInstaller)) {
    try {
        Write-Host "正在下载，请稍候..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $DockerDesktopUrl -OutFile $DockerDesktopInstaller -UseBasicParsing
        Write-Host "✓ 下载完成" -ForegroundColor Green
    } catch {
        Write-Host "错误: 下载失败 - $_" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "✓ 安装包已存在，跳过下载" -ForegroundColor Green
}
Write-Host ""

# 检查是否已安装 Docker Desktop
Write-Host "[4/8] 检查现有安装..." -ForegroundColor Green
$dockerPath = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"
$dockerDesktopInstalled = Test-Path $dockerPath
if ($dockerDesktopInstalled) {
    Write-Host "检测到已安装的 Docker Desktop，正在卸载..." -ForegroundColor Yellow
    $dockerDesktopUninstall = "C:\Program Files\Docker\Docker\Uninstall.exe"
    if (Test-Path $dockerDesktopUninstall) {
        Start-Process -FilePath $dockerDesktopUninstall -ArgumentList "/S" -Wait -NoNewWindow
        Write-Host "✓ 旧版本已卸载" -ForegroundColor Green
        Start-Sleep -Seconds 5
    }
}
Write-Host ""

# 安装 Docker Desktop
Write-Host "[5/8] 安装 Docker Desktop..." -ForegroundColor Green
Write-Host "注意: 安装过程可能需要几分钟，请耐心等待..." -ForegroundColor Yellow
try {
    # 静默安装 Docker Desktop
    # 注意: Docker Desktop 安装程序不支持自定义安装路径，它会安装到默认位置
    # 但我们可以通过安装后的配置来设置数据目录
    $installArgs = "install --quiet --accept-license --wsl-default-version=2"
    
    Write-Host "正在安装，请稍候..." -ForegroundColor Yellow
    $process = Start-Process -FilePath $DockerDesktopInstaller -ArgumentList $installArgs -Wait -PassThru -NoNewWindow
    
    if ($process.ExitCode -ne 0 -and $process.ExitCode -ne 3010) {
        Write-Host "警告: 安装程序退出代码: $($process.ExitCode)" -ForegroundColor Yellow
        Write-Host "这可能是正常的（3010 表示需要重启）" -ForegroundColor Yellow
    }
    
    Write-Host "✓ Docker Desktop 安装完成" -ForegroundColor Green
} catch {
    Write-Host "错误: 安装失败 - $_" -ForegroundColor Red
    Write-Host "请手动运行安装程序: $DockerDesktopInstaller" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 等待 Docker Desktop 服务启动
Write-Host "[6/8] 等待 Docker Desktop 启动..." -ForegroundColor Green
Write-Host "等待 30 秒以便 Docker Desktop 完全启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host ""

# 配置环境变量
Write-Host "[7/8] 配置环境变量..." -ForegroundColor Green

# 获取 Docker 实际安装路径
$dockerBinPath = "C:\Program Files\Docker\Docker\resources\bin"
$dockerCliPath = "C:\Program Files\Docker\Docker\resources\cli-plugins"

# 添加到系统 PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$pathsToAdd = @($dockerBinPath, $dockerCliPath)

foreach ($path in $pathsToAdd) {
    if ($currentPath -notlike "*$path*") {
        [Environment]::SetEnvironmentVariable("Path", $currentPath + ";$path", "Machine")
        Write-Host "✓ 已添加路径到环境变量: $path" -ForegroundColor Green
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    } else {
        Write-Host "✓ 路径已存在于环境变量: $path" -ForegroundColor Green
    }
}

# 设置 DOCKER_HOST 环境变量（如果需要）
[Environment]::SetEnvironmentVariable("DOCKER_HOST", "npipe:////./pipe/docker_engine", "Machine")
Write-Host "✓ 已设置 DOCKER_HOST 环境变量" -ForegroundColor Green

# 刷新当前会话的环境变量
$env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")

Write-Host ""

# 验证安装
Write-Host "[8/8] 验证安装..." -ForegroundColor Green

# 等待一下确保环境变量生效
Start-Sleep -Seconds 5

# 检查 Docker 版本
$dockerVersionOutput = docker --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker 安装成功" -ForegroundColor Green
    Write-Host "  $dockerVersionOutput" -ForegroundColor Cyan
} else {
    Write-Host "⚠ Docker 命令不可用，可能需要重新启动 PowerShell 或重启系统" -ForegroundColor Yellow
    Write-Host "  请重新打开 PowerShell 并运行: docker --version" -ForegroundColor Yellow
}

# 检查 Docker Compose 版本
$dockerComposeVersionOutput = docker compose version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker Compose 安装成功" -ForegroundColor Green
    Write-Host "  $dockerComposeVersionOutput" -ForegroundColor Cyan
} else {
    $dockerComposeVersionOutput = docker-compose --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Docker Compose (v1) 安装成功" -ForegroundColor Green
        Write-Host "  $dockerComposeVersionOutput" -ForegroundColor Cyan
    } else {
        Write-Host "⚠ Docker Compose 命令不可用，可能需要重新启动 PowerShell 或重启系统" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "安装完成！" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "重要提示:" -ForegroundColor Yellow
Write-Host "1. 请重新启动 PowerShell 或重启系统以使环境变量生效" -ForegroundColor Yellow
Write-Host "2. 启动 Docker Desktop 应用程序" -ForegroundColor Yellow
Write-Host "3. 运行以下命令验证安装:" -ForegroundColor Yellow
Write-Host "   docker --version" -ForegroundColor Cyan
Write-Host "   docker compose version" -ForegroundColor Cyan
Write-Host ""
Write-Host "注意: Docker Desktop 默认安装到 C:\Program Files\Docker\" -ForegroundColor Yellow
Write-Host "如需更改数据存储位置，请在 Docker Desktop 设置中配置" -ForegroundColor Yellow
Write-Host ""


