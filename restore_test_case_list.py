import os

file_path = r'd:\Demo\test-genius\frontend\src\views\test-case\TestCaseList.vue'

with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# 紧急恢复脚本
# 问题：之前的脚本 replace("", "'") 在每个字符之间插入了 '
# 导致 'abc' 变成了 '''a'b'c'''
# 解决方案：
# 1. 识别原始的单引号，它们现在变成了 '''
# 2. 移除插入的单引号 '
# 3. 恢复原始单引号

# 步骤 1: 保护原始单引号
# 注意：这假设原始文件中没有连续的三个单引号，这在常规代码中通常成立
text = text.replace("'''", "__REAL_QUOTE__")

# 步骤 2: 移除所有剩余的单引号
text = text.replace("'", "")

# 步骤 3: 恢复原始单引号
text = text.replace("__REAL_QUOTE__", "'")

# 步骤 4: 修复可能因保护机制失效而残留的问题
# 例如，如果原始是 "''" (空字符串)，变成了 "''''''" -> "__REAL_QUOTE____REAL_QUOTE__" -> "''" (正确)
# 如果原始是 "' '"，变成了 "''' '' '" -> "__REAL_QUOTE__ __REAL_QUOTE__" -> "' '" (正确)

# 额外的清理：之前的脚本还引入了重复的 IDID, 人人 等，这里尝试再次清理
import re
text = re.sub(r"IDID", "ID", text)
text = re.sub(r"人人", "人", text)
text = text.replace("开始导?", "开始导入")
text = text.replace("态态态", "态")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)

print("Restored TestCaseList.vue")
