import os
import re

src_dir = r'd:\Demo\test-genius\frontend\src'

patterns = [
    (r"\?[^'\s]+'", "Missing quote after ? (e.g. ?禁用')"),
    (r"=== '[^']+' ''[^']", "Corrupted ternary (=== '1' ''warn)"),
    (r"\|\|\s*-\s*\}\}", "Unquoted dash in interpolation (|| - }})"),
    (r"from (?!['\"@])[\w\-\/]+", "Unquoted import (from vue)"),
    (r"message:\s*[^'\s{]", "Unquoted message (message: 请输入)"),
    (r"label=\"[^\"]+IDID\"", "Repeated IDID"),
    (r"placeholder=\"[^\"]+人人\"", "Repeated 人人"),
    (r"{{.*\|\|\s*-\s*}}", "Unquoted dash interpolation (strict)")
]

for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith('.vue'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            for pat, desc in patterns:
                matches = re.finditer(pat, content)
                for m in matches:
                    print(f"File: {path}\n  Error: {desc}\n  Match: {m.group(0).strip()[:50]}...\n")
                    break # show one per file per pattern to save space
