package com.outsera.razzies.producer.application.port.in;

import com.outsera.razzies.producer.domain.AwardIntervals;

public interface GetAwardIntervalsUseCase {

    /** Producers with the shortest and the longest interval between two consecutive wins, ties included. */
    AwardIntervals awardIntervals();
}
