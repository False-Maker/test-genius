package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 添加知识库文档请求（代理到 Python 用）
 */
@Data
public class DocumentAddRequestDTO {
    private Long kbId;
    private String docCode;
    @NotBlank(message = "文档名称不能为空")
    private String docName;
    @NotBlank(message = "文档类型不能为空")
    private String docType;
    @NotBlank(message = "文档内容不能为空")
    private String docContent;
    private String docCategory;
    private String docUrl;
    private Long creatorId;
}
