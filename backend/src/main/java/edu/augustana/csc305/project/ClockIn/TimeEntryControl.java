package edu.augustana.csc305.project.ClockIn;

import edu.augustana.csc305.project.model.User;
import org.bson.types.ObjectId;

public class TimeEntryControl {

    private final TimeEntryService service;

    public  TimeEntryControl(TimeEntryService serv){
        this.service = serv;
    }

    private  User mockUser(){
        User u = new User();
        u.setId(new ObjectId("67vjhfiueihfe8569ha"));
        u.setName("Test User");
        u.setEmail("test@exam.com");
        return u;
    }
    public  TimeEntry clockIn() {
        return  service.clockIn(mockUser());
    }

    public  TimeEntry clockOut() {
        return service.clockOut(mockUser());
    }

    public TimeEntry startBreak(){
        return service.startBreak(mockUser());
    }
    public TimeEntry endBreak(){
        return service.endBreak(mockUser());
    }

}
