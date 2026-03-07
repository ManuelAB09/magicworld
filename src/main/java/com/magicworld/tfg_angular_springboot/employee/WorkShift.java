package com.magicworld.tfg_angular_springboot.employee;

import java.time.LocalTime;

public enum WorkShift {
    MORNING(LocalTime.of(8, 0), LocalTime.of(16, 0)),
    AFTERNOON(LocalTime.of(14, 0), LocalTime.of(22, 0)),
    FULL_DAY(LocalTime.of(9, 0), LocalTime.of(17, 0));

    private final LocalTime startTime;
    private final LocalTime endTime;

    WorkShift(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}

