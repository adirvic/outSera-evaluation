package com.outsera.razzies.shared.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Stable JSON contract for paginated collections (decoupled from Spring Data's internal Page format). */
public record PageResponse<T>(List<T> content, PageMetadata page) {

    public record PageMetadata(int number, int size, long totalElements, int totalPages) {
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return of(page, Function.identity());
    }

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                new PageMetadata(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }
}
