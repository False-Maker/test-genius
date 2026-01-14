#Requires -Version 5.1
# Cursor 自动配置脚本
# 用于配置 Cursor 每次打开新对话并自动保存到项目目录
#
# 功能：
# 1. 自动检测或创建项目工作区存储目录
# 2. 创建符号链接，将系统路径指向项目目录
# 3. 确保每次对话都保存到项目的 .cursor 目录

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

param(
    [switch]$Force,
    [string]$ProjectPath = $PSScriptRoot + "\.."
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Cursor 自动配置脚本" -ForegroundColor Cyan
Write-Host "配置对话自动保存到项目目录" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告: 未以管理员身份运行，符号链接创建可能失败" -ForegroundColor Yellow
    Write-Host "建议: 右键点击 PowerShell，选择'以管理员身份运行'" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "是否继续? (y/n)"
    if ($continue -ne "y" -and $continue -ne "Y") {
        exit 0
    }
}

# 标准化路径
$ProjectPath = Resolve-Path $ProjectPath -ErrorAction SilentlyContinue
if (-not $ProjectPath) {
    $ProjectPath = $PSScriptRoot + "\.."
    $ProjectPath = Resolve-Path $ProjectPath
}

Write-Host "项目路径: $ProjectPath" -ForegroundColor Green

# 确保 .cursor 目录存在
$cursorDir = Join-Path $ProjectPath ".cursor"
$workspaceStorageDir = Join-Path $cursorDir "workspaceStorage"

if (-not (Test-Path $cursorDir)) {
    New-Item -ItemType Directory -Path $cursorDir -Force | Out-Null
    Write-Host "已创建 .cursor 目录" -ForegroundColor Green
}

if (-not (Test-Path $workspaceStorageDir)) {
    New-Item -ItemType Directory -Path $workspaceStorageDir -Force | Out-Null
    Write-Host "已创建 workspaceStorage 目录" -ForegroundColor Green
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
    Write-Host "警告: 未找到 Cursor 系统工作区存储目录" -ForegroundColor Yellow
    Write-Host "这可能是首次使用 Cursor，将在首次打开项目时自动创建" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "配置说明:" -ForegroundColor Cyan
    Write-Host "  1. 打开 Cursor 并打开此项目" -ForegroundColor White
    Write-Host "  2. 在 Cursor 中进行一次对话（任意对话即可）" -ForegroundColor White
    Write-Host "  3. 关闭 Cursor" -ForegroundColor White
    Write-Host "  4. 再次运行此脚本完成配置" -ForegroundColor White
    Write-Host ""
    Write-Host "或者，如果你知道工作区 ID，可以手动指定:" -ForegroundColor Yellow
    Write-Host "  .\auto-setup-cursor.ps1 -WorkspaceId '你的工作区ID'" -ForegroundColor Cyan
    exit 0
}

# 查找工作区 ID
Write-Host ""
Write-Host "正在查找工作区 ID..." -ForegroundColor Yellow

# 方法1: 从项目目录中查找（如果已有）
$projectWorkspaces = Get-ChildItem $workspaceStorageDir -Directory -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -match "^[a-f0-9]{32,}$" -or $_.Name.Length -gt 20
}

# 方法2: 从系统目录中查找匹配的项目
$systemWorkspaces = Get-ChildItem $systemWorkspaceStorage -Directory -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -match "^[a-f0-9]{32,}$" -or $_.Name.Length -gt 20
}

$workspaceId = $null

# 优先使用项目目录中已有的工作区 ID
if ($projectWorkspaces.Count -gt 0) {
    if ($projectWorkspaces.Count -eq 1) {
        $workspaceId = $projectWorkspaces[0].Name
        Write-Host "找到项目工作区: $workspaceId" -ForegroundColor Green
    } else {
        Write-Host "找到多个项目工作区:" -ForegroundColor Yellow
        $index = 1
        foreach ($ws in $projectWorkspaces) {
            Write-Host "  $index. $($ws.Name)" -ForegroundColor Cyan
            $index++
        }
        $selected = Read-Host "请选择工作区编号 (1-$($projectWorkspaces.Count))"
        try {
            $selectedIndex = [int]$selected - 1
            if ($selectedIndex -ge 0 -and $selectedIndex -lt $projectWorkspaces.Count) {
                $workspaceId = $projectWorkspaces[$selectedIndex].Name
            }
        } catch {
            Write-Host "无效的输入" -ForegroundColor Red
            exit 1
        }
    }
}

