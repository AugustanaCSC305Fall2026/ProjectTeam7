package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jobs")
public class Job {
    @Id
    private ObjectId id;
    private boolean acceptingApplications;

    private String title;
    private String department;
    private String description;
    private ObjectId managerId;
    public Job() {
    }

    public Job(ObjectId id, String title, String department, String description) {
        this.id = id;
        this.title = title;
        this.department = department;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ObjectId getManagerId() {return managerId;}

    public void setManagerId(ObjectId managerId) {this.managerId = managerId;}

    public boolean isAcceptingApplications() {
        return acceptingApplications;
    }
    
    public void setAcceptingApplications(boolean acceptingApplications) {
        this.acceptingApplications = acceptingApplications;
    }
}