#!/bin/bash
# Java代码文档完整性检查脚本
# 检查Java类和方法是否缺少JavaDoc注释

echo "=========================================="
echo "Java代码文档完整性检查"
echo "=========================================="

JAVA_SRC_DIR="backend-java/test-design-assistant-core/src/main/java"
TOTAL_CLASSES=0
CLASSES_WITHOUT_DOC=0
TOTAL_METHODS=0
METHODS_WITHOUT_DOC=0

# 检查类是否缺少JavaDoc
echo ""
echo "检查类文档..."
for file in $(find "$JAVA_SRC_DIR" -name "*.java" -type f); do
    # 跳过测试文件
    if [[ "$file" == *"test"* ]] || [[ "$file" == *"Test"* ]]; then
        continue
    fi
    
    TOTAL_CLASSES=$((TOTAL_CLASSES + 1))
    
    # 检查类是否有JavaDoc注释（查找 /** 开头的注释，在类定义之前）
    if ! grep -q "/\*\*" "$file" || ! grep -q "@author\|@date\|@since" "$file"; then
        CLASSES_WITHOUT_DOC=$((CLASSES_WITHOUT_DOC + 1))
        echo "  ⚠️  缺少文档: $file"
    fi
done

# 检查公共方法是否缺少JavaDoc
echo ""
echo "检查公共方法文档..."
for file in $(find "$JAVA_SRC_DIR" -name "*.java" -type f); do
    # 跳过测试文件
    if [[ "$file" == *"test"* ]] || [[ "$file" == *"Test"* ]]; then
        continue
    fi
    
    # 提取公共方法（public方法，排除getter/setter）
    while IFS= read -r line; do
        if [[ "$line" =~ ^[[:space:]]*public[[:space:]]+[^()]+\( ]]; then
            # 排除getter/setter
            if [[ ! "$line" =~ (get|set|is)[A-Z] ]]; then
                TOTAL_METHODS=$((TOTAL_METHODS + 1))
                # 检查方法前是否有JavaDoc
                line_num=$(grep -n "$line" "$file" | cut -d: -f1)
                if [ -n "$line_num" ]; then
                    # 检查前5行是否有JavaDoc
                    if ! sed -n "$((line_num-5)),$((line_num-1))p" "$file" | grep -q "/\*\*"; then
                        METHODS_WITHOUT_DOC=$((METHODS_WITHOUT_DOC + 1))
                        echo "  ⚠️  方法缺少文档: $file (行 $line_num)"
                    fi
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
echo "总类数: $TOTAL_CLASSES"
echo "缺少文档的类: $CLASSES_WITHOUT_DOC"
if [ $TOTAL_CLASSES -gt 0 ]; then
    CLASS_COVERAGE=$(( (TOTAL_CLASSES - CLASSES_WITHOUT_DOC) * 100 / TOTAL_CLASSES ))
    echo "类文档覆盖率: ${CLASS_COVERAGE}%"
fi

echo ""
echo "总方法数: $TOTAL_METHODS"
echo "缺少文档的方法: $METHODS_WITHOUT_DOC"
if [ $TOTAL_METHODS -gt 0 ]; then
    METHOD_COVERAGE=$(( (TOTAL_METHODS - METHODS_WITHOUT_DOC) * 100 / TOTAL_METHODS ))
    echo "方法文档覆盖率: ${METHOD_COVERAGE}%"
fi

echo ""
if [ $CLASSES_WITHOUT_DOC -eq 0 ] && [ $METHODS_WITHOUT_DOC -eq 0 ]; then
    echo "✅ 所有代码都有完整的文档注释！"
    exit 0
else
    echo "⚠️  存在缺少文档的代码，请补充JavaDoc注释"
    exit 1
fi

