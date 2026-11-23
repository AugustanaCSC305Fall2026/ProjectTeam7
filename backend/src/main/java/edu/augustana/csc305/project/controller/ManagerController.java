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
import edu.augustana.csc305.project.dto.ManagerConversationDto;
import edu.augustana.csc305.project.dto.CreateGroupConversationRequest;
import edu.augustana.csc305.project.dto.GroupConversationDto;
import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartManagerConversationRequest;
import edu.augustana.csc305.project.service.ManagerMessagingService;
import edu.augustana.csc305.project.dto.ManagerJobApplicationStatusRequest;
import edu.augustana.csc305.project.dto.ManagerJobApplicationDto;
import edu.augustana.csc305.project.dto.ManagerReviewJobApplicationRequest;
import edu.augustana.csc305.project.dto.ManagerEmployeeOptionDto;
import edu.augustana.csc305.project.service.CorrectionRequestService;
import edu.augustana.csc305.project.dto.CorrectionRequestDto;
import edu.augustana.csc305.project.dto.PendingCorrectionDto;
import edu.augustana.csc305.project.dto.ReviewCorrectionRequest;
import org.springframework.format.annotation.DateTimeFormat;
import edu.augustana.csc305.project.dto.SupervisorAvailabilityDto;
import edu.augustana.csc305.project.service.AvailabilityService;
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
    private final ManagerMessagingService managerMessagingService;
    private final CorrectionRequestService correctionRequestService;
    private final AvailabilityService availabilityService;

    public ManagerController(ManagerService managerService,
                             ManagerMessagingService managerMessagingService,
                             CorrectionRequestService correctionRequestService,
                             AvailabilityService availabilityService) {
        this.managerService = managerService;
        this.managerMessagingService = managerMessagingService;
        this.correctionRequestService = correctionRequestService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/job")
    public ManagerJobDto getAssignedJob(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getAssignedJob(managerId);
    }

    @PatchMapping("/job/applications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setAssignedJobApplications(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody ManagerJobApplicationStatusRequest request) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        managerService.setAssignedJobApplications(
                managerId, request.acceptingApplications());
    }

    @GetMapping("/applications")
    public List<ManagerJobApplicationDto> getJobApplications(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getJobApplications(managerId);
    }

    @PatchMapping("/applications/{applicationId}")
    public ManagerJobApplicationDto reviewJobApplication(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String applicationId,
            @RequestBody ManagerReviewJobApplicationRequest request) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        String status = request == null ? null : request.status();
        return managerService.reviewJobApplication(
                managerId, applicationId, status);
    }

    @GetMapping("/employees")
    public List<ManagerEmployeeDto> getEmployees(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployees(managerId);
    }
    @GetMapping("/availability")
    public List<SupervisorAvailabilityDto> getEmployeeAvailability(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId managerId = new ObjectId(authenticatedUser.id());

        return managerService.getEmployeeAvailability(
                managerId, availabilityService);
    }

    @GetMapping("/employee-candidates")
    public List<ManagerEmployeeOptionDto> getEmployeeCandidates(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployeeCandidates(managerId);
    }

    @GetMapping("/hours")
    public List<ManagerEmployeeHoursDto> getEmployeeHours(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        ObjectId managerId = new ObjectId(authenticatedUser.id());
        return managerService.getEmployeeHours(managerId);
    }

    @GetMapping("/corrections/pending")
    public List<PendingCorrectionDto> getPendingCorrections(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId managerId = new ObjectId(authenticatedUser.id());

        return correctionRequestService.getPendingCorrectionsForManager(managerId);
    }

    @PatchMapping("/corrections/{requestId}/approve")
    public CorrectionRequestDto approveCorrection(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String requestId,
            @RequestBody(required = false) ReviewCorrectionRequest request) {

        ObjectId managerId = new ObjectId(authenticatedUser.id());

        return correctionRequestService.approveForManager(
                managerId, requestId, request);
    }

    @PatchMapping("/corrections/{requestId}/reject")
    public CorrectionRequestDto rejectCorrection(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String requestId,
            @RequestBody(required = false) ReviewCorrectionRequest request) {

        ObjectId managerId = new ObjectId(authenticatedUser.id());

        return correctionRequestService.rejectForManager(
                managerId, requestId, request);
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

    @GetMapping("/messages/conversations")
    public List<ManagerConversationDto> getConversations(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return managerMessagingService.getConversations(new ObjectId(authenticatedUser.id()));
    }

    @PostMapping("/messages/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ManagerConversationDto startConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody StartManagerConversationRequest request) {
        return managerMessagingService.startConversation(
                new ObjectId(authenticatedUser.id()), request);
    }

    @GetMapping("/messages/groups")
    public List<GroupConversationDto> getGroups(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return managerMessagingService.getGroupConversations(
                new ObjectId(authenticatedUser.id()));
    }

    @PostMapping("/messages/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupConversationDto createGroup(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody CreateGroupConversationRequest request) {
        return managerMessagingService.createGroup(
                new ObjectId(authenticatedUser.id()), request);
    }

    @GetMapping("/messages/conversations/{conversationId}")
    public List<MessageDto> getMessages(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {
        return managerMessagingService.getMessages(
                new ObjectId(authenticatedUser.id()), conversationId);
    }

    @PostMapping("/messages/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto sendMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId,
            @RequestBody SendMessageRequest request) {
        return managerMessagingService.sendMessage(
                new ObjectId(authenticatedUser.id()), conversationId, request);
    }
}
