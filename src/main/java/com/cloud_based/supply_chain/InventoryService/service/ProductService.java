package com.cloud_based.supply_chain.InventoryService.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud_based.supply_chain.InventoryService.Repository.ProductRepository;
import com.cloud_based.supply_chain.InventoryService.model.Product;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Method to add a new product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Method to update an existing product
    public Product updateProduct(String id, Product productDetails) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setProductName(productDetails.getProductName());
            product.setCategory(productDetails.getCategory());
            product.setPrice(productDetails.getPrice());
            product.setQuantity(productDetails.getQuantity());
            product.setDescription(productDetails.getDescription());
            return productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }

    // Method to get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Method to get a product by ID
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Method to delete a product
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    // Method to find products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}
