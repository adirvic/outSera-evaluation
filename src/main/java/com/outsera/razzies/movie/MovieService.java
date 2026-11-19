package com.outsera.razzies.movie;

import com.outsera.razzies.producer.ProducerService;
import com.outsera.razzies.shared.exception.ResourceNotFoundException;
import com.outsera.razzies.studio.StudioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository repository;
    private final ProducerService producerService;
    private final StudioService studioService;

    public MovieService(MovieRepository repository, ProducerService producerService, StudioService studioService) {
        this.repository = repository;
        this.producerService = producerService;
        this.studioService = studioService;
    }

    public Page<MovieResponse> list(MovieFilter filter, Pageable pageable) {
        return repository.findAll(filter.toSpecification(), pageable).map(MovieResponse::from);
    }

    public MovieResponse get(long id) {
        return MovieResponse.from(find(id));
    }

    @Transactional
    public MovieResponse create(MovieRequest request) {
        var movie = new Movie(request.year(), request.title().strip(), request.winner(),
                producerService.resolveByNames(request.producers()).values(),
                studioService.resolveByNames(request.studios()).values());
        return MovieResponse.from(repository.save(movie));
    }

    @Transactional
    public MovieResponse update(long id, MovieRequest request) {
        Movie movie = find(id);
        movie.update(request.year(), request.title().strip(), request.winner(),
                producerService.resolveByNames(request.producers()).values(),
                studioService.resolveByNames(request.studios()).values());
        return MovieResponse.from(movie);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(find(id));
    }

    private Movie find(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie", id));
    }
}
