package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.Job;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JobRepository {
    private final MongoCollection<Job> jobs;

    public JobRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        jobs = database.getCollection("jobs", Job.class);
    }

    public List<Job> findAll() {
        List<Job> results = new ArrayList<>();

        for (Job job : jobs.find()) {
            results.add(job);
        }

        return results;
    }

    public Optional<Job> findById(ObjectId id) {
        Job job = jobs.find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(job);
    }

    public void storeJob(Job job) {
        jobs.insertOne(job);
    }

    public void assignManager(ObjectId jobId, ObjectId managerId) {
        jobs.updateOne(
                Filters.eq("_id", jobId),
                Updates.set("managerId", managerId)
        );
    }
}