
import os
import re

def parse_lint_output():
    file_path = 'lint_output_final_final.txt'
    output_path = 'errors_list.txt'
    
    if not os.path.exists(file_path):
        print("lint_output.txt not found")
        return

    lines = []
    try:
        with open(file_path, 'r', encoding='utf-16') as f:
            lines = f.readlines()
    except UnicodeError:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                lines = f.readlines()
        except Exception as e:
            print(f"Error reading file: {e}")
            return

    current_file = "Unknown File"
    errors = []
    
    # Check if a line is a file path (simplified)
    # ESLint output usually prints the file path, then errors.
    # Paths often start with d:\ or / or src/
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # Heuristic for filename: contains / or \ and (usually) ends with extension or is a path
        # But ESLint just prints the path. 
        # Error lines start with "  Line:Col" or "  Line  error"
        # Standard ESLint format:
        # /path/to/file.js
        #   1:1  error  Message
        
        is_error_line = False
        parts = line.split()
        if len(parts) > 2 and parts[1] == 'error':
             is_error_line = True
        elif 'Parsing error' in line or 'error  Parsing error' in line:
             is_error_line = True
        
        if is_error_line:
            errors.append(f"File: {current_file}")
            errors.append(f"  {line}")
        elif (line.startswith('d:\\') or line.startswith('/') or 'test-genius' in line) and not line.startswith('File:'):
             # Likely a filename
             current_file = line
        
        if 'problems' in line and 'errors' in line:
             errors.append(f"\nSUMMARY: {line}")

    with open(output_path, 'w', encoding='utf-8') as f:
        # Deduplicate consecutive file headers
        last_file = ""
        for err in errors:
            if err.startswith("File:"):
                if err == last_file:
                    continue
                last_file = err
            f.write(err + "\n")
    
    print(f"Errors extracted to {output_path}")

if __name__ == "__main__":
    parse_lint_output()
