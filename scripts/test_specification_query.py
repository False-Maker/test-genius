#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
测试test_specification表的查询
"""
import psycopg2

DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'test_design_assistant',
    'user': 'postgres',
    'password': 'postgres'
}

def test_query():
    """测试Repository中使用的查询"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 测试Repository中的查询
        query = """
            SELECT * FROM test_specification ts WHERE 
            (:specName IS NULL OR ts.spec_name ILIKE '%' || :specName || '%') AND 
            (:specType IS NULL OR ts.spec_type = :specType) AND 
            (:isActive IS NULL OR ts.is_active = :isActive)
            LIMIT 10 OFFSET 0
        """
        
        # 使用参数化查询
        cursor.execute(query.replace(':specName', 'NULL').replace(':specType', 'NULL').replace(':isActive', 'NULL'))
        results = cursor.fetchall()
        
        print(f"[OK] 查询成功，返回 {len(results)} 条记录")
        
        # 测试带参数的查询
        query2 = """
            SELECT * FROM test_specification ts WHERE 
            (NULL IS NULL OR ts.spec_name ILIKE '%' || NULL || '%') AND 
            (NULL IS NULL OR ts.spec_type = NULL) AND 
            (NULL IS NULL OR ts.is_active = NULL)
            LIMIT 10 OFFSET 0
        """
        
        cursor.execute(query2)
        results2 = cursor.fetchall()
        print(f"[OK] 带NULL参数的查询成功，返回 {len(results2)} 条记录")
        
        cursor.close()
        conn.close()
        return True
        
    except Exception as e:
        print(f"[ERROR] 查询失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    test_query()

