# Cursor 界面设置说明

## 概述

虽然 Cursor 是基于 VS Code 的，但**工作区存储路径（对话历史保存位置）无法直接在 Cursor 的设置界面中配置**。这是因为工作区存储路径是 Cursor 的内部机制，需要通过系统级别的符号链接来实现。

## 为什么不能在界面中设置？

1. **系统级存储**：Cursor 的工作区存储是系统级别的，默认存储在系统应用数据目录
2. **架构限制**：VS Code/Cursor 的架构决定了工作区存储路径是固定的
3. **符号链接方案**：需要通过创建符号链接来"重定向"存储路径到项目目录

## 可以在 Cursor 界面中设置的选项

虽然不能直接设置存储路径，但你可以在 Cursor 的设置界面中配置以下相关选项：

### 1. 打开设置界面

- **快捷键**：`Ctrl+,`（Windows/Linux）或 `Cmd+,`（macOS）
- **菜单**：File → Preferences → Settings
- **命令面板**：`Ctrl+Shift+P` → 输入 "Preferences: Open Settings"

### 2. 可配置的相关选项

在设置界面中搜索以下关键词：

#### Agent 相关设置
- `cursor.agent.*` - Agent 相关设置
- `cursor.chat.*` - 聊天相关设置
- `cursor.composer.*` - Composer 相关设置

#### 工作区相关设置
- `workspace.*` - 工作区相关设置
- `files.exclude` - 文件排除设置（可以排除 `.cursor` 目录）
- `search.exclude` - 搜索排除设置

### 3. 项目级设置文件

你可以在项目根目录创建 `.vscode/settings.json` 文件来配置项目级设置：

```json
{
  // 排除 .cursor 目录的搜索和文件监视（提高性能）
  "files.exclude": {
    "**/.cursor/workspaceStorage/**": false
  },
  "search.exclude": {
    "**/.cursor/workspaceStorage/**": true
  },
  "files.watcherExclude": {
    "**/.cursor/workspaceStorage/**": true
  }
}
```

**注意**：这个文件只能配置 VS Code/Cursor 的常规设置，**不能配置工作区存储路径**。

## 正确的配置方法

### 方法1：使用自动化脚本（推荐）

这是最简单的方法，一键完成所有配置：

```powershell
# 以管理员身份运行 PowerShell
cd D:\Demo\test-genius
.\scripts\auto-setup-cursor.ps1
```

### 方法2：手动创建符号链接

如果脚本无法使用，可以手动创建符号链接：

1. **查找工作区 ID**
   - 在 Cursor 中按 `Ctrl+Shift+P`
   - 输入 "Workspace: Show Workspace Storage"
   - 从路径中提取工作区 ID

2. **创建符号链接**
   ```powershell
   # 以管理员身份运行
   $workspaceId = "你的工作区ID"
   $projectPath = "D:\Demo\test-genius"
   $targetPath = "$projectPath\.cursor\workspaceStorage\$workspaceId"
   $sourcePath = "$env:APPDATA\Cursor\User\workspaceStorage\$workspaceId"
   
   # 创建目录
   New-Item -ItemType Directory -Path "$projectPath\.cursor\workspaceStorage" -Force
   
   # 如果源目录存在，迁移数据
   if (Test-Path $sourcePath) {
       Copy-Item $sourcePath $targetPath -Recurse -Force
       Remove-Item $sourcePath -Recurse -Force
   }
   
   # 创建符号链接
   New-Item -ItemType SymbolicLink -Path $sourcePath -Target $targetPath -Force
   ```

## 验证配置

配置完成后，可以通过以下方式验证：

1. **在 Cursor 中进行一次对话**
2. **检查项目目录**
   ```powershell
   # 检查 .cursor 目录是否有新文件
   Get-ChildItem "D:\Demo\test-genius\.cursor\workspaceStorage" -Recurse
   ```
3. **重启 Cursor**，确认对话历史仍然存在

## 常见问题

### Q: 为什么不能在设置界面中直接配置？

**A**: 因为工作区存储路径是 Cursor 的内部机制，是硬编码的系统路径。需要通过符号链接来"重定向"到项目目录。

### Q: 有没有其他方法可以在界面中设置？

**A**: 目前没有。Cursor 官方也没有提供这个选项。符号链接是最可靠的方法。

### Q: 配置后需要重启 Cursor 吗？

**A**: 是的，配置符号链接后需要完全关闭并重新打开 Cursor 才能生效。

### Q: 每次打开项目都需要配置吗？

**A**: 不需要。配置一次后，只要符号链接存在，就会一直生效。

## 总结

- ❌ **不能**在 Cursor 的设置界面中直接配置工作区存储路径
- ✅ **可以**使用自动化脚本一键配置
- ✅ **可以**手动创建符号链接
- ✅ **可以**在 `.vscode/settings.json` 中配置其他相关设置

**推荐使用**：`scripts/auto-setup-cursor.ps1` 自动化脚本，最简单可靠。

---

**相关文档**：
- `docs/Cursor自动保存配置指南.md` - 详细配置指南
- `scripts/auto-setup-cursor.ps1` - 自动配置脚本
- `docs/Cursor配置说明.md` - 完整配置说明

