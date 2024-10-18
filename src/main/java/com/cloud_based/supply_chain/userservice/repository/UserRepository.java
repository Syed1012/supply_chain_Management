package com.cloud_based.supply_chain.userservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cloud_based.supply_chain.userservice.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by email for login purposes
    Optional<User> findByEmail(String email);
    
    // Find user by email for login purposes
    Optional<User> findByUsername(String username);

    // Find user by phone number for other functionalities
    Optional<User> findByPhonenum(String phonenum);
}
