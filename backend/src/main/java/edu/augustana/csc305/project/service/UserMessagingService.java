package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.MessagingContactDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartConversationRequest;
import edu.augustana.csc305.project.dto.UserConversationDto;
import edu.augustana.csc305.project.model.Conversation;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.Message;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.ConversationRepository;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.MessageRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMessagingService {
    private final UserRepository userRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public UserMessagingService(
            UserRepository userRepository,
            EmploymentRepository employmentRepository,
            JobRepository jobRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<MessagingContactDto> getContacts(ObjectId userId) {
        User user = findUser(userId);
        Map<ObjectId, String> relationships = new LinkedHashMap<>();

        if (user.getRoles().contains("ADMIN")) {
            for (User candidate : userRepository.findAll()) {
                if (!candidate.getId().equals(userId)) {
                    relationships.put(candidate.getId(), "Supervisor");
                }
            }
        } else {
            for (Job job : getUserJobs(userId)) {
                if (job.getManagerId() != null && !job.getManagerId().equals(userId)) {
                    relationships.put(job.getManagerId(), "Manager");
                }
                for (Employment employment : employmentRepository.findActiveByJobId(job.getId())) {
                    if (!employment.getUserId().equals(userId)) {
                        relationships.putIfAbsent(employment.getUserId(), "Coworker");
                    }
                }
            }
            for (User candidate : userRepository.findAll()) {
                if (candidate.getRoles().contains("ADMIN") && !candidate.getId().equals(userId)) {
                    relationships.put(candidate.getId(), "Supervisor");
                }
            }
        }

        return relationships.entrySet().stream()
                .map(entry -> {
                    User contact = findUser(entry.getKey());
                    return new MessagingContactDto(
                            contact.getId().toHexString(), displayName(contact),
                            contact.getEmail(), entry.getValue());
                })
                .toList();
    }

    public List<UserConversationDto> getConversations(ObjectId userId) {
        List<UserConversationDto> results = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findByMemberId(userId)) {
            if (!"DIRECT".equals(conversation.getType())) {
                continue;
            }
            User otherUser = findOtherMember(conversation, userId);
            Message lastMessage = messageRepository
                    .findLatestByConversationId(conversation.getId()).orElse(null);
            results.add(new UserConversationDto(
                    conversation.getId().toHexString(),
                    otherUser.getId().toHexString(),
                    displayName(otherUser), otherUser.getEmail(),
                    conversation.getUpdatedAt(),
                    lastMessage == null ? null : lastMessage.getBody()));
        }
        results.sort((first, second) -> second.updatedAt().compareTo(first.updatedAt()));
        return results;
    }

    public UserConversationDto startConversation(
            ObjectId userId, StartConversationRequest request) {
        ObjectId recipientId = parseId(request == null ? null : request.recipientId(),
                "recipientId");
        if (recipientId.equals(userId) || !isAllowedContact(userId, recipientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "RECIPIENT_NOT_ALLOWED");
        }
        User recipient = findUser(recipientId);
        Conversation conversation = conversationRepository
                .findDirectConversation(userId, recipientId)
                .orElseGet(() -> createConversation(userId, recipient));
        Message lastMessage = messageRepository
                .findLatestByConversationId(conversation.getId()).orElse(null);
        return new UserConversationDto(
                conversation.getId().toHexString(), recipient.getId().toHexString(),
                displayName(recipient), recipient.getEmail(), conversation.getUpdatedAt(),
                lastMessage == null ? null : lastMessage.getBody());
    }

    public List<MessageDto> getMessages(ObjectId userId, String conversationIdValue) {
        Conversation conversation = findMemberConversation(userId, conversationIdValue);
        return messageRepository.findByConversationId(conversation.getId()).stream()
                .map(message -> toMessageDto(message, findUser(message.getSenderId())))
                .toList();
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
        return toMessageDto(message, findUser(userId));
    }

    private boolean isAllowedContact(ObjectId userId, ObjectId recipientId) {
        User user = findUser(userId);
        if (user.getRoles().contains("ADMIN")) {
            return !recipientId.equals(userId);
        }
        return getContacts(userId).stream()
                .anyMatch(contact -> contact.id().equals(recipientId.toHexString()));
    }

    private List<Job> getUserJobs(ObjectId userId) {
        return employmentRepository.findByUserId(userId).stream()
                .filter(Employment::isActive)
                .map(Employment::getJobId)
                .map(jobRepository::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private Conversation createConversation(ObjectId userId, User recipient) {
        Date now = new Date();
        Conversation conversation = new Conversation(
                new ObjectId(), "DIRECT", null, userId,
                List.of(userId, recipient.getId()), now, now);
        conversationRepository.store(conversation);
        return conversation;
    }

    private Conversation findMemberConversation(ObjectId userId, String conversationIdValue) {
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

    private User findOtherMember(Conversation conversation, ObjectId userId) {
        ObjectId otherId = conversation.getMemberIds().stream()
                .filter(memberId -> !memberId.equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_CONVERSATION_MEMBERS"));
        return findUser(otherId);
    }

    private User findUser(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));
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

    private String displayName(User user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return String.join(" ", user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
    }
}
