package com.magicworld.tfg_angular_springboot.employee;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum BreakGroup {
    A(LocalTime.of(12, 0), LocalTime.of(12, 30)),
    B(LocalTime.of(12, 30), LocalTime.of(13, 0)),
    C(LocalTime.of(13, 0), LocalTime.of(13, 30)),
    D(LocalTime.of(13, 30), LocalTime.of(14, 0));

    private final LocalTime startTime;
    private final LocalTime endTime;

    BreakGroup(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

}

