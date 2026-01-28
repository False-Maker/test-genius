"""
通用工具
"""
import logging
from typing import Dict, Any, List
from app.services.agent_engine import BaseTool

logger = logging.getLogger(__name__)


class WebSearchTool(BaseTool):
    """网络搜索工具"""
    
    def __init__(self):
        schema = {
            "name": "web_search",
            "description": "使用搜索引擎搜索网络信息，获取最新的技术文档、解决方案等",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "搜索关键词或问题"
                    },
                    "num_results": {
                        "type": "integer",
                        "description": "返回结果数量（默认5）",
                        "default": 5
                    }
                },
                "required": ["query"]
            }
        }
        super().__init__("web_search", "网络搜索", schema)
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行网络搜索
        
        注意：这是一个模拟实现，实际需要接入真实的搜索API（如Google Search API、Bing API等）
        """
        try:
            query = arguments.get("query")
            num_results = arguments.get("num_results", 5)
            
            logger.info(f"执行网络搜索: {query}")
            
            # TODO: 集成真实的搜索API
            # 当前返回模拟结果
            mock_results = [
                {
                    "title": f"搜索结果示例 - {query}",
                    "url": "https://example.com/article1",
                    "snippet": f"这是关于 {query} 的模拟搜索结果1",
                    "published_date": "2026-01-20"
                },
                {
                    "title": f"{query} 技术文档",
                    "url": "https://docs.example.com/article2",
                    "snippet": f"这里提供了 {query} 的详细技术说明和实现方案",
                    "published_date": "2026-01-18"
                },
                {
                    "title": f"最佳实践 - {query}",
                    "url": "https://blog.example.com/article3",
                    "snippet": f"本文分享了 {query} 的最佳实践和注意事项",
                    "published_date": "2026-01-15"
                }
            ]
            
            return {
                "success": True,
                "query": query,
                "count": len(mock_results[:num_results]),
                "results": mock_results[:num_results]
            }
        except Exception as e:
            logger.error(f"网络搜索失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }


class CodeAnalysisTool(BaseTool):
    """代码分析工具"""
    
    def __init__(self):
        schema = {
            "name": "code_analysis",
            "description": "分析代码质量、复杂度、潜在问题等",
            "parameters": {
                "type": "object",
                "properties": {
                    "code": {
                        "type": "string",
                        "description": "要分析的代码"
                    },
                    "language": {
                        "type": "string",
                        "description": "编程语言（python, java, javascript等）"
                    },
                    "analysis_type": {
                        "type": "string",
                        "description": "分析类型（quality, complexity, security, all）",
                        "enum": ["quality", "complexity", "security", "all"],
                        "default": "all"
                    }
                },
                "required": ["code"]
            }
        }
        super().__init__("code_analysis", "代码分析", schema)
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行代码分析
        """
        try:
            code = arguments.get("code", "")
            language = arguments.get("language", "unknown")
            analysis_type = arguments.get("analysis_type", "all")
            
            logger.info(f"执行代码分析: 语言={language}, 类型={analysis_type}")
            
            issues = []
            suggestions = []
            complexity_score = 0
            
            # 简单的代码分析逻辑
            lines = code.split('\n')
            line_count = len(lines)
            
            # 质量分析
            if "quality" in analysis_type or analysis_type == "all":
                # 检查代码行数
                if line_count > 100:
                    suggestions.append("建议将代码拆分为更小的函数（当前代码行数较多）")
                
                # 检查注释率
                comment_lines = sum(1 for line in lines if line.strip().startswith('#') or 
                                  line.strip().startswith('//') or line.strip().startswith('/*'))
                if comment_lines > 0:
                    comment_ratio = comment_lines / line_count
                    if comment_ratio < 0.1:
                        issues.append("注释过少，建议添加更多注释说明代码逻辑")
                
                # 检查重复代码
                if len(set(lines)) < len(lines) * 0.8:
                    issues.append("检测到重复代码，建议提取公共函数")
            
            # 复杂度分析
            if "complexity" in analysis_type or analysis_type == "all":
                # 计算圈复杂度（简化版本）
                complexity_keywords = ['if', 'elif', 'for', 'while', 'case', 'catch', '&&', '||']
                complexity = sum(code.count(keyword) for keyword in complexity_keywords)
                complexity_score = complexity + 1
                
                if complexity_score > 10:
                    issues.append(f"圈复杂度较高({complexity_score})，建议简化逻辑")
                elif complexity_score > 20:
                    issues.append(f"圈复杂度过高({complexity_score})，强烈建议重构")
            
            # 安全分析
            if "security" in analysis_type or analysis_type == "all":
                # 检查常见安全问题
                if "eval(" in code or "exec(" in code:
                    issues.append("检测到eval/exec，存在代码注入风险")
                
                if "input(" in code and language == "python":
                    suggestions.append("建议使用argparse或其他参数解析库代替input()")
                
                if "select *" in code.lower():
                    suggestions.append("建议避免使用SELECT *，明确指定需要的字段")
            
            return {
                "success": True,
                "language": language,
                "analysis_type": analysis_type,
                "line_count": line_count,
                "complexity_score": complexity_score,
                "issues": issues,
                "suggestions": suggestions
            }
        except Exception as e:
            logger.error(f"代码分析失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }


class DocumentParserTool(BaseTool):
    """文档解析工具"""
    
    def __init__(self):
        schema = {
            "name": "document_parser",
            "description": "解析各种格式的文档（PDF、Word、Excel、Markdown、HTML等），提取文本内容",
            "parameters": {
                "type": "object",
                "properties": {
                    "document_type": {
                        "type": "string",
                        "description": "文档类型（pdf, word, excel, markdown, html, text）"
                    },
                    "content": {
                        "type": "string",
                        "description": "文档内容（文本形式，或Base64编码的文件内容）"
                    },
                    "extract_type": {
                        "type": "string",
                        "description": "提取类型（text, structure, all）",
                        "enum": ["text", "structure", "all"],
                        "default": "all"
                    }
                },
                "required": ["document_type", "content"]
            }
        }
        super().__init__("document_parser", "文档解析", schema)
    
    def execute(self, arguments: Dict[str, Any], context: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        执行文档解析
        """
        try:
            document_type = arguments.get("document_type", "").lower()
            content = arguments.get("content", "")
            extract_type = arguments.get("extract_type", "all")
            
            logger.info(f"解析文档: 类型={document_type}, 提取类型={extract_type}")
            
            result = {
                "document_type": document_type,
                "extract_type": extract_type
            }
            
            # 文本内容提取
            if extract_type in ["text", "all"]:
                if document_type == "text":
                    text_content = content
                elif document_type == "markdown":
                    # 简单的Markdown文本提取
                    import re
                    text_content = re.sub(r'#+\s*', '', content)  # 移除标题符号
                    text_content = re.sub(r'\*{1,2}', '', text_content)  # 移除粗体标记
                    text_content = re.sub(r'_{1,2}', '', text_content)  # 移除斜体标记
                    text_content = re.sub(r'`{1,3}', '', text_content)  # 移除代码标记
                    text_content = re.sub(r'\[.*?\]\(.*?\)', '', text_content)  # 移除链接
                elif document_type == "html":
                    # 简单的HTML文本提取
                    import re
                    text_content = re.sub(r'<[^>]+>', ' ', content)  # 移除HTML标签
                    text_content = re.sub(r'\s+', ' ', text_content).strip()  # 压缩空白
                else:
                    # 其他格式返回原文（实际应使用专业库解析）
                    text_content = content
                
                result["text"] = text_content
                result["text_length"] = len(text_content)
            
            # 结构信息提取
            if extract_type in ["structure", "all"]:
                structure = {
                    "sections": [],
                    "lists": [],
                    "code_blocks": [],
                    "links": []
                }
                
                if document_type == "markdown":
                    # Markdown结构提取
                    import re
                    # 提取标题
                    for match in re.finditer(r'^#+\s+(.+)$', content, re.MULTILINE):
                        structure["sections"].append(match.group(1).strip())
                    
                    # 提取列表
                    for match in re.finditer(r'^\s*[-*+]\s+(.+)$', content, re.MULTILINE):
                        structure["lists"].append(match.group(1).strip())
                    
                    # 提取代码块
                    for match in re.finditer(r'```(\w+)?\n(.*?)\n```', content, re.DOTALL):
                        structure["code_blocks"].append(match.group(2))
                    
                    # 提取链接
                    for match in re.finditer(r'\[([^\]]+)\]\(([^)]+)\)', content):
                        structure["links"].append({
                            "text": match.group(1),
                            "url": match.group(2)
                        })
                
                elif document_type == "html":
                    # HTML结构提取
                    import re
                    # 提取标题
                    for match in re.finditer(r'<h[1-6][^>]*>([^<]+)</h[1-6]>', content, re.IGNORECASE):
                        structure["sections"].append(re.sub(r'<[^>]+>', '', match.group(1)))
                    
                    # 提取列表
                    for match in re.finditer(r'<li[^>]*>([^<]+)</li>', content, re.IGNORECASE):
                        structure["lists"].append(re.sub(r'<[^>]+>', '', match.group(1)))
                    
                    # 提取链接
                    for match in re.finditer(r'<a[^>]+href="([^"]+)"[^>]*>([^<]+)</a>', content, re.IGNORECASE):
                        structure["links"].append({
                            "text": match.group(2),
                            "url": match.group(1)
                        })
                
                result["structure"] = structure
            
            return {
                "success": True,
                "data": result
            }
        except Exception as e:
            logger.error(f"文档解析失败: {str(e)}", exc_info=True)
            return {
                "success": False,
                "error": str(e)
            }
