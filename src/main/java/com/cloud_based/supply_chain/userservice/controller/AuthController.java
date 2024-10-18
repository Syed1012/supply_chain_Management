package com.cloud_based.supply_chain.userservice.controller;

import com.cloud_based.supply_chain.userservice.config.JwtUtil;
import com.cloud_based.supply_chain.userservice.model.AuthenticationRequest;
import com.cloud_based.supply_chain.userservice.model.AuthenticationResponse;
import com.cloud_based.supply_chain.userservice.model.User;
import com.cloud_based.supply_chain.userservice.repository.UserRepository;
import com.cloud_based.supply_chain.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // User Registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authRequest) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect email or password");
        }

        // Get User from repository
        Optional<User> optionalUser = userRepository.findByEmail(authRequest.getEmail());

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get(); // Get the actual User object

        // Generate JWT using user details
        final String jwt = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
