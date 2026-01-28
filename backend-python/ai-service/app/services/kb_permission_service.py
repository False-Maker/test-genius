"""
知识库权限服务
管理知识库的访问权限
"""
import logging
from typing import List, Dict, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text

logger = logging.getLogger(__name__)


class KBPermissionService:
    """知识库权限服务"""
    
    def __init__(self, db: Session):
        """
        初始化权限服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
    
    def grant_permission(
        self,
        kb_id: int,
        user_id: int,
        permission_type: str
    ) -> bool:
        """
        授予权限
        
        Args:
            kb_id: 知识库ID
            user_id: 用户ID
            permission_type: 权限类型（read/write/admin）
            
        Returns:
            是否成功
        """
        try:
            # 检查知识库是否存在
            check_sql = """
            SELECT id FROM knowledge_base WHERE id = :kb_id
            """
            result = self.db.execute(text(check_sql), {"kb_id": kb_id})
            if not result.fetchone():
                logger.warning(f"知识库不存在: {kb_id}")
                return False
            
            # 检查权限是否已存在
            check_exist_sql = """
            SELECT id FROM knowledge_base_permission 
            WHERE kb_id = :kb_id 
            AND user_id = :user_id 
            AND permission_type = :permission_type
            """
            result = self.db.execute(
                text(check_exist_sql),
                {"kb_id": kb_id, "user_id": user_id, "permission_type": permission_type}
            )
            if result.fetchone():
                logger.warning(f"权限已存在: kb_id={kb_id}, user_id={user_id}, permission_type={permission_type}")
                return True  # 已存在，视为成功
            
            # 插入权限
            insert_sql = """
            INSERT INTO knowledge_base_permission 
            (kb_id, user_id, permission_type)
            VALUES 
            (:kb_id, :user_id, :permission_type)
            RETURNING id
            """
            
            result = self.db.execute(
                text(insert_sql),
                {
                    "kb_id": kb_id,
                    "user_id": user_id,
                    "permission_type": permission_type
                }
            )
            self.db.commit()
            
            logger.info(f"权限授予成功: kb_id={kb_id}, user_id={user_id}, permission_type={permission_type}")
            return True
            
        except Exception as e:
            logger.error(f"授予权限失败: {str(e)}")
            self.db.rollback()
            return False
    
    def revoke_permission(
        self,
        kb_id: int,
        user_id: int,
        permission_type: str
    ) -> bool:
        """
        撤销权限
        
        Args:
            kb_id: 知识库ID
            user_id: 用户ID
            permission_type: 权限类型
            
        Returns:
            是否成功
        """
        try:
            delete_sql = """
            DELETE FROM knowledge_base_permission
            WHERE kb_id = :kb_id
            AND user_id = :user_id
            AND permission_type = :permission_type
            """
            
            result = self.db.execute(
                text(delete_sql),
                {
                    "kb_id": kb_id,
                    "user_id": user_id,
                    "permission_type": permission_type
                }
            )
            self.db.commit()
            
            logger.info(f"权限撤销成功: kb_id={kb_id}, user_id={user_id}, permission_type={permission_type}")
            return True
            
        except Exception as e:
            logger.error(f"撤销权限失败: {str(e)}")
            self.db.rollback()
            return False
    
    def check_permission(
        self,
        kb_id: int,
        user_id: int,
        permission_type: str
    ) -> bool:
        """
        检查权限
        
        Args:
            kb_id: 知识库ID
            user_id: 用户ID
            permission_type: 权限类型
            
        Returns:
            是否有权限
        """
        try:
            check_sql = """
            SELECT id FROM knowledge_base_permission
            WHERE kb_id = :kb_id
            AND user_id = :user_id
            AND permission_type = :permission_type
            LIMIT 1
            """
            
            result = self.db.execute(
                text(check_sql),
                {
                    "kb_id": kb_id,
                    "user_id": user_id,
                    "permission_type": permission_type
                }
            )
            
            has_permission = result.fetchone() is not None
            return has_permission
            
        except Exception as e:
            logger.error(f"检查权限失败: {str(e)}")
            return False
    
    def get_user_kb_list(
        self,
        user_id: int,
        permission_type: Optional[str] = None
    ) -> List[Dict]:
        """
        获取用户的知识库列表
        
        Args:
            user_id: 用户ID
            permission_type: 权限类型过滤（可选）
            
        Returns:
            知识库列表
        """
        try:
            query_sql = """
            SELECT 
                kb.id, kb.kb_code, kb.kb_name, kb.kb_description,
                kb.kb_type, kb.embedding_model, kb.chunking_strategy,
                kb.chunk_size, kb.chunk_overlap, kb.is_active,
                kb.creator_id, kb.create_time, kb.update_time
            FROM knowledge_base kb
            INNER JOIN knowledge_base_permission kbp 
                ON kb.id = kbp.kb_id
            WHERE kbp.user_id = :user_id
            AND kb.is_active = '1'
            """
            
            params = {"user_id": user_id}
            
            if permission_type:
                query_sql += " AND kbp.permission_type = :permission_type"
                params["permission_type"] = permission_type
            
            query_sql += " ORDER BY kb.update_time DESC"
            
            result = self.db.execute(text(query_sql), params)
            rows = result.fetchall()
            
            kb_list = []
            for row in rows:
                kb_list.append({
                    "id": row[0],
                    "kb_code": row[1],
                    "kb_name": row[2],
                    "kb_description": row[3],
                    "kb_type": row[4],
                    "embedding_model": row[5],
                    "chunking_strategy": row[6],
                    "chunk_size": row[7],
                    "chunk_overlap": row[8],
                    "is_active": row[9],
                    "creator_id": row[10],
                    "create_time": row[11],
                    "update_time": row[12]
                })
            
            logger.info(f"获取用户知识库列表成功: user_id={user_id}, 数量={len(kb_list)}")
            return kb_list
            
        except Exception as e:
            logger.error(f"获取用户知识库列表失败: {str(e)}")
            return []
    
    def get_kb_permissions(
        self,
        kb_id: int
    ) -> List[Dict]:
        """
        获取知识库的权限列表
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            权限列表
        """
        try:
            query_sql = """
            SELECT 
                kbp.id, kbp.kb_id, kbp.user_id, kbp.permission_type,
                kbp.create_time
            FROM knowledge_base_permission kbp
            WHERE kbp.kb_id = :kb_id
            ORDER BY kbp.create_time DESC
            """
            
            result = self.db.execute(text(query_sql), {"kb_id": kb_id})
            rows = result.fetchall()
            
            permissions = []
            for row in rows:
                permissions.append({
                    "id": row[0],
                    "kb_id": row[1],
                    "user_id": row[2],
                    "permission_type": row[3],
                    "create_time": row[4]
                })
            
            logger.info(f"获取知识库权限列表成功: kb_id={kb_id}, 数量={len(permissions)}")
            return permissions
            
        except Exception as e:
            logger.error(f"获取知识库权限列表失败: {str(e)}")
            return []
    
    def batch_grant_permissions(
        self,
        kb_id: int,
        user_ids: List[int],
        permission_type: str
    ) -> Dict:
        """
        批量授予权限
        
        Args:
            kb_id: 知识库ID
            user_ids: 用户ID列表
            permission_type: 权限类型
            
        Returns:
            操作结果
        """
        try:
            success_count = 0
            failed_count = 0
            errors = []
            
            for user_id in user_ids:
                if self.grant_permission(kb_id, user_id, permission_type):
                    success_count += 1
                else:
                    failed_count += 1
                    errors.append(f"user_id={user_id}")
            
            result = {
                "success_count": success_count,
                "failed_count": failed_count,
                "errors": errors
            }
            
            logger.info(f"批量授予权限完成: kb_id={kb_id}, 成功={success_count}, 失败={failed_count}")
            return result
            
        except Exception as e:
            logger.error(f"批量授予权限失败: {str(e)}")
            return {
                "success_count": 0,
                "failed_count": len(user_ids),
                "errors": [str(e)]
            }
    
    def batch_revoke_permissions(
        self,
        kb_id: int,
        user_ids: List[int],
        permission_type: Optional[str] = None
    ) -> Dict:
        """
        批量撤销权限
        
        Args:
            kb_id: 知识库ID
            user_ids: 用户ID列表
            permission_type: 权限类型（可选，不指定则删除所有权限）
            
        Returns:
            操作结果
        """
        try:
            success_count = 0
            failed_count = 0
            errors = []
            
            for user_id in user_ids:
                # 如果未指定权限类型，删除该用户在该知识库的所有权限
                if permission_type is None:
                    # 获取该用户在该知识库的所有权限类型
                    get_permissions_sql = """
                    SELECT DISTINCT permission_type 
                    FROM knowledge_base_permission
                    WHERE kb_id = :kb_id AND user_id = :user_id
                    """
                    result = self.db.execute(
                        text(get_permissions_sql),
                        {"kb_id": kb_id, "user_id": user_id}
                    )
                    perm_types = [row[0] for row in result.fetchall()]
                    
                    # 删除所有权限
                    all_success = True
                    for pt in perm_types:
                        if not self.revoke_permission(kb_id, user_id, pt):
                            all_success = False
                    
                    if all_success:
                        success_count += 1
                    else:
                        failed_count += 1
                        errors.append(f"user_id={user_id}")
                else:
                    # 删除指定权限类型
                    if self.revoke_permission(kb_id, user_id, permission_type):
                        success_count += 1
                    else:
                        failed_count += 1
                        errors.append(f"user_id={user_id}")
            
            result = {
                "success_count": success_count,
                "failed_count": failed_count,
                "errors": errors
            }
            
            logger.info(f"批量撤销权限完成: kb_id={kb_id}, 成功={success_count}, 失败={failed_count}")
            return result
            
        except Exception as e:
            logger.error(f"批量撤销权限失败: {str(e)}")
            return {
                "success_count": 0,
                "failed_count": len(user_ids),
                "errors": [str(e)]
            }

