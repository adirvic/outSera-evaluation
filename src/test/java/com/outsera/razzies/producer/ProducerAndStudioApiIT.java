package com.outsera.razzies.producer;

import com.outsera.razzies.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ProducerAndStudioApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchesProducersByNameAndReturnsOneById() throws Exception {
        String json = mockMvc.perform(get("/api/v1/producers").param("name", "matthew vaughn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", contains("Matthew Vaughn")))
                .andReturn().getResponse().getContentAsString();
        long id = JsonPath.<Number>read(json, "$.content[0].id").longValue();

        mockMvc.perform(get("/api/v1/producers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Matthew Vaughn"));
    }

    @Test
    void listsProducersSortedByName() throws Exception {
        mockMvc.perform(get("/api/v1/producers").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", contains("A. Kitman Ho", "Aaron Seltzer")));
    }

    @Test
    void returnsNotFoundForUnknownProducer() throws Exception {
        mockMvc.perform(get("/api/v1/producers/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void producersAreReadOnly() throws Exception {
        mockMvc.perform(post("/api/v1/producers"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void searchesStudiosByName() throws Exception {
        mockMvc.perform(get("/api/v1/studios").param("name", "cannon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", contains("Cannon Films")));
    }

    @Test
    void returnsNotFoundForUnknownStudio() throws Exception {
        mockMvc.perform(get("/api/v1/studios/{id}", 999_999))
                .andExpect(status().isNotFound());
    }
}
