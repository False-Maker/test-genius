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

# 尝试导入LangChain相关模块（新版本 0.3.x）
try:
    from langchain_openai import ChatOpenAI
    from langchain_core.messages import HumanMessage
except ImportError:
    try:
        # 兼容旧版本
        from langchain.chat_models import ChatOpenAI
        from langchain.schema import HumanMessage
    except ImportError:
        ChatOpenAI = None
        HumanMessage = None

try:
    from langchain_community.chat_models import ChatTongyi
except ImportError:
    ChatTongyi = None


class DeepSeekAdapter:
    """DeepSeek模型适配器"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str, 
                   max_tokens: int, temperature: float):
        """
        创建DeepSeek LLM实例
        DeepSeek兼容OpenAI API
        """
        if ChatOpenAI is None:
            # 如果ChatOpenAI不可用，使用HTTP方式
            return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)
        
        # LangChain 0.3.x 兼容：新版本 API
        # 注意：新版本可能使用不同的参数名，这里提供兼容性处理
        try:
            # 尝试新版本 API（langchain-openai 0.2.x）
            return ChatOpenAI(
                model=model_version,
                api_key=api_key,
                base_url=api_endpoint,  # 新版本使用 base_url
                max_tokens=max_tokens,
                temperature=temperature,
                timeout=60.0
            )
        except (TypeError, ValueError):
            # 兼容旧版本 API（langchain-openai 0.0.x）
            try:
                return ChatOpenAI(
                    model=model_version,
                    openai_api_key=api_key,
                    openai_api_base=api_endpoint,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=60
                )
            except Exception:
                # 如果都失败，使用 HTTP 适配器
                return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)


class DoubaoAdapter:
    """豆包模型适配器（字节跳动）"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str,
                   max_tokens: int, temperature: float):
        """
        创建豆包 LLM实例
        豆包兼容OpenAI API
        """
        if ChatOpenAI is None:
            # 如果ChatOpenAI不可用，使用HTTP方式
            return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)
        
        # LangChain 0.3.x 兼容：新版本 API
        # 注意：新版本可能使用不同的参数名，这里提供兼容性处理
        try:
            # 尝试新版本 API（langchain-openai 0.2.x）
            return ChatOpenAI(
                model=model_version,
                api_key=api_key,
                base_url=api_endpoint,  # 新版本使用 base_url
                max_tokens=max_tokens,
                temperature=temperature,
                timeout=60.0
            )
        except (TypeError, ValueError):
            # 兼容旧版本 API（langchain-openai 0.0.x）
            try:
                return ChatOpenAI(
                    model=model_version,
                    openai_api_key=api_key,
                    openai_api_base=api_endpoint,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=60
                )
            except Exception:
                # 如果都失败，使用 HTTP 适配器
                return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)


class KimiAdapter:
    """Kimi模型适配器（月之暗面）"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str,
                   max_tokens: int, temperature: float):
        """
        创建Kimi LLM实例
        Kimi兼容OpenAI API
        """
        if ChatOpenAI is None:
            # 如果ChatOpenAI不可用，使用HTTP方式
            return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)
        
        # LangChain 0.3.x 兼容：新版本 API
        # 注意：新版本可能使用不同的参数名，这里提供兼容性处理
        try:
            # 尝试新版本 API（langchain-openai 0.2.x）
            return ChatOpenAI(
                model=model_version,
                api_key=api_key,
                base_url=api_endpoint,  # 新版本使用 base_url
                max_tokens=max_tokens,
                temperature=temperature,
                timeout=60.0
            )
        except (TypeError, ValueError):
            # 兼容旧版本 API（langchain-openai 0.0.x）
            try:
                return ChatOpenAI(
                    model=model_version,
                    openai_api_key=api_key,
                    openai_api_base=api_endpoint,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=60
                )
            except Exception:
                # 如果都失败，使用 HTTP 适配器
                return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)


class QianwenAdapter:
    """通义千问模型适配器（阿里巴巴）"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str,
                   max_tokens: int, temperature: float):
        """
        创建通义千问 LLM实例
        通义千问使用DashScope API，需要特殊处理
        """
        # 通义千问使用DashScope SDK，这里使用HTTP方式调用
        # 如果使用LangChain的ChatTongyi，需要安装dashscope包
        # 这里先使用OpenAI兼容方式，如果API不兼容，需要单独实现
        if ChatTongyi is not None:
            try:
                return ChatTongyi(
                    model=model_version,
                    dashscope_api_key=api_key,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=60
                )
            except Exception:
                pass
        
        # 如果ChatTongyi不可用，使用HTTP方式
        return HTTPLLMAdapter.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature
        )


