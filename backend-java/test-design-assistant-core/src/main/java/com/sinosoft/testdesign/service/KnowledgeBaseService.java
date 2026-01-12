package com.sinosoft.testdesign.service;

import java.util.List;
import java.util.Map;

/**
 * 知识库服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface KnowledgeBaseService {
    
    /**
     * 初始化知识库表结构
     */
    boolean initKnowledgeBase();
    
    /**
     * 添加知识库文档
     */
    Long addDocument(String docCode, String docName, String docType, String docContent, 
                     String docCategory, String docUrl, Long creatorId);
    
    /**
     * 语义检索知识库文档
     */
    List<Map<String, Object>> searchDocuments(String queryText, String docType, 
                                                int topK, double similarityThreshold);
    
    /**
     * 关键词检索知识库文档
     */
    List<Map<String, Object>> searchDocumentsByKeyword(String keyword, String docType, int topK);
}

