package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.ManagerEmployeeDto;
import edu.augustana.csc305.project.dto.ManagerJobApplicationDto;
import edu.augustana.csc305.project.dto.ManagerJobDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.JobApplication;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobApplicationRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import edu.augustana.csc305.project.dto.ManagerAddEmployeeRequest;
import edu.augustana.csc305.project.dto.HoursEntryDto;
import edu.augustana.csc305.project.dto.ManagerEmployeeHoursDto;
import edu.augustana.csc305.project.dto.ManagerTimeEntryEditRequest;
import edu.augustana.csc305.project.dto.TimeEntryDto;
import edu.augustana.csc305.project.model.TimeEntry;
import edu.augustana.csc305.project.repository.TimeEntryRepository;
import edu.augustana.csc305.project.dto.ShiftDto;
import edu.augustana.csc305.project.repository.ShiftRepository;
import edu.augustana.csc305.project.dto.CreateShiftRequestDto;
import edu.augustana.csc305.project.dto.ManagerCreateShiftRequest;
import edu.augustana.csc305.project.dto.ManagerEmployeeOptionDto;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class ManagerService {
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final EmploymentRepository employmentRepository;
    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftService shiftService;

    public ManagerService(
            JobRepository jobRepository,
            JobApplicationRepository jobApplicationRepository,
            EmploymentRepository employmentRepository,
            UserRepository userRepository,
            TimeEntryRepository timeEntryRepository,
            ShiftRepository shiftRepository,
            ShiftService shiftService) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.employmentRepository = employmentRepository;
        this.userRepository = userRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.shiftRepository = shiftRepository;
        this.shiftService = shiftService;
    }

    public ManagerJobDto getAssignedJob(ObjectId managerId) {
        Job job = findAssignedJob(managerId);

        return new ManagerJobDto(
                job.getId().toHexString(),
                job.getTitle(),
                job.getDepartment(),
                job.getDescription(),
                job.isAcceptingApplications());
    }

    public void setAssignedJobApplications(
                ObjectId managerId,
                boolean acceptingApplications) {
        Job job = findAssignedJob(managerId);

        boolean updated = jobRepository.setAcceptingApplications(
                job.getId(),
                acceptingApplications);

        if (!updated) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MANAGER_JOB_NOT_FOUND");
        }
        }

    public List<ManagerJobApplicationDto> getJobApplications(ObjectId managerId) {
        Job job = findAssignedJob(managerId);

        return jobApplicationRepository.findByJobId(job.getId())
                .stream()
                .map(this::toManagerJobApplicationDto)
                .toList();
    }

    public ManagerJobApplicationDto reviewJobApplication(
            ObjectId managerId,
            String applicationIdValue,
            String targetStatus) {
        if (applicationIdValue == null || !ObjectId.isValid(applicationIdValue)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_APPLICATION_ID");
        }

        if (!JobApplication.STATUS_APPROVED.equals(targetStatus)
                && !JobApplication.STATUS_REJECTED.equals(targetStatus)
                && !JobApplication.STATUS_WAITLISTED.equals(targetStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_APPLICATION_DECISION");
        }

        Job job = findAssignedJob(managerId);
        ObjectId applicationId = new ObjectId(applicationIdValue);
        JobApplication application = jobApplicationRepository
                .findByIdAndJobId(applicationId, job.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND"));

        String currentStatus = application.getStatus();
        if (currentStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "APPLICATION_STATUS_TRANSITION_NOT_ALLOWED");
        }

        boolean pendingDecision = JobApplication.STATUS_PENDING.equals(currentStatus);
        boolean waitlistedDecision = JobApplication.STATUS_WAITLISTED.equals(currentStatus)
                && !JobApplication.STATUS_WAITLISTED.equals(targetStatus);

        if (!pendingDecision && !waitlistedDecision) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "APPLICATION_STATUS_TRANSITION_NOT_ALLOWED");
        }

        boolean updated = jobApplicationRepository.updateStatusIfCurrentStatusIn(
                applicationId,
                job.getId(),
                List.of(currentStatus),
                targetStatus);
        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "APPLICATION_ALREADY_REVIEWED");
        }

        application.setStatus(targetStatus);
        return toManagerJobApplicationDto(application);
    }

    private ManagerJobApplicationDto toManagerJobApplicationDto(
            JobApplication application) {
        User applicant = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "APPLICATION_APPLICANT_NOT_FOUND"));

        return new ManagerJobApplicationDto(
                application.getId().toHexString(),
                applicant.getId().toHexString(),
                applicant.getName(),
                applicant.getEmail(),
                application.getApplicationStatement(),
                application.getStatus());
    }

    public List<ManagerEmployeeDto> getEmployees(ObjectId managerId) {
        Job job = findAssignedJob(managerId);
        List<ManagerEmployeeDto> employees = new ArrayList<>();

        for (Employment employment :
                employmentRepository.findActiveByJobId(job.getId())) {
            User user = userRepository.findById(employment.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND"));

            employees.add(new ManagerEmployeeDto(
                    user.getId().toHexString(),
                    employment.getId().toHexString(),
                    user.getName(),
                    user.getEmail()));
        }

        return employees;
    }

    public List<ManagerEmployeeOptionDto> getEmployeeCandidates(
                ObjectId managerId) {
        Job job = findAssignedJob(managerId);

        List<ObjectId> activeUserIds = employmentRepository
                .findActiveByJobId(job.getId())
                .stream()
                .map(Employment::getUserId)
                .toList();

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles().contains("USER"))
                .filter(user -> !activeUserIds.contains(user.getId()))
                .map(user -> new ManagerEmployeeOptionDto(
                        user.getId().toHexString(),
                        user.getName(),
                        user.getEmail()))
                .toList();
        }

    public List<ManagerEmployeeHoursDto> getEmployeeHours(ObjectId managerId) {
        Job job = findAssignedJob(managerId);
        List<Employment> activeEmployments =
                employmentRepository.findActiveByJobId(job.getId());

        List<ObjectId> employmentIds = activeEmployments.stream()
                .map(Employment::getId)
                .toList();

        List<TimeEntry> timeEntries =
                timeEntryRepository.findByEmploymentIds(employmentIds);

        Map<ObjectId, List<TimeEntry>> entriesByEmployment = new HashMap<>();
        for (TimeEntry entry : timeEntries) {
            entriesByEmployment
                    .computeIfAbsent(entry.getEmploymentId(), key -> new ArrayList<>())
                    .add(entry);
        }

        List<ManagerEmployeeHoursDto> results = new ArrayList<>();

        for (Employment employment : activeEmployments) {
            User user = userRepository.findById(employment.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND"));

            List<HoursEntryDto> employeeEntries = entriesByEmployment
                    .getOrDefault(employment.getId(), List.of())
                    .stream()
                    .map(entry -> new HoursEntryDto(
                            entry.getId().toHexString(),
                            employment.getId().toHexString(),
                            job.getId().toHexString(),
                            job.getTitle(),
                            entry.getClockInAt(),
                            entry.getClockOutAt(),
                            entry.getBreakMinutes(),
                            entry.isOnBreak(),
                            entry.getWorkedMinutes()))
                    .toList();

            int totalMinutes = employeeEntries.stream()
                    .mapToInt(HoursEntryDto::workedMinutes)
                    .sum();

            results.add(new ManagerEmployeeHoursDto(
                    user.getId().toHexString(),
                    employment.getId().toHexString(),
                    user.getName(),
                    user.getEmail(),
                    employeeEntries,
                    totalMinutes));
        }

        return results;
    }

    public ManagerEmployeeDto addEmployee(
            ObjectId managerId,
            ManagerAddEmployeeRequest request) {
        Job job = findAssignedJob(managerId);

        if (request == null || request.userId() == null
                || !ObjectId.isValid(request.userId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_USER_ID");
        }

        if (request.payRateCents() == null || request.payRateCents() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "PAY_RATE_MUST_BE_POSITIVE");
        }

        User employee = userRepository.findById(new ObjectId(request.userId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        if (!employee.getRoles().contains("USER")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "USER_IS_NOT_AN_EMPLOYEE");
        }

        if (employmentRepository.hasActiveEmploymentForUserAndJob(
                employee.getId(), job.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "EMPLOYMENT_ALREADY_ACTIVE");
        }

        Employment employment = new Employment(
                new ObjectId(),
                employee.getId(),
                job.getId(),
                request.payRateCents(),
                new Date(),
                true);
        employmentRepository.storeEmployment(employment);

        return new ManagerEmployeeDto(
                employee.getId().toHexString(),
                employment.getId().toHexString(),
                employee.getName(),
                employee.getEmail());
    }

    public void removeEmployee(ObjectId managerId, String employmentIdValue) {
        Job job = findAssignedJob(managerId);

        if (employmentIdValue == null || !ObjectId.isValid(employmentIdValue)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_EMPLOYMENT_ID");
        }

        boolean deactivated = employmentRepository.deactivateActiveByIdAndJobId(
                new ObjectId(employmentIdValue), job.getId());

        if (!deactivated) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND_FOR_MANAGER");
        }
    }

    public List<ShiftDto> getEmployeeShifts(
            ObjectId managerId,
            Instant from,
            Instant to) {
        Job job = findAssignedJob(managerId);

        if (from == null || to == null || !to.isAfter(from)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_SHIFT_RANGE");
        }

        List<ObjectId> employmentIds = employmentRepository
                .findActiveByJobId(job.getId())
                .stream()
                .map(Employment::getId)
                .toList();

        return shiftRepository
                .findByEmploymentIdsAndRange(employmentIds, from, to)
                .stream()
                .map(shiftService::toDto)
                .toList();
    }

        public ShiftDto createEmployeeShift(
        ObjectId managerId,
        ManagerCreateShiftRequest request) {
    Job job = findAssignedJob(managerId);

    if (request == null
            || request.employmentId() == null
            || request.employmentId().isBlank()
            || !ObjectId.isValid(request.employmentId())) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "INVALID_EMPLOYMENT_ID");
    }

    ObjectId employmentId = new ObjectId(request.employmentId());
    Employment employment = employmentRepository.findById(employmentId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND"));

    if (!employment.isActive()) {
        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "INACTIVE_EMPLOYMENT");
    }

    if (!job.getId().equals(employment.getJobId())) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND_FOR_MANAGER");
    }

    CreateShiftRequestDto shiftRequest = new CreateShiftRequestDto(
            employmentId.toHexString(),
            job.getId().toHexString(),
            request.startAt(),
            request.endAt(),
            request.note());

    return shiftService.toDto(
            shiftService.createShift(shiftRequest, managerId));
}

    public TimeEntryDto editEmployeeTimeEntry(
            ObjectId managerId,
            String timeEntryIdValue,
            ManagerTimeEntryEditRequest request) {
        Job job = findAssignedJob(managerId);

        if (timeEntryIdValue == null || !ObjectId.isValid(timeEntryIdValue)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_TIME_ENTRY_ID");
        }

        if (request == null
                || request.clockInAt() == null
                || request.clockOutAt() == null
                || !request.clockOutAt().isAfter(request.clockInAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE");
        }

        ObjectId timeEntryId = new ObjectId(timeEntryIdValue);
        TimeEntry entry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "TIME_ENTRY_NOT_FOUND"));

        Employment employment = employmentRepository.findById(entry.getEmploymentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "EMPLOYMENT_NOT_FOUND"));

        if (!job.getId().equals(employment.getJobId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "TIME_ENTRY_NOT_FOUND");
        }

        if (entry.getClockOutAt() == null || entry.isOnBreak()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "TIME_ENTRY_NOT_CLOSED");
        }

        int workedMinutes = WorkedMinutesCalculator.calculate(
                request.clockInAt(),
                request.clockOutAt(),
                entry.getBreakMinutes());

        TimeEntry updatedEntry = timeEntryRepository.applyCorrection(
                        entry.getId(),
                        entry.getUserId(),
                        entry.getClockInAt(),
                        entry.getClockOutAt(),
                        request.clockInAt(),
                        request.clockOutAt(),
                        workedMinutes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "TIME_ENTRY_CHANGED"));

        return TimeEntryDto.from(updatedEntry);
    }

    private Job findAssignedJob(ObjectId managerId) {
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
}
