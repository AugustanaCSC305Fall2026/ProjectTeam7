package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.AdminJobDto;
import edu.augustana.csc305.project.dto.AdminUserDto;
import edu.augustana.csc305.project.dto.AssignEmploymentRequest;
import edu.augustana.csc305.project.dto.CreateJobRequest;
import edu.augustana.csc305.project.dto.EmploymentAssignmentDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AdminEmploymentService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmploymentRepository employmentRepository;

    public AdminEmploymentService(UserRepository userRepository,
                                  JobRepository jobRepository,
                                  EmploymentRepository employmentRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.employmentRepository = employmentRepository;
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
                        job.getDescription()))
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
                job.getDescription()
        );
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
