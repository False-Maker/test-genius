# 阶段3: Python后端空实现修复计划

> **计划类型**: 功能实现
> **预计工时**: 2-3周
> **依赖**: 阶段1完成（P0问题已修复）
> **优先级**: P1 - 高优先级

---

## TL;DR

> **目标**: 实现Python后端所有空占位符服务，使AI功能完整可用
>
> **需要实现的服务**:
> 1. **Agent引擎** - AI对话和工作流执行引擎
> 2. **工具管理器** - Agent工具注册和执行
> 3. **文档解析服务** - Word/PDF文档内容提取
> 4. **工作流引擎** - 可视化工作流执行
> 5. **页面解析服务** - UI元素识别和提取
> 6. **模型适配器** - 多LLM统一接口
>
> **预计成果**: Python AI服务功能完整，可用于生产

---

## Context

### 问题来源

Python后端有11个文件存在 `pass` 占位符，这些是核心AI服务：

| 文件 | 功能 | 状态 |
|------|------|------|
| agent_engine.py | Agent对话引擎 | 空实现 |
| agent_tool_manager.py | 工具管理器 | 空实现 |
| agent_tool_registry.py | 工具注册表 | 空实现 |
| document_parser_service.py | 文档解析 | 空实现 |
| page_parser_service.py | 页面解析 | 空实现 |
| workflow_engine.py | 工作流引擎 | 空实现 |
| workflow_nodes/base_node.py | 工作流节点基类 | 空实现 |
| model_adapter.py | 模型适配器 | 空实现 |
| requirement_router.py | 需求分析路由 | 部分空实现 |
| main.py | 代理异常处理 | pass |

### 技术背景

- **框架**: FastAPI + LangChain 0.3.x
- **LLM**: 支持DeepSeek、OpenAI、Kimi、千问、豆包
- **数据库**: PostgreSQL + SQLAlchemy
- **文档处理**: python-docx, PyPDF2

---

## Work Objectives

### Core Objective

实现所有空占位符的Python服务，使AI能力（Agent、工作流、文档解析）完整可用。

### Concrete Deliverables

1. **Agent系统** (3个文件):
   - `agent_engine.py` - 对话引擎，支持多轮对话、工具调用
   - `agent_tool_manager.py` - 工具管理，支持动态注册和执行
   - `agent_tool_registry.py` - 工具注册表，预置常用工具

2. **文档处理** (2个文件):
   - `document_parser_service.py` - Word/PDF解析
   - `page_parser_service.py` - UI页面元素提取

3. **工作流系统** (2个文件):
   - `workflow_engine.py` - 工作流执行引擎
   - `workflow_nodes/base_node.py` - 节点基类

4. **模型适配** (1个文件):
   - `model_adapter.py` - 多LLM统一适配器

### Definition of Done

- [ ] 所有服务文件无 `pass` 占位符
- [ ] 所有公共方法都有实现
- [ ] 服务能正常导入和初始化
- [ ] API端点能正确调用服务

### Must Have

- 必须兼容LangChain 0.3.x
- 必须支持现有的API接口签名
- 必须正确处理错误和异常

### Must NOT Have

- 不能修改已有的API路由接口
- 不能改变数据库表结构
- 不能依赖外部付费API（除LLM本身）

---

## Verification Strategy

### Test Decision

- **Infrastructure exists**: YES (pytest)
- **Automated tests**: Tests-after (每个服务实现后添加单元测试)
- **Framework**: pytest + pytest-asyncio

### QA Policy

每个服务实现后：
1. 导入测试：`python -c "from app.services.xxx import XXX"`
2. 初始化测试：创建实例无错误
3. 基本功能测试：调用公共方法不崩溃

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (模型适配 - 基础):
├── Task 1: 实现ModelAdapterFactory [unspecified-high]

Wave 2 (Agent系统 - 并行):
├── Task 2: 实现AgentToolRegistry工具注册表 [unspecified-high]
├── Task 3: 实现AgentToolManager工具管理器 [unspecified-high]
└── Task 4: 实现AgentEngine对话引擎 [unspecified-high]

