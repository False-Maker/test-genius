"""
用例复用服务
实现用例检索、相似用例推荐、用例组合等功能
"""
import logging
from typing import List, Dict, Optional
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)


class CaseReuseService:
    """用例复用服务"""
    
    def __init__(self, db: Session):
        """
        初始化用例复用服务
        
        Args:
            db: 数据库会话
        """
        self.db = db
        self.embedding_service = EmbeddingService(db)
    
    def init_case_vector_table(self) -> bool:
        """
        初始化用例向量表（如果使用pgvector）
        
        Returns:
            是否初始化成功
        """
        try:
            # 检查并安装pgvector扩展
            if not self.embedding_service.install_pgvector_extension():
                logger.warning("pgvector扩展未安装，将使用关系型数据库存储")
                return False
            
            # 在test_case表上添加向量列（如果不存在）
            # 注意：这里假设test_case表已存在，只添加向量列
            try:
                add_column_sql = """
                ALTER TABLE test_case 
                ADD COLUMN IF NOT EXISTS embedding vector(512);  -- BAAI/bge-small-zh-v1.5
                """
                self.db.execute(text(add_column_sql))
                self.db.commit()
                
                # 创建向量索引
                create_index_sql = """
                CREATE INDEX IF NOT EXISTS idx_test_case_embedding 
                ON test_case 
                USING ivfflat (embedding vector_cosine_ops)
                WITH (lists = 100);
                """
                self.db.execute(text(create_index_sql))
                self.db.commit()
                
                logger.info("用例向量表初始化成功")
                return True
                
            except Exception as e:
                # 如果列已存在，忽略错误
                if "already exists" in str(e).lower() or "duplicate" in str(e).lower():
                    logger.info("用例向量列已存在")
                    return True
                raise
                
        except Exception as e:
            logger.error(f"初始化用例向量表失败: {str(e)}")
            self.db.rollback()
            return False
    
    def update_case_embedding(self, case_id: int) -> bool:
        """
        更新用例的向量表示
        
        Args:
            case_id: 用例ID
            
        Returns:
            是否更新成功
        """
        try:
            # 查询用例信息
            query_sql = """
            SELECT case_name, pre_condition, test_step, expected_result
            FROM test_case
            WHERE id = :case_id
            """
            result = self.db.execute(text(query_sql), {"case_id": case_id})
            row = result.fetchone()
            
            if not row:
                logger.warning(f"用例不存在: {case_id}")
                return False
            
            # 构建用例文本（用于向量化）
            case_text = f"{row[0]}\n{row[1] or ''}\n{row[2] or ''}\n{row[3] or ''}"
            
            # 生成向量
            embedding = self.embedding_service.get_embedding(case_text)
            
            if embedding is None:
                logger.warning(f"用例向量化失败: {case_id}")
                return False
            
            # 更新用例向量
            update_sql = """
            UPDATE test_case
            SET embedding = :embedding::vector
            WHERE id = :case_id
            """
            self.db.execute(
                text(update_sql),
                {
                    "case_id": case_id,
                    "embedding": str(embedding)
                }
            )
            self.db.commit()
            
            logger.info(f"用例向量更新成功: {case_id}")
            return True
            
        except Exception as e:
            logger.error(f"更新用例向量失败: {str(e)}")
            self.db.rollback()
            return False
    
    def search_similar_cases(
        self,
        case_text: str,
        layer_id: Optional[int] = None,
        method_id: Optional[int] = None,
        top_k: int = 10,
        similarity_threshold: float = 0.7
    ) -> List[Dict]:
        """
        搜索相似用例（基于语义相似度）
        
        Args:
            case_text: 用例文本（用例名称、测试步骤等）
            layer_id: 测试分层ID（可选，用于过滤）
            method_id: 测试方法ID（可选，用于过滤）
            top_k: 返回前K个结果
            similarity_threshold: 相似度阈值（0-1）
            
        Returns:
            相似用例列表，按相似度排序
        """
        try:
            # 获取查询文本的向量
            query_embedding = self.embedding_service.get_embedding(case_text)
            
            if query_embedding is None:
                logger.warning("查询文本向量化失败，使用关键词检索")
                return self.search_cases_by_keyword(case_text, layer_id, method_id, top_k)
            
            # 构建SQL查询
            search_sql = """
            SELECT 
                id, case_code, case_name, requirement_id, layer_id, method_id,
                case_type, case_priority, pre_condition, test_step, expected_result,
                case_status, create_time,
                1 - (embedding <=> :query_embedding::vector) as similarity
            FROM test_case
            WHERE case_status IN ('已审核', '待审核')
            AND embedding IS NOT NULL
            """
            
            params = {
                "query_embedding": str(query_embedding),
                "similarity_threshold": similarity_threshold,
                "top_k": top_k
            }
            
            if layer_id:
                search_sql += " AND layer_id = :layer_id"
                params["layer_id"] = layer_id
            
            if method_id:
                search_sql += " AND method_id = :method_id"
                params["method_id"] = method_id
            
            search_sql += """
            AND (1 - (embedding <=> :query_embedding::vector)) >= :similarity_threshold
            ORDER BY embedding <=> :query_embedding::vector
            LIMIT :top_k
            """
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            cases = []
            for row in rows:
                cases.append({
                    "id": row[0],
                    "case_code": row[1],
                    "case_name": row[2],
                    "requirement_id": row[3],
                    "layer_id": row[4],
                    "method_id": row[5],
                    "case_type": row[6],
                    "case_priority": row[7],
                    "pre_condition": row[8],
                    "test_step": row[9],
                    "expected_result": row[10],
                    "case_status": row[11],
                    "create_time": row[12],
                    "similarity": float(row[13]) if row[13] else 0.0
                })
            
            logger.info(f"相似用例检索完成，找到 {len(cases)} 个用例")
            return cases
            
        except Exception as e:
            logger.error(f"相似用例检索失败: {str(e)}")
            # 降级到关键词检索
            return self.search_cases_by_keyword(case_text, layer_id, method_id, top_k)
    
    def search_cases_by_keyword(
        self,
        keyword: str,
        layer_id: Optional[int] = None,
        method_id: Optional[int] = None,
        top_k: int = 10
    ) -> List[Dict]:
        """
        关键词检索用例（降级方案）
        
        Args:
            keyword: 关键词
            layer_id: 测试分层ID（可选）
            method_id: 测试方法ID（可选）
            top_k: 返回前K个结果
            
        Returns:
            用例列表
        """
        try:
            search_sql = """
            SELECT 
                id, case_code, case_name, requirement_id, layer_id, method_id,
                case_type, case_priority, pre_condition, test_step, expected_result,
                case_status, create_time
            FROM test_case
            WHERE case_status IN ('已审核', '待审核')
            AND (case_name LIKE :keyword OR test_step LIKE :keyword)
            """
            
            params = {
                "keyword": f"%{keyword}%",
                "top_k": top_k
            }
            
            if layer_id:
                search_sql += " AND layer_id = :layer_id"
                params["layer_id"] = layer_id
            
            if method_id:
                search_sql += " AND method_id = :method_id"
                params["method_id"] = method_id
            
            search_sql += " ORDER BY create_time DESC LIMIT :top_k"
            
            result = self.db.execute(text(search_sql), params)
            rows = result.fetchall()
            
            cases = []
            for row in rows:
                cases.append({
                    "id": row[0],
                    "case_code": row[1],
                    "case_name": row[2],
                    "requirement_id": row[3],
                    "layer_id": row[4],
                    "method_id": row[5],
                    "case_type": row[6],
                    "case_priority": row[7],
                    "pre_condition": row[8],
                    "test_step": row[9],
                    "expected_result": row[10],
                    "case_status": row[11],
                    "create_time": row[12],
                    "similarity": 0.0  # 关键词检索没有相似度
                })
            
            logger.info(f"关键词检索完成，找到 {len(cases)} 个用例")
            return cases
            
        except Exception as e:
            logger.error(f"关键词检索失败: {str(e)}")
            return []
    
    def recommend_similar_cases(
        self,
        case_id: int,
        top_k: int = 5
    ) -> List[Dict]:
        """
        推荐相似用例（基于现有用例）
        
        Args:
            case_id: 用例ID
            top_k: 返回前K个推荐用例
            
        Returns:
            推荐用例列表
        """
        try:
            # 查询用例信息
            query_sql = """
            SELECT case_name, pre_condition, test_step, expected_result, layer_id, method_id
            FROM test_case
            WHERE id = :case_id
            """
            result = self.db.execute(text(query_sql), {"case_id": case_id})
            row = result.fetchone()
            
            if not row:
                logger.warning(f"用例不存在: {case_id}")
                return []
            
            # 构建用例文本
            case_text = f"{row[0]}\n{row[1] or ''}\n{row[2] or ''}\n{row[3] or ''}"
            
            # 搜索相似用例（排除自己）
            similar_cases = self.search_similar_cases(
                case_text=case_text,
                layer_id=row[4],
                method_id=row[5],
                top_k=top_k + 1,  # 多查一个，排除自己
                similarity_threshold=0.6
            )
            
            # 过滤掉自己
            similar_cases = [c for c in similar_cases if c["id"] != case_id]
            
            return similar_cases[:top_k]
            
        except Exception as e:
            logger.error(f"推荐相似用例失败: {str(e)}")
            return []
    
    def create_case_suite(
        self,
        suite_name: str,
        case_ids: List[int],
        creator_id: Optional[int] = None
    ) -> Optional[int]:
        """
        创建用例组合（测试套件）
        
        Args:
            suite_name: 套件名称
            case_ids: 用例ID列表
            creator_id: 创建人ID
            
        Returns:
            套件ID，如果失败返回None
        """
        try:
            # 生成套件编码
            from datetime import datetime
            suite_code = f"SUITE-{datetime.now().strftime('%Y%m%d')}-{len(case_ids)}"
            
            # 创建套件表（如果不存在）
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS test_case_suite (
                id BIGSERIAL PRIMARY KEY,
                suite_code VARCHAR(100) UNIQUE NOT NULL,
                suite_name VARCHAR(500) NOT NULL,
                suite_description TEXT,
                creator_id BIGINT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS test_suite_case (
                id BIGSERIAL PRIMARY KEY,
                suite_id BIGINT NOT NULL,
                case_id BIGINT NOT NULL,
                case_order INT DEFAULT 0,
                FOREIGN KEY (suite_id) REFERENCES test_case_suite(id) ON DELETE CASCADE,
                FOREIGN KEY (case_id) REFERENCES test_case(id) ON DELETE CASCADE
            );
            
            CREATE INDEX IF NOT EXISTS idx_suite_code ON test_case_suite(suite_code);
            CREATE INDEX IF NOT EXISTS idx_suite_case_suite_id ON test_suite_case(suite_id);
            CREATE INDEX IF NOT EXISTS idx_suite_case_case_id ON test_suite_case(case_id);
            """
            
            self.db.execute(text(create_table_sql))
            
            # 插入套件
            insert_suite_sql = """
            INSERT INTO test_case_suite (suite_code, suite_name, creator_id)
            VALUES (:suite_code, :suite_name, :creator_id)
            RETURNING id
            """
            result = self.db.execute(
                text(insert_suite_sql),
                {
                    "suite_code": suite_code,
                    "suite_name": suite_name,
                    "creator_id": creator_id
                }
            )
            suite_id = result.fetchone()[0]
            
            # 插入套件用例关联
            for index, case_id in enumerate(case_ids):
                insert_case_sql = """
                INSERT INTO test_suite_case (suite_id, case_id, case_order)
                VALUES (:suite_id, :case_id, :case_order)
                """
                self.db.execute(
                    text(insert_case_sql),
                    {
                        "suite_id": suite_id,
                        "case_id": case_id,
                        "case_order": index
                    }
                )
            
            self.db.commit()
            logger.info(f"用例套件创建成功: {suite_code}, ID: {suite_id}")
            return suite_id
            
        except Exception as e:
            logger.error(f"创建用例套件失败: {str(e)}")
            self.db.rollback()
            return None

