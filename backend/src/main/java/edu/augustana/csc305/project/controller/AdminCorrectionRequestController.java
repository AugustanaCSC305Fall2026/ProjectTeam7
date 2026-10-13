package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.CorrectionRequestDto;
import edu.augustana.csc305.project.dto.PendingCorrectionDto;
import edu.augustana.csc305.project.dto.ReviewCorrectionRequest;
import edu.augustana.csc305.project.service.CorrectionRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@RestController
@RequestMapping("/admin/corrections")
public class AdminCorrectionRequestController {

    private final CorrectionRequestService correctionRequestService;

    public AdminCorrectionRequestController(
            CorrectionRequestService correctionRequestService) {
        this.correctionRequestService = correctionRequestService;
    }

    @GetMapping("/pending")
    public List<PendingCorrectionDto> getPendingCorrections() {
        return correctionRequestService.getPendingCorrections();
    }

    @PatchMapping("/{requestId}/approve")
public CorrectionRequestDto approveCorrection(
        @PathVariable String requestId,
        @RequestBody(required = false) ReviewCorrectionRequest request) {
            String reviewNote = request == null ? null : request.reviewNote();
            return correctionRequestService.approve(requestId, reviewNote);
    }

    @PatchMapping("/{requestId}/reject")
    public CorrectionRequestDto rejectCorrection(
            @PathVariable String requestId,
            @RequestBody(required = false) ReviewCorrectionRequest request) {
            String reviewNote = request == null ? null : request.reviewNote();
            return correctionRequestService.reject(requestId, reviewNote);
    }

    @ExceptionHandler(CorrectionRequestService.CorrectionException.class)
public ResponseEntity<ApiError> handleCorrectionError(
        CorrectionRequestService.CorrectionException exception) {

        HttpStatus status = switch (exception.getCode()) {
            case "CORRECTION_NOT_PENDING",
                "TIME_ENTRY_CHANGED" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status)
                .body(new ApiError(exception.getCode(), exception.getCode()));
    }

    public record ApiError(String code, String message) {
    }
}