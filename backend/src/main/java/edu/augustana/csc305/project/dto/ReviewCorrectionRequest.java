package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record ReviewCorrectionRequest(
        String reviewNote,
        Instant approvedClockInAt,
        Instant approvedClockOutAt
) {
}