"""
上下文注入服务测试
"""
import sys
import os
import io

# 设置标准输出编码为UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# 添加app目录到Python路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from app.services.context_injection_service import ContextInjectionService, create_context_injection_service


def test_create_service():
    """测试创建服务"""
    service = ContextInjectionService(max_context_length=2000, max_documents=5)
    assert service.max_context_length == 2000
    assert service.max_documents == 5
    print("[PASS] 测试创建服务成功")
    return True


def test_inject_context_empty_docs():
    """测试空文档列表的上下文注入"""
    service = ContextInjectionService()
    prompt_template = "根据以下上下文回答问题：\n{{context}}\n\n问题：{{query}}"
    
    result = service.inject_context(
        query="测试问题",
        retrieved_docs=[],
        prompt_template=prompt_template
    )
    
    assert "prompt" in result
    assert "context" in result
    assert "citations" in result
    assert "used_docs" in result
    assert result["citations"] == []
    assert result["used_docs"] == []
    assert "暂无相关上下文" in result["prompt"]
    print("[PASS] 测试空文档列表的上下文注入成功")


def test_inject_context_with_docs():
    """测试有文档的上下文注入"""
    service = ContextInjectionService(max_context_length=5000, max_documents=3)
    prompt_template = "根据以下上下文回答问题：\n{{context}}\n\n问题：{{query}}"
    
    # 模拟检索到的文档
    docs = [
        {
            "id": 1,
            "doc_code": "DOC001",
            "doc_name": "测试文档1",
            "doc_type": "业务规则",
            "content": "这是一个测试文档的内容，包含一些业务规则说明。",
            "similarity": 0.95
        },
        {
            "id": 2,
            "doc_code": "DOC002",
            "doc_name": "测试文档2",
            "doc_type": "规范",
            "content": "这是另一个测试文档，包含规范说明。",
            "similarity": 0.88
        }
    ]
    
    result = service.inject_context(
        query="测试问题",
        retrieved_docs=docs,
        prompt_template=prompt_template,
        include_citations=True
    )
    
    assert "prompt" in result
    assert "context" in result
    assert len(result["used_docs"]) == 2
    assert len(result["citations"]) == 2
    assert result["used_docs"][0]["citation_id"] == "[1]"
    assert result["used_docs"][1]["citation_id"] == "[2]"
    assert "测试文档1" in result["context"]
    assert "测试文档2" in result["context"]
    print("[PASS] 测试有文档的上下文注入成功")


def test_select_documents_by_score():
    """测试按分数选择文档"""
    service = ContextInjectionService(max_context_length=1000, max_documents=2)
    
    # 模拟多个文档，分数不同
    docs = [
        {"id": 1, "content": "低分文档" * 10, "similarity": 0.5},
        {"id": 2, "content": "高分文档" * 10, "similarity": 0.95},
        {"id": 3, "content": "中分文档" * 10, "similarity": 0.75},
    ]
    
    selected = service.select_documents(docs, max_length=1000, max_docs=2)
    
    assert len(selected) <= 2
    # 应该选择分数最高的文档
    assert selected[0]["id"] == 2 or selected[1]["id"] == 2
    print("[PASS] 测试按分数选择文档成功")


def test_select_documents_by_length():
    """测试按长度限制选择文档"""
    service = ContextInjectionService()
    
    # 模拟长文档
    docs = [
        {"id": 1, "content": "A" * 500, "similarity": 0.95},
        {"id": 2, "content": "B" * 500, "similarity": 0.90},
    ]
    
    selected = service.select_documents(docs, max_length=400, max_docs=10)
    
    # 由于长度限制，应该只选择一个文档
    assert len(selected) == 1
    # 应该选择分数高的
    assert selected[0]["id"] == 1
    print("[PASS] 测试按长度限制选择文档成功")


def test_format_context():
    """测试上下文格式化"""
    service = ContextInjectionService()
    
    docs = [
        {
            "content": "测试内容1",
            "doc_name": "文档1",
            "doc_type": "类型1",
            "citation_id": "[1]",
            "citation_index": 1
        },
        {
            "content": "测试内容2",
            "doc_name": "文档2",
            "doc_type": "类型2",
            "citation_id": "[2]",
            "citation_index": 2
        }
    ]
    
    context = service.format_context(docs, include_citations=True)
    
    assert "[1]" in context
    assert "[2]" in context
    assert "文档1" in context
    assert "文档2" in context
    assert "类型1" in context
    assert "类型2" in context
    assert "测试内容1" in context
    assert "测试内容2" in context
    print("[PASS] 测试上下文格式化成功")


def test_extract_citations():
    """测试从响应中提取引用"""
    service = ContextInjectionService()
    
    # 模拟文档
    docs = [
        {
            "id": 1,
            "doc_code": "DOC001",
            "doc_name": "文档1",
            "doc_type": "规范",
            "content": "文档1的内容",
            "citation_id": "[1]",
            "citation_index": 1
        },
        {
            "id": 2,
            "doc_code": "DOC002",
            "doc_name": "文档2",
            "doc_type": "业务规则",
            "content": "文档2的内容",
            "citation_id": "[2]",
            "citation_index": 2
        }
    ]
    
    # 模拟模型响应
    response = "根据[1]文档1的内容和[2]文档2的内容，我们得出以下结论。"
    
    citations = service.extract_citations(response, docs)
    
    assert len(citations) == 2
    assert citations[0]["index"] == 1
    assert citations[1]["index"] == 2
    assert citations[0]["doc_name"] == "文档1"
    assert citations[1]["doc_name"] == "文档2"
    print("[PASS] 测试从响应中提取引用成功")


