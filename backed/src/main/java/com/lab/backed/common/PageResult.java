package com.lab.backed.common;

import lombok.Data;

/**
 * 分页响应
 */
@Data
public class PageResult<T> {
    private Long total;
    private java.util.List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, java.util.List<T> list) {
        this.total = total;
        this.list = list;
    }
}
