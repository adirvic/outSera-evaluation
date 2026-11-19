package com.outsera.razzies.producer.application;

import com.outsera.razzies.producer.application.port.in.GetAwardIntervalsUseCase;
import com.outsera.razzies.producer.application.port.out.ProducerWinRepository;
import com.outsera.razzies.producer.domain.AwardIntervalCalculator;
import com.outsera.razzies.producer.domain.AwardIntervals;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Always computed over the current state of the database, since movies can be changed through the API. */
@Service
@Transactional(readOnly = true)
public class AwardIntervalService implements GetAwardIntervalsUseCase {

    private final ProducerWinRepository wins;

    public AwardIntervalService(ProducerWinRepository wins) {
        this.wins = wins;
    }

    @Override
    public AwardIntervals awardIntervals() {
        return AwardIntervalCalculator.calculate(wins.findAllWins());
    }
}
