package com.outsera.razzies.movie.application.port.in;

import java.util.List;

/** Data to create a movie or replace an existing one. Producers and studios are given by name. */
public record MovieCommand(int year, String title, boolean winner, List<String> producers, List<String> studios) {

    public MovieCommand {
        producers = producers == null ? List.of() : List.copyOf(producers);
        studios = studios == null ? List.of() : List.copyOf(studios);
    }
}
