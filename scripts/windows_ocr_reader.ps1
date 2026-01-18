# Windows OCR 图片文字识别脚本 (PowerShell版本)
# 使用 Windows 自带的 OCR 功能识别 image 目录下的所有图片

param(
    [string]$ImageDir = ".\image",
    [string]$OutputFile = ".\ocr_results.json"
)

# 检查是否安装了必要的模块
if (-not (Get-Module -ListAvailable -Name "WindowsOCR")) {
    Write-Warning "未检测到 WindowsOCR 模块，尝试使用 Windows.Media.Ocr API"
}

# 获取图片目录的绝对路径
$imagePath = Resolve-Path -Path $ImageDir -ErrorAction Stop
$outputPath = Join-Path (Split-Path -Parent $ImageDir) (Split-Path -Leaf $OutputFile)

Write-Host "Windows OCR 图片识别工具" -ForegroundColor Cyan
Write-Host "=" * 60

# 获取所有图片文件
$imageFiles = Get-ChildItem -Path $imagePath -Include *.png,*.jpg,*.jpeg,*.bmp,*.tiff -Recurse | 
    Sort-Object { [int]($_.BaseName -replace '\D', '') }

Write-Host "找到 $($imageFiles.Count) 个图片文件"
Write-Host "=" * 60

$results = @()

# 遍历每个图片文件
$index = 0
foreach ($imageFile in $imageFiles) {
    $index++
    Write-Host "[$index/$($imageFiles.Count)] 正在识别: $($imageFile.Name)" -ForegroundColor Yellow
    
    try {
        # 尝试使用 Windows.Media.Ocr API
        Add-Type -AssemblyName System.Drawing
        
        # 注意: Windows.Media.Ocr 需要通过 Windows Runtime 访问
        # PowerShell 本身不直接支持 Windows Runtime API
        # 这里提供一个基础的框架，实际 OCR 功能需要通过 Python 脚本实现
        
        $result = @{
            file_path = $imageFile.FullName
            file_name = $imageFile.Name
            text = ""
            words = @()
            word_count = 0
            lines = @()
            line_count = 0
            recognized_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
            note = "PowerShell 版本需要使用 Python 脚本进行实际 OCR 识别"
        }
        
        Write-Host "  ⚠ PowerShell 版本仅提供框架，实际 OCR 识别请使用 Python 脚本" -ForegroundColor Gray
        
    } catch {
        $result = @{
            file_path = $imageFile.FullName
            file_name = $imageFile.Name
            text = ""
            words = @()
            word_count = 0
            lines = @()
            line_count = 0
            error = $_.Exception.Message
            recognized_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        }
        Write-Host "  ✗ 识别失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    $results += $result
}

# 保存结果到 JSON 文件
$results | ConvertTo-Json -Depth 10 | Out-File -FilePath $outputPath -Encoding UTF8

Write-Host "=" * 60
Write-Host "识别完成，结果已保存到: $outputPath" -ForegroundColor Green
Write-Host ""
Write-Host "注意: PowerShell 版本仅提供框架，实际 OCR 识别请使用 Python 脚本:" -ForegroundColor Yellow
Write-Host "  python scripts\windows_ocr_reader.py" -ForegroundColor Yellow

