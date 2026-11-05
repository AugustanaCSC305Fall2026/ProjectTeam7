package edu.augustana.csc305.project.dto;

import java.util.Date;

public record MessageDto(
        String id,
        String conversationId,
        String senderId,
        String senderName,
        String body,
        Date sentAt
) {
}
