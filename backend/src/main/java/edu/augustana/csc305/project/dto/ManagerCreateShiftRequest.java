package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record ManagerCreateShiftRequest(
        String employmentId,
        Instant startAt,
        Instant endAt,
        String note
) {
}