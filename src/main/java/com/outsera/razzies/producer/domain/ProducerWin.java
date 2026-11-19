package com.outsera.razzies.producer.domain;

/** A producer credited on a winning movie of the given year. */
public record ProducerWin(String producer, int year) {
}
