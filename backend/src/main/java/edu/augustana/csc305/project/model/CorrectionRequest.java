package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "correctionRequests")
public class CorrectionRequest {
    @Id
    private ObjectId id;

    private ObjectId userId;
    private ObjectId timeEntryId;
    private Instant originalClockInAt;
    private Instant originalClockOutAt;
    private Instant proposedClockInAt;
    private Instant proposedClockOutAt;
    private Instant approvedClockInAt;
    private Instant approvedClockOutAt;
    private String reason;
    private CorrectionStatus status;
    private String reviewNote;
    private Instant createdAt;

    public CorrectionRequest() {
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getUserId() { return userId; }
    public void setUserId(ObjectId userId) { this.userId = userId; }

    public ObjectId getTimeEntryId() { return timeEntryId; }
    public void setTimeEntryId(ObjectId timeEntryId) { this.timeEntryId = timeEntryId; }

    public Instant getOriginalClockInAt() { return originalClockInAt; }
    public void setOriginalClockInAt(Instant originalClockInAt) { this.originalClockInAt = originalClockInAt; }

    public Instant getOriginalClockOutAt() { return originalClockOutAt; }
    public void setOriginalClockOutAt(Instant originalClockOutAt) { this.originalClockOutAt = originalClockOutAt; }

    public Instant getProposedClockInAt() { return proposedClockInAt; }
    public void setProposedClockInAt(Instant proposedClockInAt) { this.proposedClockInAt = proposedClockInAt; }

    public Instant getProposedClockOutAt() { return proposedClockOutAt; }
    public void setProposedClockOutAt(Instant proposedClockOutAt) { this.proposedClockOutAt = proposedClockOutAt; }

    public Instant getApprovedClockInAt() { return approvedClockInAt; }
    public void setApprovedClockInAt(Instant approvedClockInAt) {
        this.approvedClockInAt = approvedClockInAt;
    }

    public Instant getApprovedClockOutAt() { return approvedClockOutAt; }
    public void setApprovedClockOutAt(Instant approvedClockOutAt) {
        this.approvedClockOutAt = approvedClockOutAt;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public CorrectionStatus getStatus() { return status; }
    public void setStatus(CorrectionStatus status) { this.status = status; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
