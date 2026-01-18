#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复前端 Vue 文件中的语法错误
"""
import re
import os
import sys
from pathlib import Path

# 设置输出编码
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

def fix_requirement_list():
    """修复 RequirementList.vue 中的无效结束标签"""
    file_path = Path('src/views/requirement/RequirementList.vue')
    
    if not file_path.exists():
        print(f"文件不存在: {file_path}")
        return False
    
    content = file_path.read_text(encoding='utf-8')
    original_content = content
    
    # 修复缺失的 el-form-item 开始标签
    # 在第 1484 行 </el-form-item> 之后，第 1492 行 <el-input> 之前添加
    # 使用 DOTALL 标志匹配跨行内容
    pattern = r'(        </el-form-item>\r?\n\r?\n\r?\n\r?\n\r?\n\r?\n)(                  <el-input)'
    replacement = r'\1        <el-form-item label="需求描述" prop="requirementDescription">\n\2'
    
    content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    if content != original_content:
        file_path.write_text(content, encoding='utf-8')
        print(f"✓ 已修复 {file_path}")
        return True
    else:
        print(f"  {file_path} 无需修复")
        return False

def fix_scss_warnings():
    """修复 SCSS 文件中的废弃警告"""
    scss_files = [
        Path('src/styles/main.scss'),
        Path('src/styles/variables.scss')
    ]
    
    fixed = False
    for file_path in scss_files:
        if not file_path.exists():
            continue
        
        content = file_path.read_text(encoding='utf-8')
        original_content = content
        
        # 替换 @import 为 @use (如果可能)
        # 注意：这需要更复杂的处理，这里只做基本修复
        
        # 替换 lighten() 和 darken()
        if 'variables.scss' in str(file_path):
            # lighten($color, 10%) -> color.scale($color, $lightness: 10%)
            content = re.sub(
                r'lighten\(([^,]+),\s*(\d+)%\)',
                r'color.scale(\1, $lightness: \2%)',
                content
            )
            # darken($color, 10%) -> color.scale($color, $lightness: -10%)
            content = re.sub(
                r'darken\(([^,]+),\s*(\d+)%\)',
                r'color.scale(\1, $lightness: -\2%)',
                content
            )
        
        if content != original_content:
            file_path.write_text(content, encoding='utf-8')
            print(f"✓ 已修复 SCSS 警告 {file_path}")
            fixed = True
    
    return fixed

def main():
    """主函数"""
    print("开始修复前端问题...")
    
    os.chdir(Path(__file__).parent)
    
    fixed_files = []
    
    # 修复 RequirementList.vue
    if fix_requirement_list():
        fixed_files.append('RequirementList.vue')
    
    # 修复 SCSS 警告
    if fix_scss_warnings():
        fixed_files.append('SCSS files')
    
    if fixed_files:
        print(f"\n✓ 已修复 {len(fixed_files)} 个问题:")
        for f in fixed_files:
            print(f"  - {f}")
    else:
        print("\n 未发现需要修复的问题")

if __name__ == '__main__':
    main()

