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
import edu.augustana.csc305.project.repository.UserRepository;
import edu.augustana.csc305.project.dto.PendingCorrectionDto;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.dto.ReviewCorrectionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Instant;

@Service
public class CorrectionRequestService {
    private final TimeEntryRepository timeEntryRepository;
    private final CorrectionRequestRepository correctionRequestRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public CorrectionRequestService(TimeEntryRepository timeEntryRepository,
                                    CorrectionRequestRepository correctionRequestRepository,
                                    EmploymentRepository employmentRepository,
                                    JobRepository jobRepository, UserRepository userRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.correctionRequestRepository = correctionRequestRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public List<PendingCorrectionDto> getPendingCorrections() {
        return correctionRequestRepository.findPending()
                .stream()
                .map(this::toPendingCorrectionDto)
                .toList();
    }

    public List<PendingCorrectionDto> getPendingCorrectionsForManager(
        ObjectId managerId) {

        Job managerJob = findManagerJob(managerId);

        return correctionRequestRepository.findPending()
                .stream()
                .filter(request -> {
                        TimeEntry entry = timeEntryRepository
                                .findByIdAndUserId(
                                        request.getTimeEntryId(),
                                        request.getUserId())
                                .orElse(null);

                        if (entry == null) {
                        return false;
                        }

                        return employmentRepository
                                .findById(entry.getEmploymentId())
                                .map(employment ->
                                        managerJob.getId().equals(employment.getJobId()))
                                .orElse(false);
                })
                .map(this::toPendingCorrectionDto)
                .toList();
        }
        private Job findManagerJob(ObjectId managerId) {
                List<Job> jobs = jobRepository.findByManagerId(managerId);

                if (jobs.isEmpty()) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "MANAGER_JOB_NOT_FOUND");
                }

                if (jobs.size() > 1) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT, "MANAGER_HAS_MULTIPLE_JOBS");
                }

                return jobs.get(0);
        }

        public CorrectionRequestDto approveForManager(
                ObjectId managerId,
                String requestId,
                ReviewCorrectionRequest reviewRequest) {
                        verifyManagerCorrectionAccess(managerId, requestId);
                        return approve(requestId, reviewRequest);
        }

        public CorrectionRequestDto rejectForManager(
                ObjectId managerId,
                String requestId,
                ReviewCorrectionRequest reviewRequest) {
                        verifyManagerCorrectionAccess(managerId, requestId);
                        return reject(requestId, reviewRequest);
        }

        private void verifyManagerCorrectionAccess(
                ObjectId managerId,
                String requestId) {

                Job managerJob = findManagerJob(managerId);

                ObjectId correctionId =
                        parseObjectId(requestId, "correctionRequestId");

                CorrectionRequest request = correctionRequestRepository
                        .findPendingById(correctionId)
                        .orElseThrow(() -> new CorrectionException(
                                "CORRECTION_NOT_PENDING"));

                TimeEntry entry = timeEntryRepository
                        .findByIdAndUserId(
                                request.getTimeEntryId(),
                                request.getUserId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "TIME_ENTRY_NOT_FOUND"));

                Employment employment = employmentRepository
                        .findById(entry.getEmploymentId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND"));

                if (!managerJob.getId().equals(employment.getJobId())) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "CORRECTION_NOT_FOUND");
                }
        }

        public CorrectionRequestDto approve(
            String requestId,
            ReviewCorrectionRequest reviewRequest) {
                return review(requestId, CorrectionStatus.APPROVED, reviewRequest);
        }

    public CorrectionRequestDto reject(
            String requestId,
            ReviewCorrectionRequest reviewRequest) {
                return review(requestId, CorrectionStatus.REJECTED, reviewRequest);
        }

    private CorrectionRequestDto review(
            String requestId,
            CorrectionStatus newStatus,
            ReviewCorrectionRequest reviewRequest) {

        ObjectId correctionRequestId =
                parseObjectId(requestId, "correctionRequestId");

        CorrectionRequest request = correctionRequestRepository
                .findPendingById(correctionRequestId)
                .orElseThrow(() -> new CorrectionException(
                        "CORRECTION_NOT_PENDING"));

        TimeEntry entry = timeEntryRepository
                .findByIdAndUserId(request.getTimeEntryId(), request.getUserId())
                .orElseThrow(() -> new IllegalStateException(
                        "Correction request refers to a missing time entry"));

        String reviewNote =
                reviewRequest == null ? null : reviewRequest.reviewNote();
        
        Instant approvedClockInAt = null;
        Instant approvedClockOutAt = null;
        if (newStatus == CorrectionStatus.APPROVED) {
            approvedClockInAt =
                    reviewRequest == null ? null : reviewRequest.approvedClockInAt();
            approvedClockOutAt =
                    reviewRequest == null ? null : reviewRequest.approvedClockOutAt();

            if (approvedClockInAt == null
                    || approvedClockOutAt == null
                    || !approvedClockOutAt.isAfter(approvedClockInAt)) {
                throw new CorrectionException("INVALID_APPROVED_RANGE");
            }

            int workedMinutes = WorkedMinutesCalculator.calculate(
                approvedClockInAt,
                approvedClockOutAt,
                entry.getBreakMinutes());


            entry = timeEntryRepository.applyCorrection(
                            request.getTimeEntryId(),
                            request.getUserId(),
                            request.getOriginalClockInAt(),
                            request.getOriginalClockOutAt(),
                            approvedClockInAt,
                            approvedClockOutAt,
                            workedMinutes)
                    .orElseThrow(() -> new CorrectionException(
                            "TIME_ENTRY_CHANGED"));
        }

        CorrectionRequest reviewedRequest = correctionRequestRepository
        .updatePendingStatus(
                correctionRequestId,
                newStatus,
                reviewNote,
                approvedClockInAt,
                approvedClockOutAt)
        .orElseThrow(() -> new CorrectionException(
                "CORRECTION_NOT_PENDING"));

        String jobTitle = employmentRepository.findById(entry.getEmploymentId())
                .map(Employment::getJobId)
                .flatMap(jobRepository::findById)
                .map(Job::getTitle)
                .orElseThrow(() -> new IllegalStateException(
                        "Time entry refers to a job that does not exist"));

        return toDto(reviewedRequest, jobTitle);
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

    private PendingCorrectionDto toPendingCorrectionDto(
        CorrectionRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Correction request refers to a missing user"
                        ));

        TimeEntry entry = timeEntryRepository
                .findByIdAndUserId(
                        request.getTimeEntryId(),
                        request.getUserId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Correction request refers to a missing time entry"
                        ));

        String jobTitle = employmentRepository
                .findById(entry.getEmploymentId())
                .map(Employment::getJobId)
                .flatMap(jobRepository::findById)
                .map(Job::getTitle)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Time entry refers to a missing job"
                        ));

        String employeeName = user.getName();
        if (user.getFirstName() != null || user.getLastName() != null) {
            employeeName = (
                    (user.getFirstName() == null ? "" : user.getFirstName())
                            + " "
                            + (user.getLastName() == null ? "" : user.getLastName())
            ).trim();
        }

        return new PendingCorrectionDto(
                request.getId().toHexString(),
                employeeName,
                user.getEmail(),
                jobTitle,
                request.getTimeEntryId().toHexString(),
                request.getOriginalClockInAt(),
                request.getOriginalClockOutAt(),
                request.getProposedClockInAt(),
                request.getProposedClockOutAt(),
                request.getReason(),
                request.getStatus(),
                request.getCreatedAt()
        );
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
                request.getApprovedClockInAt(),
                request.getApprovedClockOutAt(),
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
