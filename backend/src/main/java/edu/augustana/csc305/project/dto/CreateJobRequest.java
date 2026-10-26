package edu.augustana.csc305.project.dto;

public record CreateJobRequest(
        String title,
        String department,
        String description
){
}
