package com.cloud_based.supply_chain.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloud_based.supply_chain.userservice.config.JwtUtil;
import com.cloud_based.supply_chain.userservice.model.User;
import com.cloud_based.supply_chain.userservice.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // Logger instance

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User registerdUser = userService.registerUser(user);
        return ResponseEntity.ok(registerdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User loginRequest) {
        try {
            // Extract email and password from the request body
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            // Proceed with authentication logic of user
            User user = userService.loginUser(email, password);

            // Extract necessary details for token generation
            String username = user.getUsername();
            String userId = user.getId(); // Assuming you have a getId() method for userId
            Integer role = user.getRole(); // Assuming you have a getRole() method for the user's role

            // Generate JWT token
            String token = jwtUtil.generateToken(username, userId, role);

            // Log the token in the terminal
            logger.info("Generated JWT Token: {}", token);

            return ResponseEntity.ok("Login successful for user: " + user.getUsername() + " token: " + token);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

}
