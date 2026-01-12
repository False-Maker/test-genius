# 测试设计助手系统项目主体框架开发Prompt

## 任务目标
基于当前目录下的所有设计文档，开发测试设计助手系统的主体框架。系统旨在为保险行业个险核心系统提供智能化测试设计支持，实现测试设计的标准化、智能化和自动化。

## 系统概述

### 系统定位
测试设计助手系统是为保险行业个险核心系统测试团队开发的智能化测试设计工具，包含两个核心模块：
1. **标准化测试设计流程模块**：建设标准化的测试设计工作台，实现测试设计的全面线上化
2. **提示词工程用例生成模块**：基于大语言模型和提示词工程，自动生成高质量测试用例

### 技术架构
- **架构模式**：基于MaaS大模型应用开发平台的分层架构
- **后端技术栈**：
  - Java 17 + Spring Boot 3.x（核心业务服务）
  - Python 3.10+ + FastAPI（AI能力服务）
  - Spring Cloud（微服务框架）
- **前端技术栈**：Vue 3 + Element Plus + TypeScript
- **数据库**：
  - PostgreSQL 14+（主数据库）
  - Milvus/PGVector（向量数据库，用于语义检索）
  - Neo4j（图数据库，用于知识图谱）
- **AI能力**：LangChain + 多模型支持（DeepSeek、豆包、Kimi、千问等）
- **中间件**：Redis（缓存）、RocketMQ（消息队列）、MinIO（对象存储）

## 执行步骤

### 第一步：文档分析与理解
1. **读取当前目录下的所有设计文档**（包括但不限于）：
   - README_测试设计助手系统.md（系统概述）
   - 07_测试设计助手系统架构设计方案.md（架构设计）
   - 08_核心模块详细设计-测试设计助手系统.md（模块设计）
   - 09_测试用例生成算法设计.md（算法设计）
   - 10_知识库设计方案.md（知识库设计）
   - 11_测试设计助手系统技术选型建议.md（技术选型）
   - 12_测试设计助手系统实施路线图.md（实施计划）
   - 13_关键风险点与应对方案.md（风险控制）
   - 14_测试用例质量评估标准.md（质量标准）

2. **分析文档结构**，识别系统的核心模块、技术架构、依赖关系：
   - 核心模块：标准化测试设计流程模块、提示词工程用例生成模块
   - 技术架构：分层架构（应用层、能力层、知识库层、模型层等）
   - 数据模型：需求、用例、模板、知识库等核心实体
   - 接口设计：RESTful API接口定义

3. **提取关键信息**：
   - **系统架构**：基于MaaS平台的分层架构，前后端分离，微服务化
   - **核心模块**：
     * 标准化测试设计流程模块：需求管理、测试分层选择、测试方法选择、用例管理、用例复用
     * 提示词工程用例生成模块：提示词模板管理、智能用例生成、多模型调用、质量评估
   - **数据模型**：需求表、用例表、模板表、知识库表等（详见核心模块详细设计）
   - **技术选型**：Java + Spring Boot + Python + FastAPI，PostgreSQL + Milvus + Neo4j
   - **测试分层**：个人级、业务案例、功能案例、接口案例、场景案例
   - **测试方法**：等价类划分、边界值分析、场景法、决策表、状态转换法

### 第二步：项目主体框架搭建
基于文档分析结果，搭建项目的主体框架，包括：

#### 1. 项目结构
创建符合技术选型的项目目录结构：

