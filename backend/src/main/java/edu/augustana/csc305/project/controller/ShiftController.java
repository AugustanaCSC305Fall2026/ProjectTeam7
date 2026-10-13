package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.OpenShiftDto;
import edu.augustana.csc305.project.dto.ShiftDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.ShiftService;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping("/{id}/offer")
    public ShiftDto offerShift(
            @PathVariable String id,
            @RequestBody OfferShiftRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId shiftId = new ObjectId(id);
        ObjectId userId = new ObjectId(authenticatedUser.id());

        return ShiftDto.fromShift(
                shiftService.offerShift(
                        shiftId,
                        userId,
                        request.reason()
                )
        );
    }

    @DeleteMapping("/{id}/offer")
    public ShiftDto withdrawOffer(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId shiftId = new ObjectId(id);
        ObjectId userId = new ObjectId(authenticatedUser.id());

        return ShiftDto.fromShift(
                shiftService.withdrawOffer(shiftId, userId)
        );
    }

    @GetMapping("/me")
    public List<ShiftDto> getMyShifts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId userId =
                new ObjectId(authenticatedUser.id());

        return shiftService.getMyShifts(userId)
                .stream()
                .map(ShiftDto::fromShift)
                .toList();
    }

    @GetMapping("/open")
    public List<OpenShiftDto> getOpenShifts(
            @RequestParam String from,
            @RequestParam String to,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId userId =
                new ObjectId(authenticatedUser.id());

        return shiftService.getOpenShifts(
                userId,
                Instant.parse(from),
                Instant.parse(to)
        );
    }

    @PostMapping("/{id}/claim")
    public ShiftDto claimShift(
            @PathVariable String id,
            @RequestBody ClaimShiftRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        ObjectId shiftId = new ObjectId(id);
        ObjectId employmentId =
                new ObjectId(request.employmentId());
        ObjectId userId =
                new ObjectId(authenticatedUser.id());

        return ShiftDto.fromShift(
                shiftService.claimShift(
                        shiftId,
                        employmentId,
                        userId
                )
        );
    }

    @GetMapping("/upcoming")
    public List<ShiftDto> getUpcomingShifts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to) {

        Instant rangeStart =
                from == null ? Instant.now() : from;

        Instant rangeEnd =
                to == null
                        ? rangeStart.plus(14, ChronoUnit.DAYS)
                        : to;

        if (!rangeEnd.isAfter(rangeStart)) {
            throw new IllegalArgumentException(
                    "The to time must be after the from time"
            );
        }

        return shiftService.getUpcomingShifts(
                new ObjectId(authenticatedUser.id()),
                rangeStart,
                rangeEnd
        );
    }

    public record OfferShiftRequest(String reason) {
    }

    public record ClaimShiftRequest(String employmentId) {
    }
}