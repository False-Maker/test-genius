package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.PageElementInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 页面元素信息数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface PageElementInfoRepository extends JpaRepository<PageElementInfo, Long>, 
        JpaSpecificationExecutor<PageElementInfo> {
    
    /**
     * 根据元素编码查询
     */
    Optional<PageElementInfo> findByElementCode(String elementCode);
    
    /**
     * 根据页面URL查询元素列表
     */
    List<PageElementInfo> findByPageUrl(String pageUrl);
    
    /**
     * 根据元素类型查询元素列表
     */
    List<PageElementInfo> findByElementType(String elementType);
    
    /**
     * 根据页面URL和元素类型查询元素列表
     */
    List<PageElementInfo> findByPageUrlAndElementType(String pageUrl, String elementType);
    
    /**
     * 分页查询元素信息列表（带过滤条件）
     */
    @Query(value = "SELECT * FROM page_element_info pei WHERE " +
            "(:pageUrl IS NULL OR pei.page_url ILIKE '%' || :pageUrl || '%') AND " +
            "(:elementType IS NULL OR pei.element_type = :elementType)",
            countQuery = "SELECT COUNT(*) FROM page_element_info pei WHERE " +
            "(:pageUrl IS NULL OR pei.page_url ILIKE '%' || :pageUrl || '%') AND " +
            "(:elementType IS NULL OR pei.element_type = :elementType)",
            nativeQuery = true)
    Page<PageElementInfo> findWithFilters(
            @Param("pageUrl") String pageUrl,
            @Param("elementType") String elementType,
            Pageable pageable);
}

