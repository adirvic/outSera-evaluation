package com.outsera.razzies.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Canonical form for people / company names, so that "Joel  Silver " and "Joel Silver"
 * resolve to the same producer regardless of where the name came from (CSV or API).
 */
public final class Names {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private Names() {
    }

    public static String normalize(String name) {
        return WHITESPACE.matcher(name.strip()).replaceAll(" ");
    }

    /** Normalizes, drops blanks and removes duplicates while keeping the original order. */
    public static List<String> normalizeAll(Collection<String> names) {
        var result = new LinkedHashSet<String>();
        names.stream()
                .filter(Objects::nonNull)
                .map(Names::normalize)
                .filter(name -> !name.isEmpty())
                .forEach(result::add);
        return List.copyOf(result);
    }
}
