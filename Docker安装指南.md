# Docker 和 Docker Compose 安装指南

## 安装要求
- **安装路径**: D:\Develop\
- **Docker 版本**: 20.x.x
- **Docker Compose 版本**: v2.x.x
- **操作系统**: Windows 10/11

## 重要说明

### 关于安装路径
Docker Desktop for Windows 的**可执行文件**必须安装在默认位置（`C:\Program Files\Docker\`），这是由 Docker Desktop 的安装程序决定的。

但是，您可以：
1. 将 **Docker 数据目录**（镜像、容器等）移动到 `D:\Develop\Docker\`
2. 将 **Docker Compose** 的配置文件放在 `D:\Develop\Docker\`

## 安装步骤

### 方法一：使用安装脚本（推荐）

1. **以管理员身份运行 PowerShell**

2. **执行安装脚本**：
```powershell
cd D:\Sinosoft\test-genius
.\install-docker.ps1
```

3. **脚本会自动完成**：
   - 检查 WSL2 支持
   - 下载 Docker Desktop 安装包
   - 安装 Docker Desktop
   - 配置环境变量
   - 验证安装

### 方法二：手动安装

#### 步骤 1: 检查系统要求

1. **启用 WSL2**（Docker Desktop 需要）：
```powershell
# 以管理员身份运行 PowerShell
wsl --install
```

2. **重启系统**（安装 WSL2 后需要重启）

#### 步骤 2: 下载 Docker Desktop

访问：https://www.docker.com/products/docker-desktop/

或直接下载：
- https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

#### 步骤 3: 安装 Docker Desktop

1. 运行下载的安装程序
2. 安装过程中确保勾选：
   - ✅ Use WSL 2 instead of Hyper-V (推荐)
   - ✅ Add shortcut to desktop
3. 安装完成后，点击 "Close and restart"

#### 步骤 4: 配置环境变量

环境变量会自动配置，但可以手动验证：

1. 打开 **系统属性** → **高级** → **环境变量**
2. 在 **系统变量** 的 `Path` 中添加：
   - `C:\Program Files\Docker\Docker\resources\bin`
   - `C:\Program Files\Docker\Docker\resources\cli-plugins`

#### 步骤 5: 配置 Docker 数据目录到 D:\Develop\

1. **启动 Docker Desktop**

2. **打开设置**：
   - 点击系统托盘中的 Docker 图标
   - 选择 **Settings** → **Resources** → **Advanced**

3. **配置数据目录**：
   - 如果支持，修改 **Disk image location** 为 `D:\Develop\Docker\data`
   - 或者创建符号链接（见下方）

4. **使用符号链接迁移数据**（推荐）：
```powershell
# 以管理员身份运行 PowerShell

# 停止 Docker Desktop
Stop-Service -Name "com.docker.service" -ErrorAction SilentlyContinue

# 创建目标目录
New-Item -ItemType Directory -Path "D:\Develop\Docker\data" -Force

# 备份现有数据（如果存在）
$dockerDataPath = "$env:LOCALAPPDATA\Docker"
if (Test-Path $dockerDataPath) {
    Copy-Item -Path "$dockerDataPath\*" -Destination "D:\Develop\Docker\data\" -Recurse -Force
}

# 创建符号链接（需要删除原目录或重命名）
Rename-Item -Path $dockerDataPath -NewName "$dockerDataPath.backup"
New-Item -ItemType SymbolicLink -Path $dockerDataPath -Target "D:\Develop\Docker\data"

# 启动 Docker Desktop
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

#### 步骤 6: 验证安装

重新打开 PowerShell（以获取新的环境变量），运行：

```powershell
# 检查 Docker 版本
docker --version
# 应输出: Docker version 20.10.x, build xxxxx

# 检查 Docker Compose 版本
docker compose version
# 应输出: Docker Compose version v2.x.x

# 测试 Docker 运行
docker run hello-world
```

## 安装后配置

### 配置 Docker 镜像加速（可选）

1. 打开 Docker Desktop → **Settings** → **Docker Engine**
2. 添加以下配置：
```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
```
3. 点击 **Apply & Restart**

### 配置 Docker Compose 工作目录

在项目根目录创建 `docker-compose.yml` 或使用 `D:\Develop\Docker\` 作为工作目录。

## 常见问题

### 1. WSL 2 安装失败

**解决方案**：
```powershell
# 启用 Windows 功能
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# 重启系统
Restart-Computer
```

### 2. Docker Desktop 启动失败

**检查事项**：
- 确保已启用虚拟化（BIOS 设置）
- 确保 WSL 2 已正确安装：`wsl --status`
- 检查 Windows 功能：启用 Hyper-V 或使用 WSL 2

### 3. 环境变量不生效

**解决方案**：
- 重新打开 PowerShell 窗口
- 或重启系统
- 手动添加到系统环境变量

### 4. Docker 命令找不到

**解决方案**：
```powershell
# 检查环境变量
$env:Path -split ';' | Select-String -Pattern 'Docker'

# 如果不存在，手动添加
[Environment]::SetEnvironmentVariable(
    "Path",
    [Environment]::GetEnvironmentVariable("Path", "Machine") + ";C:\Program Files\Docker\Docker\resources\bin",
    "Machine"
)
```

## 验证清单

安装完成后，确认以下项目：

- [ ] Docker Desktop 可以正常启动
- [ ] `docker --version` 显示版本 20.x.x
- [ ] `docker compose version` 显示版本 v2.x.x
- [ ] `docker run hello-world` 成功运行
- [ ] 环境变量已正确配置
- [ ] Docker 数据目录已迁移到 `D:\Develop\Docker\`（可选）

## 卸载 Docker

如需卸载：

1. 通过 **设置** → **应用** → **Docker Desktop** → **卸载**
2. 或运行卸载程序：
```powershell
& "C:\Program Files\Docker\Docker\Uninstall.exe" /S
```

## 参考资源

- Docker Desktop 官方文档：https://docs.docker.com/desktop/windows/
- Docker Compose 文档：https://docs.docker.com/compose/
- WSL 2 安装指南：https://docs.microsoft.com/zh-cn/windows/wsl/install


