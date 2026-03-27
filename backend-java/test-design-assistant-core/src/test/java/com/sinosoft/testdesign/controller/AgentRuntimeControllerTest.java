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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Agent运行时Controller测试")
@WebMvcTest(AgentRuntimeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AgentRuntimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIServiceClient aiServiceClient;

    @Test
    @DisplayName("Agent对话-成功")
    void testChat_Success() throws Exception {
        when(aiServiceClient.post(contains("/api/v1/agent/chat"), any()))
                .thenReturn(Map.of(
                        "content", "agent reply",
                        "iterations", 1,
                        "tokens_used", 20
                ));

        mockMvc.perform(post("/v1/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "session_id": 1,
                                  "message": "hi"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("agent reply"))
                .andExpect(jsonPath("$.data.tokens_used").value(20));
    }

    @Test
    @DisplayName("获取会话历史-成功")
    void testGetSessionHistory_Success() throws Exception {
        when(aiServiceClient.get(contains("/api/v1/agent/sessions/1/history")))
                .thenReturn(Map.of(
                        "code", 200,
                        "message", "success",
                        "data", List.of(
                                Map.of("role", "user", "content", "hello"),
                                Map.of("role", "assistant", "content", "world")
                        )
                ));

        mockMvc.perform(get("/v1/agent/sessions/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].role").value("user"))
                .andExpect(jsonPath("$.data[1].content").value("world"));
    }
}
