package edu.augustana.csc305.project.dto;

import java.util.List;

public record ManagerEmployeeHoursDto(
        String employeeId,
        String employmentId,
        String employeeName,
        String email,
        List<HoursEntryDto> entries,
        int totalMinutes
) {
}