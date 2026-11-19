package com.outsera.razzies.dataload.application;

import com.outsera.razzies.dataload.application.port.in.ImportMoviesUseCase;
import com.outsera.razzies.dataload.application.port.in.ImportedMovie;
import com.outsera.razzies.movie.application.port.out.MovieRepository;
import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.producer.application.port.in.ResolveProducersUseCase;
import com.outsera.razzies.producer.domain.Producer;
import com.outsera.razzies.shared.domain.Names;
import com.outsera.razzies.studio.application.port.in.ResolveStudiosUseCase;
import com.outsera.razzies.studio.domain.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MovieImportService implements ImportMoviesUseCase {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

    private final MovieRepository movies;
    private final ResolveProducersUseCase producers;
    private final ResolveStudiosUseCase studios;

    public MovieImportService(MovieRepository movies, ResolveProducersUseCase producers, ResolveStudiosUseCase studios) {
        this.movies = movies;
        this.producers = producers;
        this.studios = studios;
    }

    @Override
    @Transactional
    public int importAll(List<ImportedMovie> rows) {
        List<ImportedMovie> distinct = mergeDuplicates(rows);

        // Resolve every distinct name up front: one select + one batched insert per entity type,
        // instead of one lookup per row.
        Map<String, Producer> producersByName = producers.resolveByNames(
                distinct.stream().flatMap(row -> row.producers().stream()).toList());
        Map<String, Studio> studiosByName = studios.resolveByNames(
                distinct.stream().flatMap(row -> row.studios().stream()).toList());

        movies.saveAll(distinct.stream()
                .map(row -> Movie.create(row.year(), row.title(), row.winner(),
                        pick(producersByName, row.producers()), pick(studiosByName, row.studios())))
                .toList());
        return distinct.size();
    }

    /**
     * Rows with the same year and title (ignoring case and extra spaces) describe the same movie. Storing
     * both would count a second win in the same year (interval 0); dropping one could lose producers. They
     * are merged instead: producers and studios are joined, and the movie is a winner if any row says so.
     */
    private static List<ImportedMovie> mergeDuplicates(List<ImportedMovie> rows) {
        var byKey = new LinkedHashMap<String, ImportedMovie>();
        for (ImportedMovie row : rows) {
            String key = row.year() + "|" + Names.normalize(row.title()).toLowerCase(Locale.ROOT);
            byKey.merge(key, row, (first, duplicate) -> {
                log.warn("Line {} repeats the movie of line {} ({}, {}); merging them",
                        duplicate.line(), first.line(), duplicate.title(), duplicate.year());
                return new ImportedMovie(first.line(), first.year(), first.title(),
                        union(first.studios(), duplicate.studios()),
                        union(first.producers(), duplicate.producers()),
                        first.winner() || duplicate.winner());
            });
        }
        return new ArrayList<>(byKey.values());
    }

    private static List<String> union(List<String> first, List<String> second) {
        var all = new ArrayList<>(first);
        all.addAll(second);
        return Names.normalizeAll(all);
    }

    private static <T> List<T> pick(Map<String, T> byName, List<String> names) {
        return Names.normalizeAll(names).stream().map(byName::get).toList();
    }
}
