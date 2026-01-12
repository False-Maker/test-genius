package com.sinosoft.testdesign.service;

import java.util.List;
import java.util.Map;

/**
 * 用例复用服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface CaseReuseService {
    
    /**
     * 初始化用例向量表
     */
    boolean initCaseVectorTable();
    
    /**
     * 更新用例的向量表示
     */
    boolean updateCaseEmbedding(Long caseId);
    
    /**
     * 搜索相似用例（基于语义相似度）
     */
    List<Map<String, Object>> searchSimilarCases(String caseText, Long layerId, Long methodId, 
                                                  int topK, double similarityThreshold);
    
    /**
     * 关键词检索用例
     */
    List<Map<String, Object>> searchCasesByKeyword(String keyword, Long layerId, Long methodId, int topK);
    
    /**
     * 推荐相似用例（基于现有用例）
     */
    List<Map<String, Object>> recommendSimilarCases(Long caseId, int topK);
    
    /**
     * 创建用例组合（测试套件）
     */
    Long createCaseSuite(String suiteName, List<Long> caseIds, Long creatorId);
}

