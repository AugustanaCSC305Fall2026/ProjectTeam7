package edu.augustana.csc305.project.dto;

import java.util.List;

public record SupervisorAvailabilityDto(
        String userId,
        String employeeName,
        List<AvailabilityWindowDto> windows
) { }