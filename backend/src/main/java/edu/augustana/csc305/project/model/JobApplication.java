package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;

public class JobApplication {

    private ObjectId id;

    private ObjectId userId;
    private ObjectId jobId;
    private String applicationStatement = "";
    private String status;


    public JobApplication(ObjectId userId, ObjectId jobId, String status) {
        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
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

    public String getApplicationStatement() {
        return applicationStatement;
    }

    public void setApplicationStatement(String applicationStatement) {
        this.applicationStatement = applicationStatement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
