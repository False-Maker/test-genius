#!/usr/bin/env python
# -*- coding: utf-8 -*-
import re

file_path = 'frontend/src/views/data-document/DataDocumentGeneration.vue'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# 修复字符串编码问题
content = re.sub(r"正交[^']*\?}\.xlsx", "正交表'}.xlsx", content)
content = re.sub(r"正交[^']*\?}\.docx", "正交表'}.docx", content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Fixed string encoding issues')

