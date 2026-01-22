package org.accesspointprogram.Login.Registration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; 

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;


// This class initializes the connection to the MongoDB database and provides a connection so that the program can interact with it.
public class DatabaseService { // ------------------------------------------

    private final MongoClient client;
    private final MongoDatabase database;
    private final MongoCollection<Document> users;

    public DatabaseService() {

        client = MongoClients.create(
                "mongodb+srv://Javier2025:AC25@cluster0.rmboum5.mongodb.net/"
        );
        database = client.getDatabase("USER_DATA");
        users = database.getCollection("users");
    }


    //This boolean function checks to see if there the email being registered is already in use.
    public boolean isEmailInUse(String email) {
        Document doc = users.find(Filters.eq("email", email.toLowerCase())).first();
        return doc != null;
    }
    // This adds a new user entry to the database with the provided email, password, and account type. It also hashes the password before storing it so that the passwords are not plain text in the database.
    // This is also only ever called after checking to make sure that the email has been verified with the 6 digit code sent to the user's email.
    public void createUser(String email, String password, String accountType) {

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12)); // Hash the password with a salt for security (12 is "industry standard" according to jBCrypt docs).

        Document doc = new Document()
                .append("user_id", UUID.randomUUID().toString())
                .append("email", email.toLowerCase())
                .append("password_hash", hashedPassword)
                .append("account_type", accountType)
                .append("account_status", "active")
                .append("email_verified", true)
                .append("is_locked", false)
                .append("failed_login_attempts", 0)
                .append("created_at", Instant.now().toString())
                .append("updated_at", Instant.now().toString())
                .append("last_login", null)
                .append("login_count", 0);

        users.insertOne(doc);
    }
    // This function handles the login validation by checking for the hashed password stored in the database against the password provided during login. 
    // if it fails then the failed login attempts are incremented and if it reaches 5 then the account is locked.
    public boolean validateLogin(String email, String password) {

        Document user = users.find(Filters.eq("email", email.toLowerCase())).first();
        if (user == null) return false;

        if (user.getBoolean("is_locked")) return false;

        String storedHash = user.getString("password_hash");

        boolean matches = BCrypt.checkpw(password, storedHash);

        if (!matches) {
            incrementFailedAttempts(email);
            return false;
        }

        users.updateOne(
                Filters.eq("email", email.toLowerCase()),
                Updates.combine(
                    Updates.set("failed_login_attempts", 0),
                    Updates.set("last_login", Instant.now().toString()),
                    Updates.inc("login_count", 1),
                    Updates.set("updated_at", Instant.now().toString())
                )
        );
        return true;
    }
    // This function handles failed logins and locks the account after 5 failed attempts. "It might be a little overkill for this project but it's good practice for real world applications." - Javier
    private void incrementFailedAttempts(String email) {

        Document user = users.find(Filters.eq("email", email.toLowerCase())).first();
        if (user == null) return;

        int attempts = user.getInteger("failed_login_attempts", 0) + 1;

        if (attempts >= 5) {
            users.updateOne(
                    Filters.eq("email", email.toLowerCase()),
                    Updates.combine(
                        Updates.set("is_locked", true),
                        Updates.set("updated_at", Instant.now().toString())
                    )
            );
        } else {
            users.updateOne(
                    Filters.eq("email", email.toLowerCase()),
                    Updates.combine(
                        Updates.set("failed_login_attempts", attempts),
                        Updates.set("updated_at", Instant.now().toString())
                    )
            );
        }
    }
    //  this is used by EmailService program to get all unverified users.
    public List<String> getUnverifiedUserEmails() {
    List<String> emails = new ArrayList<>();

    users.find(Filters.eq("email_verified", false))
            .projection(new Document("email", 1).append("_id", 0))
            .forEach(doc -> emails.add(doc.getString("email")));

    return emails;
    }

    public void close() {
        client.close();
    }
}

    
