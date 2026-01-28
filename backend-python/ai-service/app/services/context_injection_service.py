"""
上下文注入服务
参考Dify的上下文注入实现，将检索结果注入到提示词中
"""
from typing import List, Dict, Optional
import logging
import re

logger = logging.getLogger(__name__)


class ContextInjectionService:
    """上下文注入服务"""
    
    def __init__(
        self,
        max_context_length: int = 4000,
        max_documents: int = 10,
        citation_format: str = "[{index}]"
    ):
        """
        初始化上下文注入服务
        
        Args:
            max_context_length: 最大上下文长度（字符数）
            max_documents: 最大文档数量
            citation_format: 引用格式（如"[{index}]"或"[doc_{index}]"）
        """
        self.max_context_length = max_context_length
        self.max_documents = max_documents
        self.citation_format = citation_format
        logger.info(f"上下文注入服务初始化: max_length={max_context_length}, max_docs={max_documents}")
    
    def inject_context(
        self,
        query: str,
        retrieved_docs: List[Dict],
        prompt_template: str,
        include_citations: bool = True,
        context_placeholder: str = "{{context}}"
    ) -> Dict:
        """
        注入上下文到提示词
        
        Args:
            query: 原始查询
            retrieved_docs: 检索到的文档列表
            prompt_template: 提示词模板（包含{{context}}变量）
            include_citations: 是否包含引用标注
            context_placeholder: 上下文占位符
            
        Returns:
            包含上下文的提示词，包含：
            - prompt: 注入上下文后的提示词
            - context: 使用的上下文
            - citations: 引用列表
            - used_docs: 使用的文档列表
        """
        if not retrieved_docs:
            logger.warning("检索到的文档列表为空")
            return {
                "prompt": prompt_template.replace(context_placeholder, "暂无相关上下文"),
                "context": "",
                "citations": [],
                "used_docs": []
            }
        
        logger.info(f"开始注入上下文: 文档数={len(retrieved_docs)}, include_citations={include_citations}")
        
        # 1. 选择要注入的文档片段
        selected_docs = self.select_documents(
            retrieved_docs,
            self.max_context_length,
            self.max_documents
        )
        
        # 2. 添加引用标注
        if include_citations:
            selected_docs = self.add_citations(selected_docs)
        
        # 3. 格式化上下文
        context = self.format_context(selected_docs, include_citations)
        
        # 4. 替换提示词中的上下文占位符
        prompt = prompt_template.replace(context_placeholder, context)
        
        # 5. 提取引用信息
        citations = self._extract_citation_info(selected_docs)
        
        logger.info(f"上下文注入完成: 使用文档数={len(selected_docs)}, 上下文长度={len(context)}")
        
        return {
            "prompt": prompt,
            "context": context,
            "citations": citations,
            "used_docs": selected_docs
        }
    
    def select_documents(
        self,
        retrieved_docs: List[Dict],
        max_length: int,
        max_docs: int
    ) -> List[Dict]:
        """
        选择要注入的文档片段
        
        Args:
            retrieved_docs: 检索到的文档列表
            max_length: 最大总长度
            max_docs: 最大文档数量
            
        Returns:
            选中的文档列表
        """
        # 按相关性分数排序（假设有similarity或score字段）
        sorted_docs = sorted(
            retrieved_docs,
            key=lambda x: self._get_score(x),
            reverse=True
        )
        
        selected_docs = []
        total_length = 0
        
        for i, doc in enumerate(sorted_docs):
            # 检查文档数量限制
            if len(selected_docs) >= max_docs:
                logger.info(f"已达到最大文档数量限制: {max_docs}")
                break
            
            # 获取文档内容
            content = self._get_content(doc)
            content_length = len(content)
            
            # 检查长度限制（预留引用标注的空间）
            citation_length = len(self.citation_format.format(index=i + 1)) if i < 100 else 5
            if total_length + content_length + citation_length > max_length:
                # 尝试截断内容以适应限制
                remaining_length = max_length - total_length - citation_length
                if remaining_length > 100:  # 至少保留100字符
                    truncated_content = content[:remaining_length] + "..."
                    selected_doc = doc.copy()
                    selected_doc["content"] = truncated_content
                    selected_doc["truncated"] = True
                    selected_docs.append(selected_doc)
                    logger.info(f"文档{i+1}内容被截断: 原长度={content_length}, 截断后={remaining_length}")
                break
            
            # 添加完整文档
            selected_doc = doc.copy()
            selected_doc["content"] = content
            selected_docs.append(selected_doc)
            total_length += content_length + citation_length
        
        logger.info(f"选中{len(selected_docs)}个文档, 总长度={total_length}")
        return selected_docs
    
    def format_context(
        self,
        documents: List[Dict],
        include_citations: bool = True
    ) -> str:
        """
        格式化上下文
        
        Args:
            documents: 文档列表
            include_citations: 是否包含引用标注
            
        Returns:
            格式化后的上下文文本
        """
        if not documents:
            return ""
        
        context_lines = []
        
        for i, doc in enumerate(documents):
            # 获取文档内容
            content = doc.get("content", "")
            
            # 获取文档元数据
            doc_name = doc.get("doc_name", "")
            doc_type = doc.get("doc_type", "")
            
            # 添加引用标注
            if include_citations:
                citation = self.citation_format.format(index=i + 1)
                header = f"\n参考文档{citation}"
                
                # 添加文档元数据
                if doc_name or doc_type:
                    meta_parts = []
                    if doc_name:
                        meta_parts.append(f"名称: {doc_name}")
                    if doc_type:
                        meta_parts.append(f"类型: {doc_type}")
                    if meta_parts:
                        header += f" ({', '.join(meta_parts)})"
                
                context_lines.append(header)
            else:
                context_lines.append(f"\n参考文档{i+1}:")
            
            # 添加文档内容
            context_lines.append(content)
        
        # 用分隔符连接
        context = "\n" + "-" * 80 + "\n".join(context_lines) + "\n" + "-" * 80
        
        return context
    
    def add_citations(
        self,
        documents: List[Dict]
    ) -> List[Dict]:
        """
        添加引用标注到文档
        
        Args:
            documents: 文档列表
            
        Returns:
            添加引用标注后的文档列表
        """
        for i, doc in enumerate(documents):
            doc["citation_id"] = self.citation_format.format(index=i + 1)
            doc["citation_index"] = i + 1
        
        return documents
    
    def extract_citations(
        self,
        response: str,
        documents: List[Dict]
    ) -> List[Dict]:
        """
        从模型响应中提取引用
        
        Args:
            response: 模型响应
            documents: 文档列表
            
        Returns:
            引用列表，每个引用包含：
            - citation_id: 引用ID（如[1]、[2]）
            - doc_id: 文档ID
            - doc_name: 文档名称
            - content: 引用内容
        """
        # 提取响应中的引用标记（如[1]、[2]等）
        citation_pattern = r'\[(\d+)\]'
        citation_matches = re.findall(citation_pattern, response)
        
        # 去重并转换为整数
        citation_indices = list(set(int(match) for match in citation_matches))
        citation_indices.sort()
        
        # 构建引用映射
        doc_map = {doc["citation_index"]: doc for doc in documents if "citation_index" in doc}
        
        # 提取引用信息
        citations = []
        for index in citation_indices:
            if index in doc_map:
                doc = doc_map[index]
                citation = {
                    "citation_id": self.citation_format.format(index=index),
                    "index": index,
                    "doc_id": doc.get("id"),
                    "doc_code": doc.get("doc_code"),
                    "doc_name": doc.get("doc_name", ""),
                    "doc_type": doc.get("doc_type", ""),
                    "content": doc.get("content", ""),
                    "original_content": doc.get("doc_content", doc.get("content", "")),
                    "similarity": doc.get("similarity"),
                    "rerank_score": doc.get("rerank_score")
                }
                citations.append(citation)
        
        logger.info(f"从响应中提取到{len(citations)}个引用")
        return citations
    
    def format_citations(
        self,
        citations: List[Dict]
    ) -> str:
        """
        格式化引用列表
        
        Args:
            citations: 引用列表
            
        Returns:
            格式化后的引用文本
        """
        if not citations:
            return ""
        
        lines = ["\n引用来源:", "=" * 80]
        
        for citation in citations:
            lines.append(f"\n{citation['citation_id']} {citation['doc_name']}")
            if citation.get('doc_type'):
                lines.append(f"  类型: {citation['doc_type']}")
            if citation.get('similarity'):
                lines.append(f"  相似度: {citation['similarity']:.4f}")
            if citation.get('rerank_score'):
                lines.append(f"  重排序分数: {citation['rerank_score']:.4f}")
            # 显示内容摘要（前200字符）
            content = citation['content']
            if len(content) > 200:
                content = content[:200] + "..."
            lines.append(f"  内容: {content}")
        
        return "\n".join(lines)
    
    def _get_score(self, doc: Dict) -> float:
        """
        获取文档的分数
        
        Args:
            doc: 文档字典
            
        Returns:
            分数值
        """
        # 优先使用rerank_score
        if "rerank_score" in doc:
            return float(doc["rerank_score"])
        
        # 其次使用score
        if "score" in doc:
            return float(doc["score"])
        
        # 最后使用similarity
        if "similarity" in doc:
            return float(doc["similarity"])
        
        return 0.0
    
    def _get_content(self, doc: Dict) -> str:
        """
        获取文档内容
        
        Args:
            doc: 文档字典
            
        Returns:
            文档内容字符串
        """
        # 尝试多个可能的字段
        content = doc.get("content") or doc.get("doc_content") or doc.get("text") or ""
        return str(content)
    
    def _extract_citation_info(self, documents: List[Dict]) -> List[Dict]:
        """
        提取文档的引用信息
        
        Args:
            documents: 文档列表
            
        Returns:
            引用信息列表
        """
        citations = []
        for doc in documents:
            citation = {
                "citation_id": doc.get("citation_id", ""),
                "index": doc.get("citation_index", 0),
                "doc_id": doc.get("id"),
                "doc_code": doc.get("doc_code"),
                "doc_name": doc.get("doc_name", ""),
                "doc_type": doc.get("doc_type", ""),
                "content": doc.get("content", ""),
                "similarity": doc.get("similarity"),
                "rerank_score": doc.get("rerank_score")
            }
            citations.append(citation)
        
        return citations
    
    def get_context_stats(self, context: str) -> Dict:
        """
        获取上下文统计信息
        
        Args:
            context: 上下文文本
            
        Returns:
            统计信息字典
        """
        stats = {
            "length": len(context),
            "char_count": len(context),
            "word_count": len(context.split()),
            "line_count": context.count('\n') + 1
        }
        return stats
    
    def validate_context_length(self, context: str, max_length: Optional[int] = None) -> Dict:
        """
        验证上下文长度是否在限制范围内
        
        Args:
            context: 上下文文本
            max_length: 最大长度（默认使用初始化时的值）
            
        Returns:
            验证结果字典，包含：
            - valid: 是否有效
            - length: 当前长度
            - max_length: 最大长度
            - exceeded: 超出的长度
        """
        if max_length is None:
            max_length = self.max_context_length
        
        length = len(context)
        exceeded = max(0, length - max_length)
        
        return {
            "valid": exceeded == 0,
            "length": length,
            "max_length": max_length,
            "exceeded": exceeded,
            "percentage": (length / max_length * 100) if max_length > 0 else 0
        }
    
    def truncate_context_to_fit(
        self,
        context: str,
        max_length: Optional[int] = None,
        add_truncation_marker: bool = True
    ) -> str:
        """
        截断上下文以适应长度限制
        
        Args:
            context: 上下文文本
            max_length: 最大长度
            add_truncation_marker: 是否添加截断标记
            
        Returns:
            截断后的上下文
        """
        if max_length is None:
            max_length = self.max_context_length
        
        if len(context) <= max_length:
            return context
        
        # 截断文本
        truncated = context[:max_length]
        
        # 尝试在最后一个完整句子处截断
        last_period = truncated.rfind('。')
        if last_period > max_length * 0.8:  # 如果最后一个句号在80%位置之后
            truncated = truncated[:last_period + 1]
        
        # 添加截断标记
        if add_truncation_marker:
            truncated += "\n\n[上下文已截断，部分内容未显示]"
        
        return truncated


# 工厂函数
def create_context_injection_service(
    max_context_length: int = 4000,
    max_documents: int = 10,
    citation_format: str = "[{index}]"
) -> ContextInjectionService:
    """
    创建上下文注入服务
    
    Args:
        max_context_length: 最大上下文长度
        max_documents: 最大文档数量
        citation_format: 引用格式
        
    Returns:
        ContextInjectionService实例
    """
    return ContextInjectionService(
        max_context_length=max_context_length,
        max_documents=max_documents,
        citation_format=citation_format
    )

