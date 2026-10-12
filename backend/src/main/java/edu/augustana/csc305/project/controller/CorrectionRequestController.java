package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.CorrectionRequestDto;
import edu.augustana.csc305.project.dto.CreateCorrectionRequest;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.CorrectionRequestService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/corrections")
public class CorrectionRequestController {
    private final CorrectionRequestService correctionRequestService;

    public CorrectionRequestController(CorrectionRequestService correctionRequestService) {
        this.correctionRequestService = correctionRequestService;
    }

    @PostMapping
    public ResponseEntity<CorrectionRequestDto> submit(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody CreateCorrectionRequest request) {
        CorrectionRequestDto response = correctionRequestService.submit(
                new ObjectId(authenticatedUser.id()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(CorrectionRequestService.CorrectionException.class)
    public ResponseEntity<ApiError> handleCorrectionError(
            CorrectionRequestService.CorrectionException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case "NOT_YOUR_ENTRY" -> HttpStatus.FORBIDDEN;
            case "ENTRY_STILL_OPEN", "DUPLICATE_PENDING_REQUEST" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(status)
                .body(new ApiError(exception.getCode(), exception.getCode()));
    }

    public record ApiError(String code, String message) {
    }
}