class ZhipuAdapter:
    """智谱模型适配器（智谱AI）"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str,
                   max_tokens: int, temperature: float):
        """
        创建智谱 LLM实例
        智谱兼容OpenAI API
        """
        if ChatOpenAI is None:
            # 如果ChatOpenAI不可用，使用HTTP方式
            return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)
        
        # LangChain 0.3.x 兼容：新版本 API
        # 注意：新版本可能使用不同的参数名，这里提供兼容性处理
        try:
            # 尝试新版本 API（langchain-openai 0.2.x）
            return ChatOpenAI(
                model=model_version,
                api_key=api_key,
                base_url=api_endpoint,  # 新版本使用 base_url
                max_tokens=max_tokens,
                temperature=temperature,
                timeout=60.0
            )
        except (TypeError, ValueError):
            # 兼容旧版本 API（langchain-openai 0.0.x）
            try:
                return ChatOpenAI(
                    model=model_version,
                    openai_api_key=api_key,
                    openai_api_base=api_endpoint,
                    max_tokens=max_tokens,
                    temperature=temperature,
                    timeout=60
                )
            except Exception:
                # 如果都失败，使用 HTTP 适配器
                return HTTPLLMAdapter.create_llm(api_key, api_endpoint, model_version, max_tokens, temperature)


class HTTPLLMAdapter:
    """HTTP方式调用LLM的通用适配器（用于不兼容OpenAI API的模型）"""
    
    @staticmethod
    def create_llm(api_key: str, api_endpoint: str, model_version: str,
                   max_tokens: int, temperature: float):
        """创建HTTP方式调用的LLM（简化版本）"""
        # 这里返回一个可调用对象
        class HTTPLLMWrapper:
            def __init__(self, api_key, api_endpoint, model_version, max_tokens, temperature):
                self.api_key = api_key
                self.api_endpoint = api_endpoint
                self.model_version = model_version
                self.max_tokens = max_tokens
                self.temperature = temperature
            
            def invoke(self, prompt: str) -> str:
                """调用模型"""
                headers = {
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                }
                
                payload = {
                    "model": self.model_version,
                    "messages": [{"role": "user", "content": prompt}],
                    "max_tokens": self.max_tokens,
                    "temperature": self.temperature
                }
                
                with httpx.Client(timeout=60.0) as client:
                    response = client.post(
                        self.api_endpoint,
                        headers=headers,
                        json=payload
                    )
                    response.raise_for_status()
                    result = response.json()
                    
                    # 提取响应内容（根据实际API响应格式调整）
                    if "choices" in result and len(result["choices"]) > 0:
                        return result["choices"][0]["message"]["content"]
                    elif "output" in result:
                        return result["output"]["text"]
                    else:
                        return str(result)
        
        return HTTPLLMWrapper(api_key, api_endpoint, model_version, max_tokens, temperature)


class ModelAdapterFactory:
    """模型适配器工厂"""
    
    _adapters = {
        "DEEPSEEK": DeepSeekAdapter,
        "DOUBAO": DoubaoAdapter,
        "KIMI": KimiAdapter,
        "QIANWEN": QianwenAdapter,
        "ZHIPU": ZhipuAdapter,
        "智谱": ZhipuAdapter,  # 支持中文名称
    }
    
    @classmethod
    def create_llm(cls, model_type: str, api_key: str, api_endpoint: str,
                    model_version: str, max_tokens: int, temperature: float):
        """
        创建LLM实例
        
        Args:
            model_type: 模型类型（DEEPSEEK/DOUBAO/KIMI/QIANWEN/ZHIPU/智谱）
            api_key: API密钥
            api_endpoint: API端点
            model_version: 模型版本
            max_tokens: 最大token数
            temperature: 温度参数
            
        Returns:
            LLM实例
        """
        adapter_class = cls._adapters.get(model_type.upper())
        if not adapter_class:
            # 默认使用HTTP适配器
            adapter_class = HTTPLLMAdapter
        
        return adapter_class.create_llm(
            api_key, api_endpoint, model_version, max_tokens, temperature
        )

