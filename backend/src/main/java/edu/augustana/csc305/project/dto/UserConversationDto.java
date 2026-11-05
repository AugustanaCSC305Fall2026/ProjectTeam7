package edu.augustana.csc305.project.dto;

import java.util.Date;

public record UserConversationDto(
        String id,
        String otherUserId,
        String otherUserName,
        String otherUserEmail,
        Date updatedAt,
        String lastMessage
) {
}
