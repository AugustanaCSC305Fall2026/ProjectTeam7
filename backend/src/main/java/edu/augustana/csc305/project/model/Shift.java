package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "shifts")
public class Shift {
    @Id
    private ObjectId id;

    private ObjectId employmentId;
    private ObjectId jobId;
    private Instant startAt;
    private Instant endAt;
    private ShiftStatus status;
    private String note;

    public Shift() {
    }

    public Shift(ObjectId id, ObjectId employmentId, ObjectId jobId,
                 Instant startAt, Instant endAt, ShiftStatus status, String note) {
        this.id = id;
        this.employmentId = employmentId;
        this.jobId = jobId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.note = note;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getEmploymentId() { return employmentId; }
    public void setEmploymentId(ObjectId employmentId) { this.employmentId = employmentId; }

    public ObjectId getJobId() { return jobId; }
    public void setJobId(ObjectId jobId) { this.jobId = jobId; }

    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }

    public ShiftStatus getStatus() { return status; }
    public void setStatus(ShiftStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
