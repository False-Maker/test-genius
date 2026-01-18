#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
检查test_specification表结构
"""
import psycopg2
import sys

DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'test_design_assistant',
    'user': 'postgres',
    'password': 'postgres'
}

def check_table():
    """检查表是否存在以及字段是否正确"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 检查表是否存在
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_schema = 'public' 
                AND table_name = 'test_specification'
            );
        """)
        exists = cursor.fetchone()[0]
        
        if not exists:
            print("[ERROR] 表 test_specification 不存在")
            return False
        
        print("[OK] 表 test_specification 存在")
        
        # 检查字段
        cursor.execute("""
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_schema = 'public' 
            AND table_name = 'test_specification'
            ORDER BY ordinal_position;
        """)
        
        columns = cursor.fetchall()
        print("\n表字段:")
        for col in columns:
            print(f"  - {col[0]} ({col[1]}, nullable={col[2]})")
        
        # 检查关键字段
        required_fields = ['id', 'spec_code', 'spec_name', 'spec_type', 'is_active', 'spec_name']
        existing_fields = [col[0] for col in columns]
        
        missing_fields = [f for f in required_fields if f not in existing_fields]
        if missing_fields:
            print(f"\n[ERROR] 缺少字段: {missing_fields}")
            return False
        
        print("\n[OK] 所有必需字段都存在")
        
        # 尝试查询
        try:
            cursor.execute("SELECT COUNT(*) FROM test_specification")
            count = cursor.fetchone()[0]
            print(f"\n[OK] 表中有 {count} 条记录")
        except Exception as e:
            print(f"\n[ERROR] 查询表时出错: {str(e)}")
            return False
        
        cursor.close()
        conn.close()
        return True
        
    except Exception as e:
        print(f"[ERROR] 检查表时出错: {str(e)}")
        return False

if __name__ == '__main__':
    success = check_table()
    sys.exit(0 if success else 1)

