package edu.augustana.csc305.project.dto;

public record EmploymentSummaryDto(
        String employmentId,
        String jobId,
        String jobTitle,
        String department
) {
}