package com.outsera.razzies.shared.adapter.out.persistence;

import com.outsera.razzies.shared.application.InvalidSortException;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;
import java.util.function.Supplier;

/** Translates the application's pagination types to / from Spring Data. */
public final class PersistencePages {

    private PersistencePages() {
    }

    public static Pageable toPageable(PageQuery query) {
        Sort sort = Sort.by(query.sort().stream()
                .map(order -> order.ascending() ? Sort.Order.asc(order.property()) : Sort.Order.desc(order.property()))
                .toList());
        return PageRequest.of(query.page(), query.size(), sort);
    }

    /**
     * Runs a paged query and maps its entities to domain objects. An unknown sort property surfaces as
     * {@link InvalidSortException}, so no Spring Data type leaks out of the adapter.
     */
    public static <E, T> PageResult<T> fetch(Supplier<Page<E>> query, Function<E, T> mapper) {
        Page<E> page;
        try {
            page = query.get();
        } catch (PropertyReferenceException e) {
            throw new InvalidSortException(e.getPropertyName());
        }
        return new PageResult<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
