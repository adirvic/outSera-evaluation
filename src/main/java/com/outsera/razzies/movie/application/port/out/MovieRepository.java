package com.outsera.razzies.movie.application.port.out;

import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.movie.domain.MovieCriteria;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;

import java.util.Collection;
import java.util.Optional;

/** Outbound port: storage of movies. Producers and studios must already be persisted (have ids). */
public interface MovieRepository {

    PageResult<Movie> find(MovieCriteria criteria, PageQuery page);

    Optional<Movie> findById(long id);

    boolean existsById(long id);

    /** Inserts a new movie or updates an existing one, returning it as stored. */
    Movie save(Movie movie);

    /** Inserts new movies in batch. */
    void saveAll(Collection<Movie> movies);

    void deleteById(long id);
}
