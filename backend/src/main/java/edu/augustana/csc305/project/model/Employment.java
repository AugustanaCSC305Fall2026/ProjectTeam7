package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "employments")
public class Employment {
    @Id
    private ObjectId id;

    private ObjectId userId;
    private ObjectId jobId;
    private int payRateCents;
    private Date startDate;
    private boolean active;

    public Employment() {
    }

    public Employment(ObjectId id, ObjectId userId, ObjectId jobId,
                      int payRateCents, Date startDate, boolean active) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.payRateCents = payRateCents;
        this.startDate = startDate;
        this.active = active;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getJobId() {
        return jobId;
    }

    public void setJobId(ObjectId jobId) {
        this.jobId = jobId;
    }

    public int getPayRateCents() {
        return payRateCents;
    }

    public void setPayRateCents(int payRateCents) {
        this.payRateCents = payRateCents;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}