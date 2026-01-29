# 上下文注入服务使用指南

## 概述

`ContextInjectionService` 是一个用于将检索到的文档内容注入到提示词中的服务，支持引用溯源、上下文窗口管理等高级功能。

## 功能特性

- ✅ 上下文注入到提示词
- ✅ 智能文档选择（按相关性排序、长度控制、数量限制）
- ✅ 引用标注功能
- ✅ 上下文格式化
- ✅ 引用溯源（从模型响应中提取引用）
- ✅ 上下文窗口管理（max_context_length、max_documents）
- ✅ 上下文验证和截断
- ✅ 上下文统计信息

## 基本使用

### 1. 创建服务实例

```python
from app.services.context_injection_service import ContextInjectionService, create_context_injection_service

# 方式1：直接创建
service = ContextInjectionService(
    max_context_length=4000,  # 最大上下文长度
    max_documents=10,          # 最大文档数量
    citation_format="[{index}]" # 引用格式
)

# 方式2：使用工厂函数创建
service = create_context_injection_service(
    max_context_length=4000,
    max_documents=10
)
```

### 2. 注入上下文到提示词

```python
from app.services.knowledge_base_service import KnowledgeBaseService
from app.services.hybrid_retriever import HybridRetriever

# 1. 初始化服务
kb_service = KnowledgeBaseService(db)
hybrid_retriever = HybridRetriever(kb_service, bm25_retriever)
context_service = ContextInjectionService(max_context_length=4000, max_documents=5)

# 2. 检索相关文档
query = "如何设计测试用例的边界值？"
retrieved_docs = hybrid_retriever.search(
    query=query,
    top_k=10,
    method="weighted"
)

# 3. 定义提示词模板
prompt_template = """
你是一个专业的测试设计助手。请根据以下参考文档回答用户的问题。

{{context}}

问题：{{query}}

请给出详细的答案，并在回答中引用相关文档。
"""

# 4. 注入上下文
result = context_service.inject_context(
    query=query,
    retrieved_docs=retrieved_docs,
    prompt_template=prompt_template,
    include_citations=True
)

# 5. 获取结果
final_prompt = result["prompt"]
context = result["context"]
citations = result["citations"]
used_docs = result["used_docs"]

print("最终提示词：")
print(final_prompt)
```

### 3. 从模型响应中提取引用

```python
# 假设模型生成的响应
model_response = """
根据[1]边界值测试规范，边界值测试应该包括以下场景：
- 正常边界值
- 边界值+1
- 边界值-1

同时，参考[2]测试设计方法，建议使用等价类划分法辅助测试设计。
"""

# 提取引用
citations = context_service.extract_citations(
    response=model_response,
    documents=used_docs
)

# 格式化引用列表
citation_text = context_service.format_citations(citations)
print(citation_text)

# 输出示例：
# 引用来源:
# ================================================================================
# 
# [1] 边界值测试规范
#   类型: 规范
#   相似度: 0.9523
#   内容: 边界值测试应该包括正常边界值、边界值+1、边界值-1...
# 
# [2] 测试设计方法
#   类型: 业务规则
#   相似度: 0.8845
#   内容: 建议使用等价类划分法辅助测试设计...
```

## 高级功能

### 1. 文档选择策略

服务会自动按以下优先级选择文档：

1. 按相关性分数排序（rerank_score > score > similarity）
2. 受限于文档数量（max_documents）
3. 受限于上下文长度（max_context_length）

```python
# 调整参数以获得更好的结果
service = ContextInjectionService(
    max_context_length=8000,  # 增加上下文长度
    max_documents=15,          # 增加文档数量
    citation_format="[ref_{index}]"  # 自定义引用格式
)

# 选择文档
selected_docs = service.select_documents(
    retrieved_docs=retrieved_docs,
    max_length=8000,
    max_docs=15
)
```

### 2. 上下文验证

```python
# 验证上下文长度是否在限制范围内
context = "很长的上下文内容..."
validation = service.validate_context_length(context)

if not validation["valid"]:
    print(f"上下文超出限制 {validation['exceeded']} 字符")
    print(f"当前长度: {validation['length']}")
    print(f"最大长度: {validation['max_length']}")
    print(f"使用率: {validation['percentage']:.1f}%")
```

### 3. 上下文截断

```python
# 截断上下文以适应长度限制
context = "很长的上下文内容..."
truncated = service.truncate_context_to_fit(
    context=context,
    max_length=4000,
    add_truncation_marker=True
)

print(f"截断后长度: {len(truncated)}")
```

### 4. 获取上下文统计信息

```python
# 获取统计信息
stats = service.get_context_stats(context)
print(f"字符数: {stats['char_count']}")
print(f"词数: {stats['word_count']}")
print(f"行数: {stats['line_count']}")
```

## 完整示例：与重排序服务集成

