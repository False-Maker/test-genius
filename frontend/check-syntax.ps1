# 前端语法检查脚本
# 检查 TypeScript 类型错误和 ESLint 语法错误

$ErrorActionPreference = "Stop"
$script:hasErrors = $false

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始前端语法检查..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. TypeScript 类型检查（使用 vue-tsc 检查 Vue + TypeScript）
Write-Host "1. 检查 TypeScript 类型错误..." -ForegroundColor Yellow
try {
    npm run type-check 2>&1 | Tee-Object -Variable typeCheckOutput
    if ($LASTEXITCODE -ne 0) {
        $script:hasErrors = $true
        Write-Host "❌ TypeScript 类型检查失败" -ForegroundColor Red
    } else {
        Write-Host "✅ TypeScript 类型检查通过" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ TypeScript 类型检查执行失败: $_" -ForegroundColor Red
    $script:hasErrors = $true
}
Write-Host ""

# 2. ESLint 语法检查（不自动修复）
Write-Host "2. 检查 ESLint 语法错误..." -ForegroundColor Yellow
try {
    # 使用 ESLint 检查但不自动修复
    npx eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --ignore-path .gitignore 2>&1 | Tee-Object -Variable eslintOutput
    if ($LASTEXITCODE -ne 0) {
        $script:hasErrors = $true
        Write-Host "❌ ESLint 语法检查失败" -ForegroundColor Red
    } else {
        Write-Host "✅ ESLint 语法检查通过" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ ESLint 语法检查执行失败: $_" -ForegroundColor Red
    $script:hasErrors = $true
}
Write-Host ""

# 3. 总结
Write-Host "========================================" -ForegroundColor Cyan
if ($script:hasErrors) {
    Write-Host "❌ 检查完成，发现错误！" -ForegroundColor Red
    exit 1
} else {
    Write-Host "✅ 所有检查通过！" -ForegroundColor Green
    exit 0
}

