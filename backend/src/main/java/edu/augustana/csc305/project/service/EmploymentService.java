package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.EmploymentSummaryDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmploymentService {
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;

    public EmploymentService(
            EmploymentRepository employmentRepository,
            JobRepository jobRepository) {
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
    }

    public List<EmploymentSummaryDto> getActiveEmploymentsForUser(ObjectId userId) {
        List<EmploymentSummaryDto> summaries = new ArrayList<>();

        for (Employment employment : employmentRepository.findByUserId(userId)) {
            if (!employment.isActive()) {
                continue;
            }

            Job job = jobRepository.findById(employment.getJobId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Employment refers to a job that does not exist"));

            summaries.add(new EmploymentSummaryDto(
                    employment.getId().toHexString(),
                    job.getId().toHexString(),
                    job.getTitle(),
                    job.getDepartment()
            ));
        }

        return summaries;
    }
}