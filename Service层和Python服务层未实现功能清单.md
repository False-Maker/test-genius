# Service层和Python服务层未实现功能清单

本文档详细列出了测试设计助手系统中Service层和Python服务层的未实现功能（TODO标记），包括功能描述、代码位置、影响范围和实现建议。

---

## 一、Service层未实现功能（TODO标记）

### 1. 规约检查功能 - 根据ID列表查询规约

#### 功能描述
在规约检查功能中，当用户指定了规约ID列表时，需要根据这些ID查询对应的规约对象列表。

#### 代码位置
- **Controller层**: `SpecificationCheckController.java`
  - 第63行：`checkCompliance` 方法中调用
  - 第90行：`injectSpecification` 方法中调用
  - 第114行：`generateComplianceReport` 方法中调用
- **Service接口**: `SpecificationCheckService.java`
  - 第50行：定义了 `getSpecificationsByIds(List<Long> ids)` 方法接口
- **Service实现**: `SpecificationCheckServiceImpl.java`
  - 第480-486行：已实现 `getSpecificationsByIds` 方法

#### 当前状态
✅ **已实现** - `SpecificationCheckServiceImpl.java` 中已经实现了该方法，使用 `specificationRepository.findByIdIn(ids)` 进行查询。

#### 代码实现
```java
@Override
public List<TestSpecification> getSpecificationsByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return new ArrayList<>();
    }
    log.info("根据ID列表查询规约，ID数量: {}", ids.size());
    return specificationRepository.findByIdIn(ids);
}
```

#### 影响范围
- 如果该方法未实现，规约检查功能中指定规约ID列表的功能将无法正常工作
- 目前该功能已实现，可以正常使用

#### 实现建议
- 当前实现已满足需求
- 建议添加缓存机制，提高查询性能
- 建议添加规约ID有效性验证，过滤不存在的ID

---

### 2. 流程文档生成 - 文件导出功能

#### 功能描述
将生成的场景图和路径图（Mermaid代码）导出为文件（PNG、SVG、PDF等格式），而不是仅返回Mermaid代码字符串。

#### 代码位置
- **Service实现**: `FlowDocumentGenerationServiceImpl.java`
  - 第112-122行：`exportSceneDiagramFile` 方法
  - 第125-132行：`exportPathDiagramFile` 方法

#### 当前状态
❌ **未实现** - 当前方法只返回占位符URL，没有真正实现文件导出逻辑。

#### 代码片段
```java
@Override
public String exportSceneDiagramFile(String mermaidCode, String format, String fileName) {
    log.info("导出场景图文件，格式：{}，文件名：{}", format, fileName);
    
    // 注意：文件导出功能需要根据实际需求实现
    // 方案1：使用Python服务（Mermaid CLI）将Mermaid代码转换为PNG/SVG/PDF
    // 方案2：前端使用Mermaid.js渲染后导出
    // 方案3：使用Puppeteer服务（需要Node.js）
    // 当前实现：返回占位符URL，实际文件导出由前端处理或通过Python服务实现
    log.warn("文件导出功能暂未实现，返回占位符URL。实际导出需要前端处理或Python服务支持。");
    return "/v1/flow-documents/files/" + fileName;
}
```

#### 影响范围
- 无法导出场景图和路径图为图片或PDF文件
- 用户只能在前端查看Mermaid代码，无法下载为文件
- 影响文档的完整性和可分享性

#### 实现建议

**方案1：使用Python服务（推荐）**
- 在Python服务中集成Mermaid CLI工具
- 创建API接口：`/api/v1/flow-documents/export`
- 接收Mermaid代码和格式参数，返回文件URL或文件流
- 支持格式：PNG、SVG、PDF

**方案2：前端处理**
- 前端使用Mermaid.js渲染图表
- 使用html2canvas或类似库将图表转换为图片
- 前端直接下载文件

