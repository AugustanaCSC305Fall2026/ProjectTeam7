package edu.augustana.csc305.project.dto;

import edu.augustana.csc305.project.model.Shift;
import edu.augustana.csc305.project.model.ShiftOffer;
import edu.augustana.csc305.project.model.ShiftStatus;

import java.time.Instant;

public record ShiftDto(
        String id,
        String employmentId,
        String jobId,
        String jobTitle,
        Instant startAt,
        Instant endAt,
        ShiftStatus status,
        String note,
        ShiftOffer offer,
        String createdByUserId,
        boolean canOffer
) {

    public static ShiftDto fromShift(Shift shift) {
        return new ShiftDto(
                shift.getId() == null ? null : shift.getId().toHexString(),
                shift.getEmploymentId() == null
                        ? null
                        : shift.getEmploymentId().toHexString(),
                shift.getJobId() == null
                        ? null
                        : shift.getJobId().toHexString(),
                null,
                shift.getStartAt(),
                shift.getEndAt(),
                shift.getStatus(),
                shift.getNote(),
                shift.getOffer(),
                shift.getCreatedByUserId() == null
                        ? null
                        : shift.getCreatedByUserId().toHexString(),
                shift.getStartAt() != null
                        && shift.getStartAt().isAfter(Instant.now())
        );
    }
}