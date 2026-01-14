# Cursor 自动保存配置指南

## 概述

本指南将帮助你配置 Cursor IDE，使每次对话自动保存到当前项目的 `.cursor` 目录中。这样，你的对话历史会保留在项目中，不会丢失。

## 快速开始

### 方法1：使用自动化脚本（推荐）

1. **以管理员身份打开 PowerShell**
   - 右键点击 PowerShell
   - 选择"以管理员身份运行"

2. **运行自动配置脚本**
   ```powershell
   cd D:\Demo\test-genius
   .\scripts\auto-setup-cursor.ps1
   ```

3. **按照提示操作**
   - 脚本会自动检测工作区 ID
   - 如果检测不到，会提示你先打开 Cursor 并打开项目

4. **重启 Cursor**
   - 完全关闭 Cursor
   - 重新打开项目
   - 配置完成！

### 方法2：手动配置

如果自动脚本无法使用，可以手动配置：

1. **打开 Cursor 并打开项目**
   - 在 Cursor 中进行一次对话（任意对话即可）
   - 这会创建系统工作区存储

2. **查找工作区 ID**
   - 在 Cursor 中，按 `Ctrl+Shift+P` 打开命令面板
   - 输入 "Workspace: Show Workspace Storage"
   - 从路径中提取工作区 ID（长字符串）

3. **运行链接脚本**
   ```powershell
   # 以管理员身份运行
   cd D:\Demo\test-genius
   .\scripts\link-cursor-history.ps1
   ```

## 工作原理

配置后，系统会创建一个符号链接，将 Cursor 的系统存储路径指向项目目录：

```
系统路径: %APPDATA%\Cursor\User\workspaceStorage\{workspace-id}
         ↓ (符号链接)
项目路径: D:\Demo\test-genius\.cursor\workspaceStorage\{workspace-id}
```

这样，Cursor 的所有对话历史都会自动保存到项目的 `.cursor` 目录中。

## 配置效果

配置成功后，你将获得以下效果：

1. ✅ **自动保存**：每次对话自动保存到项目目录
2. ✅ **项目隔离**：不同项目的对话历史分开保存
3. ✅ **历史保留**：对话历史随项目一起保留，不会丢失
4. ✅ **版本控制**：`.cursor` 目录默认被 `.gitignore` 忽略

## 验证配置

配置完成后，可以通过以下方式验证：

1. **检查符号链接**
   ```powershell
   $workspaceId = "你的工作区ID"
   $link = Get-Item "$env:APPDATA\Cursor\User\workspaceStorage\$workspaceId"
   Write-Host "链接类型: $($link.LinkType)"
   Write-Host "目标路径: $($link.Target)"
   ```

2. **测试对话保存**
   - 在 Cursor 中进行一次对话
   - 检查 `.cursor/workspaceStorage/{workspace-id}/` 目录是否有新文件生成
   - 重启 Cursor，确认对话历史仍然存在

## 常见问题

### Q1: 脚本提示"未找到工作区 ID"

**原因**：首次使用 Cursor 或尚未打开过项目

**解决方案**：
1. 打开 Cursor IDE
2. 打开项目 `D:\Demo\test-genius`
3. 在 Cursor 中进行一次对话（任意对话即可）
4. 关闭 Cursor
5. 再次运行配置脚本

### Q2: 符号链接创建失败

**错误信息**：`创建符号链接失败` 或 `需要管理员权限`

**解决方案**：
1. 确保以管理员身份运行 PowerShell
2. 在 Windows 设置中启用开发者模式：
   - 设置 → 更新和安全 → 开发者选项 → 启用开发者模式
3. 或者运行以下命令启用符号链接支持：
   ```powershell
   New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name SymlinkEvaluation -Value 1 -PropertyType DWORD -Force
   ```

### Q3: 对话历史仍然无法加载

**可能原因**：
1. 工作区 ID 不匹配
2. 文件路径不正确
3. Cursor 缓存问题

**解决方案**：
1. 检查工作区 ID 是否正确
2. 检查符号链接是否正确创建
3. 清除 Cursor 缓存并重启：
   - 完全关闭 Cursor
   - 删除 `%APPDATA%\Cursor\Cache`（可选）
   - 重新打开项目

### Q4: 每次打开 Cursor 都是新对话吗？

**回答**：不是。配置后，对话历史会保留在项目目录中。每次打开 Cursor 时，会加载之前的对话历史。

如果你想要每次打开新的对话界面，可以：
1. 在 Cursor 中点击"新建对话"按钮
2. 或者使用快捷键创建新对话

### Q5: 多个项目如何配置？

**回答**：每个项目都需要单独配置。运行配置脚本时，脚本会自动检测当前项目的工作区 ID。

## 目录结构

配置后的目录结构：

```
test-genius/
├── .cursor/
│   ├── README.md
│   ├── config.json
│   └── workspaceStorage/
│       └── {workspace-id}/
│           ├── anysphere.cursor-retrieval/
│           ├── images/
│           ├── state.vscdb
│           ├── state.vscdb.backup
│           └── workspace.json
└── ...
```

## 版本控制

`.cursor/` 目录默认被 `.gitignore` 忽略，因为：
- 包含个人对话历史
- 可能包含敏感信息
- 文件较大且频繁变化

如果需要共享某些对话历史：
1. 在 `.cursor/` 下创建 `shared/` 目录
2. 将需要共享的文件放在 `shared/` 中
3. 更新 `.gitignore` 允许 `shared/` 目录

## 备份建议

定期备份 `.cursor/` 目录以保留重要的对话历史：

```powershell
# 备份示例
$backupPath = "cursor-backup-$(Get-Date -Format 'yyyyMMdd').zip"
Compress-Archive -Path ".cursor" -DestinationPath $backupPath
```

## 相关文档

- `docs/Cursor配置说明.md` - 详细配置说明
- `docs/Cursor历史对话加载说明.md` - 历史对话加载说明
- `scripts/auto-setup-cursor.ps1` - 自动配置脚本
- `scripts/link-cursor-history.ps1` - 链接脚本

## 技术支持

如果遇到问题，请：
1. 查看相关文档
2. 检查脚本输出的错误信息
3. 验证符号链接是否正确创建
4. 查看 Cursor 的日志文件

---

**最后更新**：2025-01-13  
**适用版本**：Cursor IDE 最新版本

