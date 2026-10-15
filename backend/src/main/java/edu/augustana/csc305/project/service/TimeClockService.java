package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.ClockInRequest;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.EntrySource;
import edu.augustana.csc305.project.model.Shift;
import edu.augustana.csc305.project.model.ShiftStatus;
import edu.augustana.csc305.project.model.TimeEntry;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.ShiftRepository;
import edu.augustana.csc305.project.repository.TimeEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
public class TimeClockService {

    private final TimeEntryRepository timeEntryRepository;
    private final EmploymentRepository employmentRepository;
    private final ShiftRepository shiftRepository;

    public TimeClockService(
            TimeEntryRepository timeEntryRepository,
            EmploymentRepository employmentRepository,
            ShiftRepository shiftRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.employmentRepository = employmentRepository;
        this.shiftRepository = shiftRepository;
    }

    public TimeEntry clockIn(ObjectId userId, ClockInRequest request) {
        if (request == null
                || request.employmentId() == null
                || !ObjectId.isValid(request.employmentId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_EMPLOYMENT_ID");
        }

        ObjectId employmentId = new ObjectId(request.employmentId());

        Employment employment = employmentRepository
                .findByIdAndUserId(employmentId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND"));

        if (!employment.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "EMPLOYMENT_INACTIVE");
        }

        ObjectId shiftId = null;
        if (request.shiftId() != null && !request.shiftId().isBlank()) {
            if (!ObjectId.isValid(request.shiftId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "INVALID_SHIFT_ID");
            }

            shiftId = new ObjectId(request.shiftId());
            Shift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "SHIFT_NOT_FOUND"));

            if (!employmentId.equals(shift.getEmploymentId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "SHIFT_EMPLOYMENT_MISMATCH");
            }

            if (shift.getStatus() != ShiftStatus.SCHEDULED) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "SHIFT_NOT_SCHEDULED");
            }
        }

        if (timeEntryRepository.findOpenByUserId(userId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ALREADY_CLOCKED_IN");
        }

        TimeEntry entry = new TimeEntry();
        entry.setId(new ObjectId());
        entry.setUserId(userId);
        entry.setEmploymentId(employmentId);
        entry.setShiftId(shiftId);
        entry.setClockInAt(Instant.now());
        entry.setClockOutAt(null);
        entry.setSource(EntrySource.WEB);
        entry.setBreakMinutes(0);
        entry.setWorkedMinutes(0);

        timeEntryRepository.storeTimeEntry(entry);
        return entry;
    }

    public TimeEntry clockOut(ObjectId userId) {
        TimeEntry openEntry = timeEntryRepository.findOpenByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "NOT_CLOCKED_IN"));

        Instant clockOutAt = Instant.now();
        long elapsedMinutes = Duration.between(
                openEntry.getClockInAt(), clockOutAt).toMinutes();
        int workedMinutes = (int) Math.max(
                0, elapsedMinutes - openEntry.getBreakMinutes());

        return timeEntryRepository.clockOutOpenEntry(
                        openEntry.getId(), userId, clockOutAt, workedMinutes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "TIME_ENTRY_ALREADY_CLOSED"));
    }
}