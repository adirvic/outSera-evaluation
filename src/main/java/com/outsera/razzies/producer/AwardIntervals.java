package com.outsera.razzies.producer;

import java.util.List;

/** Response body of {@code GET /api/v1/producers/award-intervals}, in the format required by the specification. */
public record AwardIntervals(List<AwardInterval> min, List<AwardInterval> max) {
}
