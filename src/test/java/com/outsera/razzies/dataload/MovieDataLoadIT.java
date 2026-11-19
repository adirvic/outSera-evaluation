package com.outsera.razzies.dataload;

import com.outsera.razzies.IntegrationTest;
import com.outsera.razzies.RazziesApiApplication;
import com.outsera.razzies.dataload.adapter.in.csv.InvalidMovieCsvException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class MovieDataLoadIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loadsEveryMovieOfTheProvidedCsvOnStartup() throws Exception {
        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(206));
        mockMvc.perform(get("/api/v1/movies").param("winner", "true"))
                .andExpect(jsonPath("$.page.totalElements").value(42));
    }

    @Test
    void storesEveryAttributeOfARow() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("year", "1980").param("winner", "true"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Can't Stop the Music"))
                .andExpect(jsonPath("$.content[0].studios[*].name").value(contains("Associated Film Distribution")))
                .andExpect(jsonPath("$.content[0].producers[*].name").value(contains("Allan Carr")));
    }

    @Test
    void splitsProducersSeparatedByCommaAndAnd() throws Exception {
        mockMvc.perform(get("/api/v1/movies").param("title", "Under the Cherry Moon"))
                .andExpect(jsonPath("$.content[0].producers[*].name")
                        .value(contains("Bob Cavallo", "Joe Ruffalo", "Steve Fargnoli")));
    }

    @Test
    void abortsStartupWhenTheCsvHasAnInvalidYear() {
        assertThatThrownBy(() -> startWith("classpath:csv/invalid-year.csv"))
                .isInstanceOf(InvalidMovieCsvException.class)
                .hasMessageContaining("Line 3")
                .hasMessageContaining("invalid year 'abc'");
    }

    @Test
    void abortsStartupWhenTheCsvIsMissingAColumn() {
        assertThatThrownBy(() -> startWith("classpath:csv/missing-column.csv"))
                .isInstanceOf(InvalidMovieCsvException.class)
                .hasMessageContaining("[studios]");
    }

    @Test
    void abortsStartupWhenTheCsvDoesNotExist() {
        assertThatThrownBy(() -> startWith("classpath:csv/does-not-exist.csv"))
                .isInstanceOf(InvalidMovieCsvException.class)
                .hasMessageContaining("not found");
    }

    private static void startWith(String csvLocation) {
        new SpringApplicationBuilder(RazziesApiApplication.class)
                // Command-line arguments: they take precedence over application.yml.
                .run("--razzies.data.csv-location=" + csvLocation,
                        "--server.port=0",
                        "--spring.datasource.url=jdbc:h2:mem:" + UUID.randomUUID())
                .close();
    }
}
