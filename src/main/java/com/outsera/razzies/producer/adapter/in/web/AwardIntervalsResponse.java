package com.outsera.razzies.producer.adapter.in.web;

import com.outsera.razzies.producer.domain.AwardInterval;
import com.outsera.razzies.producer.domain.AwardIntervals;

import java.util.List;

/** Response body of {@code GET /api/v1/producers/award-intervals}, in the format required by the specification. */
public record AwardIntervalsResponse(List<Item> min, List<Item> max) {

    public record Item(String producer, int interval, int previousWin, int followingWin) {

        static Item from(AwardInterval interval) {
            return new Item(interval.producer(), interval.interval(), interval.previousWin(), interval.followingWin());
        }
    }

    static AwardIntervalsResponse from(AwardIntervals intervals) {
        return new AwardIntervalsResponse(
                intervals.min().stream().map(Item::from).toList(),
                intervals.max().stream().map(Item::from).toList());
    }
}
