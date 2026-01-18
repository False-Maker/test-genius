#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Windows OCR 图片文字识别脚本
使用 Windows 自带的 OCR 功能识别 image 目录下的所有图片
"""

import os
import sys
import json
from pathlib import Path
from typing import List, Dict
from datetime import datetime

try:
    import asyncio
    from winrt.windows.media.ocr import OcrEngine
    from winrt.windows.graphics.imaging import BitmapDecoder, SoftwareBitmap
    from winrt.windows.storage.streams import InMemoryRandomAccessStream, DataReader, InputStreamOptions
    from winrt.windows.storage import FileAccessMode
    from winrt.windows.foundation import IAsyncOperation
    WINRT_AVAILABLE = True
except ImportError:
    WINRT_AVAILABLE = False
    print("警告: winrt 包未安装，请运行: pip install winrt")
    print("如果安装失败，可能需要先安装: pip install windows-runtime")


class WindowsOCRReader:
    """Windows OCR 图片识别器"""
    
    def __init__(self):
        """初始化 OCR 引擎"""
        if not WINRT_AVAILABLE:
            raise ImportError("winrt 包未安装，无法使用 Windows OCR 功能")
        
        # 获取系统支持的语言
        self.ocr_engine = OcrEngine.try_create_from_user_profile_languages()
        self.supported_languages = [lang.display_name for lang in self.ocr_engine.recognizer_languages]
        print(f"OCR 引擎初始化成功，支持的语言: {', '.join(self.supported_languages)}")
    
    def read_image_file(self, image_path: str) -> bytes:
        """
        读取图片文件为字节流
        
        Args:
            image_path: 图片文件路径
            
        Returns:
            图片文件的字节数据
        """
        with open(image_path, 'rb') as f:
            return f.read()
    
    async def recognize_image_async(self, image_path: str) -> Dict:
        """
        异步识别图片中的文字
        
        Args:
            image_path: 图片文件路径
            
        Returns:
            识别结果字典，包含：
            - file_path: 文件路径
            - file_name: 文件名
            - text: 识别的文字内容
            - words: 文字列表（包含位置信息）
            - word_count: 文字数量
            - lines: 行列表
            - line_count: 行数
        """
        try:
            # 读取图片文件
            image_bytes = self.read_image_file(image_path)
            
            # 创建内存流
            stream = InMemoryRandomAccessStream()
            writer = stream.get_output_stream_at(0)
            
            # 写入图片数据
            data_writer = DataWriter(writer)
            data_writer.write_bytes(bytearray(image_bytes))
            await data_writer.store_async()
            await writer.flush_async()
            
            # 重置流位置
            stream.seek(0)
            
            # 创建位图解码器
            decoder = await BitmapDecoder.create_async(stream)
            
            # 获取软件位图
            bitmap = await decoder.get_software_bitmap_async()
            
            # OCR 识别
            result = await self.ocr_engine.recognize_async(bitmap)
            
            # 提取文字
            all_text = ""
            words_list = []
            lines_list = []
            
            if result.text:
                all_text = result.text
                print(f"  ✓ 识别到 {len(result.lines)} 行文字")
            else:
                print(f"  ⚠ 未识别到文字")
            
            # 提取行信息
            for line in result.lines:
                line_text = ""
                line_words = []
                
                for word in line.words:
                    word_text = word.text
                    line_text += word_text + " "
                    
                    words_list.append({
                        "text": word_text,
                        "bounding_rect": {
                            "x": word.bounding_rect.x,
                            "y": word.bounding_rect.y,
                            "width": word.bounding_rect.width,
                            "height": word.bounding_rect.height
                        }
                    })
                    
                    line_words.append({
                        "text": word_text,
                        "bounding_rect": {
                            "x": word.bounding_rect.x,
                            "y": word.bounding_rect.y,
                            "width": word.bounding_rect.width,
                            "height": word.bounding_rect.height
                        }
                    })
                
                lines_list.append({
                    "text": line_text.strip(),
                    "words": line_words,
                    "bounding_rect": {
                        "x": line.bounding_rect.x,
                        "y": line.bounding_rect.y,
                        "width": line.bounding_rect.width,
                        "height": line.bounding_rect.height
                    }
                })
            
            return {
                "file_path": image_path,
                "file_name": Path(image_path).name,
                "text": all_text.strip(),
                "words": words_list,
                "word_count": len(words_list),
                "lines": lines_list,
                "line_count": len(lines_list),
                "recognized_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            print(f"  ✗ 识别失败: {str(e)}")
            return {
                "file_path": image_path,
                "file_name": Path(image_path).name,
                "text": "",
                "words": [],
                "word_count": 0,
                "lines": [],
                "line_count": 0,
                "error": str(e),
                "recognized_at": datetime.now().isoformat()
            }
    
    def recognize_image(self, image_path: str) -> Dict:
        """
        同步识别图片中的文字（内部使用异步方法）
        
        Args:
            image_path: 图片文件路径
            
        Returns:
            识别结果字典
        """
        return asyncio.run(self.recognize_image_async(image_path))
    
    def recognize_all_images(self, image_dir: str, output_file: str = None) -> List[Dict]:
        """
        识别目录下的所有图片
        
        Args:
            image_dir: 图片目录路径
            output_file: 输出文件路径（可选，JSON格式）
            
        Returns:
            所有图片的识别结果列表
        """
        image_dir_path = Path(image_dir)
        if not image_dir_path.exists():
            raise FileNotFoundError(f"目录不存在: {image_dir}")
        
        # 支持的图片格式
        image_extensions = {'.png', '.jpg', '.jpeg', '.bmp', '.tiff', '.gif'}
        
        # 获取所有图片文件
        image_files = []
        for ext in image_extensions:
            image_files.extend(image_dir_path.glob(f'*{ext}'))
            image_files.extend(image_dir_path.glob(f'*{ext.upper()}'))
        
        # 按文件名排序
        image_files = sorted(image_files, key=lambda x: int(x.stem) if x.stem.isdigit() else float('inf'))
        
        print(f"找到 {len(image_files)} 个图片文件")
        print("=" * 60)
        
        # 识别所有图片
        results = []
        for i, image_file in enumerate(image_files, 1):
            print(f"[{i}/{len(image_files)}] 正在识别: {image_file.name}")
            result = self.recognize_image(str(image_file))
            results.append(result)
            if result.get("text"):
                # 显示前100个字符
                preview = result["text"][:100].replace("\n", " ")
                print(f"  预览: {preview}...")
            print()
        
        # 保存结果到文件
        if output_file:
            output_path = Path(output_file)
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=2)
            print(f"识别结果已保存到: {output_path}")
        
        # 统计信息
        total_words = sum(r["word_count"] for r in results)
        total_lines = sum(r["line_count"] for r in results)
        success_count = sum(1 for r in results if r.get("text") and not r.get("error"))
        
        print("=" * 60)
        print(f"识别完成:")
        print(f"  - 总图片数: {len(results)}")
        print(f"  - 成功识别: {success_count}")
        print(f"  - 总文字数: {total_words}")
        print(f"  - 总行数: {total_lines}")
        
        return results


def main():
    """主函数"""
    # 获取脚本所在目录的父目录（项目根目录）
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    image_dir = project_root / "image"
    output_file = project_root / "ocr_results.json"
    
    print("Windows OCR 图片识别工具")
    print("=" * 60)
    
    if not WINRT_AVAILABLE:
        print("错误: winrt 包未安装")
        print("\n安装方法:")
        print("  pip install winrt")
        print("\n如果安装失败，请尝试:")
        print("  pip install windows-runtime")
        sys.exit(1)
    
    try:
        # 创建 OCR 读取器
        reader = WindowsOCRReader()
        
        # 识别所有图片
        results = reader.recognize_all_images(str(image_dir), str(output_file))
        
        # 生成文本报告
        report_file = project_root / "ocr_results.txt"
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("Windows OCR 识别结果报告\n")
            f.write("=" * 60 + "\n")
            f.write(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
            f.write("=" * 60 + "\n\n")
            
            for result in results:
                f.write(f"文件: {result['file_name']}\n")
                f.write("-" * 60 + "\n")
                if result.get("error"):
                    f.write(f"错误: {result['error']}\n\n")
                elif result.get("text"):
                    f.write(f"文字内容 ({result['word_count']} 个字，{result['line_count']} 行):\n")
                    f.write(result['text'])
                    f.write("\n\n")
                else:
                    f.write("未识别到文字\n\n")
                f.write("\n")
        
        print(f"\n文本报告已保存到: {report_file}")
        
    except Exception as e:
        print(f"错误: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()

