package com.cloud_based.supply_chain.InventoryService.Repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cloud_based.supply_chain.InventoryService.model.Product;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategory(String category); // Custom method to find products by category
}
