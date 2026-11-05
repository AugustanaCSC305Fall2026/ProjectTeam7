package edu.augustana.csc305.project.dto;

import java.util.Date;

public record GroupConversationDto(
        String id,
        String name,
        int memberCount,
        Date updatedAt,
        String lastMessage
) {
}
