package dao;

import models.User;

/**
 * Contract interface defining operations for User data access.
 * Implemented by UserDAO to promote loose coupling and interface-driven design.
 */
public interface UserDAOInterface {

    /**
     * Authenticates user by email and password.
     */
    User authenticate(String email, String password);

    /**
     * Checks if email address is already in use.
     */
    boolean isEmailTaken(String email);

    /**
     * Registers a new user with encrypted/plain credentials.
     */
    boolean registerUser(User user, String plainPassword);
}
