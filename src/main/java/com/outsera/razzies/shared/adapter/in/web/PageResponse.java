package com.outsera.razzies.shared.adapter.in.web;

import com.outsera.razzies.shared.application.PageResult;

import java.util.List;
import java.util.function.Function;

/** Stable JSON contract for paginated collections. */
public record PageResponse<T>(List<T> content, PageMetadata page) {

    public record PageMetadata(int number, int size, long totalElements, int totalPages) {
    }

    public static <E, T> PageResponse<T> of(PageResult<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                new PageMetadata(page.number(), page.size(), page.totalElements(), page.totalPages()));
    }
}
