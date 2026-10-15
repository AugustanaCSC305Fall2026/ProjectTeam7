package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * User object as stored in the database.
 */
@Document(collection = "users")
public class User {
    @Id private ObjectId id;
    private String firstName;
    private String lastName;
    /** Retained for compatibility with users stored by the starter application. */
    private String name;
    private String email;  // Unique: see the index created in UserRepository.
    private String passwordHash;
    private List<String> roles;

    public User() { }

    public User(ObjectId id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = List.of("USER");
    }

    public User(ObjectId id, String firstName, String lastName, String email,
                String passwordHash, List<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = joinName(firstName, lastName);
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles == null ? List.of("USER") : List.copyOf(roles);
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public List<String> getRoles() { return roles == null ? List.of("USER") : roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    private static String joinName(String firstName, String lastName) {
        return String.join(" ", firstName == null ? "" : firstName,
                lastName == null ? "" : lastName).trim();
    }
}
