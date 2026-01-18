import os

file_path = r'd:\Demo\test-genius\frontend\src\views\ui-script-template\UIScriptTemplateList.vue'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if '?' in line or '===' in line or 'title' in line and 'dialog' in line:
        print(f"Line {i+1}: {line.strip()}")
