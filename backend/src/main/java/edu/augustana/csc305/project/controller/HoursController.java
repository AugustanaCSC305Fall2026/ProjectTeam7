package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.HoursSummaryDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.HoursService;
import org.bson.types.ObjectId;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hours")
public class HoursController {

    private final HoursService hoursService;

    public HoursController(HoursService hoursService) {
        this.hoursService = hoursService;
    }

    @GetMapping
    public HoursSummaryDto getHours(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) String jobId) {

        ObjectId userId = new ObjectId(authenticatedUser.id());

        ObjectId selectedJobId = null;

        if (jobId != null && !jobId.isBlank()) {
            selectedJobId = new ObjectId(jobId);
        }

        return hoursService.getHours(userId, selectedJobId);
    }
}