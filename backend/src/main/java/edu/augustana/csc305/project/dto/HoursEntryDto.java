package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record HoursEntryDto(
        String id,
        String employmentId,
        String jobId,
        String jobTitle,
        Instant clockInAt,
        Instant clockOutAt,
        int breakMinutes,
        boolean onBreak,
        int workedMinutes
) {
}