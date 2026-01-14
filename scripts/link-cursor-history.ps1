#Requires -Version 5.1
# Cursor 历史对话文件链接脚本
# 用于将已复制到项目目录的 Cursor 历史对话文件链接到系统路径
# 这样 Cursor 就能正确加载项目目录中的对话历史

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

param(
    [string]$WorkspaceId = "",
    [string]$ProjectPath = $PSScriptRoot + "\.."
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cursor 历史对话文件链接脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告: 未以管理员身份运行，符号链接创建可能失败" -ForegroundColor Yellow
    Write-Host "建议: 右键点击 PowerShell，选择'以管理员身份运行'" -ForegroundColor Yellow
    Write-Host ""
}

# 标准化路径
$ProjectPath = Resolve-Path $ProjectPath -ErrorAction SilentlyContinue
if (-not $ProjectPath) {
    $ProjectPath = $PSScriptRoot + "\.."
    $ProjectPath = Resolve-Path $ProjectPath
}

Write-Host "项目路径: $ProjectPath" -ForegroundColor Green

# 查找项目目录中的工作区存储
$projectWorkspaceStorage = Join-Path $ProjectPath ".cursor\workspaceStorage"

if (-not (Test-Path $projectWorkspaceStorage)) {
    Write-Host "错误: 项目目录中未找到 .cursor\workspaceStorage 目录" -ForegroundColor Red
    Write-Host "请确保已将 Cursor 历史对话文件复制到项目目录" -ForegroundColor Yellow
    exit 1
}

# 如果没有提供工作区 ID，尝试从项目目录中查找
if ([string]::IsNullOrEmpty($WorkspaceId)) {
    Write-Host ""
    Write-Host "正在查找项目目录中的工作区 ID..." -ForegroundColor Yellow
    
    $workspaces = Get-ChildItem $projectWorkspaceStorage -Directory | Where-Object {
        $_.Name -match "^[a-f0-9]{32,}$" -or $_.Name.Length -gt 20
    }
    
    if ($workspaces.Count -eq 0) {
        Write-Host "未找到工作区目录" -ForegroundColor Red
        Write-Host ""
        Write-Host "请手动指定工作区 ID:" -ForegroundColor Yellow
        Write-Host "  .\link-cursor-history.ps1 -WorkspaceId '你的工作区ID'" -ForegroundColor Cyan
        exit 1
    }
    
    if ($workspaces.Count -eq 1) {
        $WorkspaceId = $workspaces[0].Name
        Write-Host "找到工作区: $WorkspaceId" -ForegroundColor Green
    } else {
        Write-Host "找到 $($workspaces.Count) 个工作区:" -ForegroundColor Green
        $index = 1
        foreach ($ws in $workspaces) {
            Write-Host "  $index. $($ws.Name)" -ForegroundColor Cyan
            $index++
        }
        
        Write-Host ""
        $selected = Read-Host "请选择工作区编号 (1-$($workspaces.Count))"
        try {
            $selectedIndex = [int]$selected - 1
            if ($selectedIndex -ge 0 -and $selectedIndex -lt $workspaces.Count) {
                $WorkspaceId = $workspaces[$selectedIndex].Name
                Write-Host "已选择工作区: $WorkspaceId" -ForegroundColor Green
            } else {
                Write-Host "无效的选择" -ForegroundColor Red
                exit 1
            }
        } catch {
            Write-Host "无效的输入" -ForegroundColor Red
            exit 1
        }
    }
}

# 设置路径
$projectWorkspacePath = Join-Path $projectWorkspaceStorage $WorkspaceId

# 检查项目目录中的工作区是否存在
if (-not (Test-Path $projectWorkspacePath)) {
    Write-Host "错误: 项目目录中未找到工作区: $projectWorkspacePath" -ForegroundColor Red
    exit 1
}

# 查找 Cursor 系统工作区存储目录
$cursorPaths = @(
    "$env:APPDATA\Cursor\User\workspaceStorage",
    "$env:LOCALAPPDATA\Cursor\User\workspaceStorage"
)

$systemWorkspaceStorage = $null
foreach ($path in $cursorPaths) {
    if (Test-Path $path) {
        $systemWorkspaceStorage = $path
        Write-Host "找到 Cursor 系统工作区存储: $path" -ForegroundColor Green
        break
    }
}

