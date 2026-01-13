package com.sinosoft.testdesign.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger配置类
 * 配置API文档的元信息和服务器信息
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * 配置OpenAPI文档信息
     * 
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("测试设计助手系统 API")
                        .version("1.0.0")
                        .description("测试设计助手系统RESTful API文档\n\n" +
                                "提供以下功能模块：\n" +
                                "- 需求管理：测试需求的创建、查询、更新、删除\n" +
                                "- 用例管理：测试用例的创建、查询、更新、删除、导入导出\n" +
                                "- 用例生成：基于AI的智能用例生成\n" +
                                "- 模型配置：大模型配置管理\n" +
                                "- 提示词模板：提示词模板管理\n" +
                                "- 知识库：知识库文档管理\n" +
                                "- 用例复用：用例检索和组合\n" +
                                "- 质量评估：用例质量评估")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@sinosoft.com"))
                        .license(new License()
                                .name("内部使用")
                                .url("https://www.sinosoft.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.test-design.sinosoft.com")
                                .description("生产环境")
                ));
    }
}

