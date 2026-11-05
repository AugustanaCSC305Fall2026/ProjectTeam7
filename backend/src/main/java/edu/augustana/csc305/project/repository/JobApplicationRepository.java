package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import edu.augustana.csc305.project.model.JobApplication;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JobApplicationRepository {
    private final MongoCollection<JobApplication> applications;

    public JobApplicationRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        applications = database.getCollection(
                "jobApplications",
                JobApplication.class);
    }

    public Optional<JobApplication> findByUserIdAndJobId(
            ObjectId userId,
            ObjectId jobId) {
        JobApplication application = applications.find(Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("jobId", jobId)
        )).first();

        return Optional.ofNullable(application);
    }

    public void storeApplication(JobApplication application) {
        applications.insertOne(application);
    }
}