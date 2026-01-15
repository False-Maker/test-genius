"""
用例生成服务
协调需求分析、提示词生成、模型调用等步骤
"""
import re
import json
import logging
from typing import List, Dict, Optional, Any
from sqlalchemy.orm import Session
from app.services.prompt_service import PromptService
from app.services.llm_service import LLMService

logger = logging.getLogger(__name__)


class CaseGenerationService:
    """用例生成服务"""
    
    def __init__(self, db: Session):
        """
        初始化用例生成服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.prompt_service = PromptService(db)
        self.llm_service = LLMService(db)
    
    def generate_cases(
        self,
        requirement_text: str,
        layer_code: str,
        method_code: str,
        model_code: str,
        template_id: Optional[int] = None,
        requirement_id: Optional[int] = None
    ) -> List[Dict]:
        """
        生成测试用例
        
        Args:
            requirement_text: 需求文本
            layer_code: 测试分层代码
            method_code: 测试方法代码
            model_code: 模型代码
            template_id: 模板ID（可选，如果不提供则自动选择）
            requirement_id: 需求ID（可选）
            
        Returns:
            生成的用例列表，每个用例包含：
            - case_name: 用例名称
            - case_type: 用例类型
            - case_priority: 用例优先级
            - pre_condition: 前置条件
            - test_step: 测试步骤
            - expected_result: 预期结果
        """
        try:
            # 1. 需求分析（简单分析，提取关键信息）
            requirement_info = self._analyze_requirement(requirement_text)
            logger.info(f"需求分析完成: {requirement_info}")
            
            # 2. 选择提示词模板
            if template_id is None:
                template = self._select_template(layer_code, method_code)
                if template:
                    template_id = template.get("id")
                    logger.info(f"自动选择模板: {template_id}")
                else:
                    raise ValueError("未找到适用的提示词模板")
            else:
                template = self.prompt_service.load_template(template_id)
                if not template:
                    raise ValueError(f"模板不存在或未启用: {template_id}")
            
            # 3. 生成提示词
            variables = self._build_prompt_variables(
                requirement_text=requirement_text,
                requirement_info=requirement_info,
                layer_code=layer_code,
                method_code=method_code
            )
            
            prompt = self.prompt_service.generate_prompt(
                template_id=template_id,
                variables=variables
            )
            logger.info(f"提示词生成完成，长度: {len(prompt)}")
            
            # 4. 调用大模型生成用例
            logger.info(f"开始调用模型生成用例: {model_code}")
            model_response = self.llm_service.call_model(
                model_code=model_code,
                prompt=prompt
            )
            
            content = model_response.get("content", "")
            logger.info(f"模型响应完成，内容长度: {len(content)}")
            
            # 5. 解析和结构化用例
            cases = self.parse_cases(content)
            logger.info(f"用例解析完成，共生成 {len(cases)} 个用例")
            
            # 6. 用例去重和合并
            cases = self._deduplicate_and_merge_cases(cases)
            logger.info(f"用例去重后，剩余 {len(cases)} 个用例")
            
            # 7. 用例质量初步检查
            cases = self._quality_check_cases(cases)
            logger.info(f"用例质量检查后，剩余 {len(cases)} 个用例")
            
            # 为每个用例添加关联信息
            for case in cases:
                case["requirement_id"] = requirement_id
                case["layer_code"] = layer_code
                case["method_code"] = method_code
                case["model_code"] = model_code
                case["template_id"] = template_id
            
            return cases
            
        except Exception as e:
            logger.error(f"生成用例失败: {str(e)}", exc_info=True)
            raise
    
    def _analyze_requirement(self, requirement_text: str) -> Dict:
        """
        分析需求文本，提取关键信息
        
        Args:
            requirement_text: 需求文本
            
        Returns:
            需求信息字典，包含：
            - keywords: 关键词列表
            - length: 文本长度
            - sentences: 句子数量
        """
        # 简单的需求分析
        # 提取关键词（去除常见停用词）
        stop_words = {"的", "了", "在", "是", "和", "与", "或", "但", "如果", "当", "则"}
        words = re.findall(r'[\u4e00-\u9fa5]+', requirement_text)
        keywords = [w for w in words if len(w) >= 2 and w not in stop_words]
        # 去重并限制数量
        keywords = list(set(keywords))[:10]
        
        # 统计句子数量
        sentences = re.split(r'[。！？\n]', requirement_text)
        sentences = [s.strip() for s in sentences if s.strip()]
        
        return {
            "keywords": keywords,
            "length": len(requirement_text),
            "sentence_count": len(sentences)
        }
    
    def _select_template(
        self,
        layer_code: str,
        method_code: str
    ) -> Optional[Dict]:
        """
        根据测试分层和方法选择适用的模板（优化版）
        
        优化策略：
        1. 优先选择同时匹配分层和方法的模板
        2. 其次选择只匹配分层的模板
        3. 再次选择只匹配方法的模板
        4. 最后选择通用模板
        
        Args:
            layer_code: 测试分层代码
            method_code: 测试方法代码
            
        Returns:
            模板信息字典，如果未找到返回None
        """
        # 1. 查找同时匹配分层和方法的模板
        templates = self.prompt_service.find_applicable_templates(
            layer_code=layer_code,
            method_code=method_code
        )
        
        if templates:
            # 优先返回第一个完全匹配的模板
            logger.info(f"找到完全匹配的模板: {len(templates)}个")
            return templates[0]
        
        # 2. 查找只匹配分层的模板
        if layer_code:
            templates = self.prompt_service.find_applicable_templates(
                layer_code=layer_code
            )
            if templates:
                logger.info(f"找到匹配分层的模板: {len(templates)}个")
                return templates[0]
        
        # 3. 查找只匹配方法的模板
        if method_code:
            templates = self.prompt_service.find_applicable_templates(
                method_code=method_code
            )
            if templates:
                logger.info(f"找到匹配方法的模板: {len(templates)}个")
                return templates[0]
        
        # 4. 如果没有找到适用的模板，返回第一个启用的通用模板
        all_templates = self.prompt_service.find_applicable_templates()
        if all_templates:
            logger.info(f"使用通用模板: {len(all_templates)}个")
            return all_templates[0]
        
        logger.warning("未找到任何可用的模板")
        return None
    
    def _build_prompt_variables(
        self,
        requirement_text: str,
        requirement_info: Dict,
        layer_code: str,
        method_code: str
    ) -> Dict[str, Any]:
        """
        构建提示词变量
        
        Args:
            requirement_text: 需求文本
            requirement_info: 需求分析信息
            layer_code: 测试分层代码
            method_code: 测试方法代码
            
        Returns:
            变量字典
        """
        return {
            "requirement_text": requirement_text,
            "requirement_keywords": ", ".join(requirement_info.get("keywords", [])),
            "layer_code": layer_code,
            "method_code": method_code,
            "sentence_count": requirement_info.get("sentence_count", 0)
        }
    
    def parse_cases(self, content: str) -> List[Dict]:
        """
        解析用例内容
        
        支持多种格式：
        1. JSON格式
        2. Markdown格式
        3. 纯文本格式
        
        Args:
            content: 用例文本内容
            
        Returns:
            解析后的用例列表，每个用例包含：
            - case_name: 用例名称
            - case_type: 用例类型（可选）
            - case_priority: 用例优先级（可选）
            - pre_condition: 前置条件（可选）
            - test_step: 测试步骤
            - expected_result: 预期结果
        """
        if not content or not content.strip():
            logger.warning("用例内容为空")
            return []
        
        # 尝试解析JSON格式
        cases = self._parse_json_format(content)
        if cases:
            logger.info("成功解析JSON格式用例")
            return cases
        
        # 尝试解析Markdown格式
        cases = self._parse_markdown_format(content)
        if cases:
            logger.info("成功解析Markdown格式用例")
            return cases
        
        # 尝试解析纯文本格式
        cases = self._parse_text_format(content)
        if cases:
            logger.info("成功解析文本格式用例")
            return cases
        
        # 如果所有解析都失败，返回一个包含原始内容的用例
        logger.warning("无法解析用例格式，返回原始内容")
        return [{
            "case_name": "生成的用例",
            "test_step": content,
            "expected_result": "请手动整理用例"
        }]
    
    def _parse_json_format(self, content: str) -> List[Dict]:
        """解析JSON格式的用例"""
        try:
            # 尝试提取JSON部分
            json_match = re.search(r'\[.*\]', content, re.DOTALL)
            if json_match:
                json_str = json_match.group(0)
                data = json.loads(json_str)
                
                if isinstance(data, list):
                    cases = []
                    for item in data:
                        if isinstance(item, dict):
                            case = {
                                "case_name": item.get("case_name") or item.get("name") or "未命名用例",
                                "case_type": item.get("case_type") or item.get("type"),
                                "case_priority": item.get("case_priority") or item.get("priority") or "中",
                                "pre_condition": item.get("pre_condition") or item.get("precondition") or "",
                                "test_step": item.get("test_step") or item.get("steps") or item.get("step") or "",
                                "expected_result": item.get("expected_result") or item.get("expected") or item.get("result") or ""
                            }
                            cases.append(case)
                    return cases
        except (json.JSONDecodeError, AttributeError) as e:
            logger.debug(f"JSON解析失败: {str(e)}")
        
        return []
    
    def _parse_markdown_format(self, content: str) -> List[Dict]:
        """解析Markdown格式的用例"""
        cases = []
        
        # 匹配用例标题（## 或 ### 开头）
        case_pattern = r'(?:^|\n)(?:#{2,3})\s*(.+?)(?:\n|$)'
        case_matches = list(re.finditer(case_pattern, content, re.MULTILINE))
        
        if not case_matches:
            # 如果没有标题，尝试按编号分割
            case_pattern = r'(?:^|\n)(?:\d+[\.、])\s*(.+?)(?:\n|$)'
            case_matches = list(re.finditer(case_pattern, content, re.MULTILINE))
        
        if case_matches:
            for i, match in enumerate(case_matches):
                case_name = match.group(1).strip()
                start_pos = match.end()
                end_pos = case_matches[i + 1].start() if i + 1 < len(case_matches) else len(content)
                case_content = content[start_pos:end_pos]
                
                # 提取测试步骤和预期结果
                test_step = ""
                expected_result = ""
                
                # 查找测试步骤
                step_match = re.search(r'(?:测试步骤|步骤|操作步骤)[:：]?\s*(.+?)(?=\n(?:预期结果|期望结果|结果)|$)', 
                                      case_content, re.DOTALL | re.IGNORECASE)
                if step_match:
                    test_step = step_match.group(1).strip()
                
                # 查找预期结果
                result_match = re.search(r'(?:预期结果|期望结果|结果)[:：]?\s*(.+?)(?=\n|$)', 
                                        case_content, re.DOTALL | re.IGNORECASE)
                if result_match:
                    expected_result = result_match.group(1).strip()
                
                # 如果没有找到结构化内容，使用整个内容作为测试步骤
                if not test_step and not expected_result:
                    test_step = case_content.strip()
                
                cases.append({
                    "case_name": case_name,
                    "test_step": test_step,
                    "expected_result": expected_result
                })
        
        return cases
    
    def _parse_text_format(self, content: str) -> List[Dict]:
        """解析纯文本格式的用例"""
        cases = []
        
        # 按段落分割
        paragraphs = [p.strip() for p in content.split('\n\n') if p.strip()]
        
        for para in paragraphs:
            # 尝试提取用例名称（第一行或包含"用例"的行）
            lines = para.split('\n')
            case_name = "生成的用例"
            
            for line in lines:
                if "用例" in line or "测试" in line:
                    case_name = line.strip()
                    break
            
            # 提取测试步骤和预期结果
            test_step = ""
            expected_result = ""
            
            # 查找测试步骤
            step_match = re.search(r'(?:测试步骤|步骤|操作)[:：]?\s*(.+?)(?=\n(?:预期|期望|结果)|$)', 
                                  para, re.DOTALL | re.IGNORECASE)
            if step_match:
                test_step = step_match.group(1).strip()
            else:
                # 如果没有找到，使用前几行作为测试步骤
                test_step = '\n'.join(lines[:3])
            
            # 查找预期结果
            result_match = re.search(r'(?:预期结果|期望结果|结果)[:：]?\s*(.+?)(?=\n|$)', 
                                    para, re.DOTALL | re.IGNORECASE)
            if result_match:
                expected_result = result_match.group(1).strip()
            
            cases.append({
                "case_name": case_name,
                "test_step": test_step,
                "expected_result": expected_result
            })
        
        return cases if cases else []
    
    def _deduplicate_and_merge_cases(self, cases: List[Dict]) -> List[Dict]:
        """
        用例去重和合并
        
        去重策略：
        1. 用例名称相似度检查（使用简单字符串匹配）
        2. 测试步骤相似度检查
        3. 合并相似用例（保留更完整的用例）
        
        Args:
            cases: 用例列表
            
        Returns:
            去重后的用例列表
        """
        if not cases or len(cases) <= 1:
            return cases
        
        # 去重：基于用例名称和测试步骤的相似度
        unique_cases = []
        seen_names = set()
        
        for case in cases:
            case_name = case.get("case_name", "").strip().lower()
            test_step = case.get("test_step", "").strip().lower()
            
            # 生成唯一标识（用例名称 + 测试步骤前50个字符）
            identifier = case_name + "|" + test_step[:50]
            
            # 检查是否已存在相似用例
            is_duplicate = False
            for seen_id in seen_names:
                # 简单的相似度检查：如果用例名称相同或测试步骤前50字符相同，认为是重复
                if identifier == seen_id:
                    is_duplicate = True
                    break
                # 如果用例名称完全相同，认为是重复
                if case_name and case_name == seen_id.split("|")[0]:
                    is_duplicate = True
                    break
            
            if not is_duplicate:
                seen_names.add(identifier)
                unique_cases.append(case)
            else:
                logger.debug(f"发现重复用例，已跳过: {case.get('case_name')}")
        
        return unique_cases
    
    def _quality_check_cases(self, cases: List[Dict]) -> List[Dict]:
        """
        用例质量初步检查
        
        检查项：
        1. 用例名称不能为空
        2. 测试步骤不能为空
        3. 预期结果不能为空（可选，但建议有）
        4. 用例名称长度检查（不能太短或太长）
        5. 测试步骤长度检查（不能太短）
        
        Args:
            cases: 用例列表
            
        Returns:
            通过质量检查的用例列表
        """
        if not cases:
            return cases
        
        quality_cases = []
        
        for case in cases:
            case_name = case.get("case_name", "").strip()
            test_step = case.get("test_step", "").strip()
            expected_result = case.get("expected_result", "").strip()
            
            # 1. 用例名称不能为空
            if not case_name:
                logger.warning("用例名称为空，跳过该用例")
                continue
            
            # 2. 测试步骤不能为空
            if not test_step:
                logger.warning(f"用例测试步骤为空，跳过: {case_name}")
                continue
            
            # 3. 用例名称长度检查（5-200字符）
            if len(case_name) < 5:
                logger.warning(f"用例名称过短（{len(case_name)}字符），跳过: {case_name}")
                continue
            if len(case_name) > 200:
                logger.warning(f"用例名称过长（{len(case_name)}字符），截断: {case_name}")
                case["case_name"] = case_name[:200]
            
            # 4. 测试步骤长度检查（至少10字符）
            if len(test_step) < 10:
                logger.warning(f"用例测试步骤过短（{len(test_step)}字符），跳过: {case_name}")
                continue
            
            # 5. 预期结果检查（可选，但建议有）
            if not expected_result:
                logger.info(f"用例预期结果为空，建议补充: {case_name}")
                # 不强制要求，但记录日志
            
            quality_cases.append(case)
        
        return quality_cases
