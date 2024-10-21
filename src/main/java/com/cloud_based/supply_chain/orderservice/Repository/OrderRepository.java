package com.cloud_based.supply_chain.orderservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.cloud_based.supply_chain.orderservice.model.Order;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    // Custom query to find orders by userId
    List<Order> findByUserId(String userId);
}
