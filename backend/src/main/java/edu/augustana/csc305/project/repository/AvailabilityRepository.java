package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import edu.augustana.csc305.project.model.Availability;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AvailabilityRepository {
    private final MongoCollection<Availability> availabilities;

    public AvailabilityRepository(MongoDatabaseFactory factory) {
        MongoDatabase database = factory.getMongoDatabase();
        availabilities = database.getCollection("availabilities", Availability.class);
        availabilities.createIndex(Indexes.ascending("userId", "dayOfWeek", "startTime"));
    }

    public List<Availability> findByUserId(ObjectId userId) {
        return availabilities.find(Filters.eq("userId", userId)).into(new ArrayList<>());
    }

    public void replaceForUser(ObjectId userId, List<Availability> windows) {
        availabilities.deleteMany(Filters.eq("userId", userId));
        if (!windows.isEmpty()) {
            availabilities.insertMany(windows);
        }
    }

    public List<Availability> findAll() {
        return availabilities.find().into(new ArrayList<>());
    }
}
