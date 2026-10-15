package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.AdminJobDto;
import edu.augustana.csc305.project.dto.AdminUserDto;
import edu.augustana.csc305.project.dto.AssignEmploymentRequest;
import edu.augustana.csc305.project.dto.EmploymentAssignmentDto;
import edu.augustana.csc305.project.service.AdminEmploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminEmploymentController {
    private final AdminEmploymentService adminEmploymentService;

    public AdminEmploymentController(AdminEmploymentService adminEmploymentService) {
        this.adminEmploymentService = adminEmploymentService;
    }

    @GetMapping("/users")
    public List<AdminUserDto> getUsers() {
        return adminEmploymentService.getUsers();
    }

    @GetMapping("/jobs")
    public List<AdminJobDto> getJobs() {
        return adminEmploymentService.getJobs();
    }

    @PostMapping("/employments")
    public ResponseEntity<EmploymentAssignmentDto> assignEmployment(
            @RequestBody AssignEmploymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminEmploymentService.assign(request));
    }

    @ExceptionHandler(AdminEmploymentService.AdminEmploymentException.class)
    public ResponseEntity<ApiError> handleAdminEmploymentError(
            AdminEmploymentService.AdminEmploymentException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiError(exception.getMessage(), exception.getMessage()));
    }

    public record ApiError(String code, String message) {
    }
}
