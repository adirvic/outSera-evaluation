package com.outsera.razzies.studio.domain;

import com.outsera.razzies.shared.domain.Names;

import java.util.Objects;

/**
 * A studio, identified by its normalized name.
 *
 * @param id   {@code null} until persisted
 * @param name normalized name (see {@link Names})
 */
public record Studio(Long id, String name) {

    public Studio {
        name = Names.normalize(Objects.requireNonNull(name, "name"));
        if (name.isEmpty()) {
            throw new IllegalArgumentException("studio name must not be blank");
        }
    }

    public static Studio named(String name) {
        return new Studio(null, name);
    }
}
