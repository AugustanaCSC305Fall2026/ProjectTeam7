package edu.augustana.csc305.project.dto;

import edu.augustana.csc305.project.model.ShiftStatus;

import java.time.Instant;

public record ShiftDto(
        String id,
        String jobTitle,
        Instant startAt,
        Instant endAt,
        ShiftStatus status,
        String note,
        boolean canOffer
) {
}
