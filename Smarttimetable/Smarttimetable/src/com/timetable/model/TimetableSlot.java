package com.timetable.model;

import java.time.LocalTime;

public class TimetableSlot {

    private final int index;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String label;

    public TimetableSlot(int index, LocalTime startTime, LocalTime endTime, String label) {
        this.index = index;
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
    }

    public int getIndex() {
        return index;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLabel() {
        return label;
    }
}
