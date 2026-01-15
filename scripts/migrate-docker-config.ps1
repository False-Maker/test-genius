# Docker 配置迁移脚本
# 将 Docker 配置从 C 盘迁移到 D 盘

Write-Host "=== Docker 配置迁移脚本 ===" -ForegroundColor Green
Write-Host ""

# 检查是否以管理员身份运行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告: 建议以管理员身份运行此脚本以确保符号链接创建成功" -ForegroundColor Yellow
    Write-Host ""
}

# 源路径和目标路径
$sourcePath = "$env:USERPROFILE\.docker"
$targetPath = "D:\Docker\.docker"

# 检查源路径是否存在
if (-not (Test-Path $sourcePath)) {
    Write-Host "源路径不存在: $sourcePath" -ForegroundColor Yellow
    Write-Host "可能 Docker 配置已经在其他位置或尚未创建" -ForegroundColor Yellow
    exit 0
}

# 检查目标路径
if (Test-Path $targetPath) {
    Write-Host "目标路径已存在: $targetPath" -ForegroundColor Yellow
    $response = Read-Host "是否覆盖? (y/n)"
    if ($response -ne "y") {
        Write-Host "取消迁移" -ForegroundColor Red
        exit 0
    }
    Remove-Item $targetPath -Recurse -Force
}

# 创建目标目录
Write-Host "创建目标目录: $targetPath" -ForegroundColor Cyan
New-Item -ItemType Directory -Path $targetPath -Force | Out-Null

# 复制文件
Write-Host "复制文件从 $sourcePath 到 $targetPath ..." -ForegroundColor Cyan
Copy-Item -Path "$sourcePath\*" -Destination $targetPath -Recurse -Force

# 验证复制
$sourceSize = (Get-ChildItem $sourcePath -Recurse -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
$targetSize = (Get-ChildItem $targetPath -Recurse -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum

if ($sourceSize -eq $targetSize) {
    Write-Host "文件复制成功!" -ForegroundColor Green
    Write-Host "源大小: $([math]::Round($sourceSize/1MB, 2)) MB" -ForegroundColor White
    Write-Host "目标大小: $([math]::Round($targetSize/1MB, 2)) MB" -ForegroundColor White
    
    # 备份原目录
    $backupPath = "$sourcePath.backup"
    Write-Host "备份原目录到: $backupPath" -ForegroundColor Cyan
    Rename-Item -Path $sourcePath -NewName $backupPath -Force
    
    # 创建符号链接
    Write-Host "创建符号链接: $sourcePath -> $targetPath" -ForegroundColor Cyan
    try {
        New-Item -ItemType SymbolicLink -Path $sourcePath -Target $targetPath -Force | Out-Null
        Write-Host "符号链接创建成功!" -ForegroundColor Green
        Write-Host ""
        Write-Host "迁移完成! Docker 配置现在存储在 D 盘" -ForegroundColor Green
        Write-Host "如果一切正常，可以删除备份目录: $backupPath" -ForegroundColor Yellow
    } catch {
        Write-Host "符号链接创建失败: $_" -ForegroundColor Red
        Write-Host "恢复原目录..." -ForegroundColor Yellow
        Rename-Item -Path $backupPath -NewName $sourcePath -Force
        Write-Host "已恢复原目录" -ForegroundColor Yellow
    }
} else {
    Write-Host "文件复制验证失败!" -ForegroundColor Red
    Write-Host "源大小: $([math]::Round($sourceSize/1MB, 2)) MB" -ForegroundColor White
    Write-Host "目标大小: $([math]::Round($targetSize/1MB, 2)) MB" -ForegroundColor White
    Write-Host "请手动检查" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

