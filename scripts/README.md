# 文档完整性检查工具使用说明

## 概述

本目录包含用于检查代码文档完整性的工具脚本，支持Java和Python代码的文档检查。

## 工具列表

### 1. setup-cursor-workspace.ps1

Cursor IDE 工作区存储配置脚本，用于将 Cursor 的 Agent 对话历史链接到项目目录。

**使用方法**：
```powershell
# 在项目根目录执行（需要管理员权限）
.\scripts\setup-cursor-workspace.ps1

# 或指定工作区 ID
.\scripts\setup-cursor-workspace.ps1 -WorkspaceId "你的工作区ID"
```

**功能**：
- 自动查找 Cursor 工作区存储目录
- 列出所有可用的工作区供选择
- 创建符号链接将对话历史保存到项目目录
- 验证配置是否正确

**要求**：
- Windows PowerShell（管理员权限）
- 已安装并运行过 Cursor IDE

**详细说明**：请参考 [Cursor配置说明.md](../docs/Cursor配置说明.md)

### 2. check-docs.py（推荐）

统一的Python检查工具，支持Java和Python代码检查。

**使用方法**：
```bash
# 在项目根目录执行
python scripts/check-docs.py
```

**功能**：
- 检查Java类的JavaDoc注释
- 检查Java方法的JavaDoc注释
- 检查Python模块的Docstring
- 检查Python类和函数的Docstring
- 生成详细的统计报告

**输出示例**：
```
==================================================
文档完整性检查工具
==================================================

==================================================
检查Java代码...
==================================================

类文档:
  总数: 45
  缺少文档: 5
  文档覆盖率: 88.9%
  详情:
    ⚠️  类缺少文档: backend-java/.../Example.java (行 10)

方法文档:
  总数: 120
  缺少文档: 15
  文档覆盖率: 87.5%
```

### 2. check-java-docs.sh

Java代码文档检查脚本（Bash版本）。

**使用方法**：
```bash
bash scripts/check-java-docs.sh
```

**功能**：
- 检查Java类的JavaDoc注释
- 检查Java公共方法的JavaDoc注释
- 自动排除测试文件

**要求**：
- 需要bash环境（Linux/macOS/Git Bash）
- 需要find和grep命令

### 3. check-python-docs.sh

Python代码文档检查脚本（Bash版本）。

**使用方法**：
```bash
bash scripts/check-python-docs.sh
```

**功能**：
- 检查Python模块的Docstring
- 检查Python类的Docstring
- 检查Python函数的Docstring
- 自动排除测试文件和__init__.py

**要求**：
- 需要bash环境（Linux/macOS/Git Bash）
- 需要find和grep命令

## 检查标准

### Java代码

**类文档要求**：
- 必须有JavaDoc注释（`/** ... */`）
- 必须包含`@author`标签
- 必须包含`@date`标签

**方法文档要求**：
- 公共方法必须有JavaDoc注释
- 必须包含`@param`标签（每个参数）
- 必须包含`@return`标签（有返回值的方法）
- 必须包含`@throws`标签（可能抛出的异常）
- Getter/Setter方法可以例外

### Python代码

**模块文档要求**：
- 文件开头10行内必须有Docstring（`"""..."""`或`'''...'''`）

**类文档要求**：
- 类定义后5行内必须有Docstring

**函数文档要求**：
- 公共函数定义后5行内必须有Docstring
- 私有函数（以`_`开头）可以例外

## 集成到CI/CD

### GitHub Actions示例

```yaml
name: Document Check

on: [push, pull_request]

jobs:
  check-docs:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.9'
      - name: Check documentation
        run: python scripts/check-docs.py
```

### GitLab CI示例

```yaml
check-docs:
  stage: test
  image: python:3.9
  script:
    - pip install -r requirements.txt
    - python scripts/check-docs.py
  only:
    - merge_requests
    - main
```

## 文档覆盖率目标

- **类/模块文档覆盖率**：≥ 90%
- **方法/函数文档覆盖率**：≥ 80%
- **公共API文档覆盖率**：100%

## 常见问题

### Q: 检查工具报告很多缺少文档，但代码中确实有注释？

A: 检查工具基于模式匹配，可能无法识别所有格式的注释。如果确认代码有文档但工具未识别，可以：
1. 检查注释格式是否符合规范
2. 提交issue报告误报
3. 手动标记为已检查

### Q: 如何排除某些文件不检查？

A: 修改脚本中的排除规则，添加文件路径模式：
```bash
# 在脚本中添加
if [[ "$file" == *"excluded_pattern"* ]]; then
    continue
fi
```

### Q: 检查工具运行很慢？

A: 可以优化脚本：
1. 减少检查的文件范围
2. 使用更高效的正则表达式
3. 并行处理（需要修改脚本）

## 相关文档

- [代码审查规范-文档完整性要求](../docs/代码审查规范-文档完整性要求.md)
- [开发任务执行Prompt](../docs/开发任务执行Prompt.md)

## 贡献

如果发现工具的问题或有改进建议，请提交Issue或Pull Request。

