package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.util.Map;

/**
 * 知识库文档响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class KnowledgeDocumentResponseDTO {
    
    /**
     * 文档ID
     */
    private Long docId;
    
    /**
     * 文档编码
     */
    private String docCode;
    
    /**
     * 文档名称
     */
    private String docName;
    
    /**
     * 文档类型
     */
    private String docType;
    
    /**
     * 文档内容（摘要或片段）
     */
    private String docContent;
    
    /**
     * 文档分类
     */
    private String docCategory;
    
    /**
     * 文档URL
     */
    private String docUrl;
    
    /**
     * 相似度分数（用于语义检索）
     */
    private Double similarity;
    
    /**
     * 其他扩展信息
     */
    private Map<String, Object> extraInfo;
}

