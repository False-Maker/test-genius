#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
直接修复编码问题
"""

import re
from pathlib import Path

def fix_file_content(file_path: Path):
    """修复文件内容"""
    try:
        # 使用 errors='replace' 读取，然后修复
        with open(file_path, 'rb') as f:
            content_bytes = f.read()
        
        # 尝试用UTF-8解码
        try:
            content = content_bytes.decode('utf-8')
        except:
            # 如果失败，用GBK尝试
            content = content_bytes.decode('gbk', errors='replace')
        
        original = content
        
        # 修复常见的截断问题
        fixes = [
            (r"'等待\?", "'等待中',"),
            (r"'处理\?", "'处理中',"),
            (r"'脚本已复制到剪贴\?", "'脚本已复制到剪贴板'"),
            (r"'请输入模型名\?", "'请输入模型名称'"),
            (r"'模型名称长度不能超过200个字\?", "'模型名称长度不能超过200个字符'"),
            (r"'请选择需\?", "'请选择需求'"),
            (r"'请输入套件名\?", "'请输入套件名称'"),
            (r"'一\?", "'一般'"),
            (r"符合性检查结\?/span>", "符合性检查结果</span>"),
            (r"不符\?", "不符合"),
            (r"callback\(new Error\('请选择需\?", "callback(new Error('请选择需求'))"),
        ]
        
        for pattern, replacement in fixes:
            content = re.sub(pattern, replacement, content)
        
        if content != original:
            # 写回文件
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False

# 需要修复的文件
files_to_fix = [
    'src/views/test-execution/UIScriptGeneration.vue',
    'src/views/test-execution/TestExecutionManagement.vue',
    'src/views/test-execution/UIScriptRepair.vue',
    'src/views/model-config/ModelConfigList.vue',
    'src/views/specification-check/SpecificationCheck.vue',
    'src/views/case-reuse/CaseReuse.vue',
    'src/views/data-document/DataDocumentGeneration.vue',
    'src/views/test-case-quality/TestCaseQuality.vue',
]

frontend_dir = Path(__file__).parent

for rel_path in files_to_fix:
    file_path = frontend_dir / rel_path
    if file_path.exists():
        print(f"Fixing {rel_path}...")
        if fix_file_content(file_path):
            print(f"  ✓ Fixed")
        else:
            print(f"  - No changes")
    else:
        print(f"  ✗ Not found: {file_path}")

print("\nDone!")

