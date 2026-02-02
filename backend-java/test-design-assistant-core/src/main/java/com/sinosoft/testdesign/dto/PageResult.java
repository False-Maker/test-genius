package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO
 *
 * @author sinosoft
 * @date 2025-01-30
 */
@Data
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页数量
     */
    private int size;

    /**
     * 总页数
     */
    private int totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}
