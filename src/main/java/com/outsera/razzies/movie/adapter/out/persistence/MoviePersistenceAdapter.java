package com.outsera.razzies.movie.adapter.out.persistence;

import com.outsera.razzies.movie.application.port.out.MovieRepository;
import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.movie.domain.MovieCriteria;
import com.outsera.razzies.producer.adapter.out.persistence.ProducerJpaEntity;
import com.outsera.razzies.producer.domain.Producer;
import com.outsera.razzies.shared.adapter.out.persistence.PersistencePages;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.studio.adapter.out.persistence.StudioJpaEntity;
import com.outsera.razzies.studio.domain.Studio;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Component
class MoviePersistenceAdapter implements MovieRepository {

    private final MovieJpaRepository jpa;
    private final EntityManager entityManager;

    MoviePersistenceAdapter(MovieJpaRepository jpa, EntityManager entityManager) {
        this.jpa = jpa;
        this.entityManager = entityManager;
    }

    @Override
    public PageResult<Movie> find(MovieCriteria criteria, PageQuery page) {
        return PersistencePages.fetch(
                () -> jpa.findAll(MovieSpecifications.matching(criteria), PersistencePages.toPageable(page)),
                MoviePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Movie> findById(long id) {
        return jpa.findById(id).map(MoviePersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsById(long id) {
        return jpa.existsById(id);
    }

    @Override
    public Movie save(Movie movie) {
        MovieJpaEntity entity = movie.id() == null
                ? new MovieJpaEntity(movie.year(), movie.title(), movie.winner())
                : jpa.getReferenceById(movie.id());
        apply(movie, entity);
        return toDomain(jpa.save(entity));
    }

    @Override
    public void saveAll(Collection<Movie> movies) {
        jpa.saveAll(movies.stream().map(movie -> {
            var entity = new MovieJpaEntity(movie.year(), movie.title(), movie.winner());
            apply(movie, entity);
            return entity;
        }).toList());
    }

    @Override
    public void deleteById(long id) {
        jpa.deleteById(id);
    }

    /** Producers and studios are referenced by id, without loading them. */
    private void apply(Movie movie, MovieJpaEntity entity) {
        entity.update(movie.year(), movie.title(), movie.winner(),
                movie.producers().stream()
                        .map(producer -> entityManager.getReference(ProducerJpaEntity.class, requireId(producer.id())))
                        .toList(),
                movie.studios().stream()
                        .map(studio -> entityManager.getReference(StudioJpaEntity.class, requireId(studio.id())))
                        .toList());
    }

    private static Long requireId(Long id) {
        return Objects.requireNonNull(id, "producers and studios must be persisted before the movie");
    }

    private static Movie toDomain(MovieJpaEntity entity) {
        return new Movie(entity.getId(), entity.getYear(), entity.getTitle(), entity.isWinner(),
                entity.getProducers().stream().map(p -> new Producer(p.getId(), p.getName())).toList(),
                entity.getStudios().stream().map(s -> new Studio(s.getId(), s.getName())).toList());
    }
}
