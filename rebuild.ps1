# Docker服务重建脚本
# 用途：快速重建Docker服务，不使用缓存
# 使用方法：
#   .\rebuild.ps1 -Service backend-java    # 重建Java后端
#   .\rebuild.ps1 -Service backend-python  # 重建Python服务
#   .\rebuild.ps1 -Service frontend        # 重建前端
#   .\rebuild.ps1                          # 重建所有服务

param(
    [Parameter(Mandatory=$false)]
    [string]$Service = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$NoCache = $true
)

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Green "================================================"
Write-ColorOutput Green "        Docker服务重建脚本"
Write-ColorOutput Green "================================================"
Write-Output ""

if ($Service) {
    Write-ColorOutput Yellow "目标服务: $Service"
} else {
    Write-ColorOutput Yellow "目标服务: 所有服务"
}

if ($NoCache) {
    Write-ColorOutput Yellow "构建选项: --no-cache (不使用缓存)"
} else {
    Write-ColorOutput Yellow "构建选项: 使用缓存"
}

Write-Output ""

# 确认操作
$confirmation = Read-Host "是否继续？(Y/N)"
if ($confirmation -ne 'Y' -and $confirmation -ne 'y') {
    Write-ColorOutput Red "操作已取消"
    exit 1
}

Write-Output ""

# 执行重建
try {
    if ($Service) {
        Write-ColorOutput Cyan "[1/2] 正在重建服务: $Service..."
        if ($NoCache) {
            docker compose build --no-cache $Service
        } else {
            docker compose build $Service
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "构建失败！"
            exit 1
        }
        
        Write-ColorOutput Cyan "[2/2] 正在启动服务: $Service..."
        docker compose up -d $Service
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "启动失败！"
            exit 1
        }
        
        Write-Output ""
        Write-ColorOutput Green "服务 $Service 重建并启动成功！"
    } else {
        Write-ColorOutput Cyan "[1/2] 正在重建所有服务..."
        if ($NoCache) {
            docker compose build --no-cache
        } else {
            docker compose build
        }
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "构建失败！"
            exit 1
        }
        
        Write-ColorOutput Cyan "[2/2] 正在启动所有服务..."
        docker compose up -d
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "启动失败！"
            exit 1
        }
        
        Write-Output ""
        Write-ColorOutput Green "所有服务重建并启动成功！"
    }
    
    Write-Output ""
    Write-ColorOutput Cyan "查看服务状态:"
    docker compose ps
    
} catch {
    Write-ColorOutput Red "发生错误: $_"
    exit 1
}

Write-Output ""
Write-ColorOutput Green "================================================"
Write-ColorOutput Green "              重建完成！"
Write-ColorOutput Green "================================================"

