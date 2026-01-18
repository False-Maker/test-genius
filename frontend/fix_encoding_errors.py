#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复前端文件中的编码错误
"""

import os
import re
import sys
from pathlib import Path

# 编码错误修复映射
FIXES = [
    # 字符串截断修复
    (r"'请输入套件名\?", "'请输入套件名称'"),
    (r"'请选择需\?", "'请选择需求'"),
    (r"'一\?", "'一般'"),
    (r">符合性检查结\?<", ">符合性检查结果<"),
    (r"'不符\?", "'不符合'"),
    (r"'用例生成任务已提交\?", "'用例生成任务已提交'"),
    # 其他常见编码问题
    (r"需求' : '新建需求\)\)", "需求' : '新建需求')"),
    (r"'编辑需求 : '新建需求\)\)", "'编辑需求' : '新建需求')"),
]

def fix_file(file_path: Path):
    """修复单个文件的编码错误"""
    try:
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()
        
        original_content = content
        
        # 应用所有修复
        for pattern, replacement in FIXES:
            content = re.sub(pattern, replacement, content)
        
        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Fixed: {file_path}")
            return True
        return False
    except Exception as e:
        print(f"✗ Error fixing {file_path}: {e}", file=sys.stderr)
        return False

def main():
    """主函数"""
    frontend_dir = Path(__file__).parent
    src_dir = frontend_dir / 'src'
    
    # 要检查的文件扩展名
    extensions = ['.vue', '.ts', '.js']
    
    fixed_count = 0
    total_count = 0
    
    # 遍历所有文件
    for ext in extensions:
        for file_path in src_dir.rglob(f'*{ext}'):
            total_count += 1
            if fix_file(file_path):
                fixed_count += 1
    
    print(f"\n修复完成: {fixed_count}/{total_count} 个文件")

if __name__ == '__main__':
    main()