**方案3：使用Node.js服务**
- 创建独立的Node.js服务
- 使用Puppeteer无头浏览器渲染Mermaid图表
- 支持更多格式和自定义样式

**推荐实现步骤**：
1. 在Python服务中创建 `flow_document_export_service.py`
2. 集成Mermaid CLI：`npm install -g @mermaid-js/mermaid-cli`
3. 实现文件转换逻辑：Mermaid代码 → 临时文件 → 转换 → 保存到文件系统
4. 返回文件URL或文件流
5. Java服务调用Python服务API

---

### 3. 数据文档生成 - AI提取参数和等价类

#### 功能描述
从测试用例中自动识别和提取输入参数，并智能划分有效等价类和无效等价类，而不是依赖用户手动输入。

#### 代码位置
- **Service实现**: `DataDocumentGenerationServiceImpl.java`
  - 第437-497行：`extractEquivalenceClassesFromSource` 方法
  - 第454行：TODO注释位置

#### 当前状态
❌ **部分实现** - 当前使用简单的关键词匹配实现，缺少AI/NLP智能提取能力。

#### 代码片段
```java
// 从用例中提取参数（简单实现：基于测试步骤中的关键词）
if (request.getAutoIdentifyParameters() != null && request.getAutoIdentifyParameters()) {
    // 注意：完整的AI/NLP参数提取功能需要调用Python AI服务实现
    // 当前实现：基于关键词的简单提取（作为占位实现）
    // 实际生产环境应调用AI服务进行智能提取
    log.warn("自动参数识别功能使用简单实现，建议后续集成AI服务进行智能提取");
    
    // 简单实现：从测试步骤中提取可能的参数
    String testStep = testCase.getTestStep();
    if (testStep != null && testStep.length() > 0) {
        // 提取可能的输入参数（简单关键词匹配）
        String[] keywords = {"输入", "输入值", "参数", "值", "金额", "数量", "日期"};
        // ... 简单匹配逻辑
    }
}
```

#### 影响范围
- 等价类表生成功能可以工作，但参数提取不够智能
- 需要用户手动输入参数定义，无法自动从用例中提取
- 等价类划分可能不够准确

#### 实现建议

**方案1：调用Python AI服务（推荐）**
- 在Python服务中创建 `parameter_extraction_service.py`
- 使用大语言模型（LLM）分析测试用例内容
- 提取输入参数、输出参数、边界值等
- 智能划分有效等价类和无效等价类

**方案2：使用NLP技术**
- 使用命名实体识别（NER）提取参数
- 使用依存句法分析理解参数关系
- 使用规则引擎划分等价类

**推荐实现步骤**：
1. 在Python服务中创建参数提取服务
2. 设计提示词模板，指导LLM提取参数和等价类
3. 调用LLM服务，解析返回结果
4. Java服务调用Python服务API
5. 处理提取结果，生成等价类表

**提示词示例**：
```
请从以下测试用例中提取输入参数，并划分有效等价类和无效等价类：

测试用例：
{testCaseContent}

要求：
1. 识别所有输入参数
2. 为每个参数划分有效等价类和无效等价类
3. 返回JSON格式结果
```

---

### 4. 测试报告模板 - 删除前检查

#### 功能描述
在删除测试报告模板前，检查是否有测试报告正在使用该模板，如果有则不允许删除。

#### 代码位置
- **Service实现**: `TestReportTemplateServiceImpl.java`
  - 第157-172行：`deleteTemplate` 方法
  - 第164-168行：检查逻辑

#### 当前状态
✅ **已实现** - 已经实现了删除前检查逻辑，会检查是否有报告使用此模板。

