from fastapi import APIRouter, HTTPException, Depends
from fastapi.concurrency import run_in_threadpool
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from sqlalchemy.orm import Session
import logging
import time
import json
import re
from app.database import get_db
from app.services.llm_service import LLMService

router = APIRouter()
logger = logging.getLogger(__name__)


class ModelConfigData(BaseModel):
    model_code: Optional[str] = None
    model_name: Optional[str] = None
    model_type: Optional[str] = None
    api_endpoint: Optional[str] = None
    api_key: Optional[str] = None
    model_version: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None



class RequirementAnalyzeRequest(BaseModel):
    requirement_text: str = Field(..., min_length=1, description="需求文本")
    prompt: Optional[str] = None
    model_code: Optional[str] = None
    max_tokens: Optional[int] = None
    temperature: Optional[float] = None
    model_cfg: Optional[ModelConfigData] = Field(None, alias="model_config")




@router.post("/requirement/analyze")
async def analyze_requirement(
    request: RequirementAnalyzeRequest,
    db: Session = Depends(get_db)
):
    start_time = time.time()
    text = request.requirement_text.strip() if request.requirement_text else ""
    if not text:
        raise HTTPException(status_code=400, detail="需求文本不能为空")

    prompt = request.prompt or _build_prompt(text)
    llm_service = LLMService(db)

    try:
        # 使用model_config中的参数优先
        effective_model_code = request.model_cfg.model_code if request.model_cfg else request.model_code
        effective_max_tokens = request.model_cfg.max_tokens if request.model_cfg else request.max_tokens
        effective_temperature = request.model_cfg.temperature if request.model_cfg else request.temperature
        
        logger.info(f"effective_model_code: {effective_model_code}")
        result = await run_in_threadpool(
            llm_service.call_model_with_config,
            model_code=effective_model_code,
            prompt=prompt,
            model_config=request.model_cfg.dict() if request.model_cfg else None
        )
        content = result.get("content", "")
        parsed = _parse_analysis_output(content)
        analysis = _normalize_analysis(parsed, text)
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.info(
            f"需求分析成功: response_time={result.get('response_time', elapsed_time)}ms"
        )
        return {
            "success": True,
            "result": analysis,
            "message": "success",
            "model_code": result.get("model_code"),
            "response_time": result.get("response_time", elapsed_time)
        }
    except Exception as e:
        elapsed_time = int((time.time() - start_time) * 1000)
        logger.error(
            f"需求分析失败: 耗时={elapsed_time}ms, 错误={str(e)}",
            exc_info=True
        )
        raise HTTPException(status_code=500, detail=f"需求分析失败: {str(e)}")


def _build_prompt(requirement_text: str) -> str:
    return (
        "请分析以下需求文档，提取测试要点和业务规则，并以JSON返回。\n\n"
        f"{requirement_text}\n\n"
        "只返回JSON，格式如下：\n"
        "{\n"
        '  "test_points": [\n'
        '    {"name": "要点名称", "description": "要点描述", "priority": "高|中|低"}\n'
        "  ],\n"
        '  "business_rules": [\n'
        '    {"name": "规则名称", "description": "规则描述", "type": "业务规则"}\n'
        "  ],\n"
        '  "key_info": {\n'
        '    "keywords": ["关键词1", "关键词2"],\n'
        '    "modules": ["模块1"],\n'
        '    "roles": ["角色1"]\n'
        "  }\n"
        "}"
    )


def _parse_analysis_output(content: str) -> Optional[Dict[str, Any]]:
    if not content or not content.strip():
        return None
    text = content.strip()
    json_block = _extract_json_block(text)
    if json_block:
        try:
            return json.loads(json_block)
        except Exception:
            pass
    try:
        return json.loads(text)
    except Exception:
        return None


def _extract_json_block(text: str) -> Optional[str]:
    fence_match = re.search(r"```json\s*([\s\S]*?)```", text, re.IGNORECASE)
    if fence_match:
        return fence_match.group(1).strip()
    obj_match = re.search(r"\{[\s\S]*\}", text)
    if obj_match:
        return obj_match.group(0)
    arr_match = re.search(r"\[[\s\S]*\]", text)
    if arr_match:
        return arr_match.group(0)
    return None


