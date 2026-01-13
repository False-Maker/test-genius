package com.sinosoft.testdesign.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 * 提供通用的测试配置和工具方法
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    
    @BeforeEach
    void setUp() {
        // 每个测试方法执行前的初始化操作
        // 子类可以重写此方法添加特定的初始化逻辑
    }
    
    /**
     * 清理测试数据
     * 子类可以重写此方法实现特定的清理逻辑
     */
    protected void cleanUp() {
        // 默认使用@Transactional自动回滚，无需手动清理
    }
}

