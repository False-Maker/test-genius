#Requires -Version 5.1
# Cursor 工作区存储配置脚本
# 用于将 Cursor 的 Agent 对话历史链接到项目目录
# 注意: 请使用 UTF-8 编码保存此文件

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

param(
    [string]$WorkspaceId = "",
    [string]$ProjectPath = $PSScriptRoot + "\.."
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cursor 工作区存储配置脚本" -ForegroundColor Cyan
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

# 查找 Cursor 工作区存储目录
$cursorPaths = @(
    "$env:APPDATA\Cursor\User\workspaceStorage",
    "$env:LOCALAPPDATA\Cursor\User\workspaceStorage"
)

$workspaceStoragePath = $null
foreach ($path in $cursorPaths) {
    if (Test-Path $path) {
        $workspaceStoragePath = $path
        Write-Host "找到 Cursor 工作区存储: $path" -ForegroundColor Green
        break
    }
}

if (-not $workspaceStoragePath) {
    Write-Host "错误: 未找到 Cursor 工作区存储目录" -ForegroundColor Red
    Write-Host "请确保已安装并运行过 Cursor IDE" -ForegroundColor Yellow
    exit 1
}

# 如果没有提供工作区 ID，尝试查找
if ([string]::IsNullOrEmpty($WorkspaceId)) {
    Write-Host ""
    Write-Host "正在查找工作区 ID..." -ForegroundColor Yellow
    
    # 列出所有工作区
    $workspaces = Get-ChildItem $workspaceStoragePath -Directory | Where-Object {
        $_.Name -match "^[a-f0-9]{32,}$" -or $_.Name.Length -gt 20
    }
    
    if ($workspaces.Count -eq 0) {
        Write-Host "未找到工作区目录" -ForegroundColor Red
        Write-Host ""
        Write-Host "请手动指定工作区 ID:" -ForegroundColor Yellow
        Write-Host "  .\setup-cursor-workspace.ps1 -WorkspaceId '你的工作区ID'" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "如何查找工作区 ID:" -ForegroundColor Yellow
        Write-Host "  1. 在 Cursor 中打开项目" -ForegroundColor White
        Write-Host "  2. 打开命令面板 (Ctrl+Shift+P)" -ForegroundColor White
        Write-Host "  3. 输入 'Workspace: Show Workspace Storage'" -ForegroundColor White
        Write-Host "  4. 从路径中提取工作区 ID" -ForegroundColor White
        exit 1
    }
    
    Write-Host "找到 $($workspaces.Count) 个工作区:" -ForegroundColor Green
    $index = 1
    foreach ($ws in $workspaces) {
        $wsInfo = ""
        $stateFile = Join-Path $ws.FullName "workspace.json"
        if (Test-Path $stateFile) {
            try {
                $wsData = Get-Content $stateFile | ConvertFrom-Json
                if ($wsData.folder) {
                    $wsInfo = " -> $($wsData.folder)"
                }
            } catch {
                # 忽略解析错误
            }
        }
        Write-Host "  $index. $($ws.Name)$wsInfo" -ForegroundColor Cyan
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

# 设置路径
$sourcePath = Join-Path $workspaceStoragePath $WorkspaceId
$targetDir = Join-Path $ProjectPath ".cursor\workspaceStorage"
$targetPath = Join-Path $targetDir $WorkspaceId

Write-Host ""
Write-Host "配置信息:" -ForegroundColor Cyan
Write-Host "  源路径: $sourcePath" -ForegroundColor White
Write-Host "  目标路径: $targetPath" -ForegroundColor White
Write-Host ""

# 检查源路径是否存在
if (-not (Test-Path $sourcePath)) {
    Write-Host "错误: 源路径不存在: $sourcePath" -ForegroundColor Red
    exit 1
}

# 创建目标目录
if (-not (Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    Write-Host "已创建目标目录: $targetDir" -ForegroundColor Green
}

# 检查目标路径是否已存在
if (Test-Path $targetPath) {
    if ((Get-Item $targetPath).LinkType -eq "SymbolicLink") {
        Write-Host "符号链接已存在: $targetPath" -ForegroundColor Yellow
        $existingTarget = (Get-Item $targetPath).Target
        if ($existingTarget -eq $sourcePath) {
            Write-Host "符号链接指向正确，无需更新" -ForegroundColor Green
            exit 0
        } else {
            Write-Host "警告: 符号链接指向不同的路径: $existingTarget" -ForegroundColor Yellow
            $confirm = Read-Host "是否删除并重新创建? (y/n)"
            if ($confirm -ne "y" -and $confirm -ne "Y") {
                exit 0
            }
            Remove-Item $targetPath -Force
        }
    } else {
        Write-Host "警告: 目标路径已存在但不是符号链接: $targetPath" -ForegroundColor Yellow
        $confirm = Read-Host "是否删除并创建符号链接? (y/n)"
        if ($confirm -ne "y" -and $confirm -ne "Y") {
            exit 0
        }
        Remove-Item $targetPath -Recurse -Force
    }
}

# 创建符号链接
try {
    New-Item -ItemType SymbolicLink -Path $targetPath -Target $sourcePath -Force | Out-Null
    Write-Host ""
    Write-Host "符号链接创建成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "配置完成! Cursor 的 Agent 对话历史现在会保存到项目目录中。" -ForegroundColor Green
    Write-Host ""
    Write-Host "提示:" -ForegroundColor Cyan
    Write-Host "  - 对话历史保存在: $targetPath" -ForegroundColor White
    Write-Host "  - 此目录已被 .gitignore 忽略，不会提交到版本控制" -ForegroundColor White
    Write-Host "  - 如需共享对话历史，请查看 docs/Cursor配置说明.md" -ForegroundColor White
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
