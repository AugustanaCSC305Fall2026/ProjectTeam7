package edu.augustana.csc305.project.dto;

import java.util.List;

public record AvailabilityResponse(String userId, List<AvailabilityWindowDto> windows) { }
