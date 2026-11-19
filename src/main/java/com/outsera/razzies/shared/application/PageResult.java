package com.outsera.razzies.shared.application;

import java.util.List;
import java.util.function.Function;

/** One page of results, independent of the persistence technology. */
public record PageResult<T>(List<T> content, int number, int size, long totalElements, int totalPages) {

    public PageResult {
        content = List.copyOf(content);
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(content.stream().<R>map(mapper).toList(), number, size, totalElements, totalPages);
    }
}
