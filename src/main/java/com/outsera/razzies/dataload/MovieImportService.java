package com.outsera.razzies.dataload;

import com.outsera.razzies.movie.Movie;
import com.outsera.razzies.movie.MovieRepository;
import com.outsera.razzies.producer.Producer;
import com.outsera.razzies.producer.ProducerService;
import com.outsera.razzies.shared.Names;
import com.outsera.razzies.studio.Studio;
import com.outsera.razzies.studio.StudioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Persists parsed CSV rows in a single transaction: all of them or none. */
@Service
public class MovieImportService {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

    private final MovieRepository movieRepository;
    private final ProducerService producerService;
    private final StudioService studioService;

    public MovieImportService(MovieRepository movieRepository, ProducerService producerService,
                              StudioService studioService) {
        this.movieRepository = movieRepository;
        this.producerService = producerService;
        this.studioService = studioService;
    }

    /** @return number of movies imported */
    @Transactional
    public int importAll(Collection<MovieCsvRecord> records) {
        // Resolve every distinct name up front: one select + one batched insert per entity type,
        // instead of one lookup per row.
        Map<String, Producer> producers = producerService.resolveByNames(
                records.stream().flatMap(r -> r.producers().stream()).toList());
        Map<String, Studio> studios = studioService.resolveByNames(
                records.stream().flatMap(r -> r.studios().stream()).toList());

        Set<String> seen = new HashSet<>();
        List<Movie> movies = new ArrayList<>(records.size());
        for (MovieCsvRecord record : records) {
            if (!seen.add(record.year() + "|" + Names.normalize(record.title()).toLowerCase(Locale.ROOT))) {
                // A duplicated row would count as a second win in the same year (interval 0).
                log.warn("Skipping duplicated movie at line {}: {} ({})", record.line(), record.title(), record.year());
                continue;
            }
            movies.add(new Movie(record.year(), Names.normalize(record.title()), record.winner(),
                    pick(producers, record.producers()), pick(studios, record.studios())));
        }
        movieRepository.saveAll(movies);
        return movies.size();
    }

    private static <T> List<T> pick(Map<String, T> byName, List<String> names) {
        return Names.normalizeAll(names).stream().map(byName::get).toList();
    }
}