Wave 3 (文档处理 - 并行):
├── Task 5: 实现DocumentParserService [unspecified-high]
└── Task 6: 实现PageParserService [unspecified-high]

Wave 4 (工作流系统):
├── Task 7: 实现工作流节点基类 [unspecified-high]
└── Task 8: 实现WorkflowEngine工作流引擎 [unspecified-high]

Wave 5 (集成测试):
├── Task 9: Agent系统集成测试 [quick]
├── Task 10: 工作流系统集成测试 [quick]
└── Task 11: 文档处理集成测试 [quick]

Critical Path: Task 1 → Task 4 → Task 8
Parallel Speedup: ~60%
Max Concurrent: 3
```

### Dependency Matrix

- **1**: — — 2, 5, 7
- **2**: — — 3
- **3**: 2 — 4
- **4**: 1, 3 — 9
- **5**: 1 — 11
- **6**: 1 — 11
- **7**: — — 8
- **8**: 1, 7 — 10
- **9**: 4 —
- **10**: 8 —
- **11**: 5, 6 —

---

## TODOs

### Wave 1: 模型适配 (1个任务)

- [ ] 1. **实现ModelAdapterFactory**

  **What to do**:
  实现 `backend-python/ai-service/app/utils/model_adapter.py`
  
  **1.1 定义LLM适配器接口**:
  ```python
  from typing import Protocol, Dict, Any, Optional
  from langchain_core.language_models import BaseChatModel
  
  class LLMAdapter(Protocol):
      """LLM适配器协议"""
      
      def create_llm(
          self,
          api_key: str,
          api_endpoint: str,
          model_version: str,
          max_tokens: int,
          temperature: float
      ) -> BaseChatModel:
          """创建LLM实例"""
          ...
  ```
  
  **1.2 实现各模型适配器**:
  - `DeepSeekAdapter` - DeepSeek API
  - `OpenAIAdapter` - OpenAI API
  - `KimiAdapter` - Moonshot API
  - `QianWenAdapter` - 阿里千问API
  - `DoubaoAdapter` - 字节豆包API
  
  **1.3 实现工厂类**:
  ```python
  class ModelAdapterFactory:
      """模型适配器工厂"""
      
      _adapters: Dict[str, Type[LLMAdapter]] = {
          "DEEPSEEK": DeepSeekAdapter,
          "OPENAI": OpenAIAdapter,
          "KIMI": KimiAdapter,
          "QIANWEN": QianWenAdapter,
          "DOUBAO": DoubaoAdapter,
      }
      
      @classmethod
      def create_llm(
          cls,
          model_type: str,
          api_key: str,
          api_endpoint: str = "",
          model_version: str = "",
          max_tokens: int = 2000,
          temperature: float = 0.7
      ) -> BaseChatModel:
          """创建LLM实例"""
          adapter_class = cls._adapters.get(model_type.upper())
          if not adapter_class:
              raise ValueError(f"不支持的模型类型: {model_type}")
          
          adapter = adapter_class()
          return adapter.create_llm(
              api_key=api_key,
              api_endpoint=api_endpoint,
              model_version=model_version,
              max_tokens=max_tokens,
              temperature=temperature
          )
  ```

  **Acceptance Criteria**:
  - [ ] 支持5种LLM类型
  - [ ] 返回兼容LangChain 0.3.x的实例
  - [ ] 能正确调用DeepSeek API

  **Commit**: `feat(python): implement ModelAdapterFactory for multi-LLM support`

---

### Wave 2: Agent系统 (3个任务)

- [ ] 2. **实现AgentToolRegistry工具注册表**

  **What to do**:
  实现 `backend-python/ai-service/app/services/agent_tool_registry.py`
  
  **2.1 定义工具接口**:
  ```python
  from typing import Callable, Dict, Any, Optional
  from pydantic import BaseModel
  
  class ToolParameter(BaseModel):
      """工具参数定义"""
      name: str
      type: str
      description: str
      required: bool = True
      default: Any = None
  
  class AgentTool:
      """Agent工具基类"""
      
      name: str = ""
      description: str = ""
      parameters: list[ToolParameter] = []
      
      def execute(self, **kwargs) -> Dict[str, Any]:
          """执行工具"""
          raise NotImplementedError
  ```
  
  **2.2 实现工具注册表**:
  ```python
  class AgentToolRegistry:
      """Agent工具注册表"""
      
      _tools: Dict[str, Type[AgentTool]] = {}
      
      @classmethod
      def register(cls, tool_class: Type[AgentTool]):
          """注册工具"""
          tool = tool_class()
          cls._tools[tool.name] = tool_class
      
      @classmethod
      def get_tool(cls, name: str) -> Optional[AgentTool]:
          """获取工具实例"""
          tool_class = cls._tools.get(name)
          if tool_class:
              return tool_class()
          return None
      
      @classmethod
      def list_tools(cls) -> Dict[str, Dict[str, Any]]:
          """列出所有工具"""
          return {
              name: {
                  "description": tool.description,
                  "parameters": [p.dict() for p in tool.parameters]
              }
              for name, tool_class in cls._tools.items()
          }
  ```
  
  **2.3 预置工具**:
  - `KnowledgeQueryTool` - 知识库查询
  - `TestCaseGenerateTool` - 测试用例生成
  - `RequirementAnalysisTool` - 需求分析
  - `DocumentSearchTool` - 文档搜索

  **Acceptance Criteria**:
  - [ ] 支持4种预置工具
  - [ ] 工具可动态注册和获取

  **Commit**: `feat(python): implement AgentToolRegistry with preset tools`

---

- [ ] 3. **实现AgentToolManager工具管理器**

  **What to do**:
  实现 `backend-python/ai-service/app/services/agent_tool_manager.py`
  
  **3.1 工具管理器**:
  ```python
  class AgentToolManager:
      """Agent工具管理器"""
      
      def __init__(self, db: Session):
          self.db = db
          self.registry = AgentToolRegistry()
      
      async def execute_tool(
          self,
          tool_name: str,
          parameters: Dict[str, Any]
      ) -> Dict[str, Any]:
          """执行工具"""
          try:
              tool = self.registry.get_tool(tool_name)
              if not tool:
                  raise ValueError(f"工具不存在: {tool_name}")
              
              result = await tool.execute(**parameters)
              
              return {
                  "success": True,
                  "result": result
              }
          except Exception as e:
              return {
                  "success": False,
                  "error": str(e)
              }
      
      def get_available_tools(self) -> Dict[str, Dict[str, Any]]:
          """获取可用工具列表"""
          return self.registry.list_tools()
  ```

  **Acceptance Criteria**:
  - [ ] 能执行注册的工具
  - [ ] 正确处理工具执行错误

  **Commit**: `feat(python): implement AgentToolManager for tool execution`

---

- [ ] 4. **实现AgentEngine对话引擎**

  **What to do**:
  实现 `backend-python/ai-service/app/services/agent_engine.py`
  
  **4.1 Agent引擎**:
  ```python
  from typing import List, Dict, Any, Optional
  from sqlalchemy.orm import Session
  from app.services.llm_service import LLMService
  from app.services.agent_tool_manager import AgentToolManager
  
  class AgentEngine:
      """Agent对话引擎"""
      
      def __init__(self, db: Session):
          self.db = db
          self.llm_service = LLMService(db)
          self.tool_manager = AgentToolManager(db)
      
      async def run_session(
          self,
          agent_id: int,
          user_message: str,
          session_id: Optional[str] = None
      ) -> Dict[str, Any]:
          """运行Agent对话会话"""
          # 1. 获取Agent配置
          agent = self._get_agent(agent_id)
          
          # 2. 加载会话历史
          history = self._load_history(session_id) if session_id else []
          
          # 3. 构建提示词
          messages = self._build_messages(agent, history, user_message)
          
          # 4. 调用LLM
          response = await self.llm_service.call_model(
              model_code=agent.default_model,
              prompt=self._messages_to_prompt(messages)
          )
          
          # 5. 检测是否需要调用工具
          tool_calls = self._detect_tool_calls(response["content"])
          
          if tool_calls:
              # 执行工具调用
              tool_results = await self._execute_tools(tool_calls)
              
              # 再次调用LLM整合结果
              final_response = await self._integrate_tool_results(
                  messages, response["content"], tool_results
              )
          else:
              final_response = response["content"]
          
          # 6. 保存会话历史
          self._save_history(session_id, user_message, final_response)
          
          return {
              "response": final_response,
              "tool_calls": tool_calls or []
          }
  ```

  **Acceptance Criteria**:
  - [ ] 支持多轮对话
  - [ ] 支持工具调用
  - [ ] 能保存会话历史

  **Commit**: `feat(python): implement AgentEngine for multi-turn conversations`

---

### Wave 3: 文档处理 (2个任务)

- [ ] 5. **实现DocumentParserService文档解析服务**

  **What to do**:
  实现 `backend-python/ai-service/app/services/document_parser_service.py`
  
  **5.1 文档解析服务**:
  ```python
  from typing import Dict, Any, List
  from pathlib import Path
  from docx import Document as DocxDocument
  import PyPDF2
  
  class DocumentParserService:
      """文档解析服务"""
      
      def parse_word(self, file_path: str) -> Dict[str, Any]:
          """解析Word文档"""
          try:
              doc = DocxDocument(file_path)
              
              # 提取文本内容
              paragraphs = [p.text for p in doc.paragraphs if p.text.strip()]
              
              # 提取表格
              tables = []
              for table in doc.tables:
                  table_data = []
                  for row in table.rows:
                      row_data = [cell.text for cell in row.cells]
                      table_data.append(row_data)
                  tables.append(table_data)
              
              # 提取标题
              headings = [
                  p.text for p in doc.paragraphs
                  if p.style.name.startswith('Heading') and p.text.strip()
              ]
              
              return {
                  "content": "\n".join(paragraphs),
                  "paragraphs": paragraphs,
                  "tables": tables,
                  "headings": headings,
                  "metadata": {
                      "paragraph_count": len(paragraphs),
                      "table_count": len(tables),
                      "heading_count": len(headings)
                  }
              }
          except Exception as e:
              raise ValueError(f"Word文档解析失败: {str(e)}")
      
      def parse_pdf(self, file_path: str) -> Dict[str, Any]:
          """解析PDF文档"""
          try:
              with open(file_path, 'rb') as file:
                  reader = PyPDF2.PdfReader(file)
                  
                  # 提取文本
                  text_content = ""
                  for page in reader.pages:
                      text_content += page.extract_text()
                  
                  return {
                      "content": text_content,
                      "metadata": {
                          "page_count": len(reader.pages),
                          "author": reader.metadata.author if reader.metadata else "",
                          "title": reader.metadata.title if reader.metadata else ""
                      }
                  }
          except Exception as e:
              raise ValueError(f"PDF文档解析失败: {str(e)}")
      
      def parse_text(self, file_path: str) -> Dict[str, Any]:
          """解析纯文本文件"""
          try:
              with open(file_path, 'r', encoding='utf-8') as f:
                  content = f.read()
              
              lines = content.split('\n')
              
              return {
                  "content": content,
                  "lines": lines,
                  "metadata": {
                      "line_count": len(lines),
                      "char_count": len(content)
                  }
              }
          except UnicodeDecodeError:
              # 尝试其他编码
              with open(file_path, 'r', encoding='gbk') as f:
                  content = f.read()
              return {"content": content, "encoding": "gbk"}
  ```

  **Acceptance Criteria**:
  - [ ] 支持Word、PDF、纯文本解析
  - [ ] 能提取文本、表格、标题

  **Commit**: `feat(python): implement DocumentParserService for Word/PDF parsing`

---

- [ ] 6. **实现PageParserService页面解析服务**

  **What to do**:
  实现 `backend-python/ai-service/app/services/page_parser_service.py`
  
  **6.1 页面解析服务**:
  ```python
  from bs4 import BeautifulSoup
  from typing import Dict, Any, List
  from selenium import webdriver
  from selenium.webdriver.common.by import By
  from selenium.webdriver.chrome.options import Options
  
  class PageParserService:
      """页面解析服务"""
      
      def parse_ui_elements(
          self,
          html: str,
          url: str = ""
      ) -> Dict[str, Any]:
          """解析UI元素"""
          soup = BeautifulSoup(html, 'html.parser')
          
          elements = []
          
          # 提取按钮
          for btn in soup.find_all(['button', 'input[type="button"]', 'a']):
              if btn.get('type') != 'button' and btn.name != 'button' and btn.name != 'a':
                  continue
              elements.append({
                  "type": "button",
                  "text": btn.get_text(strip=True),
                  "selector": self._generate_selector(btn),
                  "id": btn.get('id'),
                  "class": btn.get('class')
              })
          
          # 提取输入框
          for inp in soup.find_all(['input', 'textarea']):
              elements.append({
                  "type": "input",
                  "input_type": inp.get('type', 'text'),
                  "name": inp.get('name'),
                  "id": inp.get('id'),
                  "placeholder": inp.get('placeholder'),
                  "selector": self._generate_selector(inp)
              })
          
          # 提取下拉框
          for sel in soup.find_all('select'):
              options = [opt.get('value') for opt in sel.find_all('option')]
              elements.append({
                  "type": "select",
                  "name": sel.get('name'),
                  "id": sel.get('id'),
                  "options": options,
                  "selector": self._generate_selector(sel)
              })
          
          return {
              "url": url,
              "elements": elements,
              "metadata": {
                  "element_count": len(elements)
              }
              }
      
      def _generate_selector(self, element) -> str:
          """生成CSS选择器"""
          if element.get('id'):
              return f"#{element['id']}"
          elif element.get('class'):
              return f".{element['class'][0]}"
          else:
              return element.name
      
      async def extract_selectors(
          self,
          url: str,
          headless: bool = True
      ) -> List[Dict[str, Any]]:
          """使用浏览器提取选择器"""
          options = Options()
          if headless:
              options.add_argument('--headless')
          
          driver = webdriver.Chrome(options=options)
          
          try:
              driver.get(url)
              
              elements = []
              
              # 查找可交互元素
              for elem in driver.find_elements(By.TAG_NAME, ['button', 'input', 'select', 'a']):
                  elements.append({
                      "tag": elem.tag_name,
                      "id": elem.get_attribute('id'),
                      "name": elem.get_attribute('name'),
                      "class": elem.get_attribute('class'),
                      "text": elem.text,
                      "xpath": self._get_xpath(elem, driver)
                  })
              
              return elements
          finally:
              driver.quit()
  ```

  **Acceptance Criteria**:
  - [ ] 支持HTML解析
  - [ ] 能提取按钮、输入框、下拉框

  **Commit**: `feat(python): implement PageParserService for UI element extraction`

---

### Wave 4: 工作流系统 (2个任务)

- [ ] 7. **实现工作流节点基类**

  **What to do**:
  实现 `backend-python/ai-service/app/services/workflow_nodes/base_node.py`
  
  **7.1 节点基类**:
  ```python
  from abc import ABC, abstractmethod
  from typing import Dict, Any, Optional
  
  class WorkflowNode(ABC):
      """工作流节点基类"""
      
      def __init__(self, node_id: str, config: Dict[str, Any]):
          self.node_id = node_id
          self.config = config
      
      @abstractmethod
      async def execute(
          self,
          context: Dict[str, Any]
      ) -> Dict[str, Any]:
          """执行节点逻辑"""
          pass
      
      def validate(self) -> bool:
          """验证节点配置"""
          return True
      
      def get_inputs(self) -> List[str]:
          """获取输入参数列表"""
          return []
      
      def get_outputs(self) -> List[str]:
          """获取输出参数列表"""
          return []
  
  # 预定义节点类型
  class InputNode(WorkflowNode):
      """输入节点基类"""
      pass
  
  class ProcessNode(WorkflowNode):
      """处理节点基类"""
      pass
  
  class OutputNode(WorkflowNode):
      """输出节点基类"""
      pass
  
  class TransformNode(WorkflowNode):
      """转换节点基类"""
      pass
  
  class ControlNode(WorkflowNode):
      """控制节点基类"""
      pass
  ```

  **Acceptance Criteria**:
  - [ ] 定义5种节点类型
  - [ ] 有统一的执行接口

  **Commit**: `feat(python): implement base classes for workflow nodes`

---

- [ ] 8. **实现WorkflowEngine工作流引擎**

  **What to do**:
  实现 `backend-python/ai-service/app/services/workflow_engine.py`
  
  **8.1 工作流引擎**:
  ```python
  from typing import Dict, Any, List
  from sqlalchemy.orm import Session
  from app.services.workflow_nodes.base_node import WorkflowNode
  from app.services.workflow_nodes import *
  
  class WorkflowEngine:
      """工作流执行引擎"""
      
      def __init__(self, db: Session):
          self.db = db
          self.node_registry = self._init_node_registry()
      
      def _init_node_registry(self) -> Dict[str, type]:
          """初始化节点注册表"""
          return {
              # 输入节点
              "REQUIREMENT_INPUT": RequirementInputNode,
              "TEST_CASE_INPUT": TestCaseInputNode,
              "FILE_UPLOAD": FileUploadNode,
              
              # 处理节点
              "REQUIREMENT_ANALYSIS": RequirementAnalysisNode,
              "TEMPLATE_SELECT": TemplateSelectNode,
              "PROMPT_GENERATE": PromptGenerateNode,
              "LLM_CALL": LLMCallNode,
              "RESULT_PARSE": ResultParseNode,
              
              # 转换节点
              "FORMAT_TRANSFORM": FormatTransformNode,
              "DATA_CLEAN": DataCleanNode,
              "DATA_MERGE": DataMergeNode,
              
              # 输出节点
              "CASE_SAVE": CaseSaveNode,
              "REPORT_GENERATE": ReportGenerateNode,
              "FILE_EXPORT": FileExportNode,
              
              # 控制节点
              "CONDITION": ConditionNode,
              "LOOP": LoopNode,
          }
      
      async def execute(
          self,
          workflow_config: Dict[str, Any],
          input_data: Dict[str, Any]
      ) -> Dict[str, Any]:
          """执行工作流"""
          nodes = workflow_config.get("nodes", [])
          edges = workflow_config.get("edges", [])
          
          # 构建执行图
          execution_graph = self._build_execution_graph(nodes, edges)
          
          # 初始化执行上下文
          context = {"input": input_data, "output": {}}
          
          # 按顺序执行节点
          executed_nodes = set()
          node_outputs = {}
          
          while len(executed_nodes) < len(nodes):
              progress_made = False
              
              for node_def in nodes:
                  if node_def["id"] in executed_nodes:
                      continue
                  
                  # 检查依赖是否满足
                  if not self._dependencies_satisfied(node_def, edges, executed_nodes):
                      continue
                  
                  # 创建并执行节点
                  node = self._create_node(node_def)
                  node_outputs[node_def["id"]] = await node.execute(context)
                  
                  executed_nodes.add(node_def["id"])
                  progress_made = True
              
              if not progress_made:
                  # 检测循环依赖
                  raise ValueError("工作流存在循环依赖或无法满足的依赖")
          
          return {
              "status": "success",
              "outputs": node_outputs
          }
      
      def validate(
          self,
          workflow_config: Dict[str, Any]
      ) -> Dict[str, Any]:
          """验证工作流配置"""
          nodes = workflow_config.get("nodes", [])
          edges = workflow_config.get("edges", [])
          
          errors = []
          warnings = []
          
          # 验证节点类型
          for node in nodes:
              node_type = node.get("type")
              if node_type not in self.node_registry:
                  errors.append(f"未知的节点类型: {node_type}")
          
          # 验证连线
          edge_set = set()
          for edge in edges:
              edge_key = f"{edge['source']}->{edge['target']}"
              if edge_key in edge_set:
                  warnings.append(f"重复的连线: {edge_key}")
              edge_set.add(edge_key)
          
          return {
              "valid": len(errors) == 0,
              "errors": errors,
              "warnings": warnings
          }
  ```

  **Acceptance Criteria**:
  - [ ] 支持顺序执行节点
  - [ ] 支持依赖检查
  - [ ] 能验证工作流配置

  **Commit**: `feat(python): implement WorkflowEngine for workflow execution`

---

### Wave 5: 集成测试 (3个任务)

- [ ] 9. **Agent系统集成测试**

  **QA Scenarios**:
  ```python
  Scenario: Agent对话流程
    Tool: Bash (pytest)
    Steps:
      1. cd backend-python/ai-service
      2. pytest tests/test_agent_integration.py -v
    Expected Result: Agent能完成多轮对话
    Evidence: .sisyphus/evidence/phase3-task9-agent-test.log
  ```

  **Commit**: NO

---

- [ ] 10. **工作流系统集成测试**

  **QA Scenarios**:
  ```python
  Scenario: 工作流执行流程
    Tool: Bash (pytest)
    Steps:
      1. cd backend-python/ai-service
      2. pytest tests/test_workflow_integration.py -v
    Expected Result: 工作流能顺序执行所有节点
    Evidence: .sisyphus/evidence/phase3-task10-workflow-test.log
  ```

  **Commit**: NO

---

- [ ] 11. **文档处理集成测试**

  **QA Scenarios**:
  ```python
  Scenario: 文档解析功能
    Tool: Bash (pytest)
    Steps:
      1. cd backend-python/ai-service
      2. pytest tests/test_document_integration.py -v
    Expected Result: 能正确解析Word和PDF
    Evidence: .sisyphus/evidence/phase3-task11-document-test.log
  ```

  **Commit**: NO

---

## Final Verification Wave

- [ ] F1. **Service Import Test** — `quick`
  测试所有服务能正常导入
- [ ] F2. **API Endpoint Test** — `quick`
  测试所有API端点能正常响应
- [ ] F3. **Integration Test** — `unspecified-high`
  端到端测试Agent、工作流、文档处理
- [ ] F4. **Performance Test** — `quick`
  测试基本性能指标

---

## Commit Strategy

- **Task 1**: `feat(python): implement ModelAdapterFactory`
- **Task 2**: `feat(python): implement AgentToolRegistry`
- **Task 3**: `feat(python): implement AgentToolManager`
- **Task 4**: `feat(python): implement AgentEngine`
- **Task 5**: `feat(python): implement DocumentParserService`
- **Task 6**: `feat(python): implement PageParserService`
- **Task 7**: `feat(python): implement workflow node base classes`
- **Task 8**: `feat(python): implement WorkflowEngine`
- **Tasks 9-11**: 无commit（测试任务）

---

## Success Criteria

### Verification Commands

```bash
# 服务导入测试
cd backend-python/ai-service
python -c "
from app.services.agent_engine import AgentEngine
from app.services.workflow_engine import WorkflowEngine
from app.services.document_parser_service import DocumentParserService
from app.services.page_parser_service import PageParserService
print('All services imported successfully')
"

# API测试
pytest tests/test_api/ -v
```

### Final Checklist

- [ ] 所有服务无pass占位符
- [ ] 所有公共方法已实现
- [ ] 服务能正常导入
- [ ] API端点能调用服务
- [ ] 集成测试通过
