
import os
import re

def scan_files():
    root_dir = '.'
    
    suspicious_patterns = [
        (r'<<<<<<<', 'Git Merge Conflict Start'),
        (r'=======', 'Git Merge Conflict Middle'),
        (r'>>>>>>>', 'Git Merge Conflict End'),
        (r'[\uFFFD]', 'Replacement Character (Encoding Issue)'),
        (r'\x00', 'Null Byte (Binary Garbage)'),
    ]

    findings = {}

    for dirpath, _, filenames in os.walk(root_dir):
        # Exclude directories
        if 'node_modules' in dirpath or '.git' in dirpath or 'dist' in dirpath:
            continue
            
        for filename in filenames:
            if not filename.endswith(('.vue', '.ts', '.js', '.css', '.html', '.json')):
                continue

            full_path = os.path.join(dirpath, filename)
            
            try:
                with open(full_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                # Try to read as binary and see if it looks like garbage or wrong encoding
                try:
                    with open(full_path, 'r', encoding='gbk') as f:
                        content = f.read()
                        findings.setdefault(full_path, []).append("File encoded as GBK (not UTF-8)")
                except:
                    findings.setdefault(full_path, []).append("Binary/Corrupted file (Cannot decode as UTF-8 or GBK)")
                    continue

            # Check specific patterns
            for pattern, desc in suspicious_patterns:
                if re.search(pattern, content):
                    findings.setdefault(full_path, []).append(desc)

            # Check for basic parsing errors (very crude)
            # e.g. check for missing closing braces if file is small enough? 
            # No, that's too hard to get right with regex.
            
            # Check for "Props" or type definitions that look corrupted like the one we saw:
            # "Array.isArray(props.modelValue) 'props.modelValue"
            if re.search(r"\) '[a-zA-Z]", content): 
                 findings.setdefault(full_path, []).append("Suspected corrupted ternary operator or string (quote missing)")

            if re.search(r"undefined\s*:\s*[a-zA-Z]", content) and not re.search(r"undefined\s*:\s*['\"].*['\"]", content) and not re.search(r"undefined\s*:\s*(true|false|null|undefined|\d+)", content):
                 # This is tricky, might be valid code. 
                 pass

            # Check specifically for the missing quote pattern seen in TestCaseList
            # value: DRAFT (without quotes in object literal)
            # This is hard to regex without false positives (e.g. value: SomeEnum.DRAFT)
            # But we saw: label="草稿" value="DRAFT" which is valid in template. 
            # The error was in script: caseStatus: 'DRAFT' vs caseStatus: DRAFT
            
    # Output findings
    with open('scan_findings.txt', 'w', encoding='utf-8') as f:
        for filepath, issues in findings.items():
            f.write(f"File: {filepath}\n")
            for issue in issues:
                f.write(f"  - {issue}\n")
            f.write("\n")

    print(f"Scan complete. Found potential issues in {len(findings)} files.")

if __name__ == "__main__":
    scan_files()
