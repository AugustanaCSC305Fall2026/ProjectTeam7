package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.ShiftDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.ShiftService;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/me/shifts")
public class ShiftController {
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public List<ShiftDto> getMyShifts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant rangeStart = from == null ? Instant.now() : from;
        Instant rangeEnd = to == null ? rangeStart.plus(14, ChronoUnit.DAYS) : to;

        if (!rangeEnd.isAfter(rangeStart)) {
            throw new IllegalArgumentException("The to time must be after the from time");
        }

        return shiftService.getUpcomingShifts(
                new ObjectId(authenticatedUser.id()), rangeStart, rangeEnd);
    }
}
