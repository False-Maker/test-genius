"""
多模型适配器
支持DeepSeek、豆包、Kimi、千问、智谱等模型
使用LangChain统一接口
兼容 LangChain 0.3.x
"""

from typing import Optional, Dict, Any
import httpx
import json
import time
import logging

logger = logging.getLogger(__name__)

# 尝试导入LangChain相关模块（新版本 0.3.x）
try:
    from langchain_openai import ChatOpenAI
    from langchain_core.messages import HumanMessage
    logger.info(f"已加载 langchain_openai, ChatOpenAI={ChatOpenAI}")
except ImportError:
    try:
        # 兼容旧版本
        from langchain.chat_models import ChatOpenAI
        from langchain.schema import HumanMessage
        logger.info(f"已加载 langchain (旧版), ChatOpenAI={ChatOpenAI}")
    except ImportError:
        ChatOpenAI = None
        HumanMessage = None
        logger.warning("未找到 ChatOpenAI，将使用 HTTP 适配器")

try:
    from langchain_community.chat_models import ChatTongyi
except ImportError:
    ChatTongyi = None


# 默认超时时间（秒）
DEFAULT_TIMEOUT = 120.0


def _normalize_api_endpoint(api_endpoint: str) -> str:
    """
    规范化API端点URL
    移除可能存在的 /chat/completions 后缀，因为 LangChain 会自动追加

    Args:
        api_endpoint: 原始API端点

    Returns:
        规范化后的API端点
    """
    if not api_endpoint:
        return api_endpoint

    # 移除末尾的 /chat/completions（不区分大小写）
    endpoint = api_endpoint.rstrip("/")
    if endpoint.lower().endswith("/chat/completions"):
        endpoint = endpoint[: -len("/chat/completions")]

    return endpoint


def _create_http_client(timeout: float) -> httpx.Client:
    """
    创建带有明确超时配置的 httpx Client
    
    Args:
        timeout: 超时时间（秒）
        
    Returns:
        httpx.Client 实例
    """
    return httpx.Client(
        timeout=httpx.Timeout(
            connect=10.0,
            read=timeout,
            write=30.0,
            pool=10.0,
        )
    )


def _create_async_http_client(timeout: float) -> httpx.AsyncClient:
    """
    创建带有明确超时配置的 httpx AsyncClient
    
    Args:
        timeout: 超时时间（秒）
        
    Returns:
        httpx.AsyncClient 实例
    """
    return httpx.AsyncClient(
        timeout=httpx.Timeout(
            connect=10.0,
            read=timeout,
            write=30.0,
            pool=10.0,
        )
    )


def _create_chat_openai(
    model_version: str,
    api_key: str,
    base_url: str,
    max_tokens: int,
    temperature: float,
    request_timeout: float,
) -> Any:
    """
    创建 ChatOpenAI 实例，兼容新旧版本，并确保超时生效
    
    Args:
        model_version: 模型版本
        api_key: API密钥
        base_url: API端点（已规范化）
        max_tokens: 最大token数
        temperature: 温度参数
        request_timeout: 请求超时时间（秒）
        
    Returns:
        ChatOpenAI 实例，如果创建失败返回 None
    """
    if ChatOpenAI is None:
        return None

    http_client = _create_http_client(request_timeout)
    async_http_client = _create_async_http_client(request_timeout)

    # 方式1: 新版本 langchain-openai (0.2.x+)，使用 http_client 确保超时生效
    try:
        params = {
            "model": model_version,
            "api_key": api_key,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "timeout": request_timeout,
            "http_client": http_client,
            "http_async_client": async_http_client,
        }
        if base_url:
            params["base_url"] = base_url
        llm = ChatOpenAI(**params)
        logger.info(
            f"ChatOpenAI 创建成功(方式1-新版本): model={model_version}, "
            f"timeout={request_timeout}s, base_url={base_url or '默认'}"
        )
        return llm
    except (TypeError, ValueError) as e:
        logger.debug(f"ChatOpenAI 方式1失败: {e}")

    # 方式2: 新版本但不支持 http_client 参数
    try:
        params = {
            "model": model_version,
            "api_key": api_key,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "timeout": request_timeout,
        }
        if base_url:
            params["base_url"] = base_url
        llm = ChatOpenAI(**params)
        # 尝试手动替换底层 client 的超时配置
        _patch_openai_client_timeout(llm, request_timeout)
        logger.info(
            f"ChatOpenAI 创建成功(方式2-无http_client): model={model_version}, "
            f"timeout={request_timeout}s"
        )
        return llm
    except (TypeError, ValueError) as e:
        logger.debug(f"ChatOpenAI 方式2失败: {e}")

    # 方式3: 旧版本 langchain-openai (0.0.x)
    try:
        params = {
            "model": model_version,
            "openai_api_key": api_key,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "request_timeout": int(request_timeout),
        }
        if base_url:
            params["openai_api_base"] = base_url
        llm = ChatOpenAI(**params)
        logger.info(
            f"ChatOpenAI 创建成功(方式3-旧版本): model={model_version}, "
            f"timeout={request_timeout}s"
        )
        return llm
    except (TypeError, ValueError) as e:
        logger.debug(f"ChatOpenAI 方式3失败: {e}")

    logger.warning(f"所有 ChatOpenAI 创建方式均失败，将回退到 HTTP 适配器")
    return None


