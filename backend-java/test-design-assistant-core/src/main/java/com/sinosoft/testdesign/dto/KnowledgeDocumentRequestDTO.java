package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库文档请求DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgeDocumentRequestDTO {
    
    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long kbId;
    
    /**
     * 文档编码（创建时自动生成）
     */
    private String docCode;
    
    /**
     * 文档名称
     */
    @NotBlank(message = "文档名称不能为空")
    @Size(max = 500, message = "文档名称长度不能超过500个字符")
    private String docName;
    
    /**
     * 文档类型
     * specification-规范/business_rule-业务规则/case_template-用例模板/history_case-历史用例/other-其他
     */
    @Size(max = 50, message = "文档类型长度不能超过50个字符")
    private String docType;
    
    /**
     * 文档分类
     */
    @Size(max = 100, message = "文档分类长度不能超过100个字符")
    private String docCategory;
    
    /**
     * 文档内容
     */
    private String docContent;
    
    /**
     * 文档URL
     */
    @Size(max = 1000, message = "文档URL长度不能超过1000个字符")
    private String docUrl;
    
    /**
     * 文件路径
     */
    @Size(max = 1000, message = "文件路径长度不能超过1000个字符")
    private String filePath;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

