package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.ManagerEmployeeDto;
import edu.augustana.csc305.project.dto.ManagerJobDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagerService {
    private final JobRepository jobRepository;
    private final EmploymentRepository employmentRepository;
    private final UserRepository userRepository;

    public ManagerService(
            JobRepository jobRepository,
            EmploymentRepository employmentRepository,
            UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.employmentRepository = employmentRepository;
        this.userRepository = userRepository;
    }

    public ManagerJobDto getAssignedJob(ObjectId managerId) {
        Job job = findAssignedJob(managerId);

        return new ManagerJobDto(
                job.getId().toHexString(),
                job.getTitle(),
                job.getDepartment(),
                job.getDescription());
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
                    user.getName(),
                    user.getEmail()));
        }

        return employees;
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