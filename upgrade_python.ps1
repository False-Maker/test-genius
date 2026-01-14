# Python 3.10 升级脚本
# 此脚本用于检查并指导升级 Python 到 3.10

Write-Host "=== Python 升级检查 ===" -ForegroundColor Cyan
Write-Host ""

# 检查当前 Python 版本
Write-Host "当前 Python 版本:" -ForegroundColor Yellow
python --version
Write-Host ""

# 检查 Python 路径
Write-Host "当前 Python 路径:" -ForegroundColor Yellow
python -c "import sys; print(sys.executable)"
Write-Host ""

# 检查是否已安装 Python 3.10
Write-Host "检查是否已安装 Python 3.10..." -ForegroundColor Yellow
$python310 = Get-Command python3.10 -ErrorAction SilentlyContinue
if ($python310) {
    Write-Host "✓ 已找到 Python 3.10: $($python310.Source)" -ForegroundColor Green
    python3.10 --version
} else {
    Write-Host "✗ 未找到 Python 3.10" -ForegroundColor Red
    Write-Host ""
    Write-Host "请按照以下步骤升级:" -ForegroundColor Cyan
    Write-Host "1. 访问: https://www.python.org/downloads/release/python-31011/" -ForegroundColor White
    Write-Host "2. 下载 Windows installer (64-bit)" -ForegroundColor White
    Write-Host "3. 运行安装程序，勾选 'Add Python 3.10 to PATH'" -ForegroundColor White
    Write-Host "4. 安装完成后重新运行此脚本验证" -ForegroundColor White
}

Write-Host ""
Write-Host "=== 环境变量检查 ===" -ForegroundColor Cyan
$pathEnv = [Environment]::GetEnvironmentVariable("Path", "User")
$pythonPaths = $pathEnv -split ';' | Where-Object { $_ -like '*Python*' }
if ($pythonPaths) {
    Write-Host "PATH 中的 Python 路径:" -ForegroundColor Yellow
    $pythonPaths | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "未在 PATH 中找到 Python 路径" -ForegroundColor Yellow
}

