package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库文档语义检索请求（代理到 Python 用）
 */
@Data
public class DocumentSearchRequestDTO {
    @NotBlank(message = "检索内容不能为空")
    private String queryText;
    private String docType;
    private Integer topK = 10;
    private Double similarityThreshold = 0.7;
}
