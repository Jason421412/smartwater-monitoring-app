package com.smartwater.monitoring.network.dto;

import java.util.List;

/**
 * Generic paginated response wrapper
 * Matches backend PageResponse structure
 */
public class PageResponse<T> {
    private List<T> items;     // Backend uses "items"
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private Boolean last;

    public List<T> getContent() {
        return items;  // Map items to content for compatibility
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }
}
