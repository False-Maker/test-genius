"""
工具注册机制
支持工具的动态加载、发现和管理
"""
import importlib
import logging
from typing import Dict, List, Optional, Type
from pathlib import Path
from app.services.agent_engine import BaseTool

logger = logging.getLogger(__name__)


class ToolRegistry:
    """工具注册表"""
    
    def __init__(self):
        """初始化工具注册表"""
        self._tools: Dict[str, BaseTool] = {}
        self._tool_classes: Dict[str, Type[BaseTool]] = {}
        self._tool_implementations: Dict[str, str] = {}
    
    def register(self, tool: BaseTool):
        """
        注册工具实例
        
        Args:
            tool: 工具实例
        """
        if tool.name in self._tools:
            logger.warning(f"工具 {tool.name} 已存在，将被覆盖")
        
        self._tools[tool.name] = tool
        logger.info(f"注册工具: {tool.name}")
    
    def register_class(self, tool_class: Type[BaseTool], implementation_path: str = ""):
        """
        注册工具类（延迟实例化）
        
        Args:
            tool_class: 工具类
            implementation_path: 实现路径（用于动态加载）
        """
        if tool_class.__name__ in self._tool_classes:
            logger.warning(f"工具类 {tool_class.__name__} 已存在，将被覆盖")
        
        self._tool_classes[tool_class.__name__] = tool_class
        if implementation_path:
            self._tool_implementations[tool_class.__name__] = implementation_path
        
        logger.info(f"注册工具类: {tool_class.__name__}")
    
    def get(self, tool_name: str) -> Optional[BaseTool]:
        """
        获取工具实例
        
        Args:
            tool_name: 工具名称
            
        Returns:
            工具实例，如果不存在返回None
        """
        return self._tools.get(tool_name)
    
    def get_class(self, class_name: str) -> Optional[Type[BaseTool]]:
        """
        获取工具类
        
        Args:
            class_name: 工具类名
            
        Returns:
            工具类，如果不存在返回None
        """
        return self._tool_classes.get(class_name)
    
    def create_instance(self, class_name: str, **kwargs) -> Optional[BaseTool]:
        """
        创建工具实例
        
        Args:
            class_name: 工具类名
            **kwargs: 构造参数
            
        Returns:
            工具实例
        """
        tool_class = self._tool_classes.get(class_name)
        if not tool_class:
            logger.error(f"工具类 {class_name} 不存在")
            return None
        
        try:
            return tool_class(**kwargs)
        except Exception as e:
            logger.error(f"创建工具实例失败: {class_name}, 错误: {str(e)}", exc_info=True)
            return None
    
    def get_all(self) -> Dict[str, BaseTool]:
        """
        获取所有已注册的工具实例
        
        Returns:
            工具字典（名称 -> 实例）
        """
        return self._tools.copy()
    
    def get_all_classes(self) -> Dict[str, Type[BaseTool]]:
        """
        获取所有已注册的工具类
        
        Returns:
            工具类字典（名称 -> 类）
        """
        return self._tool_classes.copy()
    
    def get_schema(self, tool_name: str) -> Optional[Dict]:
        """
        获取工具的schema
        
        Args:
            tool_name: 工具名称
            
        Returns:
            工具schema
        """
        tool = self.get(tool_name)
        if tool:
            return tool.get_schema()
        return None
    
    def list_tools(self) -> List[Dict]:
        """
        列出所有工具的信息
        
        Returns:
            工具信息列表
        """
        tools_info = []
        for name, tool in self._tools.items():
            schema = tool.get_schema()
            tools_info.append({
                "name": schema.get("name", name),
                "description": schema.get("description", ""),
                "parameters": schema.get("parameters", {})
            })
        
        # 添加未实例化的工具类
        for name, tool_class in self._tool_classes.items():
            if name not in self._tools:
                # 创建临时实例获取schema
                try:
                    temp_tool = tool_class()
                    schema = temp_tool.get_schema()
                    tools_info.append({
                        "name": schema.get("name", name),
                        "description": schema.get("description", ""),
                        "parameters": schema.get("parameters", {}),
                        "class_only": True
                    })
                except:
                    pass
        
        return tools_info
    
    def unregister(self, tool_name: str):
        """
        注销工具
        
        Args:
            tool_name: 工具名称
        """
        if tool_name in self._tools:
            del self._tools[tool_name]
            logger.info(f"注销工具: {tool_name}")
        
        # 同时检查类注册
        for class_name in list(self._tool_classes.keys()):
            # 清理已实例化的工具类
            if tool_name == class_name.lower():
                del self._tool_classes[class_name]
                if class_name in self._tool_implementations:
                    del self._tool_implementations[class_name]
                logger.info(f"注销工具类: {class_name}")
    
    def load_from_module(self, module_path: str, class_name: str = None) -> bool:
        """
        从模块加载工具类
        
        Args:
            module_path: 模块路径（如 "app.services.agent_tools.test_tools"）
            class_name: 类名（如果不指定，则加载模块中所有BaseTool子类）
            
        Returns:
            是否加载成功
        """
        try:
            module = importlib.import_module(module_path)
            
            if class_name:
                # 加载指定类
                tool_class = getattr(module, class_name)
                if isinstance(tool_class, type) and issubclass(tool_class, BaseTool):
                    self.register_class(tool_class, module_path)
                    return True
            else:
                # 加载模块中所有工具类
                loaded = 0
                for attr_name in dir(module):
                    attr = getattr(module, attr_name)
                    if isinstance(attr, type) and issubclass(attr, BaseTool) and attr != BaseTool:
                        self.register_class(attr, f"{module_path}.{attr_name}")
                        loaded += 1
                
                logger.info(f"从模块 {module_path} 加载了 {loaded} 个工具类")
                return loaded > 0
        
        except Exception as e:
            logger.error(f"从模块加载工具失败: {module_path}, 错误: {str(e)}", exc_info=True)
            return False
    
    def load_from_directory(self, directory_path: str, pattern: str = "*.py") -> int:
        """
        从目录加载所有工具模块
        
        Args:
            directory_path: 目录路径
            pattern: 文件匹配模式
            
        Returns:
            加载的工具类数量
        """
        directory = Path(directory_path)
        if not directory.exists():
            logger.error(f"目录不存在: {directory_path}")
            return 0
        
        loaded = 0
        for file_path in directory.glob(pattern):
            if file_path.name.startswith('_') or file_path.name == '__init__.py':
                continue
            
            # 将文件路径转换为模块路径
            module_name = file_path.stem
            parent_dirs = file_path.parent.parts
            # 假设在 app.services.agent_tools 下
            if 'agent_tools' in parent_dirs:
                module_path = f"app.services.agent_tools.{module_name}"
                if self.load_from_module(module_path):
                    loaded += 1
        
        logger.info(f"从目录 {directory_path} 加载了 {loaded} 个工具模块")
        return loaded


# 全局工具注册表实例
_global_registry = None


def get_registry() -> ToolRegistry:
    """
    获取全局工具注册表实例（单例模式）
    
    Returns:
        工具注册表实例
    """
    global _global_registry
    if _global_registry is None:
        _global_registry = ToolRegistry()
        # 自动加载测试工具
        _global_registry.load_from_directory(
            "backend-python/ai-service/app/services/agent_tools",
            "*.py"
        )
    return _global_registry


def register_tool(tool: BaseTool):
    """
    便捷函数：注册工具到全局注册表
    
    Args:
        tool: 工具实例
    """
    registry = get_registry()
    registry.register(tool)


def register_tool_class(tool_class: Type[BaseTool], implementation_path: str = ""):
    """
    便捷函数：注册工具类到全局注册表
    
    Args:
        tool_class: 工具类
        implementation_path: 实现路径
    """
    registry = get_registry()
    registry.register_class(tool_class, implementation_path)

