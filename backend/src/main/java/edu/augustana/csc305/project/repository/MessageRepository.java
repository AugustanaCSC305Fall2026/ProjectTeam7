package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import edu.augustana.csc305.project.model.Message;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository {
    private final MongoCollection<Message> messages;

    public MessageRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        messages = database.getCollection("messages", Message.class);
    }

    public void store(Message message) {
        messages.insertOne(message);
    }

    public List<Message> findByConversationId(ObjectId conversationId) {
        List<Message> results = new ArrayList<>();
        for (Message message : messages.find(
                Filters.eq("conversationId", conversationId))
                .sort(Sorts.ascending("sentAt"))) {
            results.add(message);
        }
        return results;
    }

    public Optional<Message> findLatestByConversationId(ObjectId conversationId) {
        return Optional.ofNullable(messages.find(
                Filters.eq("conversationId", conversationId))
                .sort(Sorts.descending("sentAt"))
                .first());
    }
}
