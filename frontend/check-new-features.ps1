# 前端新功能检查脚本
# 使用方法：在项目根目录执行 .\frontend\check-new-features.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "前端新功能完整性检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$basePath = Split-Path -Parent $PSScriptRoot
$frontendPath = Join-Path $basePath "frontend"
$viewsPath = Join-Path $frontendPath "src\views"

# 检查页面文件
Write-Host "1. 检查页面文件..." -ForegroundColor Yellow
$pages = @(
    @{Path="test-report\TestReportList.vue"; Name="测试报告"},
    @{Path="test-coverage\TestCoverageAnalysis.vue"; Name="测试覆盖分析"},
    @{Path="test-risk-assessment\TestRiskAssessment.vue"; Name="风险评估"},
    @{Path="test-report-template\TestReportTemplateList.vue"; Name="报告模板"},
    @{Path="test-specification\TestSpecificationList.vue"; Name="测试规约"},
    @{Path="specification-check\SpecificationCheck.vue"; Name="规约检查"},
    @{Path="page-element\PageElementList.vue"; Name="页面元素"},
    @{Path="ui-script-template\UIScriptTemplateList.vue"; Name="UI脚本模板"},
    @{Path="test-case-quality\TestCaseQuality.vue"; Name="用例质量评估"}
)

$allPagesExist = $true
foreach ($page in $pages) {
    $fullPath = Join-Path $viewsPath $page.Path
    if (Test-Path $fullPath) {
        Write-Host "  ✅ $($page.Name) - 文件存在" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($page.Name) - 文件不存在: $fullPath" -ForegroundColor Red
        $allPagesExist = $false
    }
}

Write-Host ""

# 检查路由配置
Write-Host "2. 检查路由配置..." -ForegroundColor Yellow
$routerPath = Join-Path $frontendPath "src\router\index.ts"
if (Test-Path $routerPath) {
    $routerContent = Get-Content $routerPath -Raw -Encoding UTF8
    $routes = @(
        @{Path="/test-report"; Name="测试报告"},
        @{Path="/test-coverage"; Name="测试覆盖分析"},
        @{Path="/test-risk-assessment"; Name="风险评估"},
        @{Path="/test-report-template"; Name="报告模板"},
        @{Path="/test-specification"; Name="测试规约"},
        @{Path="/specification-check"; Name="规约检查"},
        @{Path="/page-element"; Name="页面元素"},
        @{Path="/ui-script-template"; Name="UI脚本模板"},
        @{Path="/test-case-quality"; Name="用例质量评估"}
    )
    
    $allRoutesExist = $true
    foreach ($route in $routes) {
        if ($routerContent -match [regex]::Escape($route.Path)) {
            Write-Host "  ✅ $($route.Name) - 路由已配置: $($route.Path)" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $($route.Name) - 路由未配置: $($route.Path)" -ForegroundColor Red
            $allRoutesExist = $false
        }
    }
} else {
    Write-Host "  ❌ 路由配置文件不存在: $routerPath" -ForegroundColor Red
    $allRoutesExist = $false
}

Write-Host ""

# 检查菜单配置
Write-Host "3. 检查菜单配置..." -ForegroundColor Yellow
$appPath = Join-Path $frontendPath "src\App.vue"
if (Test-Path $appPath) {
    $appContent = Get-Content $appPath -Raw -Encoding UTF8
    
    $menuItems = @(
        @{Index="/test-report"; Name="测试报告"},
        @{Index="/test-coverage"; Name="覆盖率分析"},
        @{Index="/test-risk-assessment"; Name="风险评估"},
        @{Index="/test-report-template"; Name="报告模板"},
        @{Index="/test-specification"; Name="测试规约"},
        @{Index="/specification-check"; Name="规约检查"},
        @{Index="/page-element"; Name="页面元素"},
        @{Index="/ui-script-template"; Name="脚本模板"},
        @{Index="/test-case-quality"; Name="质量评估"}
    )
    
    $allMenuItemsExist = $true
    foreach ($item in $menuItems) {
        if ($appContent -match [regex]::Escape($item.Index)) {
            Write-Host "  ✅ $($item.Name) - 菜单项已配置: $($item.Index)" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $($item.Name) - 菜单项未配置: $($item.Index)" -ForegroundColor Red
            $allMenuItemsExist = $false
        }
    }
    
    # 检查菜单分组
    if ($appContent -match "测试评估") {
        Write-Host "  ✅ 菜单分组'测试评估'已配置" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 菜单分组'测试评估'未配置" -ForegroundColor Red
        $allMenuItemsExist = $false
    }
    
    if ($appContent -match "测试执行") {
        Write-Host "  ✅ 菜单分组'测试执行'已配置" -ForegroundColor Green
    } else {
        Write-Host "  ❌ 菜单分组'测试执行'未配置" -ForegroundColor Red
        $allMenuItemsExist = $false
    }
} else {
    Write-Host "  ❌ App.vue 文件不存在: $appPath" -ForegroundColor Red
    $allMenuItemsExist = $false
}

Write-Host ""

# 检查API文件
Write-Host "4. 检查API文件..." -ForegroundColor Yellow
$apiPath = Join-Path $frontendPath "src\api"
$apiFiles = @(
    @{File="testReport.ts"; Name="测试报告API"},
    @{File="testCoverage.ts"; Name="测试覆盖API"},
    @{File="testRiskAssessment.ts"; Name="风险评估API"},
    @{File="testReportTemplate.ts"; Name="报告模板API"},
    @{File="testSpecification.ts"; Name="测试规约API"},
    @{File="specificationCheck.ts"; Name="规约检查API"},
    @{File="pageElement.ts"; Name="页面元素API"},
    @{File="uiScriptTemplate.ts"; Name="UI脚本模板API"},
    @{File="testCaseQuality.ts"; Name="用例质量评估API"}
)

$allApiFilesExist = $true
foreach ($api in $apiFiles) {
    $fullPath = Join-Path $apiPath $api.File
    if (Test-Path $fullPath) {
        Write-Host "  ✅ $($api.Name) - 文件存在" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($api.Name) - 文件不存在: $fullPath" -ForegroundColor Red
        $allApiFilesExist = $false
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "检查结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($allPagesExist -and $allRoutesExist -and $allMenuItemsExist -and $allApiFilesExist) {
    Write-Host ""
    Write-Host "✅ 所有检查通过！前端新功能配置完整。" -ForegroundColor Green
    Write-Host ""
    Write-Host "如果仍然看不到新功能，请尝试：" -ForegroundColor Yellow
    Write-Host "  1. 清除浏览器缓存（Ctrl+F5）" -ForegroundColor Yellow
    Write-Host "  2. 重启前端服务（npm run dev）" -ForegroundColor Yellow
    Write-Host "  3. 检查菜单是否被折叠" -ForegroundColor Yellow
    Write-Host "  4. 直接访问路由：http://localhost:3000/test-report" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "❌ 发现问题！请检查上述标记为 ❌ 的项目。" -ForegroundColor Red
    Write-Host ""
    Write-Host "建议操作：" -ForegroundColor Yellow
    Write-Host "  1. 检查文件是否存在" -ForegroundColor Yellow
    Write-Host "  2. 检查代码是否正确提交" -ForegroundColor Yellow
    Write-Host "  3. 重新拉取最新代码" -ForegroundColor Yellow
}

Write-Host ""

