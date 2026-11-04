package edu.augustana.csc305.project.dto;

public record ManagerAddEmployeeRequest(
        String userId,
        Integer payRateCents
) {
}