if (-not $systemWorkspaceStorage) {
    Write-Host "警告: 未找到 Cursor 系统工作区存储目录，将尝试创建" -ForegroundColor Yellow
    $systemWorkspaceStorage = "$env:APPDATA\Cursor\User\workspaceStorage"
    $parentDir = Split-Path $systemWorkspaceStorage -Parent
    if (-not (Test-Path $parentDir)) {
        New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
    }
    if (-not (Test-Path $systemWorkspaceStorage)) {
        New-Item -ItemType Directory -Path $systemWorkspaceStorage -Force | Out-Null
    }
}

$systemWorkspacePath = Join-Path $systemWorkspaceStorage $WorkspaceId

Write-Host ""
Write-Host "配置信息:" -ForegroundColor Cyan
Write-Host "  项目工作区路径: $projectWorkspacePath" -ForegroundColor White
Write-Host "  系统工作区路径: $systemWorkspacePath" -ForegroundColor White
Write-Host "  链接方向: 系统路径 -> 项目路径" -ForegroundColor White
Write-Host ""

# 检查系统路径是否已存在
if (Test-Path $systemWorkspacePath) {
    if ((Get-Item $systemWorkspacePath).LinkType -eq "SymbolicLink") {
        Write-Host "符号链接已存在: $systemWorkspacePath" -ForegroundColor Yellow
        $existingTarget = (Get-Item $systemWorkspacePath).Target
        if ($existingTarget -eq $projectWorkspacePath) {
            Write-Host "符号链接指向正确，无需更新" -ForegroundColor Green
            Write-Host ""
            Write-Host "配置完成! Cursor 现在会从项目目录加载对话历史。" -ForegroundColor Green
            exit 0
        } else {
            Write-Host "警告: 符号链接指向不同的路径: $existingTarget" -ForegroundColor Yellow
            $confirm = Read-Host "是否删除并重新创建? (y/n)"
            if ($confirm -ne "y" -and $confirm -ne "Y") {
                exit 0
            }
            Remove-Item $systemWorkspacePath -Force
        }
    } else {
        Write-Host "警告: 系统路径已存在但不是符号链接: $systemWorkspacePath" -ForegroundColor Yellow
        Write-Host "这可能是 Cursor 的原始数据，建议先备份" -ForegroundColor Yellow
        $confirm = Read-Host "是否删除并创建符号链接? (y/n)"
        if ($confirm -ne "y" -and $confirm -ne "Y") {
            exit 0
        }
        # 备份原目录
        $backupPath = "$systemWorkspacePath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
        Write-Host "正在备份到: $backupPath" -ForegroundColor Yellow
        Copy-Item $systemWorkspacePath $backupPath -Recurse -Force
        Remove-Item $systemWorkspacePath -Recurse -Force
    }
}

# 创建符号链接（从系统路径指向项目路径）
try {
    New-Item -ItemType SymbolicLink -Path $systemWorkspacePath -Target $projectWorkspacePath -Force | Out-Null
    Write-Host ""
    Write-Host "符号链接创建成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "配置完成! Cursor 现在会从项目目录加载对话历史。" -ForegroundColor Green
    Write-Host ""
    Write-Host "提示:" -ForegroundColor Cyan
    Write-Host "  - 对话历史保存在: $projectWorkspacePath" -ForegroundColor White
    Write-Host "  - 系统路径已链接到项目路径" -ForegroundColor White
    Write-Host "  - 请重启 Cursor 以使配置生效" -ForegroundColor White
} catch {
    Write-Host ""
    Write-Host "错误: 创建符号链接失败" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的解决方案:" -ForegroundColor Yellow
    Write-Host "  1. 以管理员身份运行此脚本" -ForegroundColor White
    Write-Host "  2. 在 Windows 设置中启用开发者模式" -ForegroundColor White
    Write-Host "  3. 运行以下命令启用符号链接支持:" -ForegroundColor White
    $regPath = "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem"
    $cmd = "New-ItemProperty -Path `"$regPath`" -Name SymlinkEvaluation -Value 1 -PropertyType DWORD -Force"
    Write-Host "     $cmd" -ForegroundColor Gray
    exit 1
}

