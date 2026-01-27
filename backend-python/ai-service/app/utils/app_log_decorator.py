"""
应用日志装饰器
用于记录模型调用和应用操作的日志
"""
import functools
import time
import uuid
import logging
from typing import Dict, Any, Optional
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


def app_log(app_type: str = "", log_request: bool = True, log_response: bool = True):
    """
    应用日志装饰器
    
    Args:
        app_type: 应用类型（如：CASE_GENERATION, UI_SCRIPT_GENERATION等）
        log_request: 是否记录请求参数
        log_response: 是否记录响应结果
    """
    def decorator(func):
        @functools.wraps(func)
        async def async_wrapper(*args, **kwargs):
            request_id = str(uuid.uuid4())
            start_time = time.time()
            result = None
            exception = None
            
            # 尝试从参数中获取db会话
            db: Optional[Session] = None
            for arg in args:
                if isinstance(arg, Session):
                    db = arg
                    break
            if db is None:
                db = kwargs.get('db')
            
            try:
                result = await func(*args, **kwargs)
                return result
            except Exception as e:
                exception = e
                raise
            finally:
                try:
                    if db:
                        record_app_log(
                            db=db,
                            request_id=request_id,
                            app_type=app_type or get_app_type_from_function(func),
                            func_name=func.__name__,
                            args=args if log_request else None,
                            kwargs=kwargs if log_request else None,
                            result=result if log_response else None,
                            exception=exception,
                            execution_time=int((time.time() - start_time) * 1000)
                        )
                except Exception as e:
                    logger.error(f"记录应用日志失败: {e}", exc_info=True)
        
        @functools.wraps(func)
        def sync_wrapper(*args, **kwargs):
            request_id = str(uuid.uuid4())
            start_time = time.time()
            result = None
            exception = None
            
            # 尝试从参数中获取db会话
            db: Optional[Session] = None
            for arg in args:
                if isinstance(arg, Session):
                    db = arg
                    break
            if db is None:
                db = kwargs.get('db')
            
            try:
                result = func(*args, **kwargs)
                return result
            except Exception as e:
                exception = e
                raise
            finally:
                try:
                    if db:
                        record_app_log(
                            db=db,
                            request_id=request_id,
                            app_type=app_type or get_app_type_from_function(func),
                            func_name=func.__name__,
                            args=args if log_request else None,
                            kwargs=kwargs if log_request else None,
                            result=result if log_response else None,
                            exception=exception,
                            execution_time=int((time.time() - start_time) * 1000)
                        )
                except Exception as e:
                    logger.error(f"记录应用日志失败: {e}", exc_info=True)
        
        # 根据函数类型返回对应的包装器
        import inspect
        if inspect.iscoroutinefunction(func):
            return async_wrapper
        else:
            return sync_wrapper
    
    return decorator


def record_app_log(
    db: Session,
    request_id: str,
    app_type: str,
    func_name: str,
    args: Optional[tuple] = None,
    kwargs: Optional[dict] = None,
    result: Optional[Any] = None,
    exception: Optional[Exception] = None,
    execution_time: int = 0
):
    """
    记录应用日志到数据库
    
    Args:
        db: 数据库会话
        request_id: 请求ID
        app_type: 应用类型
        func_name: 函数名
        args: 函数参数（位置参数）
        kwargs: 函数参数（关键字参数）
        result: 函数返回结果
        exception: 异常信息
        execution_time: 执行时间（毫秒）
    """
    try:
        # 提取模型相关信息
        model_code = None
        model_name = None
        prompt = None
        prompt_length = 0
        response = None
        response_length = 0
        tokens_input = None
        tokens_output = None
        tokens_total = None
        response_time = execution_time
        cost = None
        
        # 从kwargs中提取信息
        if kwargs:
            model_code = kwargs.get('model_code')
            prompt = kwargs.get('prompt')
            if prompt:
                prompt_length = len(prompt)
        
        # 从result中提取信息
        if result and isinstance(result, dict):
            model_code = model_code or result.get('model_code')
            model_name = result.get('model_name')
            response = result.get('content')
            if response:
                response_length = len(str(response))
            tokens_total = result.get('tokens_used')
            response_time = result.get('response_time', execution_time)
        
        # 构建请求参数JSON
        request_params = None
        if args or kwargs:
            import json
            params = {}
            if args:
                params['args'] = [str(arg) for arg in args[:3]]  # 只记录前3个参数
            if kwargs:
                # 过滤敏感信息
                safe_kwargs = {k: v for k, v in kwargs.items() 
                             if k not in ['api_key', 'password', 'secret']}
                params['kwargs'] = safe_kwargs
            request_params = json.dumps(params, ensure_ascii=False)
        
        # 构建响应数据JSON
        response_data = None
        if result:
            import json
            try:
                response_data = json.dumps(result, ensure_ascii=False, default=str)
            except:
                response_data = str(result)
        
        # 计算成本（如果有token信息）
        if tokens_total and model_code:
            cost = calculate_cost(db, model_code, tokens_input, tokens_output, tokens_total)
        
        # 插入日志记录
        sql = text("""
            INSERT INTO app_log (
                request_id, app_type, model_code, model_name,
                prompt, prompt_length, response, response_length,
                tokens_input, tokens_output, tokens_total,
                response_time, cost, status, error, error_code,
                request_params, response_data, timestamp, created_at
            ) VALUES (
                :request_id, :app_type, :model_code, :model_name,
                :prompt, :prompt_length, :response, :response_length,
                :tokens_input, :tokens_output, :tokens_total,
                :response_time, :cost, :status, :error, :error_code,
                :request_params, :response_data, :timestamp, :created_at
            )
        """)
        
        db.execute(sql, {
            'request_id': request_id,
            'app_type': app_type,
            'model_code': model_code,
            'model_name': model_name,
            'prompt': prompt,
            'prompt_length': prompt_length,
            'response': response,
            'response_length': response_length,
            'tokens_input': tokens_input,
            'tokens_output': tokens_output,
            'tokens_total': tokens_total,
            'response_time': response_time,
            'cost': cost,
            'status': 'success' if exception is None else 'failed',
            'error': str(exception) if exception else None,
            'error_code': exception.__class__.__name__ if exception else None,
            'request_params': request_params,
            'response_data': response_data,
            'timestamp': datetime.now(),
            'created_at': datetime.now()
        })
        
        db.commit()
        
    except Exception as e:
        logger.error(f"记录应用日志到数据库失败: {e}", exc_info=True)
        db.rollback()


def calculate_cost(db: Session, model_code: str, tokens_input: Optional[int], 
                   tokens_output: Optional[int], tokens_total: Optional[int]) -> Optional[float]:
    """
    计算成本
    
    Args:
        db: 数据库会话
        model_code: 模型代码
        tokens_input: 输入token数
        tokens_output: 输出token数
        tokens_total: 总token数
    
    Returns:
        成本（元）
    """
    try:
        # 查询模型成本配置
        sql = text("""
            SELECT input_price_per_1k_tokens, output_price_per_1k_tokens
            FROM model_cost_config
            WHERE model_code = :model_code AND is_active = true
        """)
        result = db.execute(sql, {'model_code': model_code}).fetchone()
        
        if not result:
            return None
        
        input_price = float(result[0]) if result[0] else 0
        output_price = float(result[1]) if result[1] else 0
        
        # 计算成本
        cost = 0.0
        if tokens_input:
            cost += (tokens_input / 1000.0) * input_price
        if tokens_output:
            cost += (tokens_output / 1000.0) * output_price
        
        # 如果没有分别的输入输出token，使用总token数
        if cost == 0 and tokens_total:
            # 假设输入占70%，输出占30%
            cost = (tokens_total / 1000.0) * (input_price * 0.7 + output_price * 0.3)
        
        return round(cost, 6)
        
    except Exception as e:
        logger.error(f"计算成本失败: {e}", exc_info=True)
        return None


def get_app_type_from_function(func) -> str:
    """
    从函数名推断应用类型
    """
    func_name = func.__name__.lower()
    
    if 'case' in func_name:
        return 'CASE_GENERATION'
    elif 'script' in func_name:
        return 'UI_SCRIPT_GENERATION'
    elif 'document' in func_name:
        return 'DOCUMENT_PARSING'
    elif 'llm' in func_name or 'model' in func_name:
        return 'MODEL_CALL'
    else:
        return 'OTHER'
