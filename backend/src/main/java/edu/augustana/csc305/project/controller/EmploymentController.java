package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.EmploymentSummaryDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.EmploymentService;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employments")
public class EmploymentController {
    private final EmploymentService employmentService;

    public EmploymentController(EmploymentService employmentService) {
        this.employmentService = employmentService;
    }

    @GetMapping("/me")
    public List<EmploymentSummaryDto> getMyEmployments(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId userId = new ObjectId(authenticatedUser.id());
        return employmentService.getActiveEmploymentsForUser(userId);
    }
}