#### 代码片段
```java
@Override
@Transactional
public void deleteTemplate(Long id) {
    log.info("删除测试报告模板: {}", id);
    
    TestReportTemplate template = templateRepository.findById(id)
            .orElseThrow(() -> new BusinessException("模板不存在"));
    
    // 检查是否被使用
    List<TestReport> reports = testReportRepository.findByTemplateId(id);
    if (!reports.isEmpty()) {
        throw new BusinessException("该模板正在被 " + reports.size() + " 个报告使用，无法删除");
    }
    
    templateRepository.delete(template);
    log.info("删除模板成功，编码: {}", template.getTemplateCode());
}
```

#### 影响范围
- 如果未实现，可能误删正在使用的模板，导致报告生成失败
- 目前该功能已实现，可以正常使用

#### 实现建议
- 当前实现已满足需求
- 建议优化错误提示，显示使用该模板的报告列表
- 建议添加"强制删除"选项（需要管理员权限）

---

## 二、Python服务层未完整实现

### 1. UI脚本生成服务 - 部分操作类型未实现

#### 功能描述
在UI脚本生成服务中，部分操作类型（action_type）的脚本生成逻辑未实现，只返回TODO占位代码。

#### 代码位置
- **Python服务**: `ui_script_generation_service.py`
  - 第286行：`_generate_action_code` 方法中，未找到定位器时的处理
  - 第359行：`_generate_selenium_action` 方法中，未实现的操作类型
  - 第402行：`_generate_playwright_action` 方法中，未实现的操作类型

#### 当前状态
❌ **部分实现** - 已实现的操作类型：`click`、`input`、`select`、`wait`、`verify`。其他操作类型未实现。

#### 已实现的操作类型
- ✅ `click` - 点击元素
- ✅ `input` - 输入内容
- ✅ `select` - 选择选项
- ✅ `wait` - 等待
- ✅ `verify` - 验证元素存在

#### 未实现的操作类型示例
以下操作类型在代码中会返回TODO占位代码：

**Selenium未实现操作**（第355-362行）：
```python
else:
    # 对于未实现的操作类型，返回基础代码框架
    logger.warning(f"未实现的操作类型: {action_type}")
    return f"""    # {action_type}操作（未实现，需要手动补充）
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    # 请根据实际需求实现{action_type}操作的具体逻辑"""
```

**Playwright未实现操作**（第403-408行）：
```python
else:
    # 对于未实现的操作类型，返回基础代码框架
    logger.warning(f"未实现的操作类型: {action_type}")
    return f"""    # {action_type}操作（未实现，需要手动补充）
    # 请根据实际需求实现{action_type}操作的具体逻辑
    page.locator("{selector}")"""
```

#### 常见未实现的操作类型
根据UI自动化测试的常见需求，以下操作类型可能需要实现：

1. **`hover`** - 鼠标悬停
2. **`double_click`** - 双击
3. **`right_click`** - 右键点击
4. **`drag_and_drop`** - 拖拽
5. **`scroll`** - 滚动
6. **`switch_frame`** - 切换iframe
7. **`switch_window`** - 切换窗口
8. **`upload_file`** - 上传文件
9. **`download_file`** - 下载文件
10. **`get_text`** - 获取文本
11. **`get_attribute`** - 获取属性
12. **`screenshot`** - 截图
13. **`execute_script`** - 执行JavaScript
14. **`clear`** - 清空内容
15. **`submit`** - 提交表单

#### 影响范围
- 当用户使用未实现的操作类型时，生成的脚本不完整
- 需要用户手动补充代码，影响自动化程度
- 影响用户体验和脚本生成质量

#### 实现建议

**优先级1（高优先级）**：
1. **`hover`** - 鼠标悬停（常用于下拉菜单）
2. **`double_click`** - 双击
3. **`right_click`** - 右键点击
4. **`scroll`** - 滚动（常用于懒加载页面）
5. **`switch_frame`** - 切换iframe（常见于富文本编辑器）

**优先级2（中优先级）**：
6. **`drag_and_drop`** - 拖拽
7. **`upload_file`** - 上传文件
8. **`get_text`** - 获取文本（用于断言）
9. **`get_attribute`** - 获取属性（用于断言）
10. **`screenshot`** - 截图（用于调试和报告）

