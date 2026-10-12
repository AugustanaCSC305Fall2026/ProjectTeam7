package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "timeEntries")
public class TimeEntry {
    @Id
    private ObjectId id;

    private ObjectId employmentId;
    private ObjectId userId;
    private ObjectId shiftId;
    private Instant clockInAt;
    private Instant clockOutAt;
    private EntrySource source;
    private int breakMinutes;
    private int workedMinutes;

    public TimeEntry() {
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getEmploymentId() { return employmentId; }
    public void setEmploymentId(ObjectId employmentId) { this.employmentId = employmentId; }

    public ObjectId getUserId() { return userId; }
    public void setUserId(ObjectId userId) { this.userId = userId; }

    public ObjectId getShiftId() { return shiftId; }
    public void setShiftId(ObjectId shiftId) { this.shiftId = shiftId; }

    public Instant getClockInAt() { return clockInAt; }
    public void setClockInAt(Instant clockInAt) { this.clockInAt = clockInAt; }

    public Instant getClockOutAt() { return clockOutAt; }
    public void setClockOutAt(Instant clockOutAt) { this.clockOutAt = clockOutAt; }

    public EntrySource getSource() { return source; }
    public void setSource(EntrySource source) { this.source = source; }

    public int getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(int breakMinutes) { this.breakMinutes = breakMinutes; }

    public int getWorkedMinutes() { return workedMinutes; }
    public void setWorkedMinutes(int workedMinutes) { this.workedMinutes = workedMinutes; }
}
