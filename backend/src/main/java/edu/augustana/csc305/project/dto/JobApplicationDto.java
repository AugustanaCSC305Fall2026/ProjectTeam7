package edu.augustana.csc305.project.dto;

public record JobApplicationDto(
        String id,
        String jobId,
        String applicationStatement,
        String status
) {
}