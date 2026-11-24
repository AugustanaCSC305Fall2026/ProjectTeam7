package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.AvailableJobDto;
import edu.augustana.csc305.project.dto.JobApplicationDto;
import edu.augustana.csc305.project.dto.SubmitJobApplicationRequest;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.JobApplication;
import edu.augustana.csc305.project.repository.JobApplicationRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JobApplicationService {
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobApplicationService(
            JobRepository jobRepository,
            JobApplicationRepository jobApplicationRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public List<AvailableJobDto> getAvailableJobs() {
        return jobRepository.findAcceptingApplications()
                .stream()
                .map(job -> new AvailableJobDto(
                        job.getId().toHexString(),
                        job.getTitle(),
                        job.getDepartment(),
                        job.getDescription()))
                .toList();
    }

    public JobApplicationDto applyToJob(
            ObjectId userId,
            String jobIdValue,
            SubmitJobApplicationRequest request) {
        if (jobIdValue == null || !ObjectId.isValid(jobIdValue)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "INVALID_JOB_ID");
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "APPLICATION_REQUEST_REQUIRED");
        }

        ObjectId jobId = new ObjectId(jobIdValue);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "JOB_NOT_FOUND"));

        if (!job.isAcceptingApplications()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "APPLICATIONS_CLOSED");
        }

        if (jobApplicationRepository
                .findByUserIdAndJobId(userId, jobId)
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "ALREADY_APPLIED");
        }

        String applicationStatement =
                request.applicationStatement() == null
                        ? ""
                        : request.applicationStatement().trim();

        JobApplication application = new JobApplication(
                userId,
                jobId,
                applicationStatement);
        try {
            jobApplicationRepository.storeApplication(application);
        } catch (MongoWriteException exception) {
            if (exception.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "ALREADY_APPLIED");
            }
            throw exception;
        }

        return new JobApplicationDto(
                application.getId().toHexString(),
                jobId.toHexString(),
                application.getApplicationStatement(),
                application.getStatus());
    }
}
