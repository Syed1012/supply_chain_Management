package com.cloud_based.supply_chain.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cloud_based.supply_chain.InventoryService.model.Product;
import com.cloud_based.supply_chain.orderservice.Repository.OrderRepository;
import com.cloud_based.supply_chain.orderservice.dto.OrderUpdateRequest;
import com.cloud_based.supply_chain.orderservice.model.Order;

// import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Assuming InventoryService URL
    private final String INVENTORY_SERVICE_URL = "http://localhost:8081/api/products";

    @Autowired
    private RestTemplate restTemplate; // Use RestTemplate to interact with InventoryService

    // Validate product IDs
    private boolean areProductIdsValid(List<String> productIds) {
        for (String productId : productIds) {
            String url = INVENTORY_SERVICE_URL + productId;
            Product product = restTemplate.getForObject(url, Product.class);
            if (product == null || product.getQuantity() <= 0) {
                return false; // Product doesn't exist or has no quantity
            }
        }
        return true;
    }

    // Create a new order
    public Order createOrder(String userId, List<String> productIds, double totalPrice) {
        // Validate product IDs and quantities
        if (!areProductIdsValid(productIds)) {
            throw new IllegalArgumentException("Invalid product IDs or insufficient quantities.");
        }
        Order order = new Order(userId, productIds, "Pending", totalPrice, java.time.LocalDate.now().toString());
        return orderRepository.save(order); // Save order in MongoDB
    }

    // Get all orders for a specific user
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    // Fetch a specific order by its ID
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    // Delete an order
    public boolean deleteOrderById(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            restoreProductQuantities(order.getProductIds()); // Restore quantities before deleting
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }

    // Restore product quantities when the order is deleted
    private void restoreProductQuantities(List<String> productIds) {
        for (String productId : productIds) {
            String url = INVENTORY_SERVICE_URL + productId;
            Product product = restTemplate.getForObject(url, Product.class);

            if (product != null) {
                product.setQuantity(product.getQuantity() + 1); // Increase quantity by 1
                restTemplate.put(INVENTORY_SERVICE_URL + productId, product);
            }
        }
    }

    // Update an existing order (not just status, but the entire order)
    public Order updateOrder(String orderId, OrderUpdateRequest orderUpdateRequest) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setProductIds(orderUpdateRequest.getProductIds());
            order.setTotalPrice(orderUpdateRequest.getTotalPrice());
            order.setOrderStatus(orderUpdateRequest.getOrderStatus());
            return orderRepository.save(order); // Save updated order
        }
        return null;
    }

    // Update order status (and reduce product quantity if the status is
    // "Completed")
    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setOrderStatus(status);
            if (status.equals("Completed")) {
                reduceProductQuantities(order.getProductIds()); // Call to InventoryService
            }
            return orderRepository.save(order);
        }
        return null;
    }

    // Reduce product quantities when the order is completed
    private void reduceProductQuantities(List<String> productIds) {
        // Count occurrences of each product ID
        Map<String, Long> productCountMap = productIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (Map.Entry<String, Long> entry : productCountMap.entrySet()) {
            String productId = entry.getKey();
            long quantityToReduce = entry.getValue();

            // Fetch product from InventoryService
            String url = INVENTORY_SERVICE_URL + productId;
            Product product = restTemplate.getForObject(url, Product.class);

            if (product != null && product.getQuantity() >= quantityToReduce) {
                product.setQuantity(product.getQuantity() - (int) quantityToReduce); // Cast to int, Decrease by the ordered quantity

                // Update the product back in InventoryService
                restTemplate.put(INVENTORY_SERVICE_URL + productId, product);
            } else {
                throw new IllegalArgumentException("Not enough quantity for product ID: " + productId);
            }
        }
    }
}