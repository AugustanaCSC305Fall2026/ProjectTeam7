package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.PendingCorrectionDto;
import edu.augustana.csc305.project.service.CorrectionRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}