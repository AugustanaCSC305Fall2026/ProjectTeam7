package edu.augustana.csc305.project.dto;

import java.util.List;

/** DTO for user data. */
public record UserDto(String id, String firstName, String lastName, List<String> roles) { }
