package edu.augustana.csc305.project.dto;

public record UserJobApplicationDto(
        String applicationId,
        String jobId,
        String jobTitle,
        String department,
        String applicationStatement,
        String status
) {
}
