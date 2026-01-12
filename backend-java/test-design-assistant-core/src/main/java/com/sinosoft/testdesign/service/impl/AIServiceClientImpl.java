package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.service.AIServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * AI服务客户端实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceClientImpl implements AIServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Override
    public Map<String, Object> post(String url, Object request) {
        log.debug("调用AI服务: POST {}", url);
        return restTemplate.postForObject(url, request, Map.class);
    }
    
    @Override
    public Map<String, Object> get(String url) {
        log.debug("调用AI服务: GET {}", url);
        return restTemplate.getForObject(url, Map.class);
    }
}

