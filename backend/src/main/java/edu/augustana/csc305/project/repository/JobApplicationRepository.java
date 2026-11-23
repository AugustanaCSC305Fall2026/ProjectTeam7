package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.JobApplication;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
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

    public List<JobApplication> findByUserId(ObjectId userId) {
        List<JobApplication> results = new ArrayList<>();

        for (JobApplication application : applications.find(
                Filters.eq("userId", userId))) {
            results.add(application);
        }

        return results;
    }

    public List<JobApplication> findByJobId(ObjectId jobId) {
        List<JobApplication> results = new ArrayList<>();

        for (JobApplication application : applications.find(
                Filters.eq("jobId", jobId))) {
            results.add(application);
        }

        return results;
    }

    public Optional<JobApplication> findByIdAndJobId(
            ObjectId applicationId,
            ObjectId jobId) {
        JobApplication application = applications.find(Filters.and(
                Filters.eq("_id", applicationId),
                Filters.eq("jobId", jobId)
        )).first();

        return Optional.ofNullable(application);
    }

    public boolean updateStatusIfCurrentStatusIn(
            ObjectId applicationId,
            ObjectId jobId,
            List<String> currentStatuses,
            String newStatus) {
        return applications.updateOne(Filters.and(
                        Filters.eq("_id", applicationId),
                        Filters.eq("jobId", jobId),
                        Filters.in("status", currentStatuses)
                ),
                Updates.set("status", newStatus)
        ).getMatchedCount() == 1;
    }

    public void storeApplication(JobApplication application) {
        applications.insertOne(application);
    }
}
