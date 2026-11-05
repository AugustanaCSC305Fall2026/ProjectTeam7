package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jobApplications")
public class JobApplication {
    @Id
    private ObjectId id;

    private ObjectId userId;
    private ObjectId jobId;
    private String applicationStatement = "";
    private String status = "PENDING";

    public JobApplication() {
    }

    public JobApplication(
            ObjectId userId,
            ObjectId jobId,
            String applicationStatement) {
        this.id = new ObjectId();
        this.userId = userId;
        this.jobId = jobId;
        this.applicationStatement = applicationStatement == null
                ? ""
                : applicationStatement;
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