```
测试设计助手系统/
├── backend-java/                    # Java后端服务（核心业务）
│   ├── test-design-assistant-api/   # API网关服务
│   ├── test-design-assistant-core/  # 核心业务服务
│   │   ├── src/main/java/
│   │   │   └── com/sinosoft/testdesign/
│   │   │       ├── controller/     # 控制器层
│   │   │       ├── service/         # 服务层
│   │   │       ├── repository/     # 数据访问层
│   │   │       ├── entity/         # 实体类
│   │   │       ├── dto/            # 数据传输对象
│   │   │       ├── config/         # 配置类
│   │   │       └── common/         # 公共类
│   │   ├── src/main/resources/
│   │   │   ├── application.yml     # 应用配置
│   │   │   └── db/migration/       # 数据库迁移脚本
│   │   └── pom.xml                 # Maven依赖管理
│   ├── test-design-workflow/        # 工作流服务（可选）
│   └── pom.xml                     # 父POM
│
├── backend-python/                 # Python后端服务（AI能力）
│   ├── ai-service/                 # AI服务
│   │   ├── app/
│   │   │   ├── main.py             # FastAPI应用入口
│   │   │   ├── api/                # API路由
│   │   │   ├── services/           # 业务服务
│   │   │   │   ├── llm_service.py  # 大模型调用服务
│   │   │   │   ├── prompt_service.py # 提示词服务
│   │   │   │   └── case_generation_service.py # 用例生成服务
│   │   │   ├── models/             # 数据模型
│   │   │   └── utils/              # 工具类
│   │   ├── requirements.txt        # Python依赖
│   │   └── Dockerfile
│   └── knowledge-base-service/    # 知识库服务（可选）
│
├── frontend/                        # 前端项目
│   ├── src/
│   │   ├── views/                  # 页面组件
│   │   │   ├── requirement/        # 需求管理
│   │   │   ├── test-case/          # 用例管理
│   │   │   ├── case-generation/    # 用例生成
│   │   │   ├── prompt-template/    # 提示词模板
│   │   │   └── knowledge-base/     # 知识库管理
│   │   ├── components/             # 公共组件
│   │   ├── api/                    # API接口
│   │   ├── store/                  # 状态管理（Pinia）
│   │   ├── router/                 # 路由配置
│   │   └── utils/                  # 工具函数
│   ├── package.json
│   └── vite.config.ts
│
├── database/                        # 数据库脚本
│   ├── init/                       # 初始化脚本
│   └── migration/                  # 迁移脚本
│
├── docs/                           # 文档目录
│   └── (所有设计文档)
│
└── docker-compose.yml              # Docker编排文件
```

#### 2. 核心模块骨架

##### 2.1 标准化测试设计流程模块基础结构

**Java后端结构**：
- **Controller层**：
  - `RequirementController`：需求管理接口
  - `TestCaseController`：用例管理接口
  - `TestLayerController`：测试分层管理接口
  - `TestMethodController`：测试方法管理接口
  - `TestCaseReviewController`：用例审核接口

- **Service层**：
  - `RequirementService`：需求管理服务
  - `RequirementAnalysisService`：需求分析服务（接口定义，具体实现可后续开发）
  - `TestCaseService`：用例管理服务
  - `TestCaseReuseService`：用例复用服务
  - `TestLayerService`：测试分层服务
  - `TestMethodService`：测试方法服务

- **Repository层**：
  - `RequirementRepository`：需求数据访问
  - `TestCaseRepository`：用例数据访问
  - `TestLayerRepository`：测试分层数据访问
  - `TestMethodRepository`：测试方法数据访问

- **Entity层**（核心实体类）：
  - `TestRequirement`：需求实体
  - `TestCase`：用例实体
  - `TestLayer`：测试分层实体
  - `TestDesignMethod`：测试设计方法实体
  - `TestCaseReview`：用例审核实体
  - `TestCaseVersion`：用例版本实体

##### 2.2 提示词工程用例生成模块基础结构

**Java后端结构**：
- **Controller层**：
  - `PromptTemplateController`：提示词模板管理接口
  - `CaseGenerationController`：用例生成接口
  - `ModelConfigController`：模型配置管理接口
  - `CaseQualityController`：用例质量评估接口

