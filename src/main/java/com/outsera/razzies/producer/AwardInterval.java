package com.outsera.razzies.producer;

public record AwardInterval(String producer, int interval, int previousWin, int followingWin) {
}
