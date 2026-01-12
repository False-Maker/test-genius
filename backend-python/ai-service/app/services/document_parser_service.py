"""
文档解析服务
支持Word和PDF文档解析
"""
import logging
import os
from typing import Dict, List, Optional
from pathlib import Path

try:
    from docx import Document
    DOCX_AVAILABLE = True
except ImportError:
    DOCX_AVAILABLE = False
    logging.warning("python-docx未安装，Word文档解析功能不可用")

try:
    import PyPDF2
    PDF_AVAILABLE = True
except ImportError:
    PDF_AVAILABLE = False
    logging.warning("PyPDF2未安装，PDF文档解析功能不可用")

logger = logging.getLogger(__name__)


class DocumentParserService:
    """文档解析服务"""
    
    def __init__(self):
        self.supported_formats = []
        if DOCX_AVAILABLE:
            self.supported_formats.append("docx")
            self.supported_formats.append("doc")
        if PDF_AVAILABLE:
            self.supported_formats.append("pdf")
    
    def parse_document(self, file_path: str) -> Dict:
        """
        解析文档
        
        Args:
            file_path: 文档文件路径
            
        Returns:
            解析结果字典，包含：
            - content: 文档文本内容
            - structure: 文档结构信息
            - metadata: 文档元数据
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文件不存在: {file_path}")
        
        file_extension = Path(file_path).suffix.lower().lstrip('.')
        
        logger.info(f"开始解析文档: 文件路径={file_path}, 文件类型={file_extension}")
        
        try:
            if file_extension in ["docx", "doc"]:
                return self._parse_word(file_path)
            elif file_extension == "pdf":
                return self._parse_pdf(file_path)
            else:
                raise ValueError(f"不支持的文件格式: {file_extension}")
        except Exception as e:
            logger.error(f"文档解析失败: 文件路径={file_path}, 错误={str(e)}", exc_info=True)
            raise
    
    def _parse_word(self, file_path: str) -> Dict:
        """解析Word文档"""
        if not DOCX_AVAILABLE:
            raise ImportError("python-docx未安装，无法解析Word文档")
        
        try:
            doc = Document(file_path)
            
            # 提取文本内容
            paragraphs = []
            for para in doc.paragraphs:
                text = para.text.strip()
                if text:
                    paragraphs.append(text)
            
            content = "\n".join(paragraphs)
            
            # 提取标题结构
            structure = self._extract_structure(paragraphs)
            
            # 提取元数据
            metadata = {
                "title": doc.core_properties.title or "",
                "author": doc.core_properties.author or "",
                "created": str(doc.core_properties.created) if doc.core_properties.created else "",
                "modified": str(doc.core_properties.modified) if doc.core_properties.modified else "",
            }
            
            logger.info(f"Word文档解析成功: 段落数={len(paragraphs)}, 字符数={len(content)}")
            
            return {
                "content": content,
                "structure": structure,
                "metadata": metadata,
                "paragraph_count": len(paragraphs),
                "char_count": len(content)
            }
            
        except Exception as e:
            logger.error(f"Word文档解析失败: {str(e)}", exc_info=True)
            raise
    
    def _parse_pdf(self, file_path: str) -> Dict:
        """解析PDF文档"""
        if not PDF_AVAILABLE:
            raise ImportError("PyPDF2未安装，无法解析PDF文档")
        
        try:
            paragraphs = []
            metadata = {}
            
            with open(file_path, 'rb') as file:
                pdf_reader = PyPDF2.PdfReader(file)
                
                # 提取元数据
                if pdf_reader.metadata:
                    metadata = {
                        "title": pdf_reader.metadata.get("/Title", ""),
                        "author": pdf_reader.metadata.get("/Author", ""),
                        "subject": pdf_reader.metadata.get("/Subject", ""),
                        "creator": pdf_reader.metadata.get("/Creator", ""),
                    }
                
                # 提取文本内容
                for page_num, page in enumerate(pdf_reader.pages):
                    try:
                        text = page.extract_text()
                        if text.strip():
                            paragraphs.append(text.strip())
                    except Exception as e:
                        logger.warning(f"PDF第{page_num + 1}页解析失败: {str(e)}")
                        continue
            
            content = "\n".join(paragraphs)
            
            # 提取结构
            structure = self._extract_structure(paragraphs)
            
            logger.info(f"PDF文档解析成功: 页数={len(pdf_reader.pages)}, 段落数={len(paragraphs)}, 字符数={len(content)}")
            
            return {
                "content": content,
                "structure": structure,
                "metadata": metadata,
                "page_count": len(pdf_reader.pages) if 'pdf_reader' in locals() else 0,
                "paragraph_count": len(paragraphs),
                "char_count": len(content)
            }
            
        except Exception as e:
            logger.error(f"PDF文档解析失败: {str(e)}", exc_info=True)
            raise
    
    def _extract_structure(self, paragraphs: List[str]) -> Dict:
        """
        提取文档结构
        
        识别标题、章节等结构信息
        """
        structure = {
            "title": "",
            "sections": [],
            "headings": []
        }
        
        if not paragraphs:
            return structure
        
        # 第一段通常作为标题
        if paragraphs:
            structure["title"] = paragraphs[0][:100]  # 取前100个字符作为标题
        
        # 识别标题（简单规则：短段落且包含数字编号的可能是标题）
        headings = []
        for para in paragraphs:
            # 简单的标题识别规则
            if len(para) < 100 and (para.startswith(("第", "一、", "二、", "三、", "1.", "2.", "3.")) or 
                                   para.endswith(("：", ":"))):
                headings.append(para)
        
        structure["headings"] = headings[:20]  # 最多保留20个标题
        
        return structure
    
    def extract_key_info(self, content: str) -> Dict:
        """
        提取关键信息
        
        从文档内容中提取需求相关的关键信息
        """
        # 简单的关键词提取（后续可以用NLP技术优化）
        keywords = []
        
        # 提取可能的关键词（长度2-6的中文词汇）
        import re
        words = re.findall(r'[\u4e00-\u9fa5]{2,6}', content)
        
        # 统计词频
        from collections import Counter
        word_freq = Counter(words)
        
        # 取频率最高的前20个词作为关键词
        keywords = [word for word, freq in word_freq.most_common(20)]
        
        return {
            "keywords": keywords,
            "content_length": len(content),
            "sentence_count": len(re.split(r'[。！？\n]', content))
        }

