package com.sinosoft.testdesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 测试设计助手系统主应用类
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TestDesignAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDesignAssistantApplication.class, args);
    }
}