**优先级3（低优先级）**：
11. **`switch_window`** - 切换窗口
12. **`download_file`** - 下载文件
13. **`execute_script`** - 执行JavaScript
14. **`clear`** - 清空内容
15. **`submit`** - 提交表单

**实现示例 - Selenium hover操作**：
```python
elif action_type == "hover":
    return f"""    # 鼠标悬停
    from selenium.webdriver.common.action_chains import ActionChains
    {element_var} = WebDriverWait(driver, 10).until(
        EC.presence_of_element_located(({by}, "{locator_value}"))
    )
    ActionChains(driver).move_to_element({element_var}).perform()"""
```

**实现示例 - Playwright hover操作**：
```python
elif action_type == "hover":
    return f"""    # 鼠标悬停
    page.hover("{selector}")"""
```

**实现步骤**：
1. 分析常见操作类型需求，确定优先级
2. 为每个操作类型实现Selenium和Playwright两种脚本生成逻辑
3. 添加单元测试，确保生成的脚本正确
4. 更新文档，说明支持的操作类型
5. 考虑使用LLM辅助生成复杂操作的代码

---

## 三、功能实现优先级建议

### P0 - 高优先级（必须实现）
1. **流程文档生成 - 文件导出功能**
   - 影响用户体验，无法导出文档
   - 建议使用Python服务实现

### P1 - 中优先级（建议实现）
1. **数据文档生成 - AI提取参数和等价类**
   - 提升自动化程度，减少手动输入
   - 建议调用Python AI服务实现

2. **UI脚本生成服务 - 常用操作类型**
   - `hover`、`double_click`、`right_click`、`scroll`、`switch_frame`
   - 这些操作在UI自动化测试中经常使用

### P2 - 低优先级（可选优化）
1. **UI脚本生成服务 - 其他操作类型**
   - `drag_and_drop`、`upload_file`、`get_text`等
   - 根据实际使用情况逐步实现

---

## 四、实现建议总结

### 1. 技术选型建议
- **文件导出**：优先使用Python服务 + Mermaid CLI
- **AI提取**：使用Python服务 + LLM（DeepSeek、豆包等）
- **脚本生成**：在现有Python服务中扩展操作类型支持

### 2. 开发流程建议
1. **需求分析**：明确功能需求和验收标准
2. **技术调研**：选择合适的技术方案和工具
3. **接口设计**：设计前后端和Python服务的API接口
4. **功能实现**：按优先级逐步实现
5. **测试验证**：编写单元测试和集成测试
6. **文档更新**：更新API文档和使用说明

### 3. 代码质量要求
- 遵循项目开发规范
- 添加必要的日志记录
- 完善错误处理机制
- 编写单元测试（覆盖率80%以上）
- 添加代码注释和文档

---

## 五、相关文件清单

### Java Service层
- `SpecificationCheckServiceImpl.java` - 规约检查服务实现
- `FlowDocumentGenerationServiceImpl.java` - 流程文档生成服务实现
- `DataDocumentGenerationServiceImpl.java` - 数据文档生成服务实现
- `TestReportTemplateServiceImpl.java` - 测试报告模板服务实现

### Python服务层
- `ui_script_generation_service.py` - UI脚本生成服务

### Controller层
- `SpecificationCheckController.java` - 规约检查控制器
- `FlowDocumentController.java` - 流程文档控制器
- `DataDocumentController.java` - 数据文档控制器

---

## 六、更新记录

| 日期 | 更新内容 | 更新人 |
|------|---------|--------|
| 2024-01-XX | 创建文档，列出所有未实现功能 | - |

---

**注意**：
- 本文档基于代码分析生成，实际实现状态可能已更新
- 建议定期检查代码，更新文档状态
- 实现功能后，请及时更新本文档，标记为"已实现"

