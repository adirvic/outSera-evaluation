package com.outsera.razzies.shared;

import com.outsera.razzies.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The web interface is served by the application itself and calls the API resources. */
@IntegrationTest
class WebUiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesTheWebInterfaceWithTheAwardIntervalsAction() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Razzies API</title>")))
                .andExpect(content().string(containsString("/producers/award-intervals")));
    }
}
