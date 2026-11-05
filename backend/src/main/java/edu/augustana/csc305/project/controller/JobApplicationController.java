package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.AvailableJobDto;
import edu.augustana.csc305.project.dto.JobApplicationDto;
import edu.augustana.csc305.project.dto.SubmitJobApplicationRequest;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.JobApplicationService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    public JobApplicationController(
            JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping("/open")
    public List<AvailableJobDto> getAvailableJobs() {
        return jobApplicationService.getAvailableJobs();
    }

    @PostMapping("/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationDto applyToJob(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String jobId,
            @RequestBody SubmitJobApplicationRequest request) {
        ObjectId userId = new ObjectId(authenticatedUser.id());
        return jobApplicationService.applyToJob(userId, jobId, request);
    }
}