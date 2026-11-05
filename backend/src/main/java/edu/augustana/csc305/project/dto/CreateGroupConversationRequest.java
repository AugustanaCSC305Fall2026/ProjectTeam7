package edu.augustana.csc305.project.dto;

import java.util.List;

public record CreateGroupConversationRequest(
        String name,
        List<String> employeeIds
) {
}
