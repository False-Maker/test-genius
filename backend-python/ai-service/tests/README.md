# Python服务测试说明

## 概述

本项目使用pytest作为测试框架，提供单元测试和API测试。

## 测试结构

```
tests/
├── __init__.py
├── conftest.py              # pytest配置和fixtures
├── test_llm_service.py      # LLM服务单元测试
├── test_model_config_service.py  # 模型配置服务单元测试
├── test_prompt_service.py   # 提示词服务单元测试
├── test_api_llm.py          # LLM API路由测试
└── test_api_main.py         # 主应用API测试
```

## 安装测试依赖

```bash
pip install -r requirements.txt
```

测试相关依赖包括：
- pytest==8.3.0
- pytest-asyncio==0.23.7
- pytest-cov==5.0.0
- pytest-mock==3.14.0

## 运行测试

### 运行所有测试

```bash
pytest
```

### 运行特定测试文件

```bash
pytest tests/test_llm_service.py
```

### 运行特定测试类

```bash
pytest tests/test_llm_service.py::TestLLMService
```

### 运行特定测试方法

```bash
pytest tests/test_llm_service.py::TestLLMService::test_call_model_success
```

### 查看测试覆盖率

```bash
pytest --cov=app --cov-report=html
```

覆盖率报告会生成在 `htmlcov/index.html`

### 查看详细输出

```bash
pytest -v
```

### 查看测试覆盖率终端输出

```bash
pytest --cov=app --cov-report=term-missing
```

## 测试配置

测试配置在 `pytest.ini` 文件中：

- 测试目录：`tests/`
- 覆盖率要求：>=70%
- 覆盖率报告格式：html, term-missing, xml
- 异步测试模式：auto

## 测试标记

使用pytest标记来分类测试：

```python
@pytest.mark.unit
def test_something():
    pass

@pytest.mark.integration
def test_integration():
    pass

@pytest.mark.api
def test_api():
    pass
```

运行特定标记的测试：

```bash
pytest -m unit      # 只运行单元测试
pytest -m integration  # 只运行集成测试
pytest -m api       # 只运行API测试
```

## Fixtures

测试中使用的fixtures定义在 `conftest.py` 中：

- `test_db`: 测试数据库会话（使用SQLite内存数据库）
- `client`: FastAPI测试客户端
- `mock_model_config`: 模拟模型配置对象
- `mock_llm_response`: 模拟LLM响应对象
- `mock_llm_instance`: 模拟LLM实例

## 测试编写规范

1. 测试类命名：`Test<ServiceName>`
2. 测试方法命名：`test_<功能描述>`
3. 使用mock隔离外部依赖
4. 测试覆盖正常流程和异常流程
5. 使用断言验证预期结果

## 注意事项

1. 测试使用SQLite内存数据库，不依赖外部数据库
2. 外部服务调用（如LLM API）使用mock模拟
3. 每个测试函数独立运行，使用fixture保证隔离
4. 覆盖率要求不低于70%

