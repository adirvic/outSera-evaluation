package com.outsera.razzies.producer.domain;

import java.util.List;

/** Producers with the shortest ({@code min}) and the longest ({@code max}) interval between consecutive wins. */
public record AwardIntervals(List<AwardInterval> min, List<AwardInterval> max) {

    public AwardIntervals {
        min = List.copyOf(min);
        max = List.copyOf(max);
    }
}
