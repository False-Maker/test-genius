#!/usr/bin/env python3
"""
文档完整性检查工具
支持Java和Python代码的文档完整性检查
"""
import os
import re
import sys
from pathlib import Path
from typing import List, Tuple, Dict
from dataclasses import dataclass


@dataclass
class DocCheckResult:
    """文档检查结果"""
    total: int = 0
    missing: int = 0
    coverage: float = 0.0
    details: List[str] = None

    def __post_init__(self):
        if self.details is None:
            self.details = []


class JavaDocChecker:
    """Java代码文档检查器"""
    
    def __init__(self, src_dir: str):
        self.src_dir = Path(src_dir)
        self.class_result = DocCheckResult()
        self.method_result = DocCheckResult()
    
    def check(self) -> Tuple[DocCheckResult, DocCheckResult]:
        """执行检查"""
        java_files = list(self.src_dir.rglob("*.java"))
        
        for java_file in java_files:
            # 跳过测试文件
            if "test" in java_file.parts or "Test" in java_file.name:
                continue
            
            self._check_class(java_file)
            self._check_methods(java_file)
        
        # 计算覆盖率
        if self.class_result.total > 0:
            self.class_result.coverage = (
                (self.class_result.total - self.class_result.missing) 
                * 100 / self.class_result.total
            )
        
        if self.method_result.total > 0:
            self.method_result.coverage = (
                (self.method_result.total - self.method_result.missing)
                * 100 / self.method_result.total
            )
        
        return self.class_result, self.method_result
    
    def _check_class(self, file_path: Path):
        """检查类文档"""
        content = file_path.read_text(encoding='utf-8')
        
        # 查找类定义
        class_pattern = r'^\s*(public\s+)?(abstract\s+)?(final\s+)?class\s+\w+'
        for match in re.finditer(class_pattern, content, re.MULTILINE):
            self.class_result.total += 1
            line_num = content[:match.start()].count('\n') + 1
            
            # 检查类前是否有JavaDoc
            before_class = content[:match.start()]
            if not re.search(r'/\*\*.*?\*/', before_class, re.DOTALL):
                self.class_result.missing += 1
                self.class_result.details.append(
                    f"  ⚠️  类缺少文档: {file_path.relative_to(self.src_dir.parent.parent)} (行 {line_num})"
                )
    
    def _check_methods(self, file_path: Path):
        """检查方法文档"""
        content = file_path.read_text(encoding='utf-8')
        
        # 查找公共方法（排除getter/setter）
        method_pattern = r'^\s*public\s+(?!.*(?:get|set|is)[A-Z])\w+\s+\w+\s*\([^)]*\)'
        for match in re.finditer(method_pattern, content, re.MULTILINE):
            self.method_result.total += 1
            line_num = content[:match.start()].count('\n') + 1
            
            # 检查方法前是否有JavaDoc
            before_method = content[:match.start()]
            # 查找最近的JavaDoc（在方法前50行内）
            lines_before = before_method.split('\n')[-50:]
            if not any('/**' in line for line in lines_before):
                self.method_result.missing += 1
                self.method_result.details.append(
                    f"  ⚠️  方法缺少文档: {file_path.relative_to(self.src_dir.parent.parent)} (行 {line_num})"
                )


