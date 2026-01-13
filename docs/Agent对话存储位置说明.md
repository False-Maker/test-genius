# Cursor Agent 对话历史存储位置说明

## 默认存储位置

Cursor 的 Agent 对话历史默认存储在以下位置：

### Windows 系统

**位置1（常用）：**
```
%APPDATA%\Cursor\User\workspaceStorage\{workspace-id}\
```
完整路径示例：
```
C:\Users\你的用户名\AppData\Roaming\Cursor\User\workspaceStorage\{workspace-id}\
```

**位置2（备选）：**
```
%LOCALAPPDATA%\Cursor\User\workspaceStorage\{workspace-id}\
```
完整路径示例：
```
C:\Users\你的用户名\AppData\Local\Cursor\User\workspaceStorage\{workspace-id}\
```

### macOS 系统
```
~/Library/Application Support/Cursor/User/workspaceStorage/{workspace-id}/
```

### Linux 系统
```
~/.config/Cursor/User/workspaceStorage/{workspace-id}/
```

## 如何找到你的工作区 ID

### 方法1：通过 Cursor 命令面板

1. 在 Cursor 中打开你的项目
2. 按 `Ctrl+Shift+P` (Windows/Linux) 或 `Cmd+Shift+P` (macOS) 打开命令面板
3. 输入并选择：`Workspace: Show Workspace Storage`
4. 会显示完整路径，从中提取工作区 ID（通常是长字符串，如 `a1b2c3d4e5f6...`）

### 方法2：直接查看目录

1. 打开文件资源管理器
2. 在地址栏输入：`%APPDATA%\Cursor\User\workspaceStorage` (Windows)
3. 你会看到多个以工作区 ID 命名的文件夹
4. 每个文件夹对应一个项目的工作区

## 手动复制到项目目录

如果你想将 Agent 对话历史保存到项目目录（`.cursor/workspaceStorage/`），可以：

### 步骤1：找到工作区 ID

使用上面的方法找到你的工作区 ID，假设为 `abc123def456...`

### 步骤2：复制文件

**Windows PowerShell：**
```powershell
# 设置变量
$workspaceId = "你的工作区ID"
$sourcePath = "$env:APPDATA\Cursor\User\workspaceStorage\$workspaceId"
$targetPath = "D:\Sinosoft\test-genius\.cursor\workspaceStorage\$workspaceId"

# 创建目标目录
New-Item -ItemType Directory -Path "D:\Sinosoft\test-genius\.cursor\workspaceStorage" -Force

# 复制文件（如果源路径存在）
if (Test-Path $sourcePath) {
    Copy-Item -Path $sourcePath -Destination $targetPath -Recurse -Force
    Write-Host "文件已复制到: $targetPath"
} else {
    Write-Host "源路径不存在，尝试 LOCALAPPDATA..."
    $sourcePath = "$env:LOCALAPPDATA\Cursor\User\workspaceStorage\$workspaceId"
    if (Test-Path $sourcePath) {
        Copy-Item -Path $sourcePath -Destination $targetPath -Recurse -Force
        Write-Host "文件已复制到: $targetPath"
    }
}
```

**手动复制：**
1. 打开文件资源管理器
2. 导航到：`%APPDATA%\Cursor\User\workspaceStorage\{你的工作区ID}`
3. 复制整个文件夹
4. 粘贴到项目目录：`D:\Sinosoft\test-genius\.cursor\workspaceStorage\`
5. 重命名文件夹为你的工作区 ID

## 查看对话历史文件

在工作区存储目录中，通常包含以下文件：

- `workspace.json` - 工作区配置
- `state.vscdb` - 状态数据库（可能包含对话历史）
- `state.vscdb-shm` - 共享内存文件
- `state.vscdb-wal` - 预写日志
- 其他 Cursor 相关的状态文件

## 注意事项

1. **备份重要对话**：在复制或移动文件前，建议先备份
2. **不要同时使用**：如果使用符号链接，不要手动复制文件，避免冲突
3. **版本控制**：`.cursor/` 目录默认被 `.gitignore` 忽略，不会提交到 Git
4. **多项目**：每个项目有独立的工作区 ID，需要分别处理

## 快速查找脚本

在项目根目录运行以下 PowerShell 命令快速查找：

```powershell
# 查找所有 Cursor 工作区
Write-Host "查找 Cursor 工作区存储..." -ForegroundColor Cyan
$paths = @(
    "$env:APPDATA\Cursor\User\workspaceStorage",
    "$env:LOCALAPPDATA\Cursor\User\workspaceStorage"
)

foreach ($path in $paths) {
    if (Test-Path $path) {
        Write-Host "`n找到: $path" -ForegroundColor Green
        $workspaces = Get-ChildItem $path -Directory
        Write-Host "工作区数量: $($workspaces.Count)" -ForegroundColor Yellow
        foreach ($ws in $workspaces) {
            Write-Host "  - $($ws.Name)" -ForegroundColor Cyan
        }
    }
}
```

## 相关文档

- [Cursor配置说明.md](./Cursor配置说明.md) - 完整配置指南
- [.cursor/README.md](../.cursor/README.md) - 项目目录说明