- **Service层**：
  - `PromptTemplateService`：提示词模板管理服务
  - `IntelligentCaseGenerationService`：智能用例生成服务（接口定义）
  - `ModelCallService`：模型调用服务（接口定义，实际调用Python服务）
  - `CaseQualityService`：用例质量评估服务

- **Repository层**：
  - `PromptTemplateRepository`：提示词模板数据访问
  - `CaseGenerationRequestRepository`：用例生成请求数据访问
  - `ModelConfigRepository`：模型配置数据访问
  - `CaseQualityRepository`：用例质量数据访问

- **Entity层**：
  - `PromptTemplate`：提示词模板实体
  - `CaseGenerationRequest`：用例生成请求实体
  - `ModelConfig`：模型配置实体
  - `TestCaseQuality`：用例质量实体

**Python后端结构**：
- **API路由**：
  - `/api/v1/llm/call`：模型调用接口
  - `/api/v1/llm/batch-call`：批量模型调用接口
  - `/api/v1/prompt/generate`：提示词生成接口
  - `/api/v1/case/generate`：用例生成接口
  - `/api/v1/case/parse`：用例解析接口

- **Service层**：
  - `LLMService`：大模型调用服务（封装LangChain）
  - `PromptService`：提示词生成服务
  - `CaseGenerationService`：用例生成服务
  - `CaseParsingService`：用例解析服务

#### 3. 基础配置

##### 3.1 Java项目配置
- **pom.xml**：配置Spring Boot、Spring Cloud、数据库、工具类等依赖
- **application.yml**：
  - 数据库连接配置（PostgreSQL）
  - Redis配置
  - 消息队列配置（RocketMQ）
  - 服务注册发现配置（Nacos）
  - 日志配置
- **数据库迁移脚本**：使用Flyway或Liquibase管理数据库版本

##### 3.2 Python项目配置
- **requirements.txt**：配置FastAPI、LangChain、数据库驱动等依赖
- **config.py**：应用配置（数据库、模型API等）
- **.env**：环境变量配置（API密钥等敏感信息）

##### 3.3 前端项目配置
- **package.json**：配置Vue 3、Element Plus、TypeScript等依赖
- **vite.config.ts**：构建配置
- **tsconfig.json**：TypeScript配置

#### 4. 数据模型

根据核心模块详细设计文档，创建核心实体类和数据库表结构：

**核心实体类（Java Entity）**：
- `TestRequirement`：需求实体（对应test_requirement表）
- `TestCase`：用例实体（对应test_case表）
- `TestLayer`：测试分层实体（对应test_layer表）
- `TestDesignMethod`：测试设计方法实体（对应test_design_method表）
- `PromptTemplate`：提示词模板实体（对应prompt_template表）
- `ModelConfig`：模型配置实体（对应model_config表）

**数据库初始化脚本**：
- 创建核心表结构（参考核心模块详细设计中的数据模型）
- 初始化基础数据（测试分层、测试方法等）
- 创建索引和约束

#### 5. API接口定义

根据核心模块详细设计，定义RESTful API接口骨架：

**需求管理接口**：
- `POST /api/v1/requirements`：创建需求
- `GET /api/v1/requirements`：查询需求列表
- `GET /api/v1/requirements/{id}`：获取需求详情
- `PUT /api/v1/requirements/{id}`：更新需求
- `POST /api/v1/requirements/{id}/analyze`：分析需求（接口定义，实现可后续开发）

**用例管理接口**：
- `POST /api/v1/test-cases`：创建用例
- `GET /api/v1/test-cases`：查询用例列表
- `GET /api/v1/test-cases/{id}`：获取用例详情
- `PUT /api/v1/test-cases/{id}`：更新用例
- `POST /api/v1/test-cases/{id}/review`：审核用例

**用例生成接口**：
- `POST /api/v1/case-generation/generate`：生成用例（接口定义）
- `POST /api/v1/case-generation/batch`：批量生成（接口定义）
- `GET /api/v1/case-generation/{id}`：查询生成任务

