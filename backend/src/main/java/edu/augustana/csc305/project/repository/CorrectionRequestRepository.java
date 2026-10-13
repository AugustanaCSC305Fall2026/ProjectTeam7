package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import edu.augustana.csc305.project.model.CorrectionRequest;
import edu.augustana.csc305.project.model.CorrectionStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Repository
public class CorrectionRequestRepository {
    private final MongoCollection<CorrectionRequest> requests;

    public CorrectionRequestRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        requests = database.getCollection("correctionRequests", CorrectionRequest.class);
    }

    public boolean hasPendingRequest(ObjectId timeEntryId, ObjectId userId) {
        return requests.find(Filters.and(
                Filters.eq("timeEntryId", timeEntryId),
                Filters.eq("userId", userId),
                Filters.eq("status", CorrectionStatus.PENDING)
        )).first() != null;
    }
    
    public List<CorrectionRequest> findPending() {
        List<CorrectionRequest> results = new ArrayList<>();

        for (CorrectionRequest request : requests.find(
                Filters.eq("status", CorrectionStatus.PENDING)
        ).sort(Sorts.ascending("createdAt"))) {
            results.add(request);
        }

        return results;
    }

    public void store(CorrectionRequest request) {
        requests.insertOne(request);
    }
}
