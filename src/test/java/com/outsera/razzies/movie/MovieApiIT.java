package com.outsera.razzies.movie;

import com.outsera.razzies.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Movie resource: HTTP verbs, status codes and error contract (Richardson maturity level 2).
 * Each test is rolled back, so the dataset shared with the other tests is never modified.
 */
@IntegrationTest
@Transactional
class MovieApiIT {

    private static final String MOVIES = "/api/v1/movies";
    private static final String NEW_MOVIE = """
            { "year": 2030, "title": "  The Sequel  ", "winner": true,
              "producers": ["Matthew Vaughn", "New Producer"], "studios": ["New Studio"] }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsMoviesPaginatedAndSortedByYear() throws Exception {
        mockMvc.perform(get(MOVIES).param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[*].year", everyItem(is(1980))))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.totalElements").value(206))
                .andExpect(jsonPath("$.page.totalPages").value(42));
    }

    @Test
    void filtersMoviesByProducerAndWinner() throws Exception {
        mockMvc.perform(get(MOVIES).param("producer", "joel silver").param("winner", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].year", contains(1990, 1991)));
    }

    @Test
    void filtersMoviesByStudio() throws Exception {
        mockMvc.perform(get(MOVIES).param("studio", "cannon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains("Hercules", "Bolero", "Cobra", "Tough Guys Don't Dance")));
    }

    @Test
    void rejectsUnknownSortProperty() throws Exception {
        mockMvc.perform(get(MOVIES).param("sort", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid sort property 'unknown'"));
    }

    @Test
    void returnsMovieById() throws Exception {
        String location = createMovie();

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Sequel"))
                .andExpect(jsonPath("$.year").value(2030))
                .andExpect(jsonPath("$.winner").value(true))
                .andExpect(jsonPath("$.producers[*].name", contains("Matthew Vaughn", "New Producer")))
                .andExpect(jsonPath("$.studios[*].name", contains("New Studio")));
    }

    @Test
    void returnsNotFoundAsProblemDetail() throws Exception {
        mockMvc.perform(get(MOVIES + "/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Movie with id 999999 was not found"));
    }

    @Test
    void returnsBadRequestForNonNumericId() throws Exception {
        mockMvc.perform(get(MOVIES + "/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsMovieAndReturnsItsLocation() throws Exception {
        mockMvc.perform(post(MOVIES).contentType(MediaType.APPLICATION_JSON).content(NEW_MOVIE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/api/v1/movies/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("The Sequel"));
    }

    @Test
    void reusesExistingProducersWhenCreatingMovieSoIntervalsAreUpdated() throws Exception {
        createMovie();

        // Matthew Vaughn won in 2002 and 2015; a new win in 2030 makes 2015-2030 the longest interval.
        mockMvc.perform(get("/api/v1/producers").param("name", "Matthew Vaughn"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mockMvc.perform(get("/api/v1/producers/award-intervals"))
                .andExpect(content().json("""
                        {
                          "min": [
                            { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
                          ],
                          "max": [
                            { "producer": "Matthew Vaughn", "interval": 15, "previousWin": 2015, "followingWin": 2030 }
                          ]
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void rejectsInvalidMovieWithFieldErrors() throws Exception {
        mockMvc.perform(post(MOVIES).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "year": -1, "title": " ", "producers": [""] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.year").exists())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.winner").exists())
                .andExpect(jsonPath("$.errors['producers[0]']").exists());
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post(MOVIES).contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post(MOVIES).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void replacesMovie() throws Exception {
        String location = createMovie();

        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "year": 2031, "title": "Renamed", "winner": false,
                                  "producers": ["Other Producer"], "studios": [] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2031))
                .andExpect(jsonPath("$.title").value("Renamed"))
                .andExpect(jsonPath("$.winner").value(false))
                .andExpect(jsonPath("$.producers[*].name", contains("Other Producer")))
                .andExpect(jsonPath("$.studios", hasSize(0)));
    }

    @Test
    void returnsNotFoundWhenReplacingUnknownMovie() throws Exception {
        mockMvc.perform(put(MOVIES + "/{id}", 999_999).contentType(MediaType.APPLICATION_JSON).content(NEW_MOVIE))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesMovie() throws Exception {
        String location = createMovie();

        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
        mockMvc.perform(delete(location)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnsupportedMethod() throws Exception {
        mockMvc.perform(patch(MOVIES + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    private String createMovie() throws Exception {
        return mockMvc.perform(post(MOVIES).contentType(MediaType.APPLICATION_JSON).content(NEW_MOVIE))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
    }
}
