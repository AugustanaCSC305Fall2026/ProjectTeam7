package edu.augustana.csc305.project.service;

import java.time.Duration;
import java.time.Instant;

public final class WorkedMinutesCalculator {
    private WorkedMinutesCalculator() {
    }

    public static int calculate(
            Instant clockInAt,
            Instant clockOutAt,
            int breakMinutes) {
        long elapsedMinutes = Duration.between(
                clockInAt, clockOutAt).toMinutes();

        return (int) Math.max(0, elapsedMinutes - breakMinutes);
    }
}