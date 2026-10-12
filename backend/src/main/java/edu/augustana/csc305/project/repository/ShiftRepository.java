package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import edu.augustana.csc305.project.model.Shift;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class ShiftRepository {
    private final MongoCollection<Shift> shifts;

    public ShiftRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        shifts = database.getCollection("shifts", Shift.class);
    }

    public List<Shift> findByEmploymentIdsAndRange(
            List<ObjectId> employmentIds, Instant from, Instant to) {
        if (employmentIds.isEmpty()) {
            return List.of();
        }

        Bson filter = Filters.and(
                Filters.in("employmentId", employmentIds),
                Filters.gte("startAt", Date.from(from)),
                Filters.lt("startAt", Date.from(to))
        );

        List<Shift> results = new ArrayList<>();
        for (Shift shift : shifts.find(filter).sort(Sorts.ascending("startAt"))) {
            results.add(shift);
        }
        return results;
    }
}
