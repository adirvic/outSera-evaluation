package com.outsera.razzies.dataload.application.port.in;

import java.util.List;

/**
 * One movie read from an external source. {@code line} is kept for diagnostics.
 */
public record ImportedMovie(long line, int year, String title, List<String> studios,
                            List<String> producers, boolean winner) {

    public ImportedMovie {
        studios = List.copyOf(studios);
        producers = List.copyOf(producers);
    }
}
