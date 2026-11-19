package com.outsera.razzies.producer;

import com.outsera.razzies.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The evaluation uses other datasets, so each scenario boots the application with a different CSV
 * (see {@code src/test/resources/csv}) and compares the whole response body.
 */
class AwardIntervalScenariosIT {

    @IntegrationTest
    abstract static class Scenario {

        @Autowired
        MockMvc mockMvc;

        void assertAwardIntervals(String expectedJson) throws Exception {
            mockMvc.perform(get("/api/v1/producers/award-intervals"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
        }
    }

    @Nested
    @TestPropertySource(properties = "razzies.data.csv-location=classpath:csv/ties.csv")
    class Ties extends Scenario {

        @Test
        void returnsEveryTiedProducerIncludingTheSameProducerInMinAndMax() throws Exception {
            assertAwardIntervals("""
                    {
                      "min": [
                        { "producer": "Producer A", "interval": 1, "previousWin": 2000, "followingWin": 2001 },
                        { "producer": "Producer B", "interval": 1, "previousWin": 2010, "followingWin": 2011 },
                        { "producer": "Producer C", "interval": 1, "previousWin": 2010, "followingWin": 2011 }
                      ],
                      "max": [
                        { "producer": "Producer A", "interval": 99, "previousWin": 2001, "followingWin": 2100 },
                        { "producer": "Producer D", "interval": 99, "previousWin": 1900, "followingWin": 1999 },
                        { "producer": "Producer E", "interval": 99, "previousWin": 2000, "followingWin": 2099 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "razzies.data.csv-location=classpath:csv/consecutive.csv")
    class ConsecutiveWins extends Scenario {

        @Test
        void comparesOnlyConsecutiveWinsAndIgnoresNominations() throws Exception {
            // Producer X wins 1990, 2000, 2001 and 2010 (rows out of order) and is only nominated in 2005.
            assertAwardIntervals("""
                    {
                      "min": [
                        { "producer": "Producer X", "interval": 1, "previousWin": 2000, "followingWin": 2001 }
                      ],
                      "max": [
                        { "producer": "Producer Y", "interval": 40, "previousWin": 1980, "followingWin": 2020 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "razzies.data.csv-location=classpath:csv/same-year.csv")
    class WinsInTheSameYear extends Scenario {

        @Test
        void countsTwoWinsInTheSameYearAsIntervalZero() throws Exception {
            assertAwardIntervals("""
                    {
                      "min": [
                        { "producer": "Producer Z", "interval": 0, "previousWin": 2015, "followingWin": 2015 }
                      ],
                      "max": [
                        { "producer": "Producer Z", "interval": 3, "previousWin": 2015, "followingWin": 2018 }
                      ]
                    }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "razzies.data.csv-location=classpath:csv/no-intervals.csv")
    class NoProducerWithTwoWins extends Scenario {

        @Test
        void returnsEmptyLists() throws Exception {
            assertAwardIntervals("""
                    { "min": [], "max": [] }
                    """);
        }
    }

    @Nested
    @TestPropertySource(properties = "razzies.data.csv-location=classpath:csv/format-variations.csv")
    class FormatVariations extends Scenario {

        @Test
        void handlesBomColumnOrderCaseWhitespaceAndProducerSeparators() throws Exception {
            assertAwardIntervals("""
                    {
                      "min": [
                        { "producer": "Producer A", "interval": 3, "previousWin": 2000, "followingWin": 2003 },
                        { "producer": "Producer C", "interval": 3, "previousWin": 2000, "followingWin": 2003 }
                      ],
                      "max": [
                        { "producer": "Producer B", "interval": 8, "previousWin": 2000, "followingWin": 2008 }
                      ]
                    }
                    """);
        }
    }
}
