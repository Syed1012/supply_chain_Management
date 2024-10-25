package com.cloud_based.supply_chain.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cloud_based.supply_chain.userservice.service.CustomUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);

            try {
                // Extract and validate only access tokens
                email = jwtUtil.extractEmail(jwtToken);
                String userId = jwtUtil.extractUserId(jwtToken);
                Integer role = jwtUtil.extractRole(jwtToken);

                // Add null check for email
                if (email == null || email.trim().isEmpty()) {
                    logger.error("Email not found in token");
                    chain.doFilter(request, response);
                    return;
                }

                // Log or use userId and role as needed
                logger.info("Extracted email: " + email);
                logger.info("Extracted userId: " + userId);
                logger.info("Extracted role: " + role);

            } catch (ExpiredJwtException e) {
                logger.error("JWT Token Expired.");
                chain.doFilter(request, response);
                return;
            } catch (Exception e) {
                logger.error("JWT Token processing failed", e);
                chain.doFilter(request, response);
                return;
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Authenticate the user if JWT is valid and user not yet authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByEmail(email);

                if (jwtUtil.validateToken(jwtToken, email)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (UsernameNotFoundException e) {
                logger.error("User not found with email: " + email);
            }
        }
        chain.doFilter(request, response);
    }
}
