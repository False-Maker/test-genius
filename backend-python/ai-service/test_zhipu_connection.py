"""
测试智谱API连接
使用提供的API Key测试智谱模型是否能正常调用
"""
import sys
import os
import io

# 设置UTF-8编码（Windows兼容）
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 添加项目路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.utils.model_adapter import ModelAdapterFactory

# 智谱API配置
API_KEY = "66cc3f6b950d463d93a4685576b23e21.Jk967nrJx2Zu8cY7"
API_ENDPOINT = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
MODEL_VERSION = "glm-4"
MAX_TOKENS = 1000
TEMPERATURE = 0.7

# 测试提示词
TEST_PROMPT = "你好，请简单介绍一下你自己。"


def test_zhipu_connection():
    """测试智谱API连接"""
    print("=" * 60)
    print("开始测试智谱API连接...")
    print("=" * 60)
    print(f"API端点: {API_ENDPOINT}")
    print(f"模型版本: {MODEL_VERSION}")
    print(f"测试提示词: {TEST_PROMPT}")
    print("-" * 60)
    
    try:
        # 创建智谱LLM实例
        print("正在创建智谱LLM实例...")
        llm = ModelAdapterFactory.create_llm(
            model_type="ZHIPU",  # 或使用 "智谱"
            api_key=API_KEY,
            api_endpoint=API_ENDPOINT,
            model_version=MODEL_VERSION,
            max_tokens=MAX_TOKENS,
            temperature=TEMPERATURE
        )
        print("[OK] LLM实例创建成功")
        print("-" * 60)
        
        # 调用模型
        print("正在调用智谱模型...")
        response = llm.invoke(TEST_PROMPT)
        
        # 提取响应内容
        if hasattr(response, 'content'):
            content = response.content
        elif hasattr(response, 'text'):
            content = response.text
        else:
            content = str(response)
        
        print("[OK] 模型调用成功！")
        print("-" * 60)
        print("模型响应:")
        print(content)
        print("=" * 60)
        print("[SUCCESS] 测试通过：智谱API连接正常！")
        return True
        
    except Exception as e:
        print("=" * 60)
        print("[FAILED] 测试失败：智谱API连接异常")
        print(f"错误信息: {str(e)}")
        print(f"错误类型: {type(e).__name__}")
        import traceback
        print("\n详细错误堆栈:")
        traceback.print_exc()
        print("=" * 60)
        return False


if __name__ == "__main__":
    success = test_zhipu_connection()
    sys.exit(0 if success else 1)

