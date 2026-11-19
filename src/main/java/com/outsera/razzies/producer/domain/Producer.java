package com.outsera.razzies.producer.domain;

import com.outsera.razzies.shared.domain.Names;

import java.util.Objects;

/**
 * A producer, identified by its normalized name.
 *
 * @param id   {@code null} until persisted
 * @param name normalized name (see {@link Names})
 */
public record Producer(Long id, String name) {

    public Producer {
        name = Names.normalize(Objects.requireNonNull(name, "name"));
        if (name.isEmpty()) {
            throw new IllegalArgumentException("producer name must not be blank");
        }
    }

    public static Producer named(String name) {
        return new Producer(null, name);
    }
}
