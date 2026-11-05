package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "userEvents")
public class UserEvents {

    @Id
    private ObjectId id;

    private ObjectId userId;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;

    public UserEvents() {
    }

    public ObjectId getId() {
    return id;
    }
    public String getIdString() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {this.id = id;}

    public ObjectId getUserId() {return userId;}
    public void setUserId(ObjectId userId) {this.userId = userId;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description; }
    public void setDescription(String description) {this.description = description; }

    public Instant getStartTime() {return startTime; }
    public void setStartTime(Instant startTime) {this.startTime = startTime; }

    public Instant getEndTime() {return endTime; }
    public void setEndTime(Instant endTime) {this.endTime = endTime; }
}

