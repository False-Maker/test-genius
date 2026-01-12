# Insurance Industry Test Design Assistant System Development Prompt (English Version)

## Project Background
The current project is a **life insurance core system for the insurance industry**. We need to develop a **Test Design Assistant System** for the testing team, aimed at improving test design efficiency and quality, achieving standardization, intelligence, and automation of test design.

## Core Functional Requirements
The system needs to include the following two core modules:

### 1. Standardized Test Design Process Module
- **Functional Positioning**: Build a standardized test design workbench to achieve full online test design
- **Core Capabilities**:
  - **Test Design Specification Management**: Integrate test design specification requirements to ensure test design complies with industry standards and company specifications
  - **Requirement Understanding and Analysis**: Help testers understand business requirements and identify test points
  - **Test Layering Selection**: Support testers in selecting appropriate test layers (Individual, Business Case, Functional Case, Interface Case, Scenario Case) based on requirements
  - **Test Design Method Selection**: Provide multiple test design methods (Equivalence Partitioning, Boundary Value Analysis, Scenario Method, Decision Table, etc.)
  - **Standardized Case Layered Structure**:
    - **Individual-level Testing**: Testing for individual user-related functions
    - **Business Case Testing**: End-to-end testing for complete business processes
    - **Functional Case Testing**: Testing for specific functional modules
    - **Interface Case Testing**: Testing for API interfaces
    - **Scenario Case Testing**: Testing for specific business scenarios
  - **Test Case Management**: Creation, editing, review, and version management of test cases
  - **Test Case Reuse**: Support for reuse and combination of test cases
  - **Test Design Workbench**: Provide a visual test design work interface

### 2. Prompt Engineering Use Case Generation Module
- **Functional Positioning**: Automatically generate high-quality test cases based on large language models and prompt engineering
- **Core Capabilities**:
  - **Prompt Template Library**: Build a prompt template library based on industry test knowledge and insurance industry characteristics
  - **Intelligent Use Case Generation**:
    - Automatically generate test cases from requirement documents
    - Generate test cases for corresponding layers based on test layered structure
    - Support use case generation for different test design methods
  - **Company-driven Models**: Drive models through different companies' (insurance companies) business characteristics to generate test cases that match specific company business scenarios
  - **Industry Test Knowledge Base**: Integrate industry test best practices and insurance industry test experience
  - **Use Case Quality Assessment**: Quality assessment and optimization suggestions for generated test cases
  - **Use Case Generation Workflow**:
    - Requirement Input → Requirement Analysis → Test Layer Selection → Prompt Generation → Use Case Generation → Use Case Review → Use Case Storage
  - **Multi-model Support**: Support calling different large language models (DeepSeek, Doubao, Kimi, Qianwen, etc.)

## System Architecture Requirements

### Platform Architecture Layers
The system should be based on the **MaaS Large Model Application Development Platform** architecture, including the following layers:

#### 1. Application Development Layer
- **Test Design Workbench**: Core work interface
  - Requirement management
  - Test design management
  - Use case management
  - Permission management
  - Test design specification configuration
- **Prompt Management Platform**: Manage prompts related to test case generation
  - Prompt template management
  - Prompt version control
  - Prompt effect evaluation
  - Prompt optimization
- **Knowledge Base Platform**: Manage test-related knowledge
  - Test design specification knowledge base
  - Insurance business knowledge base
  - Test case template library
  - Industry test best practices library
- **AI Training Platform** (Optional): Support fine-tuning of test-related models

#### 2. Capability Layer
- **Agent**: Intelligent test design assistant agent
- **Workflow**: Test design process orchestration
- **Function Call**: Test design-related function calls
- **MCP**: Model Control Protocol

#### 3. Knowledge Base Components
- **Document Management**: Management of test requirement documents and test design specification documents
- **Document Parsing**: Intelligent parsing of requirement documents to extract test points
- **Knowledge Construction**: Test knowledge graph construction
- **Document Retrieval**: Intelligent retrieval of test-related knowledge and use cases

#### 4. Model Development Layer
- **Model Inference**:
  - Large models (Chat, Reasoning, Generation)
  - Support for calling external models (DeepSeek, Doubao, Kimi, Qianwen, etc.)
