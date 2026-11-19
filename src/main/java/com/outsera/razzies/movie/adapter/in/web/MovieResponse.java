package com.outsera.razzies.movie.adapter.in.web;

import com.outsera.razzies.movie.domain.Movie;
import com.outsera.razzies.shared.adapter.in.web.NamedResource;

import java.util.Comparator;
import java.util.List;

public record MovieResponse(Long id, int year, String title, boolean winner,
                            List<NamedResource> producers, List<NamedResource> studios) {

    static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.id(),
                movie.year(),
                movie.title(),
                movie.winner(),
                movie.producers().stream()
                        .map(p -> new NamedResource(p.id(), p.name()))
                        .sorted(Comparator.comparing(NamedResource::name))
                        .toList(),
                movie.studios().stream()
                        .map(s -> new NamedResource(s.id(), s.name()))
                        .sorted(Comparator.comparing(NamedResource::name))
                        .toList());
    }
}
