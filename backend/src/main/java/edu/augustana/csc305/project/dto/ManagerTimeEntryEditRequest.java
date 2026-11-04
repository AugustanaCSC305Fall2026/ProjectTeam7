package edu.augustana.csc305.project.dto;

import java.time.Instant;

public record ManagerTimeEntryEditRequest(
        Instant clockInAt,
        Instant clockOutAt
) {
}