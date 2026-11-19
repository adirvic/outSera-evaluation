package com.outsera.razzies.movie.adapter.out.persistence;

import com.outsera.razzies.movie.domain.MovieCriteria;
import com.outsera.razzies.producer.adapter.out.persistence.ProducerJpaEntity;
import com.outsera.razzies.studio.adapter.out.persistence.StudioJpaEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Translates {@link MovieCriteria} into a JPA {@link Specification}. */
final class MovieSpecifications {

    private static final char ESCAPE = '\\';

    private MovieSpecifications() {
    }

    static Specification<MovieJpaEntity> matching(MovieCriteria criteria) {
        List<Specification<MovieJpaEntity>> specs = new ArrayList<>();
        if (criteria.year() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("year"), criteria.year()));
        }
        if (criteria.winner() != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("winner"), criteria.winner()));
        }
        if (criteria.title() != null) {
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("title")), likePattern(criteria.title()), ESCAPE));
        }
        if (criteria.producer() != null) {
            specs.add((root, query, cb) -> {
                query.distinct(true);
                Join<MovieJpaEntity, ProducerJpaEntity> producers = root.join("producers");
                return cb.like(cb.lower(producers.get("name")), likePattern(criteria.producer()), ESCAPE);
            });
        }
        if (criteria.studio() != null) {
            specs.add((root, query, cb) -> {
                query.distinct(true);
                Join<MovieJpaEntity, StudioJpaEntity> studios = root.join("studios");
                return cb.like(cb.lower(studios.get("name")), likePattern(criteria.studio()), ESCAPE);
            });
        }
        return Specification.allOf(specs);
    }

    private static String likePattern(String value) {
        String escaped = value.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
