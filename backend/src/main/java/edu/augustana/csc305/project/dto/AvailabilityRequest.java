package edu.augustana.csc305.project.dto;

import java.util.List;

public record AvailabilityRequest(List<AvailabilityWindowDto> windows) { }
