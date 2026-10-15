package edu.augustana.csc305.project.dto;

import edu.augustana.csc305.project.model.EntrySource;
import edu.augustana.csc305.project.model.TimeEntry;

import java.time.Instant;

public record TimeEntryDto(
        String id,
        String employmentId,
        String shiftId,
        Instant clockInAt,
        Instant clockOutAt,
        int breakMinutes,
        int workedMinutes,
        EntrySource source
) {
    public static TimeEntryDto from(TimeEntry entry) {
        return new TimeEntryDto(
                entry.getId().toHexString(),
                entry.getEmploymentId().toHexString(),
                entry.getShiftId() == null
                        ? null
                        : entry.getShiftId().toHexString(),
                entry.getClockInAt(),
                entry.getClockOutAt(),
                entry.getBreakMinutes(),
                entry.getWorkedMinutes(),
                entry.getSource()
        );
    }
}