package com.outsera.razzies.dataload.adapter.in.csv;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

/**
 * @param csvLocation Spring resource with the movie list, e.g. {@code classpath:data/movielist.csv}
 *                    or {@code file:/data/movielist.csv}
 */
@Validated
@ConfigurationProperties("razzies.data")
public record DataLoadProperties(@NotNull Resource csvLocation) {
}
