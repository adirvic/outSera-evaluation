package com.outsera.razzies.shared.application;

import java.util.List;

/**
 * Framework-independent page request used by the ports, so the application core does not depend
 * on Spring Data. Adapters translate it from / to their own pagination types.
 *
 * @param page zero-based page index
 * @param size page size
 * @param sort sort orders, applied in sequence
 */
public record PageQuery(int page, int size, List<Order> sort) {

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be positive");
        }
        sort = List.copyOf(sort);
    }

    public record Order(String property, boolean ascending) {
    }
}
