#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
执行SQL脚本工具
用于执行数据库初始化脚本
"""
try:
    import psycopg2
except ImportError:
    print("错误: 未安装 psycopg2")
    print("请运行: pip install psycopg2-binary")
    sys.exit(1)

import sys
import os
from pathlib import Path

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'test_design_assistant',
    'user': 'postgres',
    'password': 'postgres'
}

def execute_sql_file(conn, sql_file_path):
    """执行SQL文件"""
    cursor = None
    try:
        with open(sql_file_path, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        
        cursor = conn.cursor()
        
        # PostgreSQL的execute方法可以执行多个语句，但需要确保语句正确分割
        # 使用psycopg2的execute方法执行整个SQL文件
        # 注意：如果SQL文件包含多个语句，需要使用execute_batch或逐个执行
        
        # 先尝试执行整个文件
        try:
            cursor.execute(sql_content)
            conn.commit()
        except psycopg2.ProgrammingError as e:
            # 如果失败，尝试分割执行
            conn.rollback()
            # 简单分割SQL语句（按分号分割，忽略注释）
            statements = []
            current_statement = ""
            in_string = False
            string_char = None
            
            for char in sql_content:
                if char in ("'", '"') and (not current_statement or current_statement[-1] != '\\'):
                    if not in_string:
                        in_string = True
                        string_char = char
                    elif char == string_char:
                        in_string = False
                        string_char = None
                
                current_statement += char
                
                if not in_string and char == ';':
                    stmt = current_statement.strip()
                    if stmt and not stmt.startswith('--'):
                        statements.append(stmt)
                    current_statement = ""
            
            # 执行每个语句
            for stmt in statements:
                if stmt.strip():
                    try:
                        cursor.execute(stmt)
                    except Exception as e:
                        # 忽略某些错误（如表已存在等）
                        if 'already exists' not in str(e).lower() and 'duplicate' not in str(e).lower():
                            print(f"   警告: {str(e)[:100]}")
            
            conn.commit()
        
        print(f"[OK] 成功执行: {sql_file_path.name}")
        return True
    except Exception as e:
        print(f"[ERROR] 执行失败: {sql_file_path.name}")
        print(f"   错误信息: {str(e)}")
        if conn:
            conn.rollback()
        return False
    finally:
        if cursor:
            cursor.close()

def check_table_exists(conn, table_name):
    """检查表是否存在"""
    try:
        cursor = conn.cursor()
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_schema = 'public' 
                AND table_name = %s
            );
        """, (table_name,))
        exists = cursor.fetchone()[0]
        cursor.close()
        return exists
    except Exception as e:
        print(f"检查表 {table_name} 时出错: {str(e)}")
        return False

def main():
    """主函数"""
    # 获取项目根目录
    project_root = Path(__file__).parent.parent
    
    # SQL文件列表
    sql_files = [
        project_root / 'database' / 'init' / '01_init_tables.sql',
        project_root / 'database' / 'init' / '04_test_execution_tables.sql',
    ]
    
    # 检查文件是否存在
    for sql_file in sql_files:
        if not sql_file.exists():
            print(f"❌ SQL文件不存在: {sql_file}")
            return 1
    
    # 连接数据库
    try:
        print("正在连接数据库...")
        conn = psycopg2.connect(**DB_CONFIG)
        print("[OK] 数据库连接成功")
    except Exception as e:
        print(f"[ERROR] 数据库连接失败: {str(e)}")
        print("\n请确保:")
        print("1. PostgreSQL服务已启动")
        print("2. 数据库 'test_design_assistant' 已创建")
        print("3. 用户名和密码正确")
        return 1
    
    try:
        # 执行SQL文件
        success_count = 0
        for sql_file in sql_files:
            print(f"\n执行SQL文件: {sql_file.name}")
            if execute_sql_file(conn, sql_file):
                success_count += 1
        
        # 验证关键表是否创建成功
        print("\n验证表是否创建成功...")
        key_tables = [
            'test_report_template',
            'test_report',
            'test_coverage_analysis',
            'test_specification',
            'test_execution_task',
            'test_execution_record',
            'test_risk_assessment'
        ]
        
        all_tables_exist = True
        for table_name in key_tables:
            exists = check_table_exists(conn, table_name)
            if exists:
                print(f"  [OK] {table_name}")
            else:
                print(f"  [ERROR] {table_name} (不存在)")
                all_tables_exist = False
        
        if all_tables_exist:
            print("\n[OK] 所有关键表已成功创建！")
        else:
            print("\n[WARNING] 部分表未创建，请检查SQL脚本")
        
        return 0 if success_count == len(sql_files) and all_tables_exist else 1
        
    except Exception as e:
        print(f"\n❌ 执行过程中出错: {str(e)}")
        return 1
    finally:
        conn.close()
        print("\n数据库连接已关闭")

if __name__ == '__main__':
    sys.exit(main())

