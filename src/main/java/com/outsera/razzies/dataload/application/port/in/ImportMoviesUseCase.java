package com.outsera.razzies.dataload.application.port.in;

import java.util.List;

/** Inbound port: bulk load of the movie list. */
public interface ImportMoviesUseCase {

    /**
     * Stores every movie in a single transaction (all or none).
     *
     * @return number of movies stored, after merging duplicated rows
     */
    int importAll(List<ImportedMovie> movies);
}
