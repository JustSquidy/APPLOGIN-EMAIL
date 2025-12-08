package org.accesspointprogram;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class UserEmailService {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> userCollection;

    public UserEmailService() {
        // Replace with your MongoDB connection string if different
        this.mongoClient = MongoClients.create("mongodb+srv://Javier2025:AC25@cluster0.rmboum5.mongodb.net/");
        this.database = mongoClient.getDatabase("USER_DATA");
        this.userCollection = database.getCollection("users");
    }

    // Get emails of unverified users only
    public List<String> getUnverifiedUserEmails() {
        List<String> emails = new ArrayList<>();
        userCollection.find(new Document("email_verified", false))
                     .projection(new Document("email", 1).append("_id", 0))
                     .forEach(doc -> {
                         String email = doc.getString("email");
                         if (email != null) {
                             emails.add(email);
                         }
                     });
        return emails;
    }

    // Close the MongoDB connection when done yesah
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}