package com.outsera.razzies.shared.adapter.in.web;

import com.outsera.razzies.shared.application.PageQuery;
import org.springframework.data.domain.Pageable;

/** Translates the pagination parameters bound by Spring MVC into the application's {@link PageQuery}. */
public final class WebPages {

    private WebPages() {
    }

    public static PageQuery toQuery(Pageable pageable) {
        return new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().stream()
                .map(order -> new PageQuery.Order(order.getProperty(), order.isAscending()))
                .toList());
    }
}
