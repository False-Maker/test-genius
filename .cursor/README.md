# Cursor Agent 对话历史配置

## 说明

此目录用于存储 Cursor IDE 在当前项目中的 Agent 对话历史和相关配置。

## 快速配置（推荐）

**一键自动配置，让对话自动保存到项目目录：**

```powershell
# 以管理员身份运行 PowerShell
cd D:\Demo\test-genius
.\scripts\auto-setup-cursor.ps1
```

配置完成后，重启 Cursor，每次对话都会自动保存到项目的 `.cursor` 目录中。

详细配置说明请查看：`docs/Cursor自动保存配置指南.md`

## Agent 文件位置

Cursor 的 Agent 对话历史默认存储在以下位置：

### Windows
- `%APPDATA%\Cursor\User\workspaceStorage\` - 工作区存储
- `%LOCALAPPDATA%\Cursor\User\workspaceStorage\` - 本地工作区存储

### macOS
- `~/Library/Application Support/Cursor/User/workspaceStorage/`

### Linux
- `~/.config/Cursor/User/workspaceStorage/`

## 如何将 Agent 对话保存到项目目录

### 方法1：使用符号链接（推荐）

在 Windows 上，可以使用符号链接将 Cursor 的工作区存储指向项目目录：

```powershell
# 1. 找到当前项目的工作区 ID
# 在 Cursor 中打开项目后，工作区 ID 会显示在路径中

# 2. 创建符号链接（需要管理员权限）
# 假设工作区 ID 为 xxxxxx
$workspaceId = "xxxxxx"  # 替换为实际的工作区 ID
$targetPath = "$PWD\.cursor\workspaceStorage\$workspaceId"
$sourcePath = "$env:APPDATA\Cursor\User\workspaceStorage\$workspaceId"

# 如果源目录存在，创建符号链接
if (Test-Path $sourcePath) {
    New-Item -ItemType SymbolicLink -Path $targetPath -Target $sourcePath -Force
}
```

### 方法2：配置 Cursor 设置

1. 打开 Cursor 设置（`Ctrl+,` 或 `Cmd+,`）
2. 搜索 "workspace storage" 或 "agent history"
3. 配置存储路径指向项目目录

### 方法3：手动迁移对话历史

1. 找到 Cursor 的工作区存储目录
2. 复制相关文件到 `.cursor/workspaceStorage/` 目录
3. 在 Cursor 中重新打开项目

## 目录结构

```
.cursor/
├── README.md              # 本说明文件
├── workspaceStorage/      # 工作区存储（如果使用符号链接）
└── config.json            # 项目级 Cursor 配置（可选）
```

## 注意事项

1. **版本控制**：`.cursor` 目录通常不应该提交到 Git，因为：
   - 包含个人对话历史
   - 可能包含敏感信息
   - 文件较大且频繁变化

2. **多用户协作**：如果团队需要共享 Agent 对话历史，可以考虑：
   - 使用 `.cursor/` 目录存储共享的对话模板
   - 在文档中记录重要的对话和决策

3. **备份**：定期备份 `.cursor` 目录以保留重要的对话历史

## 相关文件

- `.gitignore` - 已配置忽略 `.cursor` 目录
- `.cursorrules` - Cursor 项目规则配置
- `docs/Cursor自动保存配置指南.md` - 详细配置指南
- `scripts/auto-setup-cursor.ps1` - 自动配置脚本

