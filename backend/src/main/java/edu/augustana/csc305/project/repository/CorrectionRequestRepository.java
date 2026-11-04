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
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;

import java.time.Instant;
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

    public Optional<CorrectionRequest> findPendingById(ObjectId correctionRequestId) {
        CorrectionRequest request = requests.find(Filters.and(
                Filters.eq("_id", correctionRequestId),
                Filters.eq("status", CorrectionStatus.PENDING)
        )).first();

        return Optional.ofNullable(request);
    }

    public void store(CorrectionRequest request) {
        requests.insertOne(request);
    }

    public Optional<CorrectionRequest> updatePendingStatus(
        ObjectId correctionRequestId,
        CorrectionStatus newStatus,
        String reviewNote,
        Instant approvedClockInAt,
        Instant approvedClockOutAt) {

        CorrectionRequest updated = requests.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", correctionRequestId),
                        Filters.eq("status", CorrectionStatus.PENDING)
                ),
                Updates.combine(
                        Updates.set("status", newStatus),
                        Updates.set("reviewNote", reviewNote),
                        Updates.set("approvedClockInAt", approvedClockInAt),
                        Updates.set("approvedClockOutAt", approvedClockOutAt)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updated);
    }
}
