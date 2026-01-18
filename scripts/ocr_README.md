# Windows OCR 图片识别脚本使用说明

## 概述

本脚本使用 Windows 自带的 OCR（光学字符识别）功能来识别 `image` 目录下的所有图片文件，提取图片中的文字内容。

## 脚本说明

### 1. Python 版本（推荐）

**文件**: `scripts/windows_ocr_reader.py`

这是功能完整的版本，使用 Windows Runtime API 调用 Windows 自带的 OCR 功能。

#### 前置要求

1. **Python 3.7+**
2. **安装 winrt 包**:
   ```powershell
   pip install winrt
   ```
   
   如果安装失败，可以尝试：
   ```powershell
   pip install windows-runtime
   ```

#### 使用方法

1. **基本使用**（识别 `image` 目录下的所有图片）:
   ```powershell
   python scripts\windows_ocr_reader.py
   ```

2. **指定自定义目录**（需要修改脚本中的 `image_dir` 变量）

#### 输出文件

脚本会在项目根目录生成以下文件：

- **`ocr_results.json`**: 详细的识别结果（JSON 格式），包含：
  - 每个文件的识别文字
  - 文字位置信息（坐标、边界框）
  - 行信息和单词信息
  - 统计信息（文字数、行数等）

- **`ocr_results.txt`**: 可读的文本报告，包含所有图片的识别文字

#### 输出格式示例

```json
[
  {
    "file_path": "D:\\Demo\\test-genius\\image\\1.png",
    "file_name": "1.png",
    "text": "识别到的完整文字内容",
    "words": [
      {
        "text": "单词",
        "bounding_rect": {
          "x": 100,
          "y": 50,
          "width": 80,
          "height": 30
        }
      }
    ],
    "word_count": 10,
    "lines": [...],
    "line_count": 3,
    "recognized_at": "2024-01-01T12:00:00"
  }
]
```

### 2. PowerShell 版本（框架）

**文件**: `scripts/windows_ocr_reader.ps1`

这是 PowerShell 版本，仅提供框架。由于 PowerShell 对 Windows Runtime API 的支持有限，实际 OCR 功能需要通过 Python 脚本实现。

## 功能特性

1. **自动识别**所有支持的图片格式（PNG、JPG、JPEG、BMP、TIFF）
2. **批量处理**，按文件名排序
3. **详细输出**，包含文字内容和位置信息
4. **错误处理**，识别失败的文件会记录错误信息
5. **统计信息**，提供识别统计（总文字数、行数、成功率等）

## 支持的语言

Windows OCR 支持多种语言，脚本会自动使用系统配置的语言。常见支持的语言包括：
- 简体中文
- 繁体中文
- 英语
- 日语
- 韩语
- 法语
- 德语
- 等等

## 常见问题

### 1. winrt 包安装失败

如果 `pip install winrt` 失败，可以尝试：

```powershell
pip install windows-runtime
```

或者使用管理员权限运行 PowerShell。

### 2. 识别效果不理想

Windows OCR 的识别效果取决于：
- 图片质量（清晰度、对比度）
- 文字大小和字体
- 图片中文字的复杂程度
- 背景复杂度

建议：
- 使用清晰、高分辨率的图片
- 确保文字与背景对比度足够
- 避免图片倾斜或变形

### 3. 无法识别中文

确保系统语言包中包含中文 OCR 支持：
1. 打开"设置" > "时间和语言" > "语言"
2. 确保已安装中文语言包
3. 在 Windows 10/11 中，OCR 语言与系统语言相关

### 4. 脚本运行缓慢

OCR 识别需要一定时间，特别是：
- 图片较大时
- 图片中包含大量文字时
- 图片数量较多时

这是正常现象，请耐心等待。

## 依赖说明

### Python 依赖

脚本只需要 `winrt` 包（Windows Runtime 支持），不需要其他第三方 OCR 库。

### 系统要求

- **操作系统**: Windows 10 或 Windows 11
- **.NET Framework**: Windows 10/11 自带
- **Python**: 3.7 或更高版本

## 使用示例

### 示例 1: 基本使用

```powershell
# 在项目根目录运行
python scripts\windows_ocr_reader.py
```

### 示例 2: 查看识别结果

```powershell
# 查看 JSON 结果
Get-Content ocr_results.json | ConvertFrom-Json | Select-Object -First 1

# 查看文本报告
Get-Content ocr_results.txt
```

### 示例 3: 在代码中使用

```python
from scripts.windows_ocr_reader import WindowsOCRReader

# 创建 OCR 读取器
reader = WindowsOCRReader()

# 识别单个图片
result = reader.recognize_image("image/1.png")
print(result["text"])

# 识别所有图片
results = reader.recognize_all_images("image")
for result in results:
    print(f"{result['file_name']}: {result['text']}")
```

## 注意事项

1. **首次运行**可能需要下载 Windows OCR 语言包
2. **识别准确性**取决于图片质量，建议使用清晰、高分辨率的图片
3. **处理大量图片**时，建议分批处理
4. **结果保存**：识别结果会自动保存，不会覆盖之前的文件（除非文件已存在）

## 技术支持

如果遇到问题，请检查：
1. Python 版本是否符合要求
2. winrt 包是否正确安装
3. 系统是否支持 OCR 功能
4. 图片文件是否可以正常打开

## 更新日志

- **v1.0.0** (2024-01-01): 初始版本，支持 Windows OCR 图片识别

