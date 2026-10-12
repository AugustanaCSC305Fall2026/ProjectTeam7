package edu.augustana.csc305.project.dto;

import edu.augustana.csc305.project.model.CorrectionStatus;

import java.time.Instant;

public record CorrectionRequestDto(
        String id,
        String timeEntryId,
        String jobTitle,
        Instant originalClockInAt,
        Instant originalClockOutAt,
        Instant proposedClockInAt,
        Instant proposedClockOutAt,
        String reason,
        CorrectionStatus status,
        String reviewNote,
        Instant createdAt
) {
}