def test_format_citations():
    """测试格式化引用列表"""
    service = ContextInjectionService()
    
    citations = [
        {
            "citation_id": "[1]",
            "index": 1,
            "doc_name": "文档1",
            "doc_type": "规范",
            "content": "文档1的内容...",
            "similarity": 0.95
        },
        {
            "citation_id": "[2]",
            "index": 2,
            "doc_name": "文档2",
            "doc_type": "业务规则",
            "content": "文档2的内容...",
            "similarity": 0.88
        }
    ]
    
    formatted = service.format_citations(citations)
    
    assert "引用来源" in formatted
    assert "[1]" in formatted
    assert "[2]" in formatted
    assert "文档1" in formatted
    assert "文档2" in formatted
    assert "0.95" in formatted
    print("[PASS] 测试格式化引用列表成功")


def test_validate_context_length():
    """测试验证上下文长度"""
    service = ContextInjectionService(max_context_length=1000)
    
    # 测试有效长度
    context = "A" * 500
    result = service.validate_context_length(context)
    assert result["valid"] is True
    assert result["length"] == 500
    assert result["exceeded"] == 0
    
    # 测试超长
    context = "A" * 1500
    result = service.validate_context_length(context)
    assert result["valid"] is False
    assert result["length"] == 1500
    assert result["exceeded"] == 500
    print("[PASS] 测试验证上下文长度成功")


def test_truncate_context_to_fit():
    """测试截断上下文以适应限制"""
    service = ContextInjectionService(max_context_length=100)
    
    context = "A" * 200 + "。这是第二个句子。"
    truncated = service.truncate_context_to_fit(context, max_length=50)
    
    # 截断后的长度应该在50-70之间（包括可能的截断标记）
    assert len(truncated) >= 50 and len(truncated) <= 70
    print("[PASS] 测试截断上下文以适应限制成功")


def test_get_context_stats():
    """测试获取上下文统计信息"""
    service = ContextInjectionService()
    
    context = "第一行\n第二行\n第三行 第四词 第五词"
    stats = service.get_context_stats(context)
    
    assert "length" in stats
    assert "char_count" in stats
    assert "word_count" in stats
    assert "line_count" in stats
    assert stats["char_count"] == len(context)
    assert stats["line_count"] == 3
    print("[PASS] 测试获取上下文统计信息成功")


def test_factory_function():
    """测试工厂函数"""
    service = create_context_injection_service(
        max_context_length=3000,
        max_documents=8,
        citation_format="[ref_{index}]"
    )
    
    assert isinstance(service, ContextInjectionService)
    assert service.max_context_length == 3000
    assert service.max_documents == 8
    assert service.citation_format == "[ref_{index}]"
    print("[PASS] 测试工厂函数成功")


def test_inject_context_without_citations():
    """测试不包含引用标注的上下文注入"""
    service = ContextInjectionService()
    prompt_template = "根据以下上下文回答问题：\n{{context}}\n\n问题：{{query}}"
    
    docs = [
        {
            "id": 1,
            "doc_code": "DOC001",
            "doc_name": "测试文档",
            "doc_type": "规范",
            "content": "测试文档内容",
            "similarity": 0.95
        }
    ]
    
    result = service.inject_context(
        query="测试问题",
        retrieved_docs=docs,
        prompt_template=prompt_template,
        include_citations=False
    )
    
    assert "prompt" in result
    assert result["context"] != ""
    # 不应该包含引用标注
    assert "[1]" not in result["context"]
    print("[PASS] 测试不包含引用标注的上下文注入成功")


def test_integration_with_reranked_docs():
    """测试与重排序结果的集成"""
    service = ContextInjectionService(max_context_length=2000, max_documents=3)
    
    # 模拟重排序后的文档
    docs = [
        {
            "id": 1,
            "doc_code": "DOC001",
            "doc_name": "高分文档",
            "content": "这是高分文档的内容，与查询最相关。",
            "similarity": 0.85,
            "rerank_score": 0.92
        },
        {
            "id": 2,
            "doc_code": "DOC002",
            "doc_name": "中分文档",
            "content": "这是中分文档的内容。",
            "similarity": 0.80,
            "rerank_score": 0.85
        },
        {
            "id": 3,
            "doc_code": "DOC003",
            "doc_name": "低分文档",
            "content": "这是低分文档的内容。",
            "similarity": 0.75,
            "rerank_score": 0.70
        }
    ]
    
    # 选择文档应该优先考虑rerank_score
    selected = service.select_documents(docs, max_length=2000, max_docs=2)
    
    assert len(selected) == 2
    assert selected[0]["id"] == 1  # rerank_score最高
    assert selected[1]["id"] == 2
    print("[PASS] 测试与重排序结果的集成成功")


if __name__ == "__main__":
    print("开始测试上下文注入服务...\n")
    
    test_create_service()
    test_inject_context_empty_docs()
    test_inject_context_with_docs()
    test_select_documents_by_score()
    test_select_documents_by_length()
    test_format_context()
    test_extract_citations()
    test_format_citations()
    test_validate_context_length()
    test_truncate_context_to_fit()
    test_get_context_stats()
    test_factory_function()
    test_inject_context_without_citations()
    test_integration_with_reranked_docs()
    
    print("\n所有测试通过！[PASS]")

