package com.outsera.razzies.dataload;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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
 *     <li>Producers are separated by {@code ","} and/or {@code " and "}; studios by {@code ","}.</li>
 *     <li>Quotes are not special: the source format does not use them and titles may contain them.</li>
 *     <li>Invalid content fails fast with the offending line number: silently dropping a row
 *     would make the award intervals wrong.</li>
 * </ul>
 */
public class MovieCsvReader {

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
            .setQuote(null)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .setAllowMissingColumnNames(true)
            .get();

    public List<MovieCsvRecord> read(Reader source) {
        try (CSVParser parser = FORMAT.parse(skipBom(source))) {
            validateHeader(parser);
            var records = new ArrayList<MovieCsvRecord>();
            for (CSVRecord row : parser) {
                records.add(toRecord(row));
            }
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read movie CSV", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Raised by Commons CSV for structural problems such as duplicated header names.
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

    private static MovieCsvRecord toRecord(CSVRecord row) {
        long line = row.getParser().getCurrentLineNumber();
        String title = value(row, TITLE);
        if (title.isEmpty()) {
            throw new InvalidMovieCsvException("Line %d: title is required".formatted(line));
        }
        return new MovieCsvRecord(line, parseYear(value(row, YEAR), line), title,
                split(value(row, STUDIOS), STUDIO_SEPARATOR),
                split(value(row, PRODUCERS), PRODUCER_SEPARATOR),
                parseWinner(value(row, WINNER), line));
    }

    private static String value(CSVRecord row, String column) {
        // A row with fewer fields than the header (e.g. empty trailing "winner") is valid.
        return row.isSet(column) ? row.get(column) : "";
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
        if (FALSE_VALUES.contains(normalized)) {
            return false;
        }
        throw new InvalidMovieCsvException("Line %d: invalid winner value '%s' (expected 'yes' or empty)"
                .formatted(line, value));
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
