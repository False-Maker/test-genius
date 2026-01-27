"""
工作流执行API路由
"""
import logging
from typing import Dict, Any, Optional
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.workflow_engine import WorkflowEngine

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/execute")
async def execute_workflow(
    workflow_config: str,
    input_data: Dict[str, Any],
    workflow_id: Optional[int] = None,
    workflow_code: Optional[str] = None,
    workflow_version: Optional[int] = None,
    db: Session = Depends(get_db)
):
    """
    执行工作流
    
    Args:
        workflow_config: 工作流配置（JSON字符串）
        input_data: 输入数据
        workflow_id: 工作流定义ID（可选）
        workflow_code: 工作流代码（可选）
        workflow_version: 工作流版本（可选）
        db: 数据库会话
        
    Returns:
        执行结果
    """
    try:
        engine = WorkflowEngine(db)
        result = engine.execute_workflow(workflow_config, input_data)
        
        # 如果提供了工作流信息，记录执行记录
        if workflow_id:
            engine.record_execution(
                execution_id=result.get("execution_id"),
                workflow_id=workflow_id,
                workflow_code=workflow_code or "",
                workflow_version=workflow_version or 1,
                status=result.get("status"),
                input_data=input_data,
                output_data=result.get("output"),
                error_message=result.get("error"),
                error_node_id=result.get("error_node")
            )
        
        return result
        
    except Exception as e:
        logger.error(f"执行工作流失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"执行工作流失败: {str(e)}")
