package edu.augustana.csc305.project.dto;

public record CreateManagerRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {
}
