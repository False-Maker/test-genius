#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
修复前端文件中的字符串截断错误
"""

import os
import re
from pathlib import Path

# 需要修复的文件和行号
FIXES = {
    'src/views/case-reuse/CaseReuse.vue': [
        (188, r"message: '请输入套件名\?", "message: '请输入套件名称'"),
    ],
    'src/views/data-document/DataDocumentGeneration.vue': [
        (249, r"message: '请选择需\?", "message: '请选择需求'"),
        (252, r"callback\(new Error\('请选择需\?", "callback(new Error('请选择需求'))"),
    ],
    'src/views/flow-document/FlowDocumentGeneration.vue': [
        (298, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/knowledge-base/KnowledgeBaseList.vue': [
        (210, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/model-config/ModelConfigList.vue': [
        (304, r"message: '请输入模型名\?", "message: '请输入模型名称'"),
        (305, r"message: '模型名称长度不能超过200个字\?", "message: '模型名称长度不能超过200个字符'"),
    ],
    'src/views/page-element/PageElementList.vue': [
        (249, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/prompt-template/PromptTemplateList.vue': [
        (773, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/requirement/RequirementList.vue': [
        (3206, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/specification-check/SpecificationCheck.vue': [
        (52, r"符合性检查结\?/span>", "符合性检查结果</span>"),
        (54, r"不符\?", "不符合"),
    ],
    'src/views/test-case-quality/TestCaseQuality.vue': [
        (157, r"level === '一\?", "level === '一般'"),
    ],
    'src/views/test-case/TestCaseList.vue': [
        (581, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/test-coverage/TestCoverageAnalysis.vue': [
        (183, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/test-execution/TestExecutionManagement.vue': [
        (361, r"PENDING: '等待\?", "PENDING: '等待中',"),
        (362, r"PROCESSING: '处理\?", "PROCESSING: '处理中',"),
    ],
    'src/views/test-execution/UIScriptGeneration.vue': [
        (262, r"PENDING: '等待\?", "PENDING: '等待中',"),
        (263, r"PROCESSING: '处理\?", "PROCESSING: '处理中',"),
    ],
    'src/views/test-execution/UIScriptRepair.vue': [
        (375, r"ElMessage\.success\('脚本已复制到剪贴\?", "ElMessage.success('脚本已复制到剪贴板')"),
    ],
    'src/views/test-report-template/TestReportTemplateList.vue': [
        (211, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/test-report/TestReportList.vue': [
        (241, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/test-risk-assessment/TestRiskAssessment.vue': [
        (187, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/test-specification/TestSpecificationList.vue': [
        (303, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
    'src/views/ui-script-template/UIScriptTemplateList.vue': [
        (261, r"message: '请选择需\?", "message: '请选择需求'"),
    ],
}

def fix_file(file_path: Path, fixes):
    """修复单个文件"""
    try:
        with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
            lines = f.readlines()
        
        modified = False
        for line_num, pattern, replacement in fixes:
            if line_num <= len(lines):
                line = lines[line_num - 1]
                if pattern in line or re.search(pattern, line):
                    new_line = re.sub(pattern, replacement, line)
                    if new_line != line:
                        lines[line_num - 1] = new_line
                        modified = True
                        print(f"  Line {line_num}: Fixed")
        
        if modified:
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.writelines(lines)
            return True
        return False
    except Exception as e:
        print(f"  Error: {e}")
        return False

def main():
    frontend_dir = Path(__file__).parent
    
    fixed_count = 0
    for rel_path, fixes in FIXES.items():
        file_path = frontend_dir / rel_path
        if file_path.exists():
            print(f"Fixing {rel_path}...")
            if fix_file(file_path, fixes):
                fixed_count += 1
                print(f"  ✓ Fixed")
            else:
                print(f"  - No changes needed")
        else:
            print(f"  ✗ File not found: {file_path}")
    
    print(f"\n修复完成: {fixed_count} 个文件")

if __name__ == '__main__':
    main()

