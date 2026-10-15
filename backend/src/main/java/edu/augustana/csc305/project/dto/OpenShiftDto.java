package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record OpenShiftDto(
        String id,
        String jobId,
        String jobTitle,
        Instant startAt,
        Instant endAt,
        String reason,
        boolean eligible
) {
}