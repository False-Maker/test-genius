# Python 3.10 升级指南

> **适用环境**: Windows  
> **当前版本**: Python 3.8.0  
> **目标版本**: Python 3.10+

---

## 📋 升级步骤

### 方法一：官方安装程序（推荐）

#### 步骤 1：下载 Python 3.10

1. 访问 Python 官方下载页面：
   - **直接下载链接**: https://www.python.org/ftp/python/3.10.11/python-3.10.11-amd64.exe
   - **或访问**: https://www.python.org/downloads/release/python-31011/
   - 选择 **Windows installer (64-bit)** 下载

2. 下载完成后，安装程序文件名为：`python-3.10.11-amd64.exe`

#### 步骤 2：安装 Python 3.10

1. **运行安装程序**
   - 双击下载的 `python-3.10.11-amd64.exe` 文件

2. **重要配置选项**
   - ✅ **必须勾选**: `Add Python 3.10 to PATH`（添加到环境变量）
   - ✅ **建议勾选**: `Install launcher for all users`（为所有用户安装启动器）
   - 选择 **Install Now** 进行默认安装
   - 或选择 **Customize installation** 自定义安装路径

3. **自定义安装（可选）**
   - 如果选择自定义安装，建议安装路径：`D:\Develop\Python\Python310\`
   - 确保勾选所有可选功能（pip、tcl/tk、Python test suite 等）

#### 步骤 3：验证安装

安装完成后，**重新打开 PowerShell 或命令提示符**，运行以下命令：

```powershell
# 检查 Python 版本
python --version
# 应该显示：Python 3.10.11

# 检查 Python 路径
python -c "import sys; print(sys.executable)"
# 应该显示：D:\Develop\Python\Python310\python.exe（或您选择的路径）

# 检查 pip 版本
pip --version
# 应该显示：pip 23.x.x from ... (python 3.10)
```

#### 步骤 4：处理多版本共存

如果系统中同时存在 Python 3.8 和 Python 3.10：

1. **使用 py 启动器（推荐）**
   ```powershell
   # 使用 Python 3.10
   py -3.10 --version
   
   # 使用 Python 3.8
   py -3.8 --version
   
   # 设置默认版本为 3.10
   py -3.10 -m pip install --upgrade pip
   ```

2. **更新环境变量优先级**
   - 打开"系统属性" → "环境变量"
   - 在"用户变量"或"系统变量"的 `Path` 中
   - 将 Python 3.10 的路径移到 Python 3.8 路径之前
   - 例如：
     ```
     D:\Develop\Python\Python310\
     D:\Develop\Python\Python310\Scripts\
     D:\Develop\Python\Python38\
     D:\Develop\Python\Python38\Scripts\
     ```

### 方法二：使用包管理器（如果已安装）

#### 使用 Chocolatey

```powershell
# 安装 Chocolatey（如果未安装）
# 以管理员身份运行 PowerShell，执行：
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 安装 Python 3.10
choco install python310 -y
```

#### 使用 winget（Windows 10/11）

```powershell
# 搜索 Python 3.10
winget search Python.Python.3.10

# 安装 Python 3.10
winget install Python.Python.3.10
```

---

## 🔧 安装后配置

### 1. 升级 pip

```powershell
python -m pip install --upgrade pip
```

### 2. 安装常用工具

```powershell
# 安装虚拟环境工具
pip install virtualenv

# 安装项目依赖（如果项目有 requirements.txt）
cd backend-python/ai-service
pip install -r requirements.txt
```

### 3. 验证项目兼容性

```powershell
# 检查项目是否能在 Python 3.10 下运行
cd backend-python/ai-service
python --version
python -m pytest --version  # 如果安装了 pytest
```

---

## ⚠️ 注意事项

1. **保留旧版本**
   - 安装 Python 3.10 不会自动卸载 Python 3.8
   - 两个版本可以共存，使用 `py -3.8` 或 `py -3.10` 指定版本

2. **环境变量**
   - 确保 `Add Python to PATH` 已勾选
   - 如果安装后 `python --version` 仍显示 3.8，需要手动调整 PATH 顺序

3. **虚拟环境**
   - 如果项目使用了虚拟环境，需要重新创建：
     ```powershell
     # 删除旧虚拟环境
     Remove-Item -Recurse -Force venv
     
     # 使用 Python 3.10 创建新虚拟环境
     python -m venv venv
     venv\Scripts\activate
     pip install -r requirements.txt
     ```

4. **IDE 配置**
   - 如果使用 VS Code、PyCharm 等 IDE，需要更新解释器路径
   - VS Code: `Ctrl+Shift+P` → "Python: Select Interpreter" → 选择 Python 3.10

---

## 🐛 常见问题

### 问题 1：安装后仍显示旧版本

**解决方案**：
1. 检查环境变量 PATH 中 Python 路径的顺序
2. 重启终端/PowerShell
3. 使用完整路径：`D:\Develop\Python\Python310\python.exe --version`

### 问题 2：pip 命令不可用

**解决方案**：
```powershell
# 使用 python -m pip 代替 pip
python -m pip install --upgrade pip
```

### 问题 3：模块导入错误

**解决方案**：
- 重新安装项目依赖：`pip install -r requirements.txt`
- 检查是否在正确的虚拟环境中

---

## ✅ 验证清单

升级完成后，请确认：

- [ ] `python --version` 显示 Python 3.10.x
- [ ] `pip --version` 显示 pip 23.x.x (python 3.10)
- [ ] 可以正常导入项目依赖的模块
- [ ] 项目可以正常启动和运行
- [ ] IDE 已配置使用 Python 3.10 解释器

---

## 📚 参考资源

- [Python 3.10 官方文档](https://docs.python.org/3.10/)
- [Python 3.10 新特性](https://docs.python.org/3.10/whatsnew/3.10.html)
- [Python Windows 安装指南](https://docs.python.org/3/using/windows.html)

---

**升级完成后，请运行项目测试确保一切正常！**

