"""
流程文档导出服务
支持将Mermaid代码导出为PNG、SVG、PDF等格式
"""
import logging
import os
import tempfile
import subprocess
import shutil
import time
from typing import Optional, Dict, Any
from pathlib import Path

logger = logging.getLogger(__name__)


class FlowDocumentExportService:
    """流程文档导出服务"""
    
    def __init__(self):
        self.temp_dir = Path(tempfile.gettempdir()) / "mermaid_exports"
        self.temp_dir.mkdir(parents=True, exist_ok=True)
        self._check_mermaid_cli()
    
    def _check_mermaid_cli(self) -> bool:
        """检查Mermaid CLI是否可用"""
        try:
            result = subprocess.run(
                ["mmdc", "--version"],
                capture_output=True,
                text=True,
                timeout=5
            )
            if result.returncode == 0:
                logger.info("Mermaid CLI已安装")
                return True
            else:
                logger.warning("Mermaid CLI未正确安装")
                return False
        except FileNotFoundError:
            logger.warning("Mermaid CLI未安装，将使用在线服务或前端渲染")
            return False
        except Exception as e:
            logger.warning(f"检查Mermaid CLI时出错: {str(e)}")
            return False
    
    def export_mermaid(
        self,
        mermaid_code: str,
        format: str = "png",
        filename: Optional[str] = None,
        width: int = 1920,
        height: int = 1080
    ) -> Dict[str, Any]:
        """
        导出Mermaid图表为文件
        
        Args:
            mermaid_code: Mermaid代码
            format: 导出格式 (png, svg, pdf)
            filename: 文件名（可选）
            width: 图片宽度（像素）
            height: 图片高度（像素）
        
        Returns:
            包含文件路径、URL等信息的字典
        """
        logger.info(f"开始导出Mermaid图表，格式: {format}, 文件名: {filename}")
        
        # 验证格式
        supported_formats = ["png", "svg", "pdf"]
        if format.lower() not in supported_formats:
            raise ValueError(f"不支持的格式: {format}，支持的格式: {supported_formats}")
        
        # 生成文件名
        if not filename:
            filename = f"mermaid_export_{int(time.time())}.{format.lower()}"
        
        # 确保文件名有正确的扩展名
        if not filename.endswith(f".{format}"):
            filename = f"{filename}.{format}"
        
        # 创建临时Mermaid文件
        mermaid_file = self.temp_dir / f"temp_{int(time.time())}.mmd"
        output_file = self.temp_dir / filename
        
        try:
            # 写入Mermaid代码到临时文件
            with open(mermaid_file, "w", encoding="utf-8") as f:
                f.write(mermaid_code)
            
            logger.info(f"Mermaid代码已写入临时文件: {mermaid_file}")
            
            # 尝试使用Mermaid CLI导出
            if self._check_mermaid_cli():
                return self._export_with_cli(mermaid_file, output_file, format, width, height)
            else:
                # 如果CLI不可用，返回Mermaid代码和在线渲染URL
                return self._export_with_online_service(mermaid_code, filename, format)
        
        except Exception as e:
            logger.error(f"导出Mermaid图表失败: {str(e)}", exc_info=True)
            raise RuntimeError(f"导出失败: {str(e)}")
        
        finally:
            # 清理临时Mermaid文件
            if mermaid_file.exists():
                try:
                    mermaid_file.unlink()
                except Exception as e:
                    logger.warning(f"删除临时文件失败: {mermaid_file}, 错误: {str(e)}")
    
    def _export_with_cli(
        self,
        mermaid_file: Path,
        output_file: Path,
        format: str,
        width: int,
        height: int
    ) -> Dict[str, Any]:
        """使用Mermaid CLI导出"""
        try:
            # 构建mmdc命令
            cmd = [
                "mmdc",
                "-i", str(mermaid_file),
                "-o", str(output_file),
                "-w", str(width),
                "-H", str(height),
                "-b", "transparent"  # 透明背景
            ]
            
            # 如果是PDF，需要额外参数
            if format == "pdf":
                cmd.extend(["-f", "pdf"])
            
            logger.info(f"执行Mermaid CLI命令: {' '.join(cmd)}")
            
            # 执行命令
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=30
            )
            
            if result.returncode != 0:
                error_msg = result.stderr or result.stdout
                logger.error(f"Mermaid CLI执行失败: {error_msg}")
                raise RuntimeError(f"Mermaid CLI执行失败: {error_msg}")
            
            if not output_file.exists():
                raise RuntimeError("导出文件未生成")
            
            file_size = output_file.stat().st_size
            logger.info(f"Mermaid图表导出成功: {output_file}, 大小: {file_size}字节")
            
            return {
                "status": "success",
                "file_path": str(output_file),
                "file_name": output_file.name,
                "file_size": file_size,
                "format": format,
                "url": f"/api/v1/flow-documents/files/{output_file.name}"
            }
        
        except subprocess.TimeoutExpired:
            logger.error("Mermaid CLI执行超时")
            raise RuntimeError("导出超时，请稍后重试")
        except Exception as e:
            logger.error(f"使用Mermaid CLI导出失败: {str(e)}", exc_info=True)
            raise
    
    def _export_with_online_service(
        self,
        mermaid_code: str,
        filename: str,
        format: str
    ) -> Dict[str, Any]:
        """使用在线服务或返回Mermaid代码"""
        import base64
        import urllib.parse
        
        # 将Mermaid代码编码为base64
        encoded_code = base64.b64encode(mermaid_code.encode("utf-8")).decode("utf-8")
        
        # 使用mermaid.ink在线服务（如果可用）
        # 注意：这是一个公共服务，可能不适合生产环境
        online_url = f"https://mermaid.ink/img/{encoded_code}"
        
        logger.warning("Mermaid CLI未安装，返回在线渲染URL。建议安装Mermaid CLI以获得更好的性能。")
        
        return {
            "status": "success",
            "file_name": filename,
            "format": format,
            "online_url": online_url,
            "mermaid_code": mermaid_code,
            "message": "Mermaid CLI未安装，返回在线渲染URL。建议安装Mermaid CLI以获得更好的性能。"
        }
    
    def get_file(self, filename: str) -> Optional[Path]:
        """获取导出的文件路径"""
        file_path = self.temp_dir / filename
        if file_path.exists():
            return file_path
        return None
    
    def cleanup_old_files(self, max_age_hours: int = 24):
        """清理旧文件"""
        import time
        current_time = time.time()
        max_age_seconds = max_age_hours * 3600
        
        cleaned_count = 0
        for file_path in self.temp_dir.iterdir():
            if file_path.is_file():
                file_age = current_time - file_path.stat().st_mtime
                if file_age > max_age_seconds:
                    try:
                        file_path.unlink()
                        cleaned_count += 1
                    except Exception as e:
                        logger.warning(f"删除旧文件失败: {file_path}, 错误: {str(e)}")
        
        if cleaned_count > 0:
            logger.info(f"清理了 {cleaned_count} 个旧文件")