# 如果项目目录中没有，尝试从系统目录中匹配
if (-not $workspaceId -and $systemWorkspaces.Count -gt 0) {
    Write-Host "正在从系统目录中查找匹配的工作区..." -ForegroundColor Yellow
    
    # 尝试通过 workspace.json 匹配项目路径
    foreach ($ws in $systemWorkspaces) {
        $workspaceJson = Join-Path $ws.FullName "workspace.json"
        if (Test-Path $workspaceJson) {
            try {
                $wsData = Get-Content $workspaceJson -Raw | ConvertFrom-Json
                if ($wsData.folder -and $wsData.folder -like "*$($ProjectPath.Replace('\', '/'))*") {
                    $workspaceId = $ws.Name
                    Write-Host "找到匹配的工作区: $workspaceId" -ForegroundColor Green
                    break
                }
            } catch {
                # 忽略解析错误
            }
        }
    }
    
    # 如果还是没找到，让用户选择
    if (-not $workspaceId) {
        Write-Host "找到 $($systemWorkspaces.Count) 个系统工作区:" -ForegroundColor Yellow
        $index = 1
        foreach ($ws in $systemWorkspaces) {
            $wsInfo = ""
            $workspaceJson = Join-Path $ws.FullName "workspace.json"
            if (Test-Path $workspaceJson) {
                try {
                    $wsData = Get-Content $workspaceJson -Raw | ConvertFrom-Json
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
        Write-Host "提示: 如果这是首次配置，请先打开 Cursor 并打开此项目，然后再次运行此脚本" -ForegroundColor Yellow
        $selected = Read-Host "请选择工作区编号 (1-$($systemWorkspaces.Count))，或按 Enter 跳过"
        
        if (-not [string]::IsNullOrWhiteSpace($selected)) {
            try {
                $selectedIndex = [int]$selected - 1
                if ($selectedIndex -ge 0 -and $selectedIndex -lt $systemWorkspaces.Count) {
                    $workspaceId = $systemWorkspaces[$selectedIndex].Name
                }
            } catch {
                Write-Host "无效的输入" -ForegroundColor Red
                exit 1
            }
        }
    }
}

if (-not $workspaceId) {
    Write-Host ""
    Write-Host "未找到工作区 ID" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按以下步骤操作:" -ForegroundColor Yellow
    Write-Host "  1. 打开 Cursor IDE" -ForegroundColor White
    Write-Host "  2. 打开此项目: $ProjectPath" -ForegroundColor White
    Write-Host "  3. 在 Cursor 中进行一次对话（任意对话即可）" -ForegroundColor White
    Write-Host "  4. 关闭 Cursor" -ForegroundColor White
    Write-Host "  5. 再次运行此脚本: .\scripts\auto-setup-cursor.ps1" -ForegroundColor White
    exit 1
}

# 设置路径
$projectWorkspacePath = Join-Path $workspaceStorageDir $workspaceId
$systemWorkspacePath = Join-Path $systemWorkspaceStorage $workspaceId

Write-Host ""
Write-Host "配置信息:" -ForegroundColor Cyan
Write-Host "  工作区 ID: $workspaceId" -ForegroundColor White
Write-Host "  项目工作区路径: $projectWorkspacePath" -ForegroundColor White
Write-Host "  系统工作区路径: $systemWorkspacePath" -ForegroundColor White
Write-Host "  链接方向: 系统路径 -> 项目路径" -ForegroundColor White
Write-Host ""

# 如果系统路径已存在且不是符号链接，需要处理
if (Test-Path $systemWorkspacePath) {
    $item = Get-Item $systemWorkspacePath
    if ($item.LinkType -eq "SymbolicLink") {
        $existingTarget = $item.Target
        if ($existingTarget -eq $projectWorkspacePath) {
            Write-Host "符号链接已存在且配置正确!" -ForegroundColor Green
            Write-Host ""
            Write-Host "配置完成! Cursor 对话已自动保存到项目目录。" -ForegroundColor Green
            exit 0
        } else {
            Write-Host "警告: 符号链接指向不同的路径: $existingTarget" -ForegroundColor Yellow
            if (-not $Force) {
                $confirm = Read-Host "是否删除并重新创建? (y/n)"
                if ($confirm -ne "y" -and $confirm -ne "Y") {
                    exit 0
                }
            }
            Remove-Item $systemWorkspacePath -Force
        }
    } else {
        Write-Host "警告: 系统路径已存在但不是符号链接" -ForegroundColor Yellow
        Write-Host "这可能是 Cursor 的原始数据，需要迁移到项目目录" -ForegroundColor Yellow
        
        if (-not $Force) {
            $confirm = Read-Host "是否迁移到项目目录并创建符号链接? (y/n)"
            if ($confirm -ne "y" -and $confirm -ne "Y") {
                exit 0
            }
        }
        
        # 如果项目目录中已有数据，先备份
        if (Test-Path $projectWorkspacePath) {
            $backupPath = "$projectWorkspacePath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
            Write-Host "正在备份项目目录中的数据到: $backupPath" -ForegroundColor Yellow
            Copy-Item $projectWorkspacePath $backupPath -Recurse -Force
            Remove-Item $projectWorkspacePath -Recurse -Force
        }
        
        # 迁移数据到项目目录
        Write-Host "正在迁移数据到项目目录..." -ForegroundColor Yellow
        Copy-Item $systemWorkspacePath $projectWorkspacePath -Recurse -Force
        
        # 备份系统目录
        $systemBackupPath = "$systemWorkspacePath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
        Write-Host "正在备份系统目录到: $systemBackupPath" -ForegroundColor Yellow
        Copy-Item $systemWorkspacePath $systemBackupPath -Recurse -Force
        
        # 删除系统目录
        Remove-Item $systemWorkspacePath -Recurse -Force
    }
} else {
    # 系统路径不存在，创建项目目录（如果不存在）
    if (-not (Test-Path $projectWorkspacePath)) {
        New-Item -ItemType Directory -Path $projectWorkspacePath -Force | Out-Null
        Write-Host "已创建项目工作区目录" -ForegroundColor Green
    }
}

# 创建符号链接（从系统路径指向项目路径）
try {
    New-Item -ItemType SymbolicLink -Path $systemWorkspacePath -Target $projectWorkspacePath -Force | Out-Null
    Write-Host ""
    Write-Host "符号链接创建成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "配置完成!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "现在 Cursor 的对话会自动保存到项目目录:" -ForegroundColor Cyan
    Write-Host "  $projectWorkspacePath" -ForegroundColor White
    Write-Host ""
    Write-Host "使用说明:" -ForegroundColor Cyan
    Write-Host "  1. 重启 Cursor IDE 以使配置生效" -ForegroundColor White
    Write-Host "  2. 每次打开 Cursor 时，对话会自动保存到项目的 .cursor 目录" -ForegroundColor White
    Write-Host "  3. 对话历史会保留在项目中，不会丢失" -ForegroundColor White
    Write-Host ""
    Write-Host "注意:" -ForegroundColor Yellow
    Write-Host "  - .cursor 目录已被 .gitignore 忽略，不会提交到版本控制" -ForegroundColor White
    Write-Host "  - 如需共享对话历史，请查看 docs/Cursor配置说明.md" -ForegroundColor White
} catch {
    Write-Host ""
    Write-Host "错误: 创建符号链接失败" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的解决方案:" -ForegroundColor Yellow
    Write-Host "  1. 以管理员身份运行此脚本" -ForegroundColor White
    Write-Host "  2. 在 Windows 设置中启用开发者模式:" -ForegroundColor White
    Write-Host "     设置 → 更新和安全 → 开发者选项 → 启用开发者模式" -ForegroundColor Gray
    Write-Host "  3. 运行以下命令启用符号链接支持:" -ForegroundColor White
    $regPath = "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem"
    $cmd = "New-ItemProperty -Path `"$regPath`" -Name SymlinkEvaluation -Value 1 -PropertyType DWORD -Force"
    Write-Host "     $cmd" -ForegroundColor Gray
    Write-Host ""
    Write-Host "或者，你可以手动运行 link-cursor-history.ps1 脚本" -ForegroundColor Yellow
    exit 1
}

