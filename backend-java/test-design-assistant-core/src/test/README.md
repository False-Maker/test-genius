# 测试说明文档

## 测试结构

```
src/test/java/com/sinosoft/testdesign/
├── common/              # 测试工具类和基类
│   ├── BaseIntegrationTest.java      # 集成测试基类
│   ├── BaseControllerTest.java      # Controller测试基类
│   └── TestDataBuilder.java         # 测试数据构建器
├── controller/          # Controller层集成测试
│   ├── RequirementControllerTest.java
│   └── PromptTemplateControllerTest.java
└── service/impl/        # Service层单元测试
    ├── RequirementServiceImplTest.java
    ├── TestCaseServiceImplTest.java
    └── PromptTemplateServiceImplTest.java
```

## 运行测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试类

```bash
mvn test -Dtest=RequirementServiceImplTest
```

### 运行特定测试方法

```bash
mvn test -Dtest=RequirementServiceImplTest#testCreateRequirement_Success
```

### 生成代码覆盖率报告

```bash
mvn clean test jacoco:report
```

覆盖率报告位置：`target/site/jacoco/index.html`

## 测试覆盖率要求

- **目标覆盖率**：>70%
- **关键Service层**：>80%
- **Controller层**：>60%

## 测试规范

### 1. 测试命名规范

- 测试类：`被测试类名 + Test`
- 测试方法：`test方法名_场景描述`

示例：
```java
@Test
@DisplayName("创建需求-成功")
void testCreateRequirement_Success() {
    // ...
}
```

### 2. 测试结构

每个测试方法应包含：
- **Given**：准备测试数据
- **When**：执行被测试方法
- **Then**：验证结果

### 3. Mock使用

- 使用`@Mock`注解Mock依赖对象
- 使用`@InjectMocks`注解注入被测试对象
- 使用`when().thenReturn()`设置Mock行为

### 4. 断言

- 使用JUnit 5的断言方法
- 验证关键业务逻辑
- 验证异常情况

## 测试框架说明

### 1. 集成测试基类

#### BaseIntegrationTest
- 提供通用的集成测试配置
- 使用`@SpringBootTest`和`@ActiveProfiles("test")`
- 自动事务回滚（`@Transactional`）

#### BaseControllerTest
- 提供Controller测试的基础配置
- 自动注入`MockMvc`和`ObjectMapper`
- 适用于所有Controller集成测试

#### TestDataBuilder
- 使用Builder模式构建测试数据
- 提供常用实体的构建器（Requirement、TestCase、PromptTemplate等）
- 自动设置默认值，简化测试数据准备

使用示例：
```java
TestRequirement requirement = TestDataBuilder.requirement()
    .withName("测试需求")
    .withType("新功能")
    .withStatus(RequirementStatus.DRAFT)
    .build();
```

### 2. 测试配置文件

- `application-test.yml`: 测试环境配置
  - 使用H2内存数据库
  - 禁用Redis（或使用Mock）
  - 简化日志配置

## 待补充测试

### Service层单元测试
- [x] RequirementServiceImplTest ✅ (15个测试用例)
- [x] TestCaseServiceImplTest ✅ (11个测试用例)
- [x] PromptTemplateServiceImplTest ✅ (16个测试用例)
- [x] ModelConfigServiceImplTest ✅ (15个测试用例)
- [x] TestCaseQualityServiceImplTest ✅ (12个测试用例)
- [x] RequirementAnalysisServiceImplTest ✅ (6个测试用例)
- [x] TestCaseImportExportServiceImplTest ✅ (6个测试用例)
- [ ] IntelligentCaseGenerationServiceImpl测试（复杂异步逻辑，建议使用集成测试）
- [ ] FileUploadServiceImpl测试
- [ ] CacheServiceImpl测试
- [ ] AIServiceClientImpl测试

### Controller层集成测试
- [x] RequirementControllerTest ✅ (5个测试用例)
- [x] PromptTemplateControllerTest ✅ (6个测试用例)
- [x] TestCaseControllerTest ✅ (8个测试用例)
- [x] ModelConfigControllerTest ✅ (10个测试用例)
- [x] CommonControllerTest ✅ (6个测试用例)
- [x] CaseGenerationControllerTest ✅ (4个测试用例)
- [x] TestCaseQualityControllerTest ✅ (4个测试用例)
- [ ] FileUploadController测试

## 测试统计

### 当前测试覆盖情况
- **Service层单元测试**: 81个测试用例
  - RequirementService: 15个
  - TestCaseService: 11个
  - PromptTemplateService: 16个
  - ModelConfigService: 15个
  - TestCaseQualityService: 12个
  - RequirementAnalysisService: 6个
  - TestCaseImportExportService: 6个
- **Controller层集成测试**: 43个测试用例
  - RequirementController: 5个
  - PromptTemplateController: 6个
  - TestCaseController: 8个
  - ModelConfigController: 10个
  - CommonController: 6个
  - CaseGenerationController: 4个
  - TestCaseQualityController: 4个
- **总计**: 124个测试用例（原有31个 + 新增93个）

### Repository层测试
- [ ] 使用`@DataJpaTest`进行Repository层测试
- [ ] 自定义查询方法测试

