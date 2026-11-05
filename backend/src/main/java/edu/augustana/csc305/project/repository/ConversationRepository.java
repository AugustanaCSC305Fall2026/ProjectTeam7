package edu.augustana.csc305.project.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import edu.augustana.csc305.project.model.Conversation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ConversationRepository {
    private final MongoCollection<Conversation> conversations;

    public ConversationRepository(MongoDatabaseFactory databaseFactory) {
        MongoDatabase database = databaseFactory.getMongoDatabase();
        conversations = database.getCollection("conversations", Conversation.class);
    }

    public void store(Conversation conversation) {
        conversations.insertOne(conversation);
    }

    public Optional<Conversation> findById(ObjectId conversationId) {
        return Optional.ofNullable(conversations.find(
                Filters.eq("_id", conversationId)).first());
    }

    public List<Conversation> findByMemberId(ObjectId memberId) {
        List<Conversation> results = new ArrayList<>();
        for (Conversation conversation : conversations.find(
                Filters.eq("memberIds", memberId))) {
            results.add(conversation);
        }
        return results;
    }

    public Optional<Conversation> findDirectConversation(ObjectId firstMemberId,
                                                          ObjectId secondMemberId) {
        for (Conversation conversation : conversations.find(
                Filters.eq("type", "DIRECT"))) {
            List<ObjectId> members = conversation.getMemberIds();
            if (members != null && members.size() == 2
                    && members.contains(firstMemberId)
                    && members.contains(secondMemberId)) {
                return Optional.of(conversation);
            }
        }
        return Optional.empty();
    }

    public void touch(ObjectId conversationId, Date updatedAt) {
        conversations.updateOne(
                Filters.eq("_id", conversationId),
                Updates.set("updatedAt", updatedAt));
    }
}
