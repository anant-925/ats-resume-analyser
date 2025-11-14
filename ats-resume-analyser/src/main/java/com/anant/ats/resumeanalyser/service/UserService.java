package com.anant.ats.resumeanalyser.service;

import com.anant.ats.resumeanalyser.model.User;
import com.anant.ats.resumeanalyser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates and saves a new user.
     * @param username The username for the new user.
     * @param password The plain-text password for the new user.
     * @param email The email for the new user.
     * @return The saved User object.
     * @throws Exception if the username or email already exists.
     */
    public User registerNewUser(String username, String password, String email) throws Exception {
        // Check if username or email already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("Username already exists: " + username);
        }
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(password);

        // Create the new user object
        User newUser = new User(username, hashedPassword, email);

        // Save to the database
        return userRepository.save(newUser);
    }
}