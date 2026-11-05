package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.ManagerConversationDto;
import edu.augustana.csc305.project.dto.CreateGroupConversationRequest;
import edu.augustana.csc305.project.dto.GroupConversationDto;
import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartManagerConversationRequest;
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
import java.util.List;

@Service
public class ManagerMessagingService {
    private final JobRepository jobRepository;
    private final EmploymentRepository employmentRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ManagerMessagingService(
            JobRepository jobRepository,
            EmploymentRepository employmentRepository,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.jobRepository = jobRepository;
        this.employmentRepository = employmentRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<ManagerConversationDto> getConversations(ObjectId managerId) {
        List<ManagerConversationDto> results = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findByMemberId(managerId)) {
            if (!"DIRECT".equals(conversation.getType())) {
                continue;
            }
            User employee = findOtherMember(conversation, managerId);
            Message lastMessage = messageRepository
                    .findLatestByConversationId(conversation.getId()).orElse(null);
            results.add(toConversationDto(conversation, employee, lastMessage));
        }
        results.sort((first, second) -> second.updatedAt().compareTo(first.updatedAt()));
        return results;
    }

    public ManagerConversationDto startConversation(
            ObjectId managerId, StartManagerConversationRequest request) {
        Job job = findAssignedJob(managerId);
        ObjectId employeeId = parseId(request == null ? null : request.employeeId(),
                "employeeId");
        User employee = findActiveEmployee(job, employeeId);

        Conversation conversation = conversationRepository
                .findDirectConversation(managerId, employeeId)
                .orElseGet(() -> createConversation(managerId, employee));
        Message lastMessage = messageRepository
                .findLatestByConversationId(conversation.getId()).orElse(null);
        return toConversationDto(conversation, employee, lastMessage);
    }

    public List<GroupConversationDto> getGroupConversations(ObjectId managerId) {
        List<GroupConversationDto> results = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findByMemberId(managerId)) {
            if (!"GROUP".equals(conversation.getType())) {
                continue;
            }
            Message lastMessage = messageRepository
                    .findLatestByConversationId(conversation.getId()).orElse(null);
            results.add(new GroupConversationDto(
                    conversation.getId().toHexString(), conversation.getName(),
                    conversation.getMemberIds().size(), conversation.getUpdatedAt(),
                    lastMessage == null ? null : lastMessage.getBody()));
        }
        results.sort((first, second) -> second.updatedAt().compareTo(first.updatedAt()));
        return results;
    }

    public GroupConversationDto createGroup(
            ObjectId managerId, CreateGroupConversationRequest request) {
        Job job = findAssignedJob(managerId);
        String name = request == null || request.name() == null
                ? "" : request.name().trim();
        if (name.isBlank() || name.length() > 100
                || request.employeeIds() == null || request.employeeIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "GROUP_NAME_AND_EMPLOYEES_ARE_REQUIRED");
        }

        List<ObjectId> employeeIds = request.employeeIds().stream()
                .map(value -> parseId(value, "employeeId"))
                .distinct()
                .toList();
        if (employeeIds.size() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "GROUP_CANNOT_HAVE_MORE_THAN_50_EMPLOYEES");
        }
        for (ObjectId employeeId : employeeIds) {
            findActiveEmployee(job, employeeId);
        }

        Date now = new Date();
        List<ObjectId> members = new ArrayList<>();
        members.add(managerId);
        members.addAll(employeeIds);
        Conversation conversation = new Conversation(
                new ObjectId(), "GROUP", name, managerId, members, now, now);
        conversationRepository.store(conversation);
        return new GroupConversationDto(
                conversation.getId().toHexString(), name, members.size(), now, null);
    }

    public List<MessageDto> getMessages(ObjectId managerId, String conversationIdValue) {
        Conversation conversation = findMemberConversation(managerId, conversationIdValue);
        List<MessageDto> results = new ArrayList<>();
        for (Message message : messageRepository.findByConversationId(conversation.getId())) {
            User sender = userRepository.findById(message.getSenderId()).orElse(null);
            results.add(toMessageDto(message, sender));
        }
        return results;
    }

    public MessageDto sendMessage(ObjectId managerId, String conversationIdValue,
                                  SendMessageRequest request) {
        Conversation conversation = findMemberConversation(managerId, conversationIdValue);
        String body = request == null || request.body() == null
                ? "" : request.body().trim();
        if (body.isBlank() || body.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "MESSAGE_BODY_MUST_BE_1_TO_2000_CHARACTERS");
        }

        Date sentAt = new Date();
        Message message = new Message(
                new ObjectId(), conversation.getId(), managerId, body, sentAt);
        messageRepository.store(message);
        conversationRepository.touch(conversation.getId(), sentAt);
        User sender = userRepository.findById(managerId).orElse(null);
        return toMessageDto(message, sender);
    }

    private Conversation createConversation(ObjectId managerId, User employee) {
        Date now = new Date();
        Conversation conversation = new Conversation(
                new ObjectId(), "DIRECT", null, managerId,
                List.of(managerId, employee.getId()), now, now);
        conversationRepository.store(conversation);
        return conversation;
    }

    private Conversation findMemberConversation(ObjectId managerId,
                                                 String conversationIdValue) {
        ObjectId conversationId = parseId(conversationIdValue, "conversationId");
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND"));
        if (conversation.getMemberIds() == null
                || !conversation.getMemberIds().contains(managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "NOT_A_CONVERSATION_MEMBER");
        }
        return conversation;
    }

    private User findActiveEmployee(Job job, ObjectId employeeId) {
        Employment employment = employmentRepository.findActiveByJobId(job.getId()).stream()
                .filter(item -> employeeId.equals(item.getUserId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "EMPLOYEE_NOT_ON_MANAGER_ROSTER"));
        return userRepository.findById(employment.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND"));
    }

    private Job findAssignedJob(ObjectId managerId) {
        return jobRepository.findByManagerId(managerId).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MANAGER_JOB_NOT_FOUND"));
    }

    private User findOtherMember(Conversation conversation, ObjectId managerId) {
        ObjectId employeeId = conversation.getMemberIds().stream()
                .filter(memberId -> !memberId.equals(managerId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_CONVERSATION_MEMBERS"));
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND"));
    }

    private ManagerConversationDto toConversationDto(
            Conversation conversation, User employee, Message lastMessage) {
        return new ManagerConversationDto(
                conversation.getId().toHexString(),
                employee.getId().toHexString(),
                displayName(employee),
                employee.getEmail(),
                conversation.getUpdatedAt(),
                lastMessage == null ? null : lastMessage.getBody());
    }

    private MessageDto toMessageDto(Message message, User sender) {
        return new MessageDto(
                message.getId().toHexString(),
                message.getConversationId().toHexString(),
                message.getSenderId().toHexString(),
                sender == null ? "Unknown user" : displayName(sender),
                message.getBody(),
                message.getSentAt());
    }

    private ObjectId parseId(String value, String field) {
        if (value == null || !ObjectId.isValid(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field.toUpperCase() + "_MUST_BE_VALID");
        }
        return new ObjectId(value);
    }

    private String displayName(User user) {
        String name = user.getName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        return String.join(" ", user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
    }
}
