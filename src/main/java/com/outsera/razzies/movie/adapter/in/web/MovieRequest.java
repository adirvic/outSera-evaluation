package com.outsera.razzies.movie.adapter.in.web;

import com.outsera.razzies.movie.application.port.in.MovieCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Body of {@code POST /api/v1/movies} and {@code PUT /api/v1/movies/{id}}. */
public record MovieRequest(
        @NotNull @Positive Integer year,
        @NotBlank @Size(max = 255) String title,
        @NotNull Boolean winner,
        List<@NotBlank @Size(max = 255) String> producers,
        List<@NotBlank @Size(max = 255) String> studios) {

    MovieCommand toCommand() {
        return new MovieCommand(year, title, winner, producers, studios);
    }
}
