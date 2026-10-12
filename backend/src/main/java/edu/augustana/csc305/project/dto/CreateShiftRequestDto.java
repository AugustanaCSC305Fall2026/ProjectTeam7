package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record CreateShiftRequestDto(
        String employmentId,
        String jobId,
        Instant startAt,
        Instant endAt,
        String note
) {
}