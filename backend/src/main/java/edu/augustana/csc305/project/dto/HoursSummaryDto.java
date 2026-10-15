package edu.augustana.csc305.project.dto;

import java.util.List;

public record HoursSummaryDto(
        List<HoursEntryDto> entries,
        int totalMinutes
) {
}