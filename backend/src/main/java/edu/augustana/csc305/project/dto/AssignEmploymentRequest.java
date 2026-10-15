package edu.augustana.csc305.project.dto;

public record AssignEmploymentRequest(
        String userId,
        String jobId,
        Integer payRateCents
) {
}
