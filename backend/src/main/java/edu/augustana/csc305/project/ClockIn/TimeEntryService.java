package edu.augustana.csc305.project.ClockIn;


import edu.augustana.csc305.project.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.time.Duration;
import java.time.Instant;

public class TimeEntryService {

    private final TimeEntryRepo repo;

    public TimeEntryService(TimeEntryRepo repo) {
        this.repo = repo;
    }

    public TimeEntry clockIn(User user) {
        ObjectId userId = user.getId();

        //Don't let them clock in twice
        if (repo.findByUserIdAndClockOutTimeIsNull(userId).isPresent()) {
            throw new IllegalStateException("You are already clocked in");
        }
        TimeEntry entry = new TimeEntry(userId, Instant.now());
        return repo.save(entry);
    }

    public TimeEntry clockOut(User user) {
        ObjectId userId = user.getId();

        TimeEntry active = repo.findByUserIdAndClockOutTimeIsNull(userId).orElseThrow(() -> new IllegalStateException("You are not clocked in"));

        Instant now = Instant.now();
        active.setClockOutTime(now);

        long secWorked = Duration.between(active.getClockInTime(), now).getSeconds();
        active.setTotalSeconds(secWorked);

        return repo.save(active);

    }

    public TimeEntry startBreak(User user) {
        ObjectId userId = user.getId();

        TimeEntry active = repo.findByUserIdAndClockOutTimeIsNull(userId).orElseThrow(() -> new IllegalStateException("You are not clocked in"));

        if (active.isOnBreak()) {
            throw new IllegalStateException("You are already on break");
        }
        active.setOnBreak(true);
        active.getBreaks().add((new BreakEntry(Instant.now())));
        return repo.save(active);
    }
        public TimeEntry endBreak(User user){
            ObjectId userId = user.getId();

            TimeEntry active = repo.findByUserIdAndClockOutTimeIsNull(userId).orElseThrow(() -> new IllegalStateException("You are not clocked in"));

            if (!active.isOnBreak()) {
                throw new IllegalStateException("You are not on break");
            }
            BreakEntry lastBreak = active.getBreaks().get(active.getBreaks().size() - 1);
            lastBreak.setBreakEnd(Instant.now());
            active.setOnBreak(false);
            return repo.save(active);

        }


    }
