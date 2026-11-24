package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.model.Conversation;
import edu.augustana.csc305.project.model.Message;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.ConversationRepository;
import edu.augustana.csc305.project.repository.MessageRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@Service
public class MessagingCoreService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingCoreService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Conversation getOrCreateDirectConversation(
            ObjectId creatorId, ObjectId recipientId) {
        return conversationRepository.findDirectConversation(creatorId, recipientId)
                .orElseGet(() -> {
                    User recipient = findUser(recipientId);
                    Date now = new Date();
                    Conversation conversation = new Conversation(
                            new ObjectId(), "DIRECT", null, creatorId,
                            List.of(creatorId, recipient.getId()), now, now);
                    conversationRepository.store(conversation);
                    return conversation;
                });
    }

    public List<MessageDto> getMessages(ObjectId userId, String conversationIdValue) {
        Conversation conversation = findMemberConversation(userId, conversationIdValue);
        return messageRepository.findByConversationId(conversation.getId()).stream()
                .map(message -> toMessageDto(message, findUser(message.getSenderId())))
                .toList();
    }

    public List<Conversation> findUserConversations(ObjectId userId) {
        return conversationRepository.findByMemberId(userId);
    }

    public MessageDto sendMessage(ObjectId userId, String conversationIdValue,
                                  SendMessageRequest request) {
        Conversation conversation = findMemberConversation(userId, conversationIdValue);
        String body = request == null || request.body() == null
                ? "" : request.body().trim();
        if (body.isBlank() || body.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "MESSAGE_BODY_MUST_BE_1_TO_2000_CHARACTERS");
        }
        Date sentAt = new Date();
        Message message = new Message(
                new ObjectId(), conversation.getId(), userId, body, sentAt);
        messageRepository.store(message);
        conversationRepository.touch(conversation.getId(), sentAt);
        MessageDto messageDto = toMessageDto(message, findUser(userId));
        for (ObjectId memberId : conversation.getMemberIds()) {
            messagingTemplate.convertAndSendToUser(
                    memberId.toHexString(), "/queue/messages", messageDto);
        }
        return messageDto;
    }

    public Conversation findMemberConversation(ObjectId userId,
                                               String conversationIdValue) {
        ObjectId conversationId = parseId(conversationIdValue, "conversationId");
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND"));
        if (conversation.getMemberIds() == null
                || !conversation.getMemberIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "NOT_A_CONVERSATION_MEMBER");
        }
        return conversation;
    }

    public User findOtherMember(Conversation conversation, ObjectId userId) {
        ObjectId otherId = conversation.getMemberIds().stream()
                .filter(memberId -> !memberId.equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_CONVERSATION_MEMBERS"));
        return findUser(otherId);
    }

    public User findUser(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
    }

    public String latestMessageBody(ObjectId conversationId) {
        return messageRepository.findLatestByConversationId(conversationId)
                .map(Message::getBody)
                .orElse(null);
    }

    public String displayName(User user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return String.join(" ", user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
    }

    private MessageDto toMessageDto(Message message, User sender) {
        return new MessageDto(
                message.getId().toHexString(), message.getConversationId().toHexString(),
                message.getSenderId().toHexString(), displayName(sender),
                message.getBody(), message.getSentAt());
    }

    private ObjectId parseId(String value, String field) {
        if (value == null || !ObjectId.isValid(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field.toUpperCase() + "_MUST_BE_VALID");
        }
        return new ObjectId(value);
    }
}
