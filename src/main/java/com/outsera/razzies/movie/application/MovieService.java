package com.outsera.razzies.movie.application;

import com.outsera.razzies.movie.application.port.in.ManageMoviesUseCase;
import com.outsera.razzies.movie.application.port.in.MovieCommand;
import com.outsera.razzies.movie.application.port.out.MovieRepository;
import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.movie.domain.MovieCriteria;
import com.outsera.razzies.producer.application.port.in.ResolveProducersUseCase;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.shared.domain.ResourceNotFoundException;
import com.outsera.razzies.studio.application.port.in.ResolveStudiosUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MovieService implements ManageMoviesUseCase {

    private final MovieRepository repository;
    private final ResolveProducersUseCase producers;
    private final ResolveStudiosUseCase studios;

    public MovieService(MovieRepository repository, ResolveProducersUseCase producers, ResolveStudiosUseCase studios) {
        this.repository = repository;
        this.producers = producers;
        this.studios = studios;
    }

    @Override
    public PageResult<Movie> list(MovieCriteria criteria, PageQuery page) {
        return repository.find(criteria, page);
    }

    @Override
    public Movie get(long id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Override
    @Transactional
    public Movie create(MovieCommand command) {
        return repository.save(Movie.create(command.year(), command.title(), command.winner(),
                List.copyOf(producers.resolveByNames(command.producers()).values()),
                List.copyOf(studios.resolveByNames(command.studios()).values())));
    }

    @Override
    @Transactional
    public Movie replace(long id, MovieCommand command) {
        Movie current = get(id);
        return repository.save(current.replaceWith(command.year(), command.title(), command.winner(),
                List.copyOf(producers.resolveByNames(command.producers()).values()),
                List.copyOf(studios.resolveByNames(command.studios()).values())));
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw notFound(id);
        }
        repository.deleteById(id);
    }

    private static ResourceNotFoundException notFound(long id) {
        return new ResourceNotFoundException("Movie", id);
    }
}
