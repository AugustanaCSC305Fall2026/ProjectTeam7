package edu.augustana.csc305.project.dto;

import edu.augustana.csc305.project.model.CorrectionStatus;

import java.time.Instant;

public record PendingCorrectionDto(
        String correctionRequestId,
        String employeeName,
        String employeeEmail,
        String jobTitle,
        String timeEntryId,
        Instant originalClockInAt,
        Instant originalClockOutAt,
        Instant proposedClockInAt,
        Instant proposedClockOutAt,
        String reason,
        CorrectionStatus status,
        Instant createdAt
) {
}