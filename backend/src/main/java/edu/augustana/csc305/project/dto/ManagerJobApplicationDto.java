package edu.augustana.csc305.project.dto;

public record ManagerJobApplicationDto(
        String applicationId,
        String applicantId,
        String applicantName,
        String applicantEmail,
        String applicationStatement,
        String status
) {
}
