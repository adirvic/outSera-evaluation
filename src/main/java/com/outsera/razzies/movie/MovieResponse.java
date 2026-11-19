package com.outsera.razzies.movie;

import com.outsera.razzies.shared.web.NamedResource;

import java.util.Comparator;
import java.util.List;

public record MovieResponse(Long id, int year, String title, boolean winner,
                            List<NamedResource> producers, List<NamedResource> studios) {

    static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getYear(),
                movie.getTitle(),
                movie.isWinner(),
                movie.getProducers().stream()
                        .map(p -> new NamedResource(p.getId(), p.getName()))
                        .sorted(Comparator.comparing(NamedResource::name))
                        .toList(),
                movie.getStudios().stream()
                        .map(s -> new NamedResource(s.getId(), s.getName()))
                        .sorted(Comparator.comparing(NamedResource::name))
                        .toList());
    }
}
