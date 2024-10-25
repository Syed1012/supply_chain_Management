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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect email or password");
        }

        Optional<User> optionalUser = userRepository.findByEmail(authRequest.getEmail());

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();

        // Ensure we're passing the email here, not the username
        final String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        final String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken));
    }

    // Refresh Access Token
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        // Extract email from refresh token
        String email = jwtUtil.extractRefreshTokenEmail(refreshToken);
 
        // Validate the refresh token using the email
        if (jwtUtil.validateRefreshToken(refreshToken, email)) {
            // Fetch the user details from the database using email
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = optionalUser.get();

            // Generate a new Access Token using the user's email
            final String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

            return ResponseEntity.ok(new AuthenticationResponse(newAccessToken, refreshToken));
        } else {
            return ResponseEntity.status(403).body("Invalid or expired refresh token");
        }
    }
}
