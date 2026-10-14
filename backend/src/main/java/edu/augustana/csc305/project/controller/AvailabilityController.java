package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.AvailabilityRequest;
import edu.augustana.csc305.project.dto.AvailabilityResponse;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.AvailabilityService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/me")
    public AvailabilityResponse getMyAvailability(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return availabilityService.getForUser(new ObjectId(authenticatedUser.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public AvailabilityResponse saveMyAvailability(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody AvailabilityRequest request) {
        return availabilityService.replaceForUser(
                new ObjectId(authenticatedUser.id()), request);
    }

    /** Used by supervisors when considering a student's availability for a shift. */
    @GetMapping("/user/{userId}")
    public AvailabilityResponse getUserAvailability(@PathVariable String userId) {
        return availabilityService.getForUser(new ObjectId(userId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidAvailability(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_AVAILABILITY", exception.getMessage()));
    }

    public record ApiError(String code, String message) { }
}
