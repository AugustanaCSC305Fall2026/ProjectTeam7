package edu.augustana.csc305.project.ClockIn;
import  edu.augustana.csc305.project.ClockIn.TimeEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;


public interface TimeEntryRepo extends  MongoRepository<TimeEntry,ObjectId>{
   Optional<TimeEntry> findByUserIdAndClockOutTimeIsNull(ObjectId userId);
}
