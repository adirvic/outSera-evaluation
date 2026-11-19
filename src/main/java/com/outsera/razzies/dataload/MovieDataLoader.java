package com.outsera.razzies.dataload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads the configured CSV into the database on startup. Any failure aborts the startup.
 * <p>
 * Runs once all singletons are created but <em>before</em> the embedded web server starts. An
 * {@code ApplicationRunner} would only run after the server is already accepting requests, leaving
 * a window in which the API answers with an empty database.
 */
@Component
public class MovieDataLoader implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(MovieDataLoader.class);

    private final DataLoadProperties properties;
    private final MovieImportService importService;
    private final MovieCsvReader reader = new MovieCsvReader();

    public MovieDataLoader(DataLoadProperties properties, MovieImportService importService) {
        this.properties = properties;
        this.importService = importService;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Resource csv = properties.csvLocation();
        if (!csv.exists()) {
            throw new InvalidMovieCsvException("Movie CSV not found: " + csv.getDescription());
        }
        List<MovieCsvRecord> records;
        try (Reader source = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8)) {
            records = reader.read(source);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + csv.getDescription(), e);
        }
        int imported = importService.importAll(records);
        log.info("Imported {} movies from {}", imported, csv.getDescription());
    }
}
