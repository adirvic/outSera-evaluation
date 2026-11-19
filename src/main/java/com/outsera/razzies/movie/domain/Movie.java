package com.outsera.razzies.movie.domain;

import com.outsera.razzies.producer.domain.Producer;
import com.outsera.razzies.studio.domain.Studio;

import java.util.List;
import java.util.Objects;

/**
 * A nominee of the Worst Picture category.
 *
 * @param id        {@code null} until persisted
 * @param winner    whether it won the award of its year
 * @param producers distinct producers, in credit order
 * @param studios   distinct studios, in credit order
 */
public record Movie(Long id, int year, String title, boolean winner, List<Producer> producers, List<Studio> studios) {

    public Movie {
        if (year <= 0) {
            throw new IllegalArgumentException("year must be positive");
        }
        title = Objects.requireNonNull(title, "title").strip();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        producers = List.copyOf(producers.stream().distinct().toList());
        studios = List.copyOf(studios.stream().distinct().toList());
    }

    public static Movie create(int year, String title, boolean winner, List<Producer> producers, List<Studio> studios) {
        return new Movie(null, year, title, winner, producers, studios);
    }

    /** The same movie (same id) with every attribute replaced. */
    public Movie replaceWith(int year, String title, boolean winner, List<Producer> producers, List<Studio> studios) {
        return new Movie(id, year, title, winner, producers, studios);
    }
}
