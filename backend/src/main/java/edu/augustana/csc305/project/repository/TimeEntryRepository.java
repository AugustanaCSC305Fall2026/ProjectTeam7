package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import edu.augustana.csc305.project.model.TimeEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.EntrySource;

import java.time.Instant;
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

    public Optional<TimeEntry> findById(ObjectId timeEntryId) {
        TimeEntry entry = timeEntries.find(
                Filters.eq("_id", timeEntryId)
        ).first();

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

    public Optional<TimeEntry> findOpenByUserId(ObjectId userId) {
        TimeEntry entry = timeEntries.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("clockOutAt", null)
        )).sort(Sorts.descending("clockInAt")).first();

        return Optional.ofNullable(entry);
    }

    public void storeTimeEntry(TimeEntry entry) {
        timeEntries.insertOne(entry);
    }

    public List<TimeEntry> findByEmploymentIds(List<ObjectId> employmentIds) {
        if (employmentIds == null || employmentIds.isEmpty()) {
                return List.of();
        }

        List<TimeEntry> results = new ArrayList<>();

        for (TimeEntry entry : timeEntries
                .find(Filters.in("employmentId", employmentIds))
                .sort(Sorts.descending("clockInAt"))) {
                results.add(entry);
        }

        return results;
        }

    public Optional<TimeEntry> clockOutOpenEntry(
            ObjectId timeEntryId,
            ObjectId userId,
            Instant clockOutAt,
            int workedMinutes) {

        TimeEntry updated = timeEntries.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", timeEntryId),
                        Filters.eq("userId", userId),
                        Filters.eq("clockOutAt", null),
                        Filters.ne("onBreak", true)
                ),
                Updates.combine(
                        Updates.set("clockOutAt", clockOutAt),
                        Updates.set("workedMinutes", workedMinutes)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updated);
    }

    public Optional<TimeEntry> startBreak(
        ObjectId timeEntryId,
        ObjectId userId,
        Instant breakStartedAt) {

        TimeEntry updated = timeEntries.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", timeEntryId),
                        Filters.eq("userId", userId),
                        Filters.eq("clockOutAt", null),
                        Filters.ne("onBreak", true)
                ),
                Updates.combine(
                        Updates.set("onBreak", true),
                        Updates.set("breakStartedAt", breakStartedAt)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updated);
    }

    public Optional<TimeEntry> endBreak(
            ObjectId timeEntryId,
            ObjectId userId,
            Instant expectedBreakStartedAt,
            int additionalBreakMinutes) {

        TimeEntry updated = timeEntries.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", timeEntryId),
                        Filters.eq("userId", userId),
                        Filters.eq("clockOutAt", null),
                        Filters.eq("onBreak", true),
                        Filters.eq("breakStartedAt", expectedBreakStartedAt)
                ),
                Updates.combine(
                        Updates.set("onBreak", false),
                        Updates.unset("breakStartedAt"),
                        Updates.inc("breakMinutes", additionalBreakMinutes)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updated);
    }


    public Optional<TimeEntry> applyCorrection(
        ObjectId timeEntryId,
        ObjectId userId,
        Instant expectedClockInAt,
        Instant expectedClockOutAt,
        Instant correctedClockInAt,
        Instant correctedClockOutAt,
        int workedMinutes) {

        TimeEntry updated = timeEntries.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", timeEntryId),
                        Filters.eq("userId", userId),
                        Filters.eq("clockInAt", expectedClockInAt),
                        Filters.eq("clockOutAt", expectedClockOutAt)
                ),
                Updates.combine(
                        Updates.set("clockInAt", correctedClockInAt),
                        Updates.set("clockOutAt", correctedClockOutAt),
                        Updates.set("workedMinutes", workedMinutes),
                        Updates.set("source", EntrySource.CORRECTION)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updated);
    }
}