def _patch_openai_client_timeout(llm: Any, timeout: float) -> None:
    """
    尝试手动修补 ChatOpenAI 底层 openai client 的超时配置
    
    Args:
        llm: ChatOpenAI 实例
        timeout: 超时时间（秒）
    """
    try:
        # langchain-openai 内部持有 openai.OpenAI client
        # 路径: llm.client -> openai 的 chat.completions 资源 -> _client -> timeout
        target_timeout = httpx.Timeout(
            connect=10.0,
            read=timeout,
            write=30.0,
            pool=10.0,
        )

        # 尝试路径1: llm.client._client.timeout (openai>=1.0)
        if hasattr(llm, 'client') and llm.client is not None:
            inner = getattr(llm.client, '_client', None)
            if inner is not None and hasattr(inner, 'timeout'):
                inner.timeout = target_timeout
                logger.info(f"已修补 openai client timeout: read={timeout}s")
                return
            # 尝试路径2: llm.client 本身就是 openai client
            if hasattr(llm.client, 'timeout'):
                llm.client.timeout = target_timeout
                logger.info(f"已修补 client timeout: read={timeout}s")
                return

        # 尝试路径3: llm.root_client (某些版本)
        if hasattr(llm, 'root_client') and llm.root_client is not None:
            if hasattr(llm.root_client, '_client'):
                llm.root_client._client.timeout = target_timeout
                logger.info(f"已修补 root_client timeout: read={timeout}s")
                return

        logger.debug("未找到可修补的 openai client timeout 路径")
    except Exception as e:
        logger.debug(f"修补 openai client timeout 失败: {e}")


