package com.outsera.razzies.producer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Computes, for every producer with two or more wins, the interval between each pair of
 * <em>consecutive</em> wins, and returns every interval tied for the global minimum and maximum.
 * <p>
 * Runs in O(n log n) over the number of wins. Results are deterministic: ordered by producer
 * name and then by year. Two wins in the same year yield an interval of {@code 0}.
 */
@Component
public class AwardIntervalCalculator {

    public AwardIntervals calculate(Collection<ProducerWin> wins) {
        var winYearsByProducer = new TreeMap<String, List<Integer>>();
        for (ProducerWin win : wins) {
            winYearsByProducer.computeIfAbsent(win.producer(), key -> new ArrayList<>()).add(win.year());
        }

        var min = new ArrayList<AwardInterval>();
        var max = new ArrayList<AwardInterval>();
        int minInterval = Integer.MAX_VALUE;
        int maxInterval = Integer.MIN_VALUE;

        for (Map.Entry<String, List<Integer>> entry : winYearsByProducer.entrySet()) {
            List<Integer> years = entry.getValue();
            years.sort(null);
            for (int i = 1; i < years.size(); i++) {
                var interval = new AwardInterval(entry.getKey(), years.get(i) - years.get(i - 1),
                        years.get(i - 1), years.get(i));

                if (interval.interval() < minInterval) {
                    minInterval = interval.interval();
                    min.clear();
                }
                if (interval.interval() == minInterval) {
                    min.add(interval);
                }

                if (interval.interval() > maxInterval) {
                    maxInterval = interval.interval();
                    max.clear();
                }
                if (interval.interval() == maxInterval) {
                    max.add(interval);
                }
            }
        }
        return new AwardIntervals(List.copyOf(min), List.copyOf(max));
    }
}
