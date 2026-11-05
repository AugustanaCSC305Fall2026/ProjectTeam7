package edu.augustana.csc305.project.dto;

import java.util.Date;

public record ManagerConversationDto(
        String id,
        String employeeId,
        String employeeName,
        String employeeEmail,
        Date updatedAt,
        String lastMessage
) {
}
