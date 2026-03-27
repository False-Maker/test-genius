package com.sinosoft.testdesign.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.service.AIServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("LLM运行时Controller测试")
@WebMvcTest(LlmController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LlmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIServiceClient aiServiceClient;

    @Test
    @DisplayName("调用单模型-成功")
    void testCallModel_Success() throws Exception {
        when(aiServiceClient.post(contains("/api/v1/llm/call"), any()))
                .thenReturn(Map.of(
                        "content", "hello",
                        "model_code", "TEST_MODEL",
                        "tokens_used", 12
                ));

        mockMvc.perform(post("/v1/llm/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model_code": "TEST_MODEL",
                                  "prompt": "hello"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("hello"))
                .andExpect(jsonPath("$.data.model_code").value("TEST_MODEL"));
    }

    @Test
    @DisplayName("并行调用-失败响应透传")
    void testParallelCall_Error() throws Exception {
        when(aiServiceClient.post(contains("/api/v1/llm/parallel-call"), any()))
                .thenReturn(Map.of(
                        "success", false,
                        "message", "parallel failed"
                ));

        mockMvc.perform(post("/v1/llm/parallel-call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prompt": "hello",
                                  "model_codes": ["A", "B"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("parallel failed"));
    }
}
