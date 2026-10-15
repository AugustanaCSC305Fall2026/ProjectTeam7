package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.ClockInRequest;
import edu.augustana.csc305.project.dto.TimeEntryDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.TimeClockService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/time-entries")
public class TimeEntryController {

    private final TimeClockService timeClockService;

    public TimeEntryController(TimeClockService timeClockService) {
        this.timeClockService = timeClockService;
    }

    @PostMapping("/clock-in")
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryDto clockIn(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody ClockInRequest request) {

        ObjectId userId = new ObjectId(authenticatedUser.id());
        return TimeEntryDto.from(timeClockService.clockIn(userId, request));
    }

    @PostMapping("/clock-out")
    public TimeEntryDto clockOut(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId userId = new ObjectId(authenticatedUser.id());
        return TimeEntryDto.from(timeClockService.clockOut(userId));
    }
}