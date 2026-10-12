package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.Shift;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;
import edu.augustana.csc305.project.model.ShiftStatus;

import org.bson.types.ObjectId;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;


@Repository
public class ShiftRepository {
    private final MongoCollection<Shift> shifts;

    public ShiftRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        shifts = database.getCollection("shifts", Shift.class);
    }

    public Optional<Shift> findById(ObjectId shiftId){
        Shift shift = shifts.find(
            Filters.eq("_id", shiftId)
        ).first();

        return Optional.ofNullable(shift);
    }

    public void storeShift(Shift shift){
        shifts.insertOne(shift);
    }

    public void updateShift(Shift shift){
        shifts.replaceOne(
            Filters.eq("_id", shift.getId()), shift
        );
    }

    public List<Shift> findOfferedShifts(){
        List<Shift> results = new ArrayList<>();

        for (Shift shift : shifts.find(Filters.eq("status", ShiftStatus.OFFERED))){
            results.add(shift);
        }
        return results;
    }

    public List<Shift> findOfferedShiftsBetween(Instant from, Instant to) {
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

    public List<Shift> findByEmploymentId(ObjectId employmentId){
        List<Shift> results = new ArrayList<>();

        for (Shift shift : shifts.find(Filters.eq("employmentId", employmentId))){
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
                        Updates.set("offer.claimedByEmploymentId", employmentId),
                        Updates.set("offer.claimedAt", claimedAt)
                ),
                new FindOneAndUpdateOptions()
                        .returnDocument(ReturnDocument.AFTER)
        );

        return Optional.ofNullable(updatedShift);
    }

}