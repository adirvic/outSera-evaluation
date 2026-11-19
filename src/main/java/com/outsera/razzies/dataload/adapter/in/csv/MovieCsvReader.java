package com.outsera.razzies.dataload.adapter.in.csv;

import com.outsera.razzies.dataload.application.port.in.ImportedMovie;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses the Golden Raspberry Awards CSV: {@code year;title;studios;producers;winner}.
 * <ul>
 *     <li>Columns are located by header name (case-insensitive), so their order does not matter.</li>
 *     <li>Values are trimmed, blank lines are ignored and an UTF-8 BOM is tolerated.</li>
 *     <li>Values may be enclosed in double quotes (RFC 4180, {@code ""} escapes a quote). A quote in
 *     the middle of an unquoted value is kept as is.</li>
 *     <li>Producers are separated by {@code ","} and/or {@code " and "}; studios by {@code ","}.</li>
 *     <li>{@code winner}: {@code yes}, {@code y} or {@code true} (any case) is a winner; anything
 *     else is a nominee, with a warning for unexpected values.</li>
 *     <li>Invalid year or missing title fail fast with the offending line number: silently dropping
 *     a row would make the award intervals wrong.</li>
 * </ul>
 */
public class MovieCsvReader {

    private static final Logger log = LoggerFactory.getLogger(MovieCsvReader.class);

    static final String YEAR = "year";
    static final String TITLE = "title";
    static final String STUDIOS = "studios";
    static final String PRODUCERS = "producers";
    static final String WINNER = "winner";
    private static final List<String> REQUIRED_COLUMNS = List.of(YEAR, TITLE, STUDIOS, PRODUCERS, WINNER);

    private static final Pattern PRODUCER_SEPARATOR = Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");
    private static final Pattern STUDIO_SEPARATOR = Pattern.compile("\\s*,\\s*");
    private static final Set<String> TRUE_VALUES = Set.of("yes", "y", "true");
    private static final Set<String> FALSE_VALUES = Set.of("", "no", "n", "false");
    private static final char BOM = '﻿';

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setQuote('"')
            .setTrailingData(true)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .setAllowMissingColumnNames(true)
            .get();

    public List<ImportedMovie> read(Reader source) {
        try (CSVParser parser = FORMAT.parse(skipBom(source))) {
            validateHeader(parser);
            var rows = new ArrayList<ImportedMovie>();
            for (CSVRecord row : parser) {
                rows.add(toMovie(row));
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read movie CSV", e);
        } catch (UncheckedIOException | IllegalArgumentException | IllegalStateException e) {
            // Raised by Commons CSV for structural problems such as duplicated header names
            // or an unterminated quoted value.
            throw new InvalidMovieCsvException("Malformed movie CSV: " + e.getMessage(), e);
        }
    }

    private static void validateHeader(CSVParser parser) {
        Set<String> header = parser.getHeaderMap().keySet().stream()
                .map(name -> name.strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<String> missing = REQUIRED_COLUMNS.stream().filter(column -> !header.contains(column)).toList();
        if (!missing.isEmpty()) {
            throw new InvalidMovieCsvException("Movie CSV is missing required column(s) " + missing
                    + "; expected header: " + String.join(";", REQUIRED_COLUMNS));
        }
    }

    private static ImportedMovie toMovie(CSVRecord row) {
        long line = row.getParser().getCurrentLineNumber();
        String title = value(row, TITLE);
        if (title.isEmpty()) {
            throw new InvalidMovieCsvException("Line %d: title is required".formatted(line));
        }
        return new ImportedMovie(line, parseYear(value(row, YEAR), line), title,
                split(value(row, STUDIOS), STUDIO_SEPARATOR),
                split(value(row, PRODUCERS), PRODUCER_SEPARATOR),
                parseWinner(value(row, WINNER), line));
    }

    private static String value(CSVRecord row, String column) {
        // A row with fewer fields than the header (e.g. empty trailing "winner") is valid.
        return row.isSet(column) ? row.get(column).strip() : "";
    }

    private static int parseYear(String value, long line) {
        try {
            int year = Integer.parseInt(value);
            if (year <= 0) {
                throw new NumberFormatException();
            }
            return year;
        } catch (NumberFormatException e) {
            throw new InvalidMovieCsvException("Line %d: invalid year '%s'".formatted(line, value));
        }
    }

    private static boolean parseWinner(String value, long line) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(normalized)) {
            return true;
        }
        if (!FALSE_VALUES.contains(normalized)) {
            log.warn("Line {}: unexpected winner value '{}', treated as a nominee (expected 'yes' or empty)", line, value);
        }
        return false;
    }

    private static List<String> split(String value, Pattern separator) {
        return Arrays.stream(separator.split(value))
                .map(String::strip)
                .filter(name -> !name.isEmpty())
                .toList();
    }

    private static Reader skipBom(Reader reader) throws IOException {
        var pushback = new PushbackReader(reader, 1);
        int first = pushback.read();
        if (first != -1 && first != BOM) {
            pushback.unread(first);
        }
        return pushback;
    }
}
