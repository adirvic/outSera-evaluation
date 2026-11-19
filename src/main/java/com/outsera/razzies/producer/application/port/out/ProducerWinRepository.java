package com.outsera.razzies.producer.application.port.out;

import com.outsera.razzies.producer.domain.ProducerWin;

import java.util.List;

/** Outbound port: the current wins of every producer. */
public interface ProducerWinRepository {

    /** One entry per (producer, winning movie). */
    List<ProducerWin> findAllWins();
}
