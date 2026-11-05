package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.CreateGroupConversationRequest;
import edu.augustana.csc305.project.dto.GroupConversationDto;
import edu.augustana.csc305.project.dto.ManagerConversationDto;
import edu.augustana.csc305.project.dto.MessageDto;
import edu.augustana.csc305.project.dto.SendMessageRequest;
import edu.augustana.csc305.project.dto.StartManagerConversationRequest;
import edu.augustana.csc305.project.model.Conversation;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.ConversationRepository;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
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
    private final ConversationRepository conversationRepository;
    private final MessagingCoreService messagingCore;

    public ManagerMessagingService(
            JobRepository jobRepository,
            EmploymentRepository employmentRepository,
            ConversationRepository conversationRepository,
            MessagingCoreService messagingCore) {
        this.jobRepository = jobRepository;
        this.employmentRepository = employmentRepository;
        this.conversationRepository = conversationRepository;
        this.messagingCore = messagingCore;
    }

    public List<ManagerConversationDto> getConversations(ObjectId managerId) {
        List<ManagerConversationDto> results = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findByMemberId(managerId)) {
            if (!"DIRECT".equals(conversation.getType())) {
                continue;
            }
            User employee = messagingCore.findOtherMember(conversation, managerId);
            results.add(new ManagerConversationDto(
                    conversation.getId().toHexString(), employee.getId().toHexString(),
                    messagingCore.displayName(employee), employee.getEmail(),
                    conversation.getUpdatedAt(),
                    messagingCore.latestMessageBody(conversation.getId())));
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
        Conversation conversation = messagingCore
                .getOrCreateDirectConversation(managerId, employeeId);
        return new ManagerConversationDto(
                conversation.getId().toHexString(), employee.getId().toHexString(),
                messagingCore.displayName(employee), employee.getEmail(),
                conversation.getUpdatedAt(),
                messagingCore.latestMessageBody(conversation.getId()));
    }

    public List<GroupConversationDto> getGroupConversations(ObjectId managerId) {
        List<GroupConversationDto> results = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findByMemberId(managerId)) {
            if (!"GROUP".equals(conversation.getType())) {
                continue;
            }
            results.add(new GroupConversationDto(
                    conversation.getId().toHexString(), conversation.getName(),
                    conversation.getMemberIds().size(), conversation.getUpdatedAt(),
                    messagingCore.latestMessageBody(conversation.getId())));
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

    public List<MessageDto> getMessages(ObjectId managerId, String conversationId) {
        return messagingCore.getMessages(managerId, conversationId);
    }

    public MessageDto sendMessage(ObjectId managerId, String conversationId,
                                  SendMessageRequest request) {
        return messagingCore.sendMessage(managerId, conversationId, request);
    }

    private User findActiveEmployee(Job job, ObjectId employeeId) {
        Employment employment = employmentRepository.findActiveByJobId(job.getId()).stream()
                .filter(item -> employeeId.equals(item.getUserId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "EMPLOYEE_NOT_ON_MANAGER_ROSTER"));
        return messagingCore.findUser(employment.getUserId());
    }

    private Job findAssignedJob(ObjectId managerId) {
        return jobRepository.findByManagerId(managerId).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "MANAGER_JOB_NOT_FOUND"));
    }

    private ObjectId parseId(String value, String field) {
        if (value == null || !ObjectId.isValid(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    field.toUpperCase() + "_MUST_BE_VALID");
        }
        return new ObjectId(value);
    }
}
