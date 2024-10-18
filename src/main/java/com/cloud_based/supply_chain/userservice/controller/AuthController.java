package com.cloud_based.supply_chain.userservice.controller;

import com.cloud_based.supply_chain.userservice.config.JwtUtil;
import com.cloud_based.supply_chain.userservice.model.AuthenticationRequest;
import com.cloud_based.supply_chain.userservice.model.AuthenticationResponse;
import com.cloud_based.supply_chain.userservice.model.User;
import com.cloud_based.supply_chain.userservice.repository.UserRepository;
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
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    // User Login: Generates both Access and Refresh tokens
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

        // Fetch user details from repository
        Optional<User> optionalUser = userRepository.findByEmail(authRequest.getEmail());

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();

        // Generate both Access and Refresh tokens
        final String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        final String refreshToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken));
    }

    // Refresh Access Token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        // Extract the username from the refresh token
        String username = jwtUtil.extractUsername(refreshToken);

        // Validate refresh token
        if (jwtUtil.validateRefreshToken(refreshToken, username)) {
            // Fetch the user details from the database
            Optional<User> optionalUser = userRepository.findByEmail(username);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = optionalUser.get();

            // Generate a new Access Token
            final String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

            return ResponseEntity.ok(new AuthenticationResponse(newAccessToken, refreshToken));
        } else {
            return ResponseEntity.status(403).body("Invalid or expired refresh token");
        }
    }
}
