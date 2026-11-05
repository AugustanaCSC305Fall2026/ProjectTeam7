package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.MessagingContactDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartConversationRequest;
import edu.augustana.csc305.project.dto.UserConversationDto;
import edu.augustana.csc305.project.model.Conversation;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMessagingService {
    private final UserRepository userRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;
    private final MessagingCoreService messagingCore;

    public UserMessagingService(
            UserRepository userRepository,
            EmploymentRepository employmentRepository,
            JobRepository jobRepository,
            MessagingCoreService messagingCore) {
        this.userRepository = userRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
        this.messagingCore = messagingCore;
    }

    public List<MessagingContactDto> getContacts(ObjectId userId) {
        User user = messagingCore.findUser(userId);
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
                    User contact = messagingCore.findUser(entry.getKey());
                    return new MessagingContactDto(
                            contact.getId().toHexString(), messagingCore.displayName(contact),
                            contact.getEmail(), entry.getValue());
                })
                .toList();
    }

    public List<UserConversationDto> getConversations(ObjectId userId) {
        List<UserConversationDto> results = new ArrayList<>();
        for (Conversation conversation : messagingCore.findUserConversations(userId)) {
            String lastMessage = messagingCore.latestMessageBody(conversation.getId());
            if ("GROUP".equals(conversation.getType())) {
                results.add(new UserConversationDto(
                        conversation.getId().toHexString(), null, null, null,
                        conversation.getUpdatedAt(), lastMessage, "GROUP", conversation.getName()));
            } else {
                User otherUser = messagingCore.findOtherMember(conversation, userId);
                results.add(new UserConversationDto(
                        conversation.getId().toHexString(), otherUser.getId().toHexString(),
                        messagingCore.displayName(otherUser), otherUser.getEmail(),
                        conversation.getUpdatedAt(), lastMessage, "DIRECT", null));
            }
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
        User recipient = messagingCore.findUser(recipientId);
        Conversation conversation = messagingCore
                .getOrCreateDirectConversation(userId, recipientId);
        return new UserConversationDto(
                conversation.getId().toHexString(), recipient.getId().toHexString(),
                messagingCore.displayName(recipient), recipient.getEmail(),
                conversation.getUpdatedAt(), messagingCore.latestMessageBody(conversation.getId()),
                "DIRECT", null);
    }

    public List<MessageDto> getMessages(ObjectId userId, String conversationId) {
        return messagingCore.getMessages(userId, conversationId);
    }

    public MessageDto sendMessage(ObjectId userId, String conversationId,
                                  SendMessageRequest request) {
        return messagingCore.sendMessage(userId, conversationId, request);
    }

    private boolean isAllowedContact(ObjectId userId, ObjectId recipientId) {
        User user = messagingCore.findUser(userId);
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

    private ObjectId parseId(String value, String field) {
        if (value == null || !ObjectId.isValid(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field.toUpperCase() + "_MUST_BE_VALID");
        }
        return new ObjectId(value);
    }
}
