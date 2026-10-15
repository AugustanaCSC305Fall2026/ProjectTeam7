package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record CreateCorrectionRequest(
        String timeEntryId,
        Instant proposedClockInAt,
        Instant proposedClockOutAt,
        String reason
) {
}
