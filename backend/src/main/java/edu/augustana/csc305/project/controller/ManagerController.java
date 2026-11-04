package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.ManagerJobDto;
import edu.augustana.csc305.project.dto.ManagerEmployeeDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.ManagerService;
import edu.augustana.csc305.project.dto.ManagerAddEmployeeRequest;
import edu.augustana.csc305.project.dto.ManagerEmployeeHoursDto;
import edu.augustana.csc305.project.dto.ManagerTimeEntryEditRequest;
import edu.augustana.csc305.project.dto.TimeEntryDto;
import edu.augustana.csc305.project.dto.ShiftDto;
import edu.augustana.csc305.project.dto.ManagerCreateShiftRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
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

    @GetMapping("/hours")
    public List<ManagerEmployeeHoursDto> getEmployeeHours(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployeeHours(managerId);
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerEmployeeDto addEmployee(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody ManagerAddEmployeeRequest request) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.addEmployee(managerId, request);
    }

    @DeleteMapping("/employees/{employmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployee(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String employmentId) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        managerService.removeEmployee(managerId, employmentId);
    }

    @PatchMapping("/time-entries/{timeEntryId}")
    public TimeEntryDto editEmployeeTimeEntry(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String timeEntryId,
            @RequestBody ManagerTimeEntryEditRequest request) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.editEmployeeTimeEntry(
                managerId, timeEntryId, request);
    }

    @GetMapping("/shifts")
    public List<ShiftDto> getEmployeeShifts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployeeShifts(managerId, from, to);
    }

    @PostMapping("/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftDto createEmployeeShift(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody ManagerCreateShiftRequest request) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.createEmployeeShift(managerId, request);
    }
}