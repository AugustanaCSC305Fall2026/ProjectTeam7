package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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

    public void storeEmployment(Employment employment) {
        employments.insertOne(employment);
    }
}
