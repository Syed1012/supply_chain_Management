package com.cloud_based.supply_chain.userservice.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloud_based.supply_chain.userservice.model.User;
import com.cloud_based.supply_chain.userservice.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // User Registration
    public User registerUser(User user) {
        // Check if the email or phone number already exists
        Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());

        if (existingUserByEmail.isPresent()) {
            throw new RuntimeException("Email or Phone number already registered");
        }

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(2); // Default role for users

        return userRepository.save(user);
    }

    // User Login
    public User loginUser(String email, String password) {
        // Find user by email
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            // Check for password match
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                return user.get();
            } else {
                throw new RuntimeException("Invalid Password");
            }
        } else {
            throw new RuntimeException("User not Found.");
        }
    }
}
