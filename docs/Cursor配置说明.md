# Cursor Agent 对话历史配置说明

## 问题说明

Cursor IDE 的 Agent 对话历史默认存储在系统应用数据目录中，而不是项目目录。这导致：
1. 不同项目的对话历史混在一起
2. 无法随项目一起版本控制（如果需要）
3. 项目迁移时对话历史丢失

## Agent 文件默认位置

### Windows
```
%APPDATA%\Cursor\User\workspaceStorage\{workspace-id}\
或
%LOCALAPPDATA%\Cursor\User\workspaceStorage\{workspace-id}\
```

### macOS
```
~/Library/Application Support/Cursor/User/workspaceStorage/{workspace-id}/
```

### Linux
```
~/.config/Cursor/User/workspaceStorage/{workspace-id}/
```

## 解决方案

### 方案1：使用项目目录存储（推荐）

已在项目中创建 `.cursor/` 目录用于存储 Cursor 相关文件。

**步骤：**

1. **找到当前项目的工作区 ID**
   - 在 Cursor 中，打开命令面板（`Ctrl+Shift+P` 或 `Cmd+Shift+P`）
   - 输入 "Workspace: Show Workspace Storage" 查看存储位置
   - 从路径中提取工作区 ID（通常是长字符串）

2. **创建符号链接（Windows）**
   ```powershell
   # 以管理员身份运行 PowerShell
   $workspaceId = "你的工作区ID"  # 替换为实际的工作区 ID
   $projectPath = "D:\Sinosoft\test-genius"  # 当前项目路径
   $targetPath = "$projectPath\.cursor\workspaceStorage\$workspaceId"
   $sourcePath = "$env:APPDATA\Cursor\User\workspaceStorage\$workspaceId"
   
   # 如果源目录存在，创建符号链接
   if (Test-Path $sourcePath) {
       New-Item -ItemType Directory -Path "$projectPath\.cursor\workspaceStorage" -Force
       New-Item -ItemType SymbolicLink -Path $targetPath -Target $sourcePath -Force
       Write-Host "符号链接创建成功: $targetPath -> $sourcePath"
   } else {
       Write-Host "源目录不存在: $sourcePath"
   }
   ```

3. **创建符号链接（macOS/Linux）**
   ```bash
   # 找到工作区 ID
   WORKSPACE_ID="你的工作区ID"
   PROJECT_PATH="/path/to/test-genius"
   
   # 创建目录
   mkdir -p "$PROJECT_PATH/.cursor/workspaceStorage"
   
   # 创建符号链接
   ln -s "$HOME/Library/Application Support/Cursor/User/workspaceStorage/$WORKSPACE_ID" \
        "$PROJECT_PATH/.cursor/workspaceStorage/$WORKSPACE_ID"
   ```

### 方案2：手动迁移对话历史

1. 找到 Cursor 的工作区存储目录
2. 复制整个工作区目录到 `.cursor/workspaceStorage/`
3. 在 Cursor 设置中配置使用项目目录

### 方案3：配置 Cursor 设置文件

在项目根目录创建或编辑 `.vscode/settings.json`（Cursor 兼容 VS Code 配置）：

```json
{
  "cursor.workspaceStorage": ".cursor/workspaceStorage",
  "cursor.agentHistory.saveToProject": true
}
```

## 项目配置

已创建以下文件：

1. **`.cursor/README.md`** - Cursor 目录说明
2. **`.cursor/config.json`** - 项目级 Cursor 配置
3. **`.gitignore`** - 已更新，忽略 `.cursor/` 目录

## 注意事项

### 版本控制

`.cursor/` 目录默认被 `.gitignore` 忽略，因为：
- 包含个人对话历史
- 可能包含敏感信息
- 文件较大且频繁变化

如果需要共享某些对话历史：
1. 在 `.cursor/` 下创建 `shared/` 目录
2. 将需要共享的文件放在 `shared/` 中
3. 更新 `.gitignore` 允许 `shared/` 目录

### 多用户协作

如果团队需要共享 Agent 对话模板或重要决策：
- 在 `docs/` 目录中记录重要的对话和决策
- 使用 `.cursor/shared/` 存储共享模板（如果启用）

### 备份建议

定期备份 `.cursor/` 目录以保留重要的对话历史：
```bash
# 备份示例
tar -czf cursor-backup-$(date +%Y%m%d).tar.gz .cursor/
```

## 快速配置（已复制文件到项目）

如果你已经将 Cursor 历史对话文件复制到项目目录（`.cursor/workspaceStorage/`），可以使用以下脚本快速链接：

```powershell
# 以管理员身份运行 PowerShell
cd D:\Sinosoft\test-genius
.\scripts\link-cursor-history.ps1
```

这个脚本会：
1. 自动检测项目目录中的工作区 ID
2. 创建从系统路径到项目路径的符号链接
3. 让 Cursor 能够加载项目目录中的对话历史

## 验证配置

配置完成后，可以通过以下方式验证：

1. 在 Cursor 中进行一次 Agent 对话
2. 检查 `.cursor/workspaceStorage/` 目录是否有新文件生成
3. 重启 Cursor，确认对话历史仍然存在
4. 检查系统路径是否已正确链接到项目路径

## 故障排除

### 符号链接创建失败

**Windows:**
- 确保以管理员身份运行 PowerShell
- 检查是否启用了开发者模式（Windows 10/11）

**macOS/Linux:**
- 确保有目录创建权限
- 检查目标路径是否正确

### 对话历史未保存到项目

1. 检查符号链接是否正确创建
2. 验证 Cursor 是否使用了正确的工作区存储路径
3. 查看 Cursor 的日志文件（通常在应用数据目录）

## 相关文档

- [Cursor 官方文档](https://cursor.sh/docs)
- `.cursor/README.md` - 目录说明
- `.cursorrules` - 项目规则配置

