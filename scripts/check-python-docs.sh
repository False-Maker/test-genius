#!/bin/bash
# Python代码文档完整性检查脚本
# 检查Python模块、类和方法是否缺少Docstring注释

echo "=========================================="
echo "Python代码文档完整性检查"
echo "=========================================="

PYTHON_SRC_DIR="backend-python/ai-service/app"
TOTAL_MODULES=0
MODULES_WITHOUT_DOC=0
TOTAL_CLASSES=0
CLASSES_WITHOUT_DOC=0
TOTAL_FUNCTIONS=0
FUNCTIONS_WITHOUT_DOC=0

# 检查模块是否缺少Docstring
echo ""
echo "检查模块文档..."
for file in $(find "$PYTHON_SRC_DIR" -name "*.py" -type f); do
    # 跳过测试文件和__init__.py
    if [[ "$file" == *"test"* ]] || [[ "$file" == *"__pycache__"* ]] || [[ "$file" == *"__init__.py" ]]; then
        continue
    fi
    
    TOTAL_MODULES=$((TOTAL_MODULES + 1))
    
    # 检查文件开头是否有Docstring（前10行）
    if ! head -10 "$file" | grep -q '"""' && ! head -10 "$file" | grep -q "'''"; then
        MODULES_WITHOUT_DOC=$((MODULES_WITHOUT_DOC + 1))
        echo "  ⚠️  缺少模块文档: $file"
    fi
done

# 检查类和方法是否缺少Docstring
echo ""
echo "检查类和方法文档..."
for file in $(find "$PYTHON_SRC_DIR" -name "*.py" -type f); do
    # 跳过测试文件
    if [[ "$file" == *"test"* ]] || [[ "$file" == *"__pycache__"* ]]; then
        continue
    fi
    
    # 提取类定义
    while IFS= read -r line; do
        if [[ "$line" =~ ^[[:space:]]*class[[:space:]]+[A-Za-z_][A-Za-z0-9_]* ]]; then
            TOTAL_CLASSES=$((TOTAL_CLASSES + 1))
            line_num=$(grep -n "$line" "$file" | cut -d: -f1)
            if [ -n "$line_num" ]; then
                # 检查类定义后是否有Docstring（检查下5行）
                if ! sed -n "$((line_num+1)),$((line_num+5))p" "$file" | grep -q '"""\|'''\'''\'''\'''; then
                    CLASSES_WITHOUT_DOC=$((CLASSES_WITHOUT_DOC + 1))
                    echo "  ⚠️  类缺少文档: $file (行 $line_num)"
                fi
            fi
        fi
    done < "$file"
    
    # 提取函数定义（def开头的，排除私有方法）
    while IFS= read -r line; do
        if [[ "$line" =~ ^[[:space:]]*def[[:space:]]+[a-zA-Z_][a-zA-Z0-9_]* ]] && [[ ! "$line" =~ ^[[:space:]]*def[[:space:]]+_[_a-z] ]]; then
            TOTAL_FUNCTIONS=$((TOTAL_FUNCTIONS + 1))
            line_num=$(grep -n "$line" "$file" | cut -d: -f1)
            if [ -n "$line_num" ]; then
                # 检查函数定义后是否有Docstring（检查下5行）
                if ! sed -n "$((line_num+1)),$((line_num+5))p" "$file" | grep -q '"""\|'''\'''\'''\'''; then
                    FUNCTIONS_WITHOUT_DOC=$((FUNCTIONS_WITHOUT_DOC + 1))
                    echo "  ⚠️  函数缺少文档: $file (行 $line_num)"
                fi
            fi
        fi
    done < "$file"
done

# 输出统计结果
echo ""
echo "=========================================="
echo "检查结果统计"
echo "=========================================="
echo "总模块数: $TOTAL_MODULES"
echo "缺少文档的模块: $MODULES_WITHOUT_DOC"
if [ $TOTAL_MODULES -gt 0 ]; then
    MODULE_COVERAGE=$(( (TOTAL_MODULES - MODULES_WITHOUT_DOC) * 100 / TOTAL_MODULES ))
    echo "模块文档覆盖率: ${MODULE_COVERAGE}%"
fi

echo ""
echo "总类数: $TOTAL_CLASSES"
echo "缺少文档的类: $CLASSES_WITHOUT_DOC"
if [ $TOTAL_CLASSES -gt 0 ]; then
    CLASS_COVERAGE=$(( (TOTAL_CLASSES - CLASSES_WITHOUT_DOC) * 100 / TOTAL_CLASSES ))
    echo "类文档覆盖率: ${CLASS_COVERAGE}%"
fi

echo ""
echo "总函数数: $TOTAL_FUNCTIONS"
echo "缺少文档的函数: $FUNCTIONS_WITHOUT_DOC"
if [ $TOTAL_FUNCTIONS -gt 0 ]; then
    FUNCTION_COVERAGE=$(( (TOTAL_FUNCTIONS - FUNCTIONS_WITHOUT_DOC) * 100 / TOTAL_FUNCTIONS ))
    echo "函数文档覆盖率: ${FUNCTION_COVERAGE}%"
fi

echo ""
if [ $MODULES_WITHOUT_DOC -eq 0 ] && [ $CLASSES_WITHOUT_DOC -eq 0 ] && [ $FUNCTIONS_WITHOUT_DOC -eq 0 ]; then
    echo "✅ 所有代码都有完整的文档注释！"
    exit 0
else
    echo "⚠️  存在缺少文档的代码，请补充Docstring注释"
    exit 1
fi

