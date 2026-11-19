package com.outsera.razzies.movie;

import com.outsera.razzies.producer.Producer;
import com.outsera.razzies.studio.Studio;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Optional filters of {@code GET /api/v1/movies}. Every non-null attribute narrows the result (AND).
 *
 * @param year     exact release year
 * @param winner   only winners ({@code true}) or only non-winning nominees ({@code false})
 * @param title    case-insensitive "contains" match on the title
 * @param producer case-insensitive "contains" match on any producer name
 * @param studio   case-insensitive "contains" match on any studio name
 */
public record MovieFilter(Integer year, Boolean winner, String title, String producer, String studio) {

    private static final char ESCAPE = '\\';

    Specification<Movie> toSpecification() {
        List<Specification<Movie>> specs = new ArrayList<>();
        if (year != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("year"), year));
        }
        if (winner != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("winner"), winner));
        }
        if (hasText(title)) {
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("title")), likePattern(title), ESCAPE));
        }
        if (hasText(producer)) {
            specs.add((root, query, cb) -> {
                query.distinct(true);
                Join<Movie, Producer> producers = root.join("producers");
                return cb.like(cb.lower(producers.get("name")), likePattern(producer), ESCAPE);
            });
        }
        if (hasText(studio)) {
            specs.add((root, query, cb) -> {
                query.distinct(true);
                Join<Movie, Studio> studios = root.join("studios");
                return cb.like(cb.lower(studios.get("name")), likePattern(studio), ESCAPE);
            });
        }
        return Specification.allOf(specs);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String likePattern(String value) {
        String escaped = value.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
