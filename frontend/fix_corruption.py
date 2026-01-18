
import os
import re

def fix_corruption():
    root_dir = 'src'
    if not os.path.exists(root_dir):
        print(f"Directory {root_dir} not found.")
        return

    # Patterns to fix
    # 1. Corrupted ternary: "condition ' trueVal : falseVal" -> "condition ? trueVal : falseVal"
    #    The replacement char might be represented variously, but usually it's a space or a specific garbage char if read as utf-8.
    #    We'll look for the pattern: logic/variable followed by " ' " or similar garbage then value : value
    
    # 2. Corrupted Chinese: "?" -> "需求", "?" -> "用例" - This is hard/risky to do blindly.
    #    But we can fix specific ones we saw in the logs if they are consistent.
    #    Saw: "按需?检索内容方式<用例<复用" -> likely "按需求检索内容方式/用例复用"
    #    Saw: "请选择需?" -> "请选择需求"
    #    Saw: "请至少添加一个参?" -> "请至少添加一个参数"
    #    Saw: "请至少添加一个因?" -> "请至少添加一个因素"
    #    Saw: "有效?" -> "有效性" or "有效"
    #    Saw: "正交?" -> "正交表"
    
    replacements = [
        # Fix ternary operators (captures: 1=condition, 2=trueVal, 3=falseVal)
        # Note: the corrupted char often looks like ' or a space in the text file representation we saw.
        # "Array.isArray(props.modelValue) 'props.modelValue : [props.modelValue]"
        (r"(Array\.isArray\([^\)]+\))\s*'([^\s:]+)\s*:\s*(\[)", r"\1 ? \2 : \3"),
        
        # General ternary fix: assume " ' " between tokens followed by :
        # Be careful not to match actual strings.
        # matching: word/parens space 'quote-like' space word/parens space :
        # (r"(\w+\.?\w+|\]|\))\s+'\s*(\w+|'[^']+'|\"[^\"]+\")\s*:", r"\1 ? \2 :"),
        
        # Specific known corruptions
        (r"按需\ufffd\?检索内容方式<用例<复用", "按需求检索内容方式/用例复用"), 
        (r"按用\ufffd\?检索内容方式<用例<复用", "按用例检索内容方式/用例复用"),
        (r"手动输入<用例<复用", "手动输入/用例复用"),
        (r"请选择需\ufffd", "请选择需求"),
        (r"请选择用\ufffd", "请选择用例"),
        (r"请至少添加一个参\ufffd", "请至少添加一个参数"),
        (r"请至少添加一个因\ufffd", "请至少添加一个因素"),
        (r"生成正交\ufffd\?", "生成正交表"),
        (r"生成等价类\ufffd", "生成等价类表"),
        (r"有效\ufffd\?", "有效性"), # or just 有效
        (r"无效\ufffd\?", "无效"),
        (r"导出正交\ufffd\?", "导出正交表"),
        (r"导出等价类\ufffd", "导出等价类表"),
        (r"类\ufffd\?'", "类型'"), # Example: 'orthogonalType' label maybe?
        
        # The specific pattern in FileUpload.vue
        # PageElementList corrections
        (r"输入.\(Input\)\"", '输入框(Input)"'),
        (r"下拉.\(Select\)\"", '下拉框(Select)"'),
        
        # FlowDocument / DataDocument corrections
        # 需? -> 需求' (Context: message: '請选择需?)
        (r"请选择需[^\n,]+,", "请选择需求',"),
        (r"请至少选择一个用[^\n\)]+\)\)", "请至少选择一个用例'))"),
        
        # General ternary fixes for weird chars
        # condition ' true : false 
        # Match: word/paren space char space word/paren space :
        (r"(\)|\]|\w)\s+[\ufffd\?']\s+(\w+|'[^']+'|\[[^\]]+\])\s*:", r"\1 ? \2 :"),
        # condition 'value
        (r"(\)|\]|\w)\s+[\ufffd\?'](\w+|'[^']+'|\[[^\]]+\])", r"\1 ? \2"), 

        # FileUpload specific check if not fixed
        (r"Array\.isArray\(props\.modelValue\)\s+[\ufffd\?']props\.modelValue", "Array.isArray(props.modelValue) ? props.modelValue"),

        # TestCaseList
        (r"label=\s*\"([^\"]+)\"\s+value=\s*([A-Z_]+)(?!\")", r'label="\1" value="\2"'),

        # Fix: activeCollapse.value = [completeness, standardization, executability, suggestions]
        (r"activeCollapse\.value\s*=\s*\[completeness, standardization, executability, suggestions\]", 
         "activeCollapse.value = ['completeness', 'standardization', 'executability', 'suggestions']"),
  
        (r"RUNNING:\s*'执行中,", "RUNNING: '执行中',"),
        (r"taskType:\s*\n", "taskType: '',\n"),
        # New pattern: condition '' true : false (seen in SpecificationCheck.vue)
        (r"(\)|\]|\w)\s+''\s+(\w+|'[^']+'|\[[^\]]+\])\s*:", r"\1 ? \2 :"),
        # Also fix corrupted title in SpecificationCheck
        (r"规约检\ufffd\?管理", "规约检查管理"),
        (r"不符\ufffd'", "不符合'"),
        
        # TestCoverageAnalysis and others with \ufffd
        (r"覆盖率\ufffd\"", '覆盖率"'),
        (r"项数\ufffd\"", '项数"'),
        (r"项\ufffd\"", '项"'),
        (r"<\/p>\ufffd<\/p>", "</p>"),
        (r"message:\s*'([^']+)\ufffd\?,", "message: '\\1',"),
        (r"message:\s*'([^']+)\?", "message: '\\1'"), # For cases without comma
        
        # Generic cleanup of \ufffd? if safe
        (r"\ufffd\?", ""), 
     ]


    count = 0
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if not filename.endswith(('.vue', '.ts', '.js')):
                continue

            full_path = os.path.join(dirpath, filename)
            
            try:
                # Try UTF-8 with replacement
                with open(full_path, 'r', encoding='utf-8', errors='replace') as f:
                    content = f.read()
            except Exception as e:
                print(f"Error reading {full_path}: {e}")
                continue

            original_content = content
            
            # Apply replacements
            for pattern, replacement in replacements:
                if isinstance(pattern, str):
                    content = content.replace(pattern, replacement)
                else: # regex
                    content = re.sub(pattern, replacement, content)
            
            # Special handling for " ' " ternary which might vary
            # Look for " ) ' " which is a common ternary start in these files
            if ") '" in content:
                 content = content.replace(") '", ") ?")
            
            if content != original_content:
                with open(full_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Fixed: {full_path}")
                count += 1

    print(f"Batch repair complete. Modified {count} files.")

if __name__ == "__main__":
    fix_corruption()