class PythonDocChecker:
    """Python代码文档检查器"""
    
    def __init__(self, src_dir: str):
        self.src_dir = Path(src_dir)
        self.module_result = DocCheckResult()
        self.class_result = DocCheckResult()
        self.function_result = DocCheckResult()
    
    def check(self) -> Tuple[DocCheckResult, DocCheckResult, DocCheckResult]:
        """执行检查"""
        python_files = list(self.src_dir.rglob("*.py"))
        
        for py_file in python_files:
            # 跳过测试文件和__init__.py
            if "test" in py_file.parts or "__pycache__" in py_file.parts:
                continue
            
            self._check_module(py_file)
            self._check_classes(py_file)
            self._check_functions(py_file)
        
        # 计算覆盖率
        for result in [self.module_result, self.class_result, self.function_result]:
            if result.total > 0:
                result.coverage = (
                    (result.total - result.missing) * 100 / result.total
                )
        
        return self.module_result, self.class_result, self.function_result
    
    def _check_module(self, file_path: Path):
        """检查模块文档"""
        self.module_result.total += 1
        
        content = file_path.read_text(encoding='utf-8')
        lines = content.split('\n')[:10]
        
        # 检查前10行是否有Docstring
        has_docstring = any('"""' in line or "'''" in line for line in lines)
        if not has_docstring:
            self.module_result.missing += 1
            self.module_result.details.append(
                f"  ⚠️  模块缺少文档: {file_path.relative_to(self.src_dir.parent.parent)}"
            )
    
    def _check_classes(self, file_path: Path):
        """检查类文档"""
        content = file_path.read_text(encoding='utf-8')
        lines = content.split('\n')
        
        class_pattern = r'^\s*class\s+(\w+)'
        for i, line in enumerate(lines):
            match = re.match(class_pattern, line)
            if match:
                self.class_result.total += 1
                
                # 检查类定义后5行内是否有Docstring
                has_docstring = False
                for j in range(i + 1, min(i + 6, len(lines))):
                    if '"""' in lines[j] or "'''" in lines[j]:
                        has_docstring = True
                        break
                
                if not has_docstring:
                    self.class_result.missing += 1
                    self.class_result.details.append(
                        f"  ⚠️  类缺少文档: {file_path.relative_to(self.src_dir.parent.parent)} (行 {i+1})"
                    )
    
    def _check_functions(self, file_path: Path):
        """检查函数文档"""
        content = file_path.read_text(encoding='utf-8')
        lines = content.split('\n')
        
        function_pattern = r'^\s*def\s+([a-zA-Z_]\w*)\s*\('
        for i, line in enumerate(lines):
            match = re.match(function_pattern, line)
            if match:
                func_name = match.group(1)
                # 跳过私有方法
                if func_name.startswith('_') and not func_name.startswith('__'):
                    continue
                
                self.function_result.total += 1
                
                # 检查函数定义后5行内是否有Docstring
                has_docstring = False
                for j in range(i + 1, min(i + 6, len(lines))):
                    if '"""' in lines[j] or "'''" in lines[j]:
                        has_docstring = True
                        break
                
                if not has_docstring:
                    self.function_result.missing += 1
                    self.function_result.details.append(
                        f"  ⚠️  函数缺少文档: {file_path.relative_to(self.src_dir.parent.parent)} (行 {i+1})"
                    )


def print_result(title: str, result: DocCheckResult):
    """打印检查结果"""
    print(f"\n{title}:")
    print(f"  总数: {result.total}")
    print(f"  缺少文档: {result.missing}")
    print(f"  文档覆盖率: {result.coverage:.1f}%")
    if result.details:
        print("  详情:")
        for detail in result.details[:10]:  # 只显示前10个
            print(detail)
        if len(result.details) > 10:
            print(f"  ... 还有 {len(result.details) - 10} 个")


def main():
    """主函数"""
    print("=" * 50)
    print("文档完整性检查工具")
    print("=" * 50)
    
    # 检查Java代码
    java_src = "backend-java/test-design-assistant-core/src/main/java"
    if os.path.exists(java_src):
        print("\n" + "=" * 50)
        print("检查Java代码...")
        print("=" * 50)
        
        java_checker = JavaDocChecker(java_src)
        class_result, method_result = java_checker.check()
        
        print_result("类文档", class_result)
        print_result("方法文档", method_result)
    
    # 检查Python代码
    python_src = "backend-python/ai-service/app"
    if os.path.exists(python_src):
        print("\n" + "=" * 50)
        print("检查Python代码...")
        print("=" * 50)
        
        python_checker = PythonDocChecker(python_src)
        module_result, class_result, function_result = python_checker.check()
        
        print_result("模块文档", module_result)
        print_result("类文档", class_result)
        print_result("函数文档", function_result)
    
    print("\n" + "=" * 50)
    print("检查完成")
    print("=" * 50)


if __name__ == "__main__":
    main()

