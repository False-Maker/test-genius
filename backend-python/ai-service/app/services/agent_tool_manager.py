"""
工具权限管理
"""
import logging
from typing import Dict, Any, List
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class ToolPermissionManager:
    """工具权限管理器"""
    
    def __init__(self, db: Session):
        """
        初始化权限管理器
        
        Args:
            db: 数据库会话
        """
        self.db = db
    
    def check_permission(self, tool_code: str, user_id: int = None) -> bool:
        """
        检查工具权限
        
        Args:
            tool_code: 工具编码
            user_id: 用户ID（可选，不指定则不检查用户权限）
            
        Returns:
            是否有权限
        """
        try:
            # 查询工具信息
            result = self.db.execute(
                text("""
                    SELECT permission_level, is_active FROM agent_tool 
                    WHERE tool_code = :tool_code
                """),
                {"tool_code": tool_code}
            )
            tool_row = result.fetchone()
            
            if not tool_row:
                logger.warning(f"工具不存在: {tool_code}")
                return False
            
            # 检查工具是否启用
            if tool_row[1] != '1':
                logger.warning(f"工具未启用: {tool_code}")
                return False
            
            permission_level = tool_row[0]  # permission_level
            
            # ADMIN级别工具，需要检查用户权限
            if permission_level == "ADMIN":
                if not user_id:
                    return False
                
                # 检查用户是否有管理员权限
                user_result = self.db.execute(
                    text("SELECT COUNT(*) FROM sys_user WHERE id = :user_id AND is_admin = '1'"),
                    {"user_id": user_id}
                )
                count = user_result.fetchone()[0]
                
                return count > 0
            
            # NORMAL级别工具，所有用户都有权限
            return True
        
        except Exception as e:
            logger.error(f"检查工具权限失败: {tool_code}, 错误: {str(e)}", exc_info=True)
            return False
    
    def check_permissions(self, tool_codes: List[str], user_id: int = None) -> Dict[str, bool]:
        """
        批量检查工具权限
        
        Args:
            tool_codes: 工具编码列表
            user_id: 用户ID
            
        Returns:
            权限字典（tool_code -> has_permission）
        """
        permissions = {}
        for tool_code in tool_codes:
            permissions[tool_code] = self.check_permission(tool_code, user_id)
        
        return permissions
    
    def get_user_tools(self, user_id: int, permission_level: str = None) -> List[Dict[str, Any]]:
        """
        获取用户可用的工具列表
        
        Args:
            user_id: 用户ID
            permission_level: 权限级别过滤（可选）
            
        Returns:
            工具列表
        """
        try:
            # 构建查询条件
            conditions = ["is_active = '1'"]
            params = {}
            
            if permission_level:
                conditions.append("permission_level = :permission_level")
                params["permission_level"] = permission_level
            
            where_clause = " AND ".join(conditions)
            
            result = self.db.execute(
                text(f"""
                    SELECT tool_code, tool_name, tool_type, tool_description, permission_level
                    FROM agent_tool
                    WHERE {where_clause}
                    ORDER BY tool_code ASC
                """),
                params
            )
            
            tools = []
            for row in result:
                tools.append({
                    "tool_code": row[0],
                    "tool_name": row[1],
                    "tool_type": row[2],
                    "tool_description": row[3],
                    "permission_level": row[4]
                })
            
            # 进一步根据用户权限过滤
            if user_id:
                # 检查用户是否是管理员
                user_result = self.db.execute(
                    text("SELECT is_admin FROM sys_user WHERE id = :user_id"),
                    {"user_id": user_id}
                )
                user_row = user_result.fetchone()
                is_admin = user_row and user_row[0] == '1'
                
                if not is_admin:
                    # 非管理员用户，只能访问NORMAL级别工具
                    tools = [t for t in tools if t["permission_level"] == "NORMAL"]
            
            return tools
        
        except Exception as e:
            logger.error(f"获取用户工具列表失败: {str(e)}", exc_info=True)
            return []
    
    def log_tool_usage(self, session_id: int, tool_code: str, user_id: int = None, 
                     result_success: bool = True, execution_time: int = 0):
        """
        记录工具使用（用于统计和审计）
        
        Args:
            session_id: 会话ID
            tool_code: 工具编码
            user_id: 用户ID
            result_success: 是否成功
            execution_time: 执行时间（毫秒）
        """
        try:
            # 如果已经通过AgentService记录，这里可以不做重复记录
            # 这个方法可以用于额外的统计和分析
            
            logger.info(
                f"工具使用记录: session={session_id}, "
                f"tool={tool_code}, user={user_id}, "
                f"success={result_success}, time={execution_time}ms"
            )
        
        except Exception as e:
            logger.error(f"记录工具使用失败: {str(e)}", exc_info=True)


class ToolPermissionDeniedException(Exception):
    """工具权限拒绝异常"""
    pass


def check_tool_permission(tool_code: str, user_id: int = None):
    """
    工具权限检查装饰器工厂
    
    用于装饰工具执行方法，在执行前检查权限
    
    用法：
    @check_tool_permission("my_tool", user_id=123)
    def my_tool_execution(arguments, context):
        # 工具逻辑
        pass
    """
    def decorator(func):
        def wrapper(*args, **kwargs):
            # 获取db会话（假设在context或第一个参数中）
            db = kwargs.get('db')
            if not db and args:
                from sqlalchemy.orm import Session
                if isinstance(args[0], Session):
                    db = args[0]
            
            if not db:
                # 如果没有db，直接执行（可能是在测试中）
                return func(*args, **kwargs)
            
            # 检查权限
            manager = ToolPermissionManager(db)
            if not manager.check_permission(tool_code, user_id):
                raise ToolPermissionDeniedException(
                    f"权限不足，无法使用工具: {tool_code}"
                )
            
            # 执行函数
            return func(*args, **kwargs)
        
        return wrapper
    
    return decorator

