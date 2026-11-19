package com.outsera.razzies.movie.adapter.in.web;

import com.outsera.razzies.movie.application.port.in.ManageMoviesUseCase;
import com.outsera.razzies.shared.adapter.in.web.PageResponse;
import com.outsera.razzies.shared.adapter.in.web.WebPages;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final ManageMoviesUseCase movies;

    public MovieController(ManageMoviesUseCase movies) {
        this.movies = movies;
    }

    @GetMapping
    public PageResponse<MovieResponse> list(@ParameterObject MovieFilter filter,
                                            @PageableDefault(size = 20, sort = {"year", "id"}, direction = Sort.Direction.ASC)
                                            @ParameterObject Pageable pageable) {
        return PageResponse.of(movies.list(filter.toCriteria(), WebPages.toQuery(pageable)), MovieResponse::from);
    }

    @GetMapping("/{id}")
    public MovieResponse get(@PathVariable long id) {
        return MovieResponse.from(movies.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Movie created; its URI is in the Location header")
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        MovieResponse created = MovieResponse.from(movies.create(request.toCommand()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public MovieResponse replace(@PathVariable long id, @Valid @RequestBody MovieRequest request) {
        return MovieResponse.from(movies.replace(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Movie deleted")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        movies.delete(id);
        return ResponseEntity.noContent().build();
    }
}
