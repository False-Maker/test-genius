"""
大模型调用服务
封装LangChain进行模型调用
兼容 LangChain 0.3.x
"""
from typing import Optional, Dict, Any, List
from sqlalchemy.orm import Session
import time
import logging
import asyncio
from concurrent.futures import ThreadPoolExecutor, as_completed
from app.services.model_config_service import ModelConfigService
from app.utils.model_adapter import ModelAdapterFactory
from app.config import LLM_REQUEST_TIMEOUT, LLM_MAX_RETRIES, LLM_RETRY_DELAY

logger = logging.getLogger(__name__)


class LLMService:
    """大模型调用服务"""
    
    def __init__(self, db: Session):
        """
        初始化LLM服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.model_config_service = ModelConfigService(db)
        # 缓存已创建的LLM实例
        self._llm_cache: Dict[str, Any] = {}
    
    def _get_llm_instance(self, model_code: str, max_tokens: Optional[int] = None,
                        temperature: Optional[float] = None,
                        request_timeout: Optional[int] = None) -> Any:
        """
        获取或创建LLM实例
        
        Args:
            model_code: 模型代码
            max_tokens: 最大token数（可选，覆盖配置）
            temperature: 温度参数（可选，覆盖配置）
            request_timeout: 请求超时时间（秒）（可选，覆盖配置）
            
        Returns:
            LLM实例
        """
        # 从数据库获取模型配置
        model_config = self.model_config_service.get_by_code(model_code)
        if not model_config:
            raise ValueError(f"模型配置不存在: {model_code}")
        
        if model_config.is_active != "1":
            raise ValueError(f"模型配置未启用: {model_code}")
        
        # 使用配置中的参数，如果提供了参数则覆盖
        max_tokens = max_tokens or model_config.max_tokens or 2000
        temperature = temperature if temperature is not None else (
            float(model_config.temperature) if model_config.temperature else 0.7
        )
        timeout = request_timeout or LLM_REQUEST_TIMEOUT
        
        # 生成缓存键
        cache_key = f"{model_code}_{max_tokens}_{temperature}_{timeout}"
        
        # 如果缓存中存在，直接返回
        if cache_key in self._llm_cache:
            return self._llm_cache[cache_key]
        
        # 创建新的LLM实例
        try:
            llm = ModelAdapterFactory.create_llm(
                model_type=model_config.model_type or "DEEPSEEK",
                api_key=model_config.api_key or "",
                api_endpoint=model_config.api_endpoint or "",
                model_version=model_config.model_version or "",
                max_tokens=max_tokens,
                temperature=temperature,
                request_timeout=timeout
            )
            
            # 缓存实例
            self._llm_cache[cache_key] = llm
            logger.info(f"创建LLM实例成功: {model_code}, timeout={timeout}s")
            
            return llm
        except Exception as e:
            logger.error(f"创建LLM实例失败: {model_code}, 错误: {str(e)}")
            raise
    
    def _call_with_retry(self, llm: Any, prompt: str, max_retries: Optional[int] = None,
                        retry_delay: Optional[float] = None) -> str:
        """
        带重试机制的模型调用
        兼容 LangChain 0.3.x 和旧版本
        
        Args:
            llm: LLM实例
            prompt: 提示词
            max_retries: 最大重试次数
            retry_delay: 重试延迟（秒）
            
        Returns:
            模型响应内容
        """
        last_error = None
        max_retries = max_retries or LLM_MAX_RETRIES
        retry_delay = retry_delay or LLM_RETRY_DELAY
        
        for attempt in range(max_retries):
            try:
                start_time = time.time()
                
                # 调用模型（兼容 LangChain 0.3.x）
                if hasattr(llm, 'invoke'):
                    # LangChain ChatModel (新版本使用 Message 对象)
                    try:
                        from langchain_core.messages import HumanMessage
                        # 尝试使用 HumanMessage（新版本 0.3.x）
                        messages = [HumanMessage(content=prompt)]
                        response = llm.invoke(messages)
                    except (ImportError, TypeError):
                        # 兼容旧版本：直接传入字符串
                        response = llm.invoke(prompt)
                    
                    # 提取响应内容（兼容新旧版本）
                    if hasattr(response, 'content'):
                        content = response.content
                    elif hasattr(response, 'text'):
                        content = response.text
                    else:
                        content = str(response)
                elif hasattr(llm, '__call__'):
                    # 可调用对象（HTTP适配器）
                    content = llm(prompt)
                else:
                    # 尝试直接调用
                    content = str(llm.invoke(prompt) if hasattr(llm, 'invoke') else llm(prompt))
                
                elapsed_time = int((time.time() - start_time) * 1000)
                logger.info(f"模型调用成功，耗时: {elapsed_time}ms")
                
                return content
                
            except Exception as e:
                last_error = e
                elapsed_time = int((time.time() - start_time) * 1000)
                logger.warning(
                    f"模型调用失败 (尝试 {attempt + 1}/{max_retries}), "
                    f"耗时: {elapsed_time}ms, 错误: {str(e)}"
                )
                
                # 如果不是最后一次尝试，等待后重试
                if attempt < max_retries - 1:
                    time.sleep(retry_delay * (attempt + 1))  # 指数退避
                else:
                    raise
        
        # 如果所有重试都失败，抛出最后一个错误
        raise last_error or Exception("模型调用失败")
    
    def call_model(
        self,
        model_code: Optional[str],
        prompt: str,
        max_tokens: Optional[int] = None,
        temperature: Optional[float] = None
    ) -> Dict[str, Any]:
        """
        调用大模型生成内容
        
        Args:
            model_code: 模型代码（可选，如果为None则使用默认模型）
            prompt: 提示词
            max_tokens: 最大token数（可选，覆盖配置）
            temperature: 温度参数（可选，覆盖配置）
            
        Returns:
            模型响应结果，包含：
            - content: 响应内容
            - model_code: 模型代码
            - tokens_used: 使用的token数（如果可用）
            - response_time: 响应时间（毫秒）
        """
        start_time = time.time()
        
        try:
            # 如果未提供model_code，使用默认模型
            if not model_code:
                default_config = self.model_config_service.get_default_config()
                if not default_config:
                    raise ValueError("没有可用的模型配置，请先配置模型")
                model_code = default_config.model_code
                logger.info(f"使用默认模型: {model_code}")
            
            # 获取LLM实例
            llm = self._get_llm_instance(model_code, max_tokens, temperature)
            
            # 调用模型（带重试）
            content = self._call_with_retry(llm, prompt)
            
            # 计算响应时间
            response_time = int((time.time() - start_time) * 1000)
            
            # 尝试获取token使用量（如果LLM支持）
            tokens_used = None
            if hasattr(llm, 'get_num_tokens'):
                try:
                    tokens_used = llm.get_num_tokens(prompt) + llm.get_num_tokens(content)
                except Exception as e:
                    logger.debug(f"获取token数量失败: {str(e)}")
                    pass
            
            logger.info(
                f"模型调用完成: {model_code}, "
                f"响应时