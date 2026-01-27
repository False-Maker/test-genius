"""
测试设计助手系统 - AI服务主应用
基于FastAPI框架
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import llm_router, case_router, prompt_router, document_router, knowledge_router, case_reuse_router, ui_script_router, flow_document_router, parameter_extraction_router, workflow_router

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

