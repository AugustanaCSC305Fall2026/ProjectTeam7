package edu.augustana.csc305.project.dto;

public record ManagerJobDto(
        String id,
        String title,
        String department,
        String description,
        boolean acceptingApplications
) {
}