def _normalize_analysis(parsed: Optional[Dict[str, Any]], requirement_text: str) -> Dict[str, Any]:
    if isinstance(parsed, list):
        parsed = {"test_points": parsed}
    if not isinstance(parsed, dict):
        return _fallback_analysis(requirement_text)

    result = {
        "test_points": _normalize_test_points(parsed.get("test_points")),
        "business_rules": _normalize_business_rules(parsed.get("business_rules")),
        "key_info": _normalize_key_info(parsed, requirement_text)
    }

    if not result["test_points"] and not result["business_rules"]:
        return _fallback_analysis(requirement_text)

    return result


def _normalize_test_points(items: Any) -> List[Dict[str, Any]]:
    if not items:
        return []
    result = []
    if isinstance(items, dict):
        items = [items]
    for item in items if isinstance(items, list) else []:
        if isinstance(item, str):
            result.append({
                "name": item[:50] if item else "测试要点",
                "description": item,
                "priority": "中"
            })
        elif isinstance(item, dict):
            name = item.get("name") or item.get("title") or item.get("point") or item.get("test_point")
            description = item.get("description") or item.get("desc") or item.get("detail") or name or ""
            priority = item.get("priority") or item.get("level") or "中"
            result.append({
                "name": name or (description[:50] if description else "测试要点"),
                "description": description or name or "",
                "priority": str(priority)
            })
    return result


def _normalize_business_rules(items: Any) -> List[Dict[str, Any]]:
    if not items:
        return []
    result = []
    if isinstance(items, dict):
        items = [items]
    for item in items if isinstance(items, list) else []:
        if isinstance(item, str):
            result.append({
                "name": item[:50] if item else "业务规则",
                "description": item,
                "type": "业务规则"
            })
        elif isinstance(item, dict):
            name = item.get("name") or item.get("title") or item.get("rule") or item.get("business_rule")
            description = item.get("description") or item.get("desc") or item.get("detail") or name or ""
            rule_type = item.get("type") or item.get("category") or "业务规则"
            result.append({
                "name": name or (description[:50] if description else "业务规则"),
                "description": description or name or "",
                "type": str(rule_type)
            })
    return result


def _normalize_key_info(parsed: Dict[str, Any], requirement_text: str) -> Dict[str, Any]:
    key_info = parsed.get("key_info") or parsed.get("keyInfo") or {}
    if not isinstance(key_info, dict):
        key_info = {}
    if "keywords" not in key_info and "关键词" in parsed:
        key_info["keywords"] = parsed.get("关键词")
    if "modules" not in key_info and "模块" in parsed:
        key_info["modules"] = parsed.get("模块")
    if "roles" not in key_info and "角色" in parsed:
        key_info["roles"] = parsed.get("角色")

    if "content_length" not in key_info:
        key_info["content_length"] = len(requirement_text)
    if "sentence_count" not in key_info:
        key_info["sentence_count"] = len([s for s in re.split(r"[。！？\n]", requirement_text) if s.strip()])
    if "keywords" not in key_info:
        key_info["keywords"] = _extract_keywords(requirement_text)

    return key_info


def _fallback_analysis(requirement_text: str) -> Dict[str, Any]:
    sentences = [s.strip() for s in re.split(r"[。！？\n]", requirement_text) if s.strip()]
    test_points = []
    business_rules = []
    for sentence in sentences:
        if re.search(r"(测试|验证|检查)", sentence):
            test_points.append({
                "name": sentence[:50],
                "description": sentence,
                "priority": "中"
            })
        if re.search(r"(规则|要求|必须|禁止)", sentence):
            business_rules.append({
                "name": sentence[:50],
                "description": sentence,
                "type": "业务规则"
            })

    if not test_points:
        test_points = [{
            "name": "功能测试",
            "description": "验证功能是否按需求实现",
            "priority": "高"
        }]
    if not business_rules:
        business_rules = [{
            "name": "业务规则",
            "description": "遵循业务规则和规范",
            "type": "业务规则"
        }]

    key_info = {
        "keywords": _extract_keywords(requirement_text),
        "content_length": len(requirement_text),
        "sentence_count": len(sentences)
    }

    return {
        "test_points": test_points,
        "business_rules": business_rules,
        "key_info": key_info
    }


def _extract_keywords(text: str) -> List[str]:
    words = re.split(r"[\s，。！？、\n]", text)
    keywords = []
    for word in words:
        word = word.strip()
        if 2 <= len(word) <= 6 and word not in keywords:
            keywords.append(word)
        if len(keywords) >= 10:
            break
    return keywords
