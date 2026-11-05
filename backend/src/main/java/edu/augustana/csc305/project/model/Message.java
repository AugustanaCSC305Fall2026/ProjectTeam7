package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "messages")
public class Message {
    @Id
    private ObjectId id;
    private ObjectId conversationId;
    private ObjectId senderId;
    private String body;
    private Date sentAt;

    public Message() {
    }

    public Message(ObjectId id, ObjectId conversationId, ObjectId senderId,
                   String body, Date sentAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.body = body;
        this.sentAt = sentAt;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public ObjectId getConversationId() { return conversationId; }
    public void setConversationId(ObjectId conversationId) { this.conversationId = conversationId; }
    public ObjectId getSenderId() { return senderId; }
    public void setSenderId(ObjectId senderId) { this.senderId = senderId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}
