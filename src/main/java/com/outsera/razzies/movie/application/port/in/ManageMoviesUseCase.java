package com.outsera.razzies.movie.application.port.in;

import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.movie.domain.MovieCriteria;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;

/** Inbound port: queries and changes on movies. Unknown ids raise {@code ResourceNotFoundException}. */
public interface ManageMoviesUseCase {

    PageResult<Movie> list(MovieCriteria criteria, PageQuery page);

    Movie get(long id);

    Movie create(MovieCommand command);

    Movie replace(long id, MovieCommand command);

    void delete(long id);
}
