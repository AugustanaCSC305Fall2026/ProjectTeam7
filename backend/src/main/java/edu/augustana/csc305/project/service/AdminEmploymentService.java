package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.AdminJobDto;
import edu.augustana.csc305.project.dto.AdminUserDto;
import edu.augustana.csc305.project.dto.AssignEmploymentRequest;
import edu.augustana.csc305.project.dto.AssignManagerRequest;
import edu.augustana.csc305.project.dto.CreateJobRequest;
import edu.augustana.csc305.project.dto.CreateManagerRequest;
import edu.augustana.csc305.project.dto.EmploymentAssignmentDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;

@Service
public class AdminEmploymentService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmploymentRepository employmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminEmploymentService(UserRepository userRepository,
                                  JobRepository jobRepository,
                                  EmploymentRepository employmentRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.employmentRepository = employmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getId().toHexString(),
                        displayFirstName(user),
                        displayLastName(user),
                        user.getEmail(),
                        user.getRoles()))
                .toList();
    }

    public List<AdminJobDto> getJobs() {
        return jobRepository.findAll().stream()
                .map(job -> new AdminJobDto(
                        job.getId().toHexString(),
                        job.getTitle(),
                        job.getDepartment(),
                        job.getDescription(),
                        job.getManagerId() == null
                                ? null
                                : job.getManagerId().toHexString()))
                .toList();
    }
    public AdminJobDto createJob(CreateJobRequest request) {
        Job job = new Job(
                null,
                request.title(),
                request.department(),
                request.description()
        );

        jobRepository.storeJob(job);

        return new AdminJobDto(
                job.getId().toHexString(),
                job.getTitle(),
                job.getDepartment(),
                job.getDescription(),
                null
        );
    }

    public AdminUserDto createManager(CreateManagerRequest request) {
        String firstName = clean(request.firstName());
        String lastName = clean(request.lastName());
        String email = clean(request.email()).toLowerCase();

        if (firstName.isBlank() || lastName.isBlank()
                || !email.contains("@") || request.password() == null
                || request.password().length() < 8) {
            throw new AdminEmploymentException("INVALID_MANAGER_DATA", 400);
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new AdminEmploymentException("EMAIL_ALREADY_REGISTERED", 409);
        }

        User manager = new User(
                null,
                firstName,
                lastName,
                email,
                passwordEncoder.encode(request.password()),
                List.of("MANAGER"));
        userRepository.storeUser(manager);

        return new AdminUserDto(
                manager.getId().toHexString(),
                manager.getFirstName(),
                manager.getLastName(),
                manager.getEmail(),
                manager.getRoles());
    }

    public AdminJobDto assignManager(AssignManagerRequest request) {
        ObjectId jobId = parseId(request.jobId(), "jobId");
        ObjectId managerId = parseId(request.managerId(), "managerId");

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AdminEmploymentException("JOB_NOT_FOUND", 404));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new AdminEmploymentException("MANAGER_NOT_FOUND", 404));

        if (!manager.getRoles().contains("MANAGER")) {
            throw new AdminEmploymentException("USER_IS_NOT_MANAGER", 400);
        }

        boolean assignedToAnotherJob = jobRepository.findByManagerId(manager.getId()).stream()
        .anyMatch(assignedJob -> !assignedJob.getId().equals(job.getId()));

        if (assignedToAnotherJob) {
            throw new AdminEmploymentException("MANAGER_ALREADY_ASSIGNED", 409);
        }

        jobRepository.assignManager(job.getId(), manager.getId());
        job.setManagerId(manager.getId());

        return new AdminJobDto(
                job.getId().toHexString(),
                job.getTitle(),
                job.getDepartment(),
                job.getDescription(),
                job.getManagerId().toHexString());
    }

    public EmploymentAssignmentDto assign(AssignEmploymentRequest request) {
        ObjectId userId = parseId(request.userId(), "userId");
        ObjectId jobId = parseId(request.jobId(), "jobId");

        if (request.payRateCents() == null || request.payRateCents() <= 0) {
            throw new AdminEmploymentException("PAY_RATE_MUST_BE_POSITIVE", 400);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AdminEmploymentException("USER_NOT_FOUND", 404));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AdminEmploymentException("JOB_NOT_FOUND", 404));

        boolean alreadyAssigned = employmentRepository.findByUserId(user.getId()).stream()
                .anyMatch(employment -> employment.isActive()
                        && employment.getJobId().equals(job.getId()));
        if (alreadyAssigned) {
            throw new AdminEmploymentException("EMPLOYMENT_ALREADY_ACTIVE", 409);
        }

        Employment employment = new Employment(
                null, user.getId(), job.getId(), request.payRateCents(), new Date(), true);
        employmentRepository.storeEmployment(employment);
        return toDto(employment);
    }

    private ObjectId parseId(String value, String field) {
        if (value == null || !ObjectId.isValid(value)) {
            throw new AdminEmploymentException(field.toUpperCase() + "_MUST_BE_VALID", 400);
        }
        return new ObjectId(value);
    }

    private EmploymentAssignmentDto toDto(Employment employment) {
        return new EmploymentAssignmentDto(
                employment.getId().toHexString(),
                employment.getUserId().toHexString(),
                employment.getJobId().toHexString(),
                employment.getPayRateCents(),
                employment.getStartDate(),
                employment.isActive());
    }

    private String displayFirstName(User user) {
        return user.getFirstName() != null ? user.getFirstName() : splitName(user, 0);
    }

    private String displayLastName(User user) {
        return user.getLastName() != null ? user.getLastName() : splitName(user, 1);
    }

    private String splitName(User user, int index) {
        String[] parts = user.getName() == null ? new String[0]
                : user.getName().trim().split("\\s+", 2);
        return parts.length > index ? parts[index] : "";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public static class AdminEmploymentException extends RuntimeException {
        private final int status;

        public AdminEmploymentException(String code, int status) {
            super(code);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
