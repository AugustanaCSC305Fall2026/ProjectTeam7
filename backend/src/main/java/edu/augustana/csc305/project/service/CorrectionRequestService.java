package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.CorrectionRequestDto;
import edu.augustana.csc305.project.dto.CreateCorrectionRequest;
import edu.augustana.csc305.project.model.CorrectionRequest;
import edu.augustana.csc305.project.model.CorrectionStatus;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.TimeEntry;
import edu.augustana.csc305.project.repository.CorrectionRequestRepository;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.TimeEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CorrectionRequestService {
    private final TimeEntryRepository timeEntryRepository;
    private final CorrectionRequestRepository correctionRequestRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;

    public CorrectionRequestService(TimeEntryRepository timeEntryRepository,
                                    CorrectionRequestRepository correctionRequestRepository,
                                    EmploymentRepository employmentRepository,
                                    JobRepository jobRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.correctionRequestRepository = correctionRequestRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
    }

    public CorrectionRequestDto submit(ObjectId userId, CreateCorrectionRequest input) {
        ObjectId timeEntryId = parseObjectId(input.timeEntryId(), "timeEntryId");
        TimeEntry entry = timeEntryRepository.findByIdAndUserId(timeEntryId, userId)
                .orElseThrow(() -> new CorrectionException("NOT_YOUR_ENTRY"));

        if (entry.getClockOutAt() == null) {
            throw new CorrectionException("ENTRY_STILL_OPEN");
        }
        if (input.proposedClockInAt() == null || input.proposedClockOutAt() == null
                || !input.proposedClockOutAt().isAfter(input.proposedClockInAt())) {
            throw new CorrectionException("INVALID_RANGE");
        }
        if (input.reason() == null || input.reason().isBlank()) {
            throw new CorrectionException("INVALID_REASON");
        }
        if (correctionRequestRepository.hasPendingRequest(timeEntryId, userId)) {
            throw new CorrectionException("DUPLICATE_PENDING_REQUEST");
        }

        String jobTitle = employmentRepository.findById(entry.getEmploymentId())
                .map(Employment::getJobId)
                .flatMap(jobRepository::findById)
                .map(Job::getTitle)
                .orElseThrow(() -> new IllegalStateException(
                        "Time entry refers to a job that does not exist"));

        CorrectionRequest request = new CorrectionRequest();
        request.setId(new ObjectId());
        request.setUserId(userId);
        request.setTimeEntryId(timeEntryId);
        request.setOriginalClockInAt(entry.getClockInAt());
        request.setOriginalClockOutAt(entry.getClockOutAt());
        request.setProposedClockInAt(input.proposedClockInAt());
        request.setProposedClockOutAt(input.proposedClockOutAt());
        request.setReason(input.reason());
        request.setStatus(CorrectionStatus.PENDING);
        request.setCreatedAt(Instant.now());
        correctionRequestRepository.store(request);

        return toDto(request, jobTitle);
    }

    private ObjectId parseObjectId(String value, String field) {
        try {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException();
            }
            return new ObjectId(value);
        } catch (IllegalArgumentException exception) {
            throw new CorrectionException("INVALID_" + field.toUpperCase());
        }
    }

    private CorrectionRequestDto toDto(CorrectionRequest request, String jobTitle) {
        return new CorrectionRequestDto(
                request.getId().toHexString(),
                request.getTimeEntryId().toHexString(),
                jobTitle,
                request.getOriginalClockInAt(),
                request.getOriginalClockOutAt(),
                request.getProposedClockInAt(),
                request.getProposedClockOutAt(),
                request.getReason(),
                request.getStatus(),
                request.getReviewNote(),
                request.getCreatedAt()
        );
    }

    public static class CorrectionException extends RuntimeException {
        private final String code;

        public CorrectionException(String code) {
            this.code = code;
        }

        public String getCode() { return code; }
    }
}
