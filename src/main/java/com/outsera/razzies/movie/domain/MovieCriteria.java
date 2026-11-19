package com.outsera.razzies.movie.domain;

/**
 * Optional filters to search movies. Every non-null attribute narrows the result (AND).
 *
 * @param year     exact release year
 * @param winner   only winners ({@code true}) or only non-winning nominees ({@code false})
 * @param title    case-insensitive "contains" match on the title
 * @param producer case-insensitive "contains" match on any producer name
 * @param studio   case-insensitive "contains" match on any studio name
 */
public record MovieCriteria(Integer year, Boolean winner, String title, String producer, String studio) {

    public MovieCriteria {
        title = blankToNull(title);
        producer = blankToNull(producer);
        studio = blankToNull(studio);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
