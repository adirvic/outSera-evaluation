package com.outsera.razzies.movie.adapter.in.web;

import com.outsera.razzies.movie.domain.MovieCriteria;

/**
 * Query parameters of {@code GET /api/v1/movies}. Every given parameter narrows the result (AND).
 *
 * @param year     exact release year
 * @param winner   only winners ({@code true}) or only non-winning nominees ({@code false})
 * @param title    case-insensitive "contains" match on the title
 * @param producer case-insensitive "contains" match on any producer name
 * @param studio   case-insensitive "contains" match on any studio name
 */
public record MovieFilter(Integer year, Boolean winner, String title, String producer, String studio) {

    MovieCriteria toCriteria() {
        return new MovieCriteria(year, winner, title, producer, studio);
    }
}
