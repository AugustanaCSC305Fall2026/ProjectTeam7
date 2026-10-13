package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import edu.augustana.csc305.project.model.TimeEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TimeEntryRepository {
    private final MongoCollection<TimeEntry> timeEntries;

    public TimeEntryRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        timeEntries = database.getCollection("timeEntries", TimeEntry.class);
    }

    public Optional<TimeEntry> findByIdAndUserId(ObjectId timeEntryId, ObjectId userId) {
        TimeEntry entry = timeEntries.find(Filters.and(
                Filters.eq("_id", timeEntryId),
                Filters.eq("userId", userId)
        )).first();

        return Optional.ofNullable(entry);
    }

    public List<TimeEntry> findByUserId(ObjectId userId) {
        List<TimeEntry> results = new ArrayList<>();

        for (TimeEntry entry : timeEntries
                .find(Filters.eq("userId", userId))
                .sort(Sorts.descending("clockInAt"))) {
            results.add(entry);
        }

        return results;
    }
}