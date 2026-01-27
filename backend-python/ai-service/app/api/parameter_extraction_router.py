"""
参数提取API路由
"""
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import logging
from sqlalchemy.orm import Session
from app.database import get_db
from app.services.parameter_extraction_service import ParameterExtractionService

router = APIRouter()
logger = logging.getLogger(__name__)


class TestCaseInfo(BaseModel):
    """测试用例信息"""
    case_name: Optional[str] = None
    test_step: Optional[str] = None
    expected_result: Optional[str] = None
    pre_condition: Optional[str] = None


class ParameterExtractionRequest(BaseModel):
    """参数提取请求"""
    test_cases: List[TestCaseInfo]
    model_code: Optional[str] = None
    use_llm: bool = True


class ParameterInfo(BaseModel):
    """参数信息"""
    name: str
    description: Optional[str] = None
    type: Optional[str] = None
    valid_equivalence_classes: List[str] = []
    invalid_equivalence_classes: List[str] = []


class ParameterExtractionResponse(BaseModel):
    """参数提取响应"""
    parameters: List[ParameterInfo]
    equivalence_classes: Dict[str, Dict[str, List[str]]]


@router.post("/extract", response_model=ParameterExtractionResponse)
async def extract_parameters(
    request: ParameterExtractionRequest,
    db: Session = Depends(get_db)
):
    """
    从测试用例中提取参数和等价类
    
    - 输入：测试用例列表
    - 输出：提取的参数和等价类
    """
    import time
    
    start_time = time.time()
    
    logger.info(f"收到参数提取请求: 用例数量={len(request.test_cases)}, 使用LLM={request.use_llm}")
    
    try:
        # 验证参数
        if not request.test_cases:
            raise HTTPException(status_code=400, detail="测试用例列表不能为空")
        
        # 转换为字典格式
        test_cases_dict = []
        for case in request.test_cases:
            test_cases_dict.append({
                "case_name": case.case_name or "",
                "test_step": case.test_step or "",
                "expected_result": case.expected_result or "",
                "pre_condition": case.pre_condition or ""
            })
        
        # 创建参数提取服务
        extraction_service = ParameterExtractionService(db)
        
        # 提取参数和等价类
        result = extraction_service.extract_parameters_and_equivalence_classes(
            test_cases=test_cases_dict,
            model_code=request.model_code,
            use_llm=request.use_llm
        )
        
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"参数提取成功: 提取到 {len(result.get('parameters', []))} 个参数, "
            f"耗时={elapsed_time}ms"
        )
        
        # 转换为响应格式
        parameters = [
            ParameterInfo(**param) for param in result.get("parameters", [])
        ]
        
        return ParameterExtractionResponse(
            parameters=parameters,
            equivalence_classes=result.get("equivalence_classes", {})
        )
    
    except ValueError as e:
        logger.error(f"参数提取失败(参数错误): {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"参数提取失败: 耗时={elapsed_time}ms, "
            f"错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(
            status_code=500,
            detail=f"参数提取失败: {str(e)}"
        )
