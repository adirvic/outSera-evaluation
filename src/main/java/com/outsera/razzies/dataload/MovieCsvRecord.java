package com.outsera.razzies.dataload;

import java.util.List;

/** One parsed CSV row. {@code line} is kept for diagnostics. */
public record MovieCsvRecord(long line, int year, String title, List<String> studios,
                             List<String> producers, boolean winner) {
}
