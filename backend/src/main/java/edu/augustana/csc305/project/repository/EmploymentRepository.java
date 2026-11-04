package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.Employment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmploymentRepository {
    private final MongoCollection<Employment> employments;

    public EmploymentRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        employments = database.getCollection("employments", Employment.class);
    }

    public List<Employment> findByUserId(ObjectId userId) {
        List<Employment> results = new ArrayList<>();

        for (Employment employment : employments.find(Filters.eq("userId", userId))) {
            results.add(employment);
        }

        return results;
    }

    public List<Employment> findActiveByJobId(ObjectId jobId) {
        List<Employment> results = new ArrayList<>();

        for (Employment employment : employments.find(
                Filters.and(
                        Filters.eq("jobId", jobId),
                        Filters.eq("active", true)))) {
            results.add(employment);
        }

        return results;
    }

    public Optional<Employment> findById(ObjectId employmentId) {
        Employment employment = employments.find(Filters.eq("_id", employmentId)).first();
        return Optional.ofNullable(employment);
    }

    public Optional<Employment> findByIdAndUserId(ObjectId employmentId, ObjectId userId) {
        Employment employment = employments.find(
                Filters.and(
                        Filters.eq("_id", employmentId),
                        Filters.eq("userId", userId)
                )
        ).first();

        return Optional.ofNullable(employment);
    }

    public boolean hasActiveEmploymentForUserAndJob(
            ObjectId userId,
            ObjectId jobId) {
        return employments.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("jobId", jobId),
                Filters.eq("active", true)
        )).first() != null;
    }

    public boolean deactivateActiveByIdAndJobId(
            ObjectId employmentId,
            ObjectId jobId) {
        return employments.updateOne(
                Filters.and(
                        Filters.eq("_id", employmentId),
                        Filters.eq("jobId", jobId),
                        Filters.eq("active", true)
                ),
                Updates.set("active", false)
        ).getModifiedCount() == 1;
    }

    public void storeEmployment(Employment employment) {
        employments.insertOne(employment);
    }
}
