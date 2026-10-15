package edu.augustana.csc305.project.dto;

public record AvailabilityWindowDto(
        String dayOfWeek,
        String startTime,
        String endTime
) { }