**提示词模板接口**：
- `POST /api/v1/prompt-templates`：创建模板
- `GET /api/v1/prompt-templates`：查询模板列表
- `GET /api/v1/prompt-templates/{id}`：获取模板详情
- `PUT /api/v1/prompt-templates/{id}`：更新模板

### 第三步：输出要求

1. **生成可运行的项目骨架代码**：
   - 项目结构完整，符合技术选型要求
   - 核心模块基础代码（Controller、Service、Repository、Entity）
   - 基础配置文件（application.yml、requirements.txt、package.json等）
   - 数据库初始化脚本

2. **确保项目结构清晰、模块划分合理**：
   - 前后端分离
   - 后端Java和Python服务分离
   - 模块职责清晰，便于后续扩展

3. **遵循文档中提到的技术规范和最佳实践**：
   - 遵循Spring Boot最佳实践
   - 遵循RESTful API设计规范
   - 遵循Vue 3 + TypeScript开发规范
   - 代码注释清晰，便于理解

4. **为后续功能开发预留扩展接口**：
   - 需求分析服务接口（具体实现可后续开发）
   - 智能用例生成服务接口（具体实现可后续开发）
   - 模型调用服务接口（具体实现可后续开发）
   - 质量评估服务接口（具体实现可后续开发）

## 注意事项

### 开发原则
- **优先实现主体架构**：先搭建框架，暂不实现具体业务逻辑
- **保持代码结构清晰**：便于后续迭代开发和维护
- **严格遵循设计文档**：技术选型和架构设计必须符合文档要求
- **确保依赖关系正确**：各个模块之间的依赖关系要正确配置

### 代码质量要求
- **代码规范**：遵循Java、Python、TypeScript代码规范
- **注释完整**：关键类、方法要有清晰的注释
- **异常处理**：基础异常处理框架要搭建好
- **日志规范**：统一日志格式和级别

### 技术实现要点
1. **Java后端**：
   - 使用Spring Boot 3.x + Spring Cloud
   - 使用JPA/MyBatis进行数据访问
   - 统一响应格式（Result<T>）
   - 统一异常处理（GlobalExceptionHandler）
   - API文档（Swagger/OpenAPI）

2. **Python后端**：
   - 使用FastAPI框架
   - 使用Pydantic进行数据验证
   - 统一异常处理
   - API文档自动生成

3. **前端**：
   - 使用Vue 3 Composition API
   - 使用TypeScript进行类型检查
   - 使用Element Plus组件库
   - 使用Pinia进行状态管理
   - 使用Axios进行HTTP请求

4. **数据库**：
   - 使用PostgreSQL作为主数据库
   - 使用Flyway或Liquibase管理数据库版本
   - 创建必要的索引和约束

### 后续开发预留
- 需求文档解析功能（接口已定义，实现可后续开发）
- 智能用例生成功能（接口已定义，实现可后续开发）
- 模型调用功能（接口已定义，实现可后续开发）
- 质量评估功能（接口已定义，实现可后续开发）
- 知识库集成（接口已定义，实现可后续开发）

## 参考文档
- 系统架构设计方案（07_测试设计助手系统架构设计方案.md）
- 核心模块详细设计（08_核心模块详细设计-测试设计助手系统.md）
- 技术选型建议（11_测试设计助手系统技术选型建议.md）
- 实施路线图（12_测试设计助手系统实施路线图.md）

## 验收标准
1. ✅ 项目结构完整，符合技术选型要求
2. ✅ 核心模块基础代码已创建（Controller、Service、Repository、Entity）
3. ✅ 基础配置文件已创建并配置正确
4. ✅ 数据库初始化脚本已创建
5. ✅ 项目可以正常启动（至少后端服务可以启动）
6. ✅ 代码结构清晰，注释完整
7. ✅ 为后续功能开发预留了扩展接口