class OpenAIAdapter:
    """OpenAI模型适配器"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建OpenAI LLM实例
        """
        normalized_endpoint = (
            _normalize_api_endpoint(api_endpoint) if api_endpoint else ""
        )

        llm = _create_chat_openai(
            model_version=model_version,
            api_key=api_key,
            base_url=normalized_endpoint,
            max_tokens=max_tokens,
            temperature=temperature,
            request_timeout=request_timeout,
        )
        if llm is not None:
            return llm

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class DeepSeekAdapter:
    """DeepSeek模型适配器"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建DeepSeek LLM实例
        DeepSeek兼容OpenAI API
        """
        llm = _create_chat_openai(
            model_version=model_version,
            api_key=api_key,
            base_url=api_endpoint,
            max_tokens=max_tokens,
            temperature=temperature,
            request_timeout=request_timeout,
        )
        if llm is not None:
            return llm

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class DoubaoAdapter:
    """豆包模型适配器（字节跳动）"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建豆包 LLM实例
        豆包兼容OpenAI API
        """
        normalized_endpoint = _normalize_api_endpoint(api_endpoint)

        llm = _create_chat_openai(
            model_version=model_version,
            api_key=api_key,
            base_url=normalized_endpoint,
            max_tokens=max_tokens,
            temperature=temperature,
            request_timeout=request_timeout,
        )
        if llm is not None:
            return llm

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class KimiAdapter:
    """Kimi模型适配器（月之暗面）"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建Kimi LLM实例
        Kimi兼容OpenAI API
        """
        normalized_endpoint = _normalize_api_endpoint(api_endpoint)

        llm = _create_chat_openai(
            model_version=model_version,
            api_key=api_key,
            base_url=normalized_endpoint,
            max_tokens=max_tokens,
            temperature=temperature,
            request_timeout=request_timeout,
        )
        if llm is not None:
            return llm

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class QianwenAdapter:
    """通义千问模型适配器（阿里巴巴）"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建通义千问 LLM实例
        通义千问使用DashScope API，需要特殊处理
        """
        if ChatTongyi is not None:
            try:
                return ChatTongyi(
                    model=model_version,
                    dashscope_api_key=api_key,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=int(request_timeout),
                )
            except Exception:
                pass

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class ZhipuAdapter:
    """智谱模型适配器（智谱AI）"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建智谱 LLM实例
        智谱兼容OpenAI API
        """
        normalized_endpoint = _normalize_api_endpoint(api_endpoint)

        llm = _create_chat_openai(
            model_version=model_version,
            api_key=api_key,
            base_url=normalized_endpoint,
            max_tokens=max_tokens,
            temperature=temperature,
            request_timeout=request_timeout,
        )
        if llm is not None:
            return llm

        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class HTTPLLMAdapter:
    """HTTP方式调用LLM的通用适配器（用于不兼容OpenAI API的模型）"""

    @staticmethod
    def create_llm(
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """创建HTTP方式调用的LLM（简化版本）"""

        class HTTPLLMWrapper:
            def __init__(
                self, api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
            ):
                self.api_key = api_key
                self.api_endpoint = api_endpoint
                self.model_version = model_version
                self.max_tokens = max_tokens
                self.temperature = temperature
                self.request_timeout = request_timeout

            def invoke(self, prompt: str) -> str:
                """调用模型"""
                headers = {
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json",
                }

                payload = {
                    "model": self.model_version,
                    "messages": [{"role": "user", "content": prompt}],
                    "max_tokens": self.max_tokens,
                    "temperature": self.temperature,
                }

                timeout_config = httpx.Timeout(
                    connect=10.0,
                    read=self.request_timeout,
                    write=30.0,
                    pool=10.0,
                )

                with httpx.Client(timeout=timeout_config) as client:
                    response = client.post(
                        self.api_endpoint, headers=headers, json=payload
                    )
                    response.raise_for_status()
                    result = response.json()

                    if "choices" in result and len(result["choices"]) > 0:
                        return result["choices"][0]["message"]["content"]
                    elif "output" in result:
                        return result["output"]["text"]
                    else:
                        return str(result)

        return HTTPLLMWrapper(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )


class ModelAdapterFactory:
    """模型适配器工厂"""

    _adapters = {
        "OPENAI": OpenAIAdapter,
        "DEEPSEEK": DeepSeekAdapter,
        "DOUBAO": DoubaoAdapter,
        "KIMI": KimiAdapter,
        "QIANWEN": QianwenAdapter,
        "ZHIPU": ZhipuAdapter,
        "智谱": ZhipuAdapter,  # 支持中文名称
    }

    @classmethod
    def create_llm(
        cls,
        model_type: str,
        api_key: str,
        api_endpoint: str,
        model_version: str,
        max_tokens: int,
        temperature: float,
        request_timeout: float = DEFAULT_TIMEOUT,
    ):
        """
        创建LLM实例

        Args:
            model_type: 模型类型（DEEPSEEK/DOUBAO/KIMI/QIANWEN/ZHIPU/智谱）
            api_key: API密钥
            api_endpoint: API端点
            model_version: 模型版本
            max_tokens: 最大token数
            temperature: 温度参数
            request_timeout: 请求超时时间（秒）

        Returns:
            LLM实例
        """
        logger.info(f"创建LLM: type={model_type}, model={model_version}, timeout={request_timeout}s")
        adapter_class = cls._adapters.get(model_type.upper())
        if not adapter_class:
            logger.warning(f"未知模型类型: {model_type}，使用 HTTP 适配器")
            adapter_class = HTTPLLMAdapter

        return adapter_class.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature, request_timeout
        )
