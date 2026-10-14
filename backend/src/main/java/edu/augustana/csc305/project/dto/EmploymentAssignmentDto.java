package edu.augustana.csc305.project.dto;

import java.util.Date;

public record EmploymentAssignmentDto(
        String employmentId,
        String userId,
        String jobId,
        int payRateCents,
        Date startDate,
        boolean active
) {
}
