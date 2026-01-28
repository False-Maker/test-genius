package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库请求DTO
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Data
public class KnowledgeBaseRequestDTO {
    
    /**
     * 知识库编码（创建时自动生成，更新时不能修改）
     */
    private String kbCode;
    
    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 500, message = "知识库名称长度不能超过500个字符")
    private String kbName;
    
    /**
     * 知识库描述
     */
    private String kbDescription;
    
    /**
     * 知识库类型
     * public-公共知识库/private-私有知识库/project-项目知识库
     */
    @Size(max = 50, message = "知识库类型长度不能超过50个字符")
    private String kbType = "private";
    
    /**
     * 嵌入模型
     * 如：text-embedding-ada-002, text-embedding-3-small
     */
    @Size(max = 100, message = "嵌入模型长度不能超过100个字符")
    private String embeddingModel;
    
    /**
     * 分块策略
     * paragraph/sentence/fixed_size/semantic/recursive
     */
    @Size(max = 50, message = "分块策略长度不能超过50个字符")
    private String chunkingStrategy = "paragraph";
    
    /**
     * 分块大小
     */
    private Integer chunkSize = 1000;
    
    /**
     * 分块重叠大小
     */
    private Integer chunkOverlap = 200;
    
    /**
     * 是否激活
     * 0-未激活/1-已激活
     */
    @Size(max = 1, message = "激活状态长度不能超过1个字符")
    private String isActive = "1";
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

