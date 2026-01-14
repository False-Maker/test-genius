# Cursor 历史对话加载说明

## 问题

如果你已经将 Cursor 的历史对话文件复制到项目目录（`.cursor/workspaceStorage/`），但 Cursor 无法加载，这是因为 Cursor 默认从系统目录读取对话历史。

## 解决方案

### 方法1：使用链接脚本（推荐）

1. **以管理员身份打开 PowerShell**
   - 右键点击 PowerShell
   - 选择"以管理员身份运行"

2. **运行链接脚本**
   ```powershell
   cd D:\Sinosoft\test-genius
   .\scripts\link-cursor-history.ps1
   ```

3. **脚本会自动：**
   - 检测项目目录中的工作区 ID
   - 创建从系统路径到项目路径的符号链接
   - 让 Cursor 能够加载项目目录中的对话历史

4. **重启 Cursor**
   - 完全关闭 Cursor
   - 重新打开项目
   - 对话历史应该能够正常加载

### 方法2：手动创建符号链接

如果你知道工作区 ID（例如：`1f69559095a7905f72e8d928b9ee81d5`），可以手动创建符号链接：

```powershell
# 以管理员身份运行
$WorkspaceId = "1f69559095a7905f72e8d928b9ee81d5"
$ProjectPath = "D:\Sinosoft\test-genius"
$SystemPath = "$env:APPDATA\Cursor\User\workspaceStorage\$WorkspaceId"
$ProjectPath = "$ProjectPath\.cursor\workspaceStorage\$WorkspaceId"

# 如果系统路径已存在，先备份
if (Test-Path $SystemPath) {
    $BackupPath = "$SystemPath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
    Copy-Item $SystemPath $BackupPath -Recurse
    Remove-Item $SystemPath -Recurse -Force
}

# 创建符号链接
New-Item -ItemType SymbolicLink -Path $SystemPath -Target $ProjectPath -Force
```

## 验证配置

配置完成后，验证步骤：

1. **检查符号链接**
   ```powershell
   # 查看符号链接是否创建成功
   Get-Item "$env:APPDATA\Cursor\User\workspaceStorage\你的工作区ID" | Select-Object LinkType, Target
   ```

2. **重启 Cursor**
   - 完全关闭 Cursor（确保所有窗口都关闭）
   - 重新打开项目

3. **检查对话历史**
   - 在 Cursor 中打开 Agent 对话面板
   - 查看之前的对话历史是否出现

## 故障排除

### 符号链接创建失败

**错误信息：** `创建符号链接失败` 或 `需要管理员权限`

**解决方案：**
1. 确保以管理员身份运行 PowerShell
2. 在 Windows 设置中启用开发者模式：
   - 设置 → 更新和安全 → 开发者选项 → 启用开发者模式

### 对话历史仍然无法加载

**可能原因：**
1. 工作区 ID 不匹配
2. 文件路径不正确
3. Cursor 缓存问题

**解决方案：**
1. 检查工作区 ID 是否正确：
   ```powershell
   # 查看项目目录中的工作区 ID
   Get-ChildItem "D:\Sinosoft\test-genius\.cursor\workspaceStorage" -Directory
   ```

2. 检查符号链接是否正确：
   ```powershell
   $WorkspaceId = "你的工作区ID"
   $Link = Get-Item "$env:APPDATA\Cursor\User\workspaceStorage\$WorkspaceId"
   Write-Host "链接类型: $($Link.LinkType)"
   Write-Host "目标路径: $($Link.Target)"
   ```

3. 清除 Cursor 缓存并重启：
   - 完全关闭 Cursor
   - 删除 `%APPDATA%\Cursor\Cache`（可选）
   - 重新打开项目

### 多个工作区 ID

如果项目目录中有多个工作区 ID，脚本会提示你选择。选择与当前项目匹配的工作区 ID。

## 相关文件

- `scripts/link-cursor-history.ps1` - 自动链接脚本
- `scripts/setup-cursor-workspace.ps1` - 工作区存储配置脚本
- `docs/Cursor配置说明.md` - 详细配置说明
- `.vscode/settings.json` - VS Code/Cursor 配置文件

## 注意事项

1. **符号链接需要管理员权限**：创建符号链接需要管理员权限，请以管理员身份运行脚本

2. **备份重要数据**：如果系统路径中已有数据，脚本会提示备份。建议手动备份重要数据

3. **版本控制**：`.cursor/` 目录默认被 `.gitignore` 忽略，不会提交到版本控制

4. **多用户协作**：如果团队需要共享对话历史，建议在 `docs/` 目录中记录重要的对话和决策

