package com.tripgoapi.infrastructure.adapter.in.admin.view;

import com.tripgoapi.application.port.in.PageResult;

/** Pagination state for the admin table footers. */
public record PageInfo(int page, int size, long total) {

    public static PageInfo of(PageResult<?> result) {
        return new PageInfo(result.page(), result.size(), result.total());
    }

    public int totalPages() {
        return total == 0 ? 1 : (int) Math.ceil((double) total / size);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages();
    }
}
