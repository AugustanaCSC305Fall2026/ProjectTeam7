
package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import edu.augustana.csc305.project.model.UserEvents;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserEventsRepository {

    private final MongoCollection<UserEvents> events;

    public UserEventsRepository(MongoDatabaseFactory factory) {
        MongoDatabase database = factory.getMongoDatabase();
        events = database.getCollection("userEvents", UserEvents.class);
    }

    public List<UserEvents> findByUserId(ObjectId userId) {
        List<UserEvents> results = new ArrayList<>();

        for (UserEvents event : events.find(Filters.eq("userId", userId))) {results.add(event);
        }

        return results;
    }

    public Optional<UserEvents> findByIdAndUserId(
            ObjectId id, ObjectId userId) {

        return Optional.ofNullable(events.find(Filters.and(Filters.eq("_id", id), Filters.eq("userId", userId))).first());
    }

    public void save(UserEvents event) {
        events.insertOne(event);
    }

    public void update(UserEvents event) {
        events.replaceOne(
            Filters.and(
                Filters.eq("_id", event.getId()),
                Filters.eq("userId", event.getUserId())
            ),
            event
        );
    }

    public boolean delete(ObjectId id, ObjectId userId) {
        return events.deleteOne(Filters.and(Filters.eq("_id", id), Filters.eq("userId", userId))).getDeletedCount() > 0;
    }
}

