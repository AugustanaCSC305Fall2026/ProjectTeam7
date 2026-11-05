package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "conversations")
public class Conversation {
    @Id
    private ObjectId id;
    private String type;
    private String name;
    private ObjectId createdBy;
    private List<ObjectId> memberIds;
    private Date createdAt;
    private Date updatedAt;

    public Conversation() {
    }

    public Conversation(ObjectId id, String type, String name, ObjectId createdBy,
                        List<ObjectId> memberIds, Date createdAt, Date updatedAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.createdBy = createdBy;
        this.memberIds = memberIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ObjectId getCreatedBy() { return createdBy; }
    public void setCreatedBy(ObjectId createdBy) { this.createdBy = createdBy; }
    public List<ObjectId> getMemberIds() { return memberIds; }
    public void setMemberIds(List<ObjectId> memberIds) { this.memberIds = memberIds; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
