package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.MessagingContactDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartConversationRequest;
import edu.augustana.csc305.project.dto.UserConversationDto;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import edu.augustana.csc305.project.service.UserMessagingService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class UserMessagingController {
    private final UserMessagingService messagingService;

    public UserMessagingController(UserMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping("/contacts")
    public List<MessagingContactDto> getContacts(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return messagingService.getContacts(new ObjectId(authenticatedUser.id()));
    }

    @GetMapping("/conversations")
    public List<UserConversationDto> getConversations(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return messagingService.getConversations(new ObjectId(authenticatedUser.id()));
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public UserConversationDto startConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody StartConversationRequest request) {
        return messagingService.startConversation(
                new ObjectId(authenticatedUser.id()), request);
    }

    @GetMapping("/conversations/{conversationId}")
    public List<MessageDto> getMessages(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {
        return messagingService.getMessages(
                new ObjectId(authenticatedUser.id()), conversationId);
    }

    @PostMapping("/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto sendMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId,
            @RequestBody SendMessageRequest request) {
        return messagingService.sendMessage(
                new ObjectId(authenticatedUser.id()), conversationId, request);
    }
}
