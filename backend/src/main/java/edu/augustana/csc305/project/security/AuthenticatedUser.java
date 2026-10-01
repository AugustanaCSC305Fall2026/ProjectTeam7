package edu.augustana.csc305.project.security;

/** The logged-in user, as identified by their JWT token. */
public record AuthenticatedUser(String id, String email) { }
