package edu.augustana.csc305.project.dto;

import java.util.List;

public record AdminUserDto(
        String id,
        String firstName,
        String lastName,
        String email,
        List<String> roles
) {
}