```python
from app.services.knowledge_base_service import KnowledgeBaseService
from app.services.bm25_retriever import BM25Retriever
from app.services.hybrid_retriever import HybridRetriever
from app.services.reranker_service import RerankerService
from app.services.context_injection_service import ContextInjectionService

# 初始化所有服务
kb_service = KnowledgeBaseService(db)
bm25_retriever = BM25Retriever(k1=1.5, b=0.75)
hybrid_retriever = HybridRetriever(
    knowledge_base_service=kb_service,
    bm25_retriever=bm25_retriever,
    vector_weight=0.7,
    bm25_weight=0.3
)
reranker = RerankerService(model_name="BAAI/bge-reranker-large")
context_service = ContextInjectionService(
    max_context_length=4000,
    max_documents=5
)

# 完整的RAG流程
query = "如何设计测试用例？"

# 1. 混合检索
retrieved_docs = hybrid_retriever.search(query=query, top_k=20)

# 2. 重排序
reranked_docs = reranker.rerank(query=query, documents=retrieved_docs, top_k=10)

# 3. 上下文注入
prompt_template = """
你是测试设计助手，请根据以下参考文档回答用户的问题。

{{context}}

问题：{{query}}

请给出详细的答案，并在回答中引用相关文档。
"""

injection_result = context_service.inject_context(
    query=query,
    retrieved_docs=reranked_docs,
    prompt_template=prompt_template,
    include_citations=True
)

# 4. 使用最终提示词调用大模型
final_prompt = injection_result["prompt"]
# 调用LLM...
# response = llm.generate(final_prompt)

# 5. 从响应中提取引用
# citations = context_service.extract_citations(response, injection_result["used_docs"])
# citation_text = context_service.format_citations(citations)
```

## 自定义配置

### 自定义引用格式

```python
# 使用不同的引用格式
service1 = ContextInjectionService(citation_format="[{index}]")  # [1], [2], [3]
service2 = ContextInjectionService(citation_format="(ref_{index})")  # (ref_1), (ref_2), (ref_3)
service3 = ContextInjectionService(citation_format="📄{index}")  # 📄1, 📄2, 📄3
```

### 自定义上下文占位符

```python
# 使用不同的占位符
result = context_service.inject_context(
    query=query,
    retrieved_docs=retrieved_docs,
    prompt_template=prompt_template,
    include_citations=True,
    context_placeholder="{{knowledge_base}}"  # 使用自定义占位符
)
```

## 最佳实践

### 1. 参数调优

- **max_context_length**: 根据模型上下文窗口设置
  - 小模型（7B）：2000-4000
  - 中等模型（13B）：4000-8000
  - 大模型（70B+）：8000-16000

- **max_documents**: 通常设置5-10个文档
  - 太少：信息不足
  - 太多：上下文冗余，增加成本

### 2. 引用标注

```python
# 始终启用引用标注，便于溯源
result = context_service.inject_context(
    query=query,
    retrieved_docs=retrieved_docs,
    prompt_template=prompt_template,
    include_citations=True  # 建议始终为True
)

# 在提示词中引导模型使用引用
prompt_template = """
...{{context}}...

请引用相关文档，使用[1]、[2]等格式标注。
"""
```

### 3. 错误处理

```python
try:
    result = context_service.inject_context(
        query=query,
        retrieved_docs=retrieved_docs,
        prompt_template=prompt_template
    )
    
    # 验证结果
    if not result["used_docs"]:
        print("警告：没有选择任何文档")
        return "抱歉，没有找到相关信息"
    
    # 检查上下文长度
    validation = context_service.validate_context_length(result["context"])
    if not validation["valid"]:
        print(f"警告：上下文超出限制 {validation['exceeded']} 字符")
    
except Exception as e:
    print(f"上下文注入失败: {str(e)}")
    return None
```

## 性能优化

### 1. 批量处理

```python
# 批量处理多个查询
queries = ["如何设计测试用例？", "边界值测试包括哪些场景？"]
for query in queries:
    result = context_service.inject_context(
        query=query,
        retrieved_docs=retrieved_docs,
        prompt_template=prompt_template
    )
    # 处理结果...
```

### 2. 缓存上下文

```python
from functools import lru_cache

@lru_cache(maxsize=100)
def get_cached_context(query: str):
    retrieved_docs = hybrid_retriever.search(query=query, top_k=10)
    result = context_service.inject_context(
        query=query,
        retrieved_docs=retrieved_docs,
        prompt_template=prompt_template
    )
    return result
```

## 常见问题

### Q1: 如何调整上下文窗口大小？

A: 根据模型的上下文窗口大小调整：
```python
# GPT-3.5: 4096 tokens
service = ContextInjectionService(max_context_length=4000)

# GPT-4: 8192 tokens
service = ContextInjectionService(max_context_length=8000)

# Claude: 100000 tokens
service = ContextInjectionService(max_context_length=50000)
```

### Q2: 如何处理超长的上下文？

A: 使用截断功能：
```python
# 验证并截断
validation = service.validate_context_length(context)
if not validation["valid"]:
    context = service.truncate_context_to_fit(
        context=context,
        max_length=service.max_context_length
    )
```

### Q3: 如何提高引用准确性？

A: 优化检索和重排序：
```python
# 1. 使用混合检索
retrieved_docs = hybrid_retriever.search(
    query=query,
    top_k=20,  # 检索更多文档
    method="weighted"
)

# 2. 使用重排序
reranked_docs = reranker.rerank(
    query=query,
    documents=retrieved_docs,
    top_k=10  # 保留最相关的10个
)

# 3. 使用重排序后的结果
result = context_service.inject_context(
    query=query,
    retrieved_docs=reranked_docs,  # 使用重排序后的文档
    prompt_template=prompt_template
)
```

## 总结

`ContextInjectionService` 提供了完整的上下文管理和引用溯源功能，与检索和重排序服务配合使用，可以构建高质量的RAG系统。

核心方法：
- `inject_context()`: 注入上下文到提示词
- `select_documents()`: 智能选择文档
- `extract_citations()`: 从响应中提取引用
- `format_citations()`: 格式化引用列表
- `validate_context_length()`: 验证上下文长度
- `truncate_context_to_fit()`: 截断上下文
- `get_context_stats()`: 获取统计信息

