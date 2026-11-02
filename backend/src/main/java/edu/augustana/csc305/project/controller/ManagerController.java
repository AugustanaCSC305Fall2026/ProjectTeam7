package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.ManagerJobDto;
import edu.augustana.csc305.project.dto.ManagerEmployeeDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.ManagerService;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {
    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/job")
    public ManagerJobDto getAssignedJob(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getAssignedJob(managerId);
    }

    @GetMapping("/employees")
    public List<ManagerEmployeeDto> getEmployees(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployees(managerId);
    }
}