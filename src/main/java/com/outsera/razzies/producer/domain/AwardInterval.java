package com.outsera.razzies.producer.domain;

public record AwardInterval(String producer, int interval, int previousWin, int followingWin) {
}
