"""
测试设计助手系统 - AI服务主应用
基于FastAPI框架
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import llm_router, case_router, prompt_router, document_router, knowledge_router, case_reuse_router, ui_script_router, flow_document_router, parameter_extraction_router, workflow_router, agent_router

app = FastAPI(
    title="测试设计助手系统 - AI服务",
    description="提供AI能力服务，包括大模型调用、用例生成等",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(llm_router.router, prefix="/api/v1/llm", tags=["大模型调用"])
app.include_router(case_router.router, prefix="/api/v1/case", tags=["用例生成"])
app.include_router(prompt_router.router, prefix="/api/v1/prompt", tags=["提示词"])
app.include_router(document_router.router, prefix="/api/v1/document", tags=["文档解析"])
app.include_router(flow_document_router.router, prefix="/api/v1/flow-documents", tags=["流程文档"])
app.include_router(parameter_extraction_router.router, prefix="/api/v1/parameter-extraction", tags=["参数提取"])
app.include_router(knowledge_router.router, prefix="/api/v1", tags=["知识库"])
app.include_router(case_reuse_router.router, prefix="/api/v1", tags=["用例复用"])
app.include_router(ui_script_router.router, prefix="/api/v1", tags=["UI脚本生成"])
app.include_router(workflow_router.router, prefix="/api/v1/workflow", tags=["工作流"])
app.include_router(agent_router.router, prefix="/api/v1", tags=["Agent"])

# Java后端API代理
from fastapi import Request
import httpx
import logging

logger = logging.getLogger(__name__)

java_backend_url = "http://localhost:8080"

@app.api_route("/java/{path:path}", methods=["GET", "POST", "PUT", "DELETE"])
async def proxy_to_java(path: str, request: Request):
    """代理请求到Java后端（使用异步httpx）"""
    try:
        url = f"{java_backend_url}/{path}"
        
        # 准备请求参数
        query_params = request.query_params.dict()
        
        # 准备请求头（排除host）
        headers = dict(request.headers)
        headers.pop("host", None)
        headers.pop("content-length", None)  # 让httpx重新计算
        
        # 准备请求体
        body = None
        if request.method not in ["GET", "HEAD"]:
            try:
                body = await request.body()
            except:
                pass
        
        # 使用异步客户端转发请求
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.request(
                method=request.method,
                url=url,
                params=query_params,
                headers=headers,
                content=body
            )
        
        # 尝试解析JSON响应
        try:
            return response.json()
        except:
            # 如果不是JSON，返回原始内容（或根据需求包装）
            return {"code": response.status_code, "message": response.text}
            
    except httpx.RequestError as e:
        logger.error(f"代理请求失败(网络错误): {str(e)}")
        return {"code": 502, "message": f"无法连接到Java后端: {str(e)}"}
    except Exception as e:
        logger.error(f"代理请求失败: {str(e)}", exc_info=True)
        return {"code": 500, "message": str(e)}


@app.get("/")
async def root():
    """根路径"""
    return {"message": "测试设计助手系统 - AI服务", "version": "1.0.0"}


@app.get("/health")
async def health():
    """健康检查"""
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

