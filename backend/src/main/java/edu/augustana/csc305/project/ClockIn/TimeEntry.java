package edu.augustana.csc305.project.ClockIn;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimeEntry {


    private ObjectId id;

    private ObjectId userId;
    private Instant clockInTime;
    private Instant clockOutTime;
    private  long totalSeconds;
    private boolean onBreak;
    private List<BreakEntry> breaks = new ArrayList<>();

    public  TimeEntry() {}

    public TimeEntry(ObjectId userId, Instant clockInTime){
        this.userId =userId;
        this.clockInTime = clockInTime;
    }
    public  ObjectId getId() {
        return id;
    }
    public  void  setId(ObjectId id){
        this.id = id;
    }
    public ObjectId getUserId() {
        return userId;
    }
    public  void  setUserId(ObjectId userId){
        this.userId = userId;
    }
    public Instant getClockInTime() {
        return clockInTime;
    }
    public void setClockInTime(Instant clockInTime) {
        this.clockInTime = clockInTime;
    }
    public Instant getClockOutTime() {
        return clockOutTime;
    }
    public void setClockOutTime(Instant clockOutTime) {
        this.clockOutTime = clockOutTime;
    }
    public long getTotalSeconds() {
        return totalSeconds;
    }
    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }
    public boolean isOnBreak(){return  onBreak;}
    public  void setOnBreak(boolean onBreak){this.onBreak = onBreak;}
    public  List<BreakEntry> getBreaks() { return  breaks;}
    public void setBreaks(List<BreakEntry> breaks){this.breaks = breaks}

}
