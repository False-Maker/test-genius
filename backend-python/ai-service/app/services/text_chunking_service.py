"""
文本分块服务
参考Dify的文本分块实现，支持多种分块策略
"""
from typing import List, Dict, Optional
from enum import Enum
import logging
import re
import hashlib

try:
    import spacy
    SPACY_AVAILABLE = True
except ImportError:
    SPACY_AVAILABLE = False
    logging.warning("spacy未安装，语义分块功能不可用")

logger = logging.getLogger(__name__)


class ChunkingStrategy(str, Enum):
    """分块策略"""
    PARAGRAPH = "paragraph"  # 按段落分块
    SENTENCE = "sentence"    # 按句子分块
    FIXED_SIZE = "fixed_size"  # 按固定长度分块
    SEMANTIC = "semantic"    # 语义分块
    RECURSIVE = "recursive"  # 递归分块


class TextChunkingService:
    """文本分块服务"""
    
    def __init__(
        self,
        strategy: ChunkingStrategy = ChunkingStrategy.PARAGRAPH,
        chunk_size: int = 1000,
        chunk_overlap: int = 200,
        min_chunk_size: int = 50,
        max_chunk_size: int = 3000
    ):
        """
        初始化分块服务
        
        Args:
            strategy: 分块策略
            chunk_size: 分块大小（字符数）
            chunk_overlap: 分块重叠大小（字符数）
            min_chunk_size: 最小分块大小
            max_chunk_size: 最大分块大小
        """
        self.strategy = strategy
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap
        self.min_chunk_size = min_chunk_size
        self.max_chunk_size = max_chunk_size
        self.nlp = None
        
        if strategy == ChunkingStrategy.SEMANTIC:
            if SPACY_AVAILABLE:
                try:
                    self.nlp = spacy.load("zh_core_web_sm")
                    logger.info("spacy模型加载成功")
                except OSError:
                    logger.warning("zh_core_web_sm模型未找到，请运行: python -m spacy download zh_core_web_sm")
            else:
                logger.warning("spacy未安装，语义分块将降级为段落分块")
    
    def chunk_text(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """
        分块文本
        
        Args:
            text: 待分块的文本
            metadata: 文档元数据（会传递给每个分块）
            
        Returns:
            分块列表，每个分块包含：
            - content: 分块内容
            - metadata: 元数据（包含原始元数据 + 分块索引、位置等）
            - chunk_id: 分块ID
        """
        if not text or not text.strip():
            logger.warning("文本为空，无法分块")
            return []
        
        # 处理元数据
        if metadata is None:
            metadata = {}
        
        # 清理文本
        cleaned_text = self._clean_text(text)
        
        # 根据策略选择分块方法
        if self.strategy == ChunkingStrategy.PARAGRAPH:
            chunks = self.chunk_by_paragraph(cleaned_text, metadata)
        elif self.strategy == ChunkingStrategy.SENTENCE:
            chunks = self.chunk_by_sentence(cleaned_text, metadata)
        elif self.strategy == ChunkingStrategy.FIXED_SIZE:
            chunks = self.chunk_by_fixed_size(cleaned_text, metadata)
        elif self.strategy == ChunkingStrategy.SEMANTIC:
            chunks = self.chunk_by_semantic(cleaned_text, metadata)
        elif self.strategy == ChunkingStrategy.RECURSIVE:
            chunks = self.chunk_recursive(cleaned_text, metadata)
        else:
            logger.warning(f"未知的分块策略: {self.strategy}，使用段落分块")
            chunks = self.chunk_by_paragraph(cleaned_text, metadata)
        
        logger.info(f"文本分块完成: 策略={self.strategy}, 分块数={len(chunks)}")
        return chunks
    
    def chunk_by_paragraph(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """按段落分块"""
        if metadata is None:
            metadata = {}
        
        chunks = []
        paragraphs = [p.strip() for p in text.split('\n') if p.strip()]
        
        for idx, paragraph in enumerate(paragraphs):
            # 如果段落过长，需要进一步分割
            if len(paragraph) > self.max_chunk_size:
                sub_chunks = self._split_long_text(paragraph, idx)
                chunks.extend(sub_chunks)
            else:
                chunks.append({
                    "content": paragraph,
                    "metadata": {
                        **metadata,
                        "chunk_index": idx,
                        "chunk_type": "paragraph",
                        "chunk_strategy": self.strategy.value
                    },
                    "chunk_id": self._generate_chunk_id(paragraph, idx),
                    "chunk_length": len(paragraph)
                })
        
        # 如果需要重叠
        if self.chunk_overlap > 0:
            chunks = self._add_overlap_to_chunks(chunks)
        
        return chunks
    
    def chunk_by_sentence(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """按句子分块"""
        if metadata is None:
            metadata = {}
        
        # 句子分隔符
        sentence_endings = r'[。！？.!?]+'
        sentences = [s.strip() for s in re.split(sentence_endings, text) if s.strip()]
        
        chunks = []
        current_chunk = ""
        chunk_index = 0
        
        for sentence in sentences:
            # 如果当前分块加上新句子超过大小限制
            if len(current_chunk) + len(sentence) > self.chunk_size and current_chunk:
                # 保存当前分块
                if len(current_chunk) >= self.min_chunk_size:
                    chunks.append({
                        "content": current_chunk.strip(),
                        "metadata": {
                            **metadata,
                            "chunk_index": chunk_index,
                            "chunk_type": "sentence_group",
                            "chunk_strategy": self.strategy.value
                        },
                        "chunk_id": self._generate_chunk_id(current_chunk, chunk_index),
                        "chunk_length": len(current_chunk)
                    })
                    chunk_index += 1
                
                # 创建新分块，包含重叠内容
                overlap_start = max(0, len(current_chunk) - self.chunk_overlap)
                current_chunk = current_chunk[overlap_start:] + " " + sentence
            else:
                if current_chunk:
                    current_chunk += " "
                current_chunk += sentence
        
        # 保存最后一个分块
        if current_chunk.strip() and len(current_chunk) >= self.min_chunk_size:
            chunks.append({
                "content": current_chunk.strip(),
                "metadata": {
                    **metadata,
                    "chunk_index": chunk_index,
                    "chunk_type": "sentence_group",
                    "chunk_strategy": self.strategy.value
                },
                "chunk_id": self._generate_chunk_id(current_chunk, chunk_index),
                "chunk_length": len(current_chunk)
            })
        
        return chunks
    
    def chunk_by_fixed_size(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """按固定长度分块（支持重叠）"""
        if metadata is None:
            metadata = {}
        
        chunks = []
        start = 0
        chunk_index = 0
        
        while start < len(text):
            end = start + self.chunk_size
            
            # 确保不在句子中间截断
            if end < len(text):
                # 向后找最近的句子结束符
                for i in range(end, min(end + 200, len(text))):
                    if text[i] in '。！？.!?':
                        end = i + 1
                        break
                else:
                    # 如果没找到句子结束符，尝试在标点符号处分割
                    for i in range(end, max(start, end - 50), -1):
                        if text[i] in '，,;；':
                            end = i + 1
                            break
            
            chunk_text = text[start:end].strip()
            
            if len(chunk_text) >= self.min_chunk_size:
                chunks.append({
                    "content": chunk_text,
                    "metadata": {
                        **metadata,
                        "chunk_index": chunk_index,
                        "chunk_type": "fixed_size",
                        "chunk_strategy": self.strategy.value,
                        "chunk_start": start,
                        "chunk_end": end
                    },
                    "chunk_id": self._generate_chunk_id(chunk_text, chunk_index),
                    "chunk_length": len(chunk_text)
                })
                chunk_index += 1
            
            # 下一个分块的起始位置（考虑重叠）
            start = end - self.chunk_overlap
            
            if start <= 0:
                start = end
        
        return chunks
    
    def chunk_by_semantic(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """语义分块（使用NLP识别段落边界）"""
        if metadata is None:
            metadata = {}
        
        # 如果spacy不可用，降级为段落分块
        if not self.nlp:
            logger.warning("spacy不可用，使用段落分块代替语义分块")
            return self.chunk_by_paragraph(text, metadata)
        
        try:
            doc = self.nlp(text)
            
            chunks = []
            current_chunk = []
            current_length = 0
            chunk_index = 0
            
            for sent in doc.sents:
                sent_text = sent.text.strip()
                
                # 如果当前分块加上新句子超过大小限制
                if current_length + len(sent_text) > self.chunk_size and current_chunk:
                    chunk_content = " ".join(current_chunk).strip()
                    
                    if len(chunk_content) >= self.min_chunk_size:
                        chunks.append({
                            "content": chunk_content,
                            "metadata": {
                                **metadata,
                                "chunk_index": chunk_index,
                                "chunk_type": "semantic",
                                "chunk_strategy": self.strategy.value
                            },
                            "chunk_id": self._generate_chunk_id(chunk_content, chunk_index),
                            "chunk_length": len(chunk_content)
                        })
                        chunk_index += 1
                    
                    # 创建新分块，保留最后几个句子作为重叠
                    overlap_count = min(len(current_chunk), self.chunk_overlap // 100 + 1)
                    current_chunk = current_chunk[-overlap_count:] if overlap_count > 0 else []
                    current_length = sum(len(s) for s in current_chunk)
                
                current_chunk.append(sent_text)
                current_length += len(sent_text)
            
            # 保存最后一个分块
            if current_chunk:
                chunk_content = " ".join(current_chunk).strip()
                if len(chunk_content) >= self.min_chunk_size:
                    chunks.append({
                        "content": chunk_content,
                        "metadata": {
                            **metadata,
                            "chunk_index": chunk_index,
                            "chunk_type": "semantic",
                            "chunk_strategy": self.strategy.value
                        },
                        "chunk_id": self._generate_chunk_id(chunk_content, chunk_index),
                        "chunk_length": len(chunk_content)
                    })
            
            return chunks
            
        except Exception as e:
            logger.error(f"语义分块失败: {str(e)}，降级为段落分块")
            return self.chunk_by_paragraph(text, metadata)
    
    def chunk_recursive(self, text: str, metadata: Optional[Dict] = None) -> List[Dict]:
        """递归分块（先按章节，再按段落）"""
        if metadata is None:
            metadata = {}
        
        chunks = []
        
        # 第一步：尝试按章节分割（Markdown标题或数字编号）
        section_pattern = r'\n#{1,3}\s+.*|\n第[一二三四五六七八九十]+[章节部分].*\n'
        sections = re.split(section_pattern, text)
        sections = [s.strip() for s in sections if s.strip()]
        
        # 如果没有找到章节，使用整个文本
        if len(sections) <= 1:
            sections = [text]
        
        for section_idx, section in enumerate(sections):
            if len(section) > self.chunk_size:
                # 如果章节太长，按段落分割
                paragraphs = [p.strip() for p in section.split('\n') if p.strip()]
                
                current_chunk = ""
                chunk_index = 0
                
                for para in paragraphs:
                    if len(current_chunk) + len(para) > self.chunk_size and current_chunk:
                        if len(current_chunk) >= self.min_chunk_size:
                            chunks.append({
                                "content": current_chunk.strip(),
                                "metadata": {
                                    **metadata,
                                    "chunk_index": len(chunks),
                                    "chunk_type": "recursive",
                                    "chunk_strategy": self.strategy.value,
                                    "section_index": section_idx
                                },
                                "chunk_id": self._generate_chunk_id(current_chunk, len(chunks)),
                                "chunk_length": len(current_chunk)
                            })
                        
                        # 重叠
                        overlap_start = max(0, len(current_chunk) - self.chunk_overlap)
                        current_chunk = current_chunk[overlap_start:] + "\n\n" + para
                    else:
                        if current_chunk:
                            current_chunk += "\n\n"
                        current_chunk += para
                
                # 保存最后一个分块
                if current_chunk.strip() and len(current_chunk) >= self.min_chunk_size:
                    chunks.append({
                        "content": current_chunk.strip(),
                        "metadata": {
                            **metadata,
                            "chunk_index": len(chunks),
                            "chunk_type": "recursive",
                            "chunk_strategy": self.strategy.value,
                            "section_index": section_idx
                        },
                        "chunk_id": self._generate_chunk_id(current_chunk, len(chunks)),
                        "chunk_length": len(current_chunk)
                    })
            else:
                # 章节长度合适，直接作为一个分块
                if len(section) >= self.min_chunk_size:
                    chunks.append({
                        "content": section,
                        "metadata": {
                            **metadata,
                            "chunk_index": len(chunks),
                            "chunk_type": "recursive",
                            "chunk_strategy": self.strategy.value,
                            "section_index": section_idx
                        },
                        "chunk_id": self._generate_chunk_id(section, len(chunks)),
                        "chunk_length": len(section)
                    })
        
        return chunks
    
    def _add_overlap_to_chunks(self, chunks: List[Dict]) -> List[Dict]:
        """添加重叠内容到分块"""
        if not chunks or self.chunk_overlap <= 0:
            return chunks
        
        overlapped_chunks = []
        
        for i, chunk in enumerate(chunks):
            content = chunk["content"]
            
            # 添加前一个分块的重叠部分
            if i > 0:
                prev_content = chunks[i - 1]["content"]
                overlap_text = prev_content[-self.chunk_overlap:] if len(prev_content) > self.chunk_overlap else prev_content
                content = overlap_text + "\n\n" + content
            
            overlapped_chunks.append({
                **chunk,
                "content": content,
                "metadata": {
                    **chunk["metadata"],
                    "has_overlap": i > 0
                }
            })
        
        return overlapped_chunks
    
    def _split_long_text(self, text: str, base_index: int) -> List[Dict]:
        """分割过长的文本"""
        chunks = []
        
        # 使用固定大小分割
        sub_chunks = self.chunk_by_fixed_size(text, {"original_chunk_index": base_index})
        
        # 更新chunk_index
        for i, chunk in enumerate(sub_chunks):
            chunk["metadata"]["chunk_index"] = base_index + i
        
        return sub_chunks
    
    def _clean_text(self, text: str) -> str:
        """清理文本"""
        # 去除多余的空白字符
        text = re.sub(r'\n{3,}', '\n\n', text)
        text = re.sub(r' {2,}', ' ', text)
        
        # 去除控制字符
        text = ''.join(c for c in text if c.isprintable() or c in '\n\r\t')
        
        return text.strip()
    
    def _validate_chunk(self, chunk: str) -> bool:
        """验证分块质量"""
        # 检查长度
        if len(chunk) < self.min_chunk_size or len(chunk) > self.max_chunk_size:
            return False
        
        # 检查是否以句子结尾
        if not chunk[-1] in '。！？.!?":\'”）':
            # 检查最后100个字符中是否有句子结束符
            if not any(c in chunk[-100:] for c in '。！？.!?'):
                return False
        
        return True
    
    def _generate_chunk_id(self, content: str, index: int) -> str:
        """生成分块ID"""
        # 使用内容的哈希值和索引生成唯一ID
        content_hash = hashlib.md5(content.encode('utf-8')).hexdigest()[:8]
        return f"chunk_{index}_{content_hash}"
    
    def get_statistics(self, chunks: List[Dict]) -> Dict:
        """获取分块统计信息"""
        if not chunks:
            return {
                "total_chunks": 0,
                "total_length": 0,
                "avg_length": 0,
                "min_length": 0,
                "max_length": 0
            }
        
        lengths = [chunk["chunk_length"] for chunk in chunks]
        
        return {
            "total_chunks": len(chunks),
            "total_length": sum(lengths),
            "avg_length": sum(lengths) / len(lengths),
            "min_length": min(lengths),
            "max_length": max(lengths),
            "strategy": self.strategy.value
        }

