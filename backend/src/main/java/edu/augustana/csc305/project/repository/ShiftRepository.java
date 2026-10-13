package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.Shift;
import edu.augustana.csc305.project.model.ShiftStatus;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ShiftRepository {

    private final MongoCollection<Shift> shifts;

    public ShiftRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        shifts = database.getCollection("shifts", Shift.class);
    }

    public Optional<Shift> findById(ObjectId shiftId) {
        Shift shift = shifts.find(
                Filters.eq("_id", shiftId)
        ).first();

        return Optional.ofNullable(shift);
    }

    public void storeShift(Shift shift) {
        shifts.insertOne(shift);
    }
    public boolean hasConflictingShift(
        ObjectId employmentId,
        Instant startAt,
        Instant endAt) {

        Bson filter = Filters.and(
                Filters.eq("employmentId", employmentId),
                Filters.ne("status", ShiftStatus.CANCELLED),
                Filters.lt("startAt", Date.from(endAt)),
                Filters.gt("endAt", Date.from(startAt))
    );

    return shifts.find(filter).first() != null;
}

    public void updateShift(Shift shift) {
        shifts.replaceOne(
                Filters.eq("_id", shift.getId()),
                shift
        );
    }

    public List<Shift> findOfferedShifts() {
        List<Shift> results = new ArrayList<>();

        for (Shift shift :
                shifts.find(Filters.eq("status", ShiftStatus.OFFERED))) {
            results.add(shift);
        }

        return results;
    }

    public List<Shift> findOfferedShiftsBetween(
            Instant from,
            Instant to) {

        List<Shift> results = new ArrayList<>();

        for (Shift shift : shifts.find(
                Filters.and(
                        Filters.eq("status", ShiftStatus.OFFERED),
                        Filters.gte("startAt", from),
                        Filters.lte("startAt", to)
                )
        )) {
            results.add(shift);
        }

        return results;
    }

    public List<Shift> findByEmploymentId(ObjectId employmentId) {
        List<Shift> results = new ArrayList<>();

        for (Shift shift :
                shifts.find(Filters.eq("employmentId", employmentId))) {
            results.add(shift);
        }

        return results;
    }

    public Optional<Shift> claimOfferedShift(
            ObjectId shiftId,
            ObjectId employmentId,
            Instant claimedAt) {

        Shift updatedShift = shifts.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", shiftId),
                        Filters.eq("status", ShiftStatus.OFFERED),
                        Filters.eq("offer.claimedByEmploymentId", null)
                ),
                Updates.combine(
                        Updates.set("employmentId", employmentId),
                        Updates.set("status", ShiftStatus.SCHEDULED),
                        Updates.set(
                                "offer.claimedByEmploymentId",
                                employmentId
                        ),
                        Updates.set(
                                "offer.claimedAt",
                                claimedAt
                        )
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updatedShift);
    }

    public List<Shift> findByEmploymentIdsAndRange(
            List<ObjectId> employmentIds,
            Instant from,
            Instant to) {

        if (employmentIds.isEmpty()) {
            return List.of();
        }

        Bson filter = Filters.and(
                Filters.in("employmentId", employmentIds),
                Filters.gte("startAt", Date.from(from)),
                Filters.lt("startAt", Date.from(to))
        );

        List<Shift> results = new ArrayList<>();

        for (Shift shift :
                shifts.find(filter).sort(Sorts.ascending("startAt"))) {
            results.add(shift);
        }

        return results;
    }
}