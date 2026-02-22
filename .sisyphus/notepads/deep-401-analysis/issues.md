# 401错误深度分析 - 根本原因发现

## 问题概述
之前修复了Python端`requirement_router.py`中的字段名（从`model_config`改为`model_cfg`），但401错误仍然存在。

## 根本原因分析

### 1. Java端发送的请求格式

**文件**: `backend-java/test-design-assistant-core/src/main/java/com/sinosoft/testdesign/service/impl/ModelCallServiceImpl.java`

**关键代码** (第78-91行):
```java
public Map<String, Object> callWithModel(ModelConfig modelConfig, String url, Map<String, Object> requestBody) {
    Map<String, Object> enrichedRequest = new HashMap<>(requestBody);

    Map<String, Object> modelInfo = new HashMap<>();
    modelInfo.put("model_code", modelConfig.getModelCode());
    modelInfo.put("model_name", modelConfig.getModelName());
    modelInfo.put("model_type", modelConfig.getModelType());
    modelInfo.put("api_endpoint", modelConfig.getApiEndpoint());
    modelInfo.put("api_key", modelConfig.getApiKey());
    modelInfo.put("model_version", modelConfig.getModelVersion());
    modelInfo.put("max_tokens", modelConfig.getMaxTokens());
    modelInfo.put("temperature", modelConfig.getTemperature());
    enrichedRequest.put("model_config", modelInfo);  // ← 这里用的是 model_config

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(enrichedRequest, headers);

    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    ...
}
```

**Java端发送的JSON**:
```json
{
  "requirement_text": "...",
  "prompt": "...",
  "model_config": {      // ← Java端发送的是 model_config
    "model_code": "...",
    "model_name": "...",
    "model_type": "...",
    "api_endpoint": "...",
    "api_key": "...",
    "model_version": "...",
    "max_tokens": 2000,
    "temperature": 0.7
  }
}
```

### 2. Python端期望的字段名

**文件**: `backend-python/ai-service/app/api/requirement_router.py`

**修复后的代码** (第29-35行):
```python
class RequirementAnalyzeRequest(BaseModel):
    requirement_text: str = Field(..., min_length=1, description="需求文本")
    prompt: Optional[str] = None
    model_code: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    model_cfg: Optional[ModelConfigData] = None  # ← 期望的是 model_cfg
```

### 3. 字段名不匹配的问题

| 端 | 字段名 |
|---|---|
| Java发送 | `model_config` |
| Python期望 | `model_cfg` |

**不匹配！** 这就是为什么修复后仍然失败的原因。

### 4. Python端实际接收到的数据

由于`model_cfg`与`model_config`不匹配，Pydantic会：
- 将`model_config`作为未知字段忽略（如果设置了`extra = 'ignore'`）
- 或者抛出验证错误

在`requirement_router.py`第54-57行:
```python
# 使用model_config中的参数优先
effective_model_code = request.model_cfg.model_code if request.model_cfg else request.model_code
effective_max_tokens = request.model_cfg.max_tokens if request.model_cfg else request.max_tokens
effective_temperature = request.model_cfg.temperature if request.model_cfg else request.temperature
```

由于`request.model_cfg`是`None`（因为字段名不匹配），代码回退到使用顶层的`model_code`、`max_tokens`、`temperature`，但这些字段在Java请求中也没有被设置（因为Java把所有参数都放在`model_config`对象里了）。

### 5. LLM服务调用失败

在`requirement_router.py`第59-64行:
```python
logger.info(f"effective_model_code: {effective_model_code}")
result = await run_in_threadpool(
    llm_service.call_model_with_config,
    model_config=request.model_cfg.dict() if request.model_cfg else None,  # ← None
    prompt=prompt
)
```

由于`request.model_cfg`是`None`，传递给`call_model_with_config`的`model_config`参数也是`None`。

在`llm_service.py`第259行:
```python
if not model_code:
    default_config = self.model_config_service.get_default_config()
    if not default_config:
        raise ValueError("没有可用的模型配置，请先配置模型")  # ← 401错误的真正原因
```

由于`model_code`也是`None`，且数据库中没有默认配置，抛出异常导致401错误。

## 解决方案

需要统一字段名。有两个选择：

### 选项1: 修改Python端，使用`model_config`（推荐）
将Python端的`model_cfg`改回`model_config`，使其与Java端一致。

### 选项2: 修改Java端，使用`model_cfg`
将Java端的`model_config`改为`model_cfg`，但这会影响所有使用该服务的地方。

**推荐选项1**，因为：
1. `model_config`是更标准的命名
2. 只需要修改Python端
3. Java端代码不需要改动

## 需要修改的文件

1. `backend-python/ai-service/app/api/requirement_router.py` - 将`model_cfg`改回`model_config`
