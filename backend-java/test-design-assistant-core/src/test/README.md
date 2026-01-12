# 单元测试说明

## 测试结构

```
src/test/java/com/sinosoft/testdesign/
├── controller/          # Controller层集成测试
│   └── RequirementControllerTest.java
└── service/impl/        # Service层单元测试
    ├── RequirementServiceImplTest.java
    └── TestCaseServiceImplTest.java
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

## 待补充测试

- [ ] TestCaseServiceImpl完整测试
- [ ] PromptTemplateServiceImpl测试
- [ ] IntelligentCaseGenerationServiceImpl测试
- [ ] TestCaseQualityServiceImpl测试
- [ ] 其他Controller测试
- [ ] Repository层测试（使用@DataJpaTest）