- **Model Fine-tuning** (Optional):
  - Test case generation dedicated models
  - Embedding models (for test case similarity matching)
- **Model Deployment**: Model service deployment
- **Model Monitoring**: Use case generation quality monitoring

#### 5. External Integration
- **External Models**: Large models such as DeepSeek, Doubao, Kimi, Qianwen
- **Test Tool Integration**: Integration with test management tools and automated testing tools

#### 6. System Integration
- **Internal System Integration**:
  - Core business systems (obtain business requirements)
  - Requirement management systems
  - Test management systems
  - Defect management systems

#### 7. Underlying Resource Services
- GPU/NPU: AI computing resources
- CPU: General-purpose computing resources
- Network: Communication services
- Storage: Test cases and knowledge base data persistence

## Business Scenario Requirements

### Insurance Industry Test Characteristics
1. **High Business Complexity**: Multiple insurance product types and complex business rules
2. **Data Sensitivity**: Involves customer privacy data, requiring strict data security controls
3. **Strict Compliance Requirements**: Must comply with insurance regulatory requirements
4. **Long Business Processes**: Complete business processes from underwriting to claims
5. **Multi-system Integration**: Requires integration with multiple external systems

### Test Design Scenarios
1. **New Feature Test Design**:
   - Receive new feature requirements
   - Understand business requirements
   - Select test layers and test methods
   - Generate test cases
   - Review and optimize test cases

2. **Regression Test Design**:
   - Identify change impact scope
   - Select test cases that need regression
   - Generate supplementary test cases

3. **Interface Test Design**:
   - Analyze interface documentation
   - Generate interface test cases
   - Cover normal scenarios, exception scenarios, and boundary scenarios

4. **Business Process Test Design**:
   - Sort out complete business processes
   - Generate end-to-end test cases
   - Cover main processes, branch processes, and exception processes

5. **Performance Test Design**:
   - Identify performance test scenarios
   - Generate performance test cases
   - Define performance indicators and acceptance criteria

### Use Case Generation Scenarios
1. **Generate Based on Requirement Documents**:
   - Input: Requirement documents (Word, PDF, Markdown, etc.)
   - Output: Structured test cases (support multiple format exports)

2. **Generate Based on Test Layers**:
   - Individual-level use cases: Personal center, personal information management, etc.
   - Business case use cases: Underwriting process, claims process, etc.
   - Functional case use cases: Underwriting functions, policy service functions, etc.
   - Interface case use cases: API interface test cases
   - Scenario case use cases: Specific business scenario test cases

3. **Generate Based on Test Methods**:
   - Equivalence partitioning method use cases
   - Boundary value analysis method use cases
   - Scenario method use cases
   - Decision table method use cases
   - State transition method use cases

## Technical Requirements
1. **Large Model Integration**: Support multi-model switching and calling, support model effect comparison
2. **Knowledge Base Construction**: Build insurance industry test knowledge base and test design specification knowledge base
3. **Workflow Orchestration**: Support flexible configuration and automation of test design processes
4. **Interface Services**: Provide standardized API interfaces, support integration with test management tools
5. **Permission Management**: Fine-grained permission control, support test case review processes
6. **Version Management**: Support version management and change tracking of test cases
7. **Template Management**: Support customization and management of test case templates
8. **Quality Assessment**: Quality assessment of generated test cases (coverage, completeness, executability, etc.)

## Output Requirements
Please provide based on the above requirements:
1. System architecture design plan
2. Detailed design of core modules (Standardized Test Design Process Module, Prompt Engineering Use Case Generation Module)
3. Test case generation algorithm design
4. Knowledge base design plan
5. Technical selection recommendations
6. Implementation roadmap
7. Key risk points and mitigation strategies
8. Test case quality assessment standards

## Special Requirements
1. **Insurance Industry Adaptation**: System design needs to consider the particularities and compliance requirements of the insurance industry
2. **Test Specification Compliance**: Generated test cases need to comply with company test design specifications and industry standards
3. **Use Case Executability**: Generated test cases should have good executability for automated testing
4. **Knowledge Accumulation**: The system should be able to continuously learn and accumulate test design experience
5. **Collaboration Support**: Support collaboration of testing teams, including use case review, knowledge sharing, etc.

