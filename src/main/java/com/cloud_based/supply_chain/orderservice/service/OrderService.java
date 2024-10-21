package com.cloud_based.supply_chain.orderservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cloud_based.supply_chain.InventoryService.model.Product;
import com.cloud_based.supply_chain.orderservice.Repository.OrderRepository;
import com.cloud_based.supply_chain.orderservice.dto.OrderUpdateRequest;
import com.cloud_based.supply_chain.orderservice.model.Order;
import com.cloud_based.supply_chain.orderservice.exception.InvalidOrderStatusException;
import com.cloud_based.supply_chain.orderservice.exception.InsufficientInventoryException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private final String INVENTORY_SERVICE_URL = "http://localhost:8081/api/products";

    @Autowired
    private RestTemplate restTemplate;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    // Validate product IDs and their quantities
    private Map<String, Integer> validateProducts(List<String> productIds) {
        // Count occurrences of each product ID
        Map<String, Long> productCountMap = productIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        // Validate each product and its quantity
        for (Map.Entry<String, Long> entry : productCountMap.entrySet()) {
            String productId = entry.getKey();
            Long requiredQuantity = entry.getValue();
            
            String url = INVENTORY_SERVICE_URL + "/product-details/" + productId;
            Product product = restTemplate.getForObject(url, Product.class);
            
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + productId);
            }
            
            if (product.getQuantity() < requiredQuantity) {
                throw new InsufficientInventoryException(
                    String.format("Insufficient quantity for product %s. Required: %d, Available: %d",
                        productId, requiredQuantity, product.getQuantity())
                );
            }
        }
        
        return productCountMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    // Create a new order
    public Order createOrder(String userId, List<String> productIds, double totalPrice) {
        // Validate products and their quantities
        validateProducts(productIds);
        
        // Create order with initial PENDING status
        Order order = new Order(userId, productIds, OrderStatus.PENDING.name(), 
                              totalPrice, java.time.LocalDate.now().toString());
        return orderRepository.save(order);
    }

    // Update order status
    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus currentStatus = OrderStatus.valueOf(order.getOrderStatus());
        OrderStatus targetStatus = OrderStatus.valueOf(newStatus);

        // Validate status transition
        if (!isValidStatusTransition(currentStatus, targetStatus)) {
            throw new InvalidOrderStatusException(
                String.format("Invalid status transition from %s to %s", currentStatus, targetStatus)
            );
        }

        // Handle quantity updates based on status change
        if (targetStatus == OrderStatus.CONFIRMED) {
            // Revalidate quantities and reduce them
            Map<String, Integer> productQuantities = validateProducts(order.getProductIds());
            reduceProductQuantities(productQuantities);
        } else if (targetStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.CONFIRMED) {
            // Restore quantities only if cancelling a confirmed order
            Map<String, Integer> productQuantities = order.getProductIds().stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
            restoreProductQuantities(productQuantities);
        }

        order.setOrderStatus(targetStatus.name());
        return orderRepository.save(order);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.PENDING) {
            return target == OrderStatus.CONFIRMED || target == OrderStatus.CANCELLED;
        }
        return false; // No other transitions allowed
    }

    // Delete an order
    public boolean deleteOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Only restore quantities if the order was CONFIRMED
        if (OrderStatus.valueOf(order.getOrderStatus()) == OrderStatus.CONFIRMED) {
            Map<String, Integer> productQuantities = order.getProductIds().stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
            restoreProductQuantities(productQuantities);
        }

        orderRepository.deleteById(orderId);
        return true;
    }

    private void reduceProductQuantities(Map<String, Integer> productQuantities) {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            int quantityToReduce = entry.getValue();

            String url = INVENTORY_SERVICE_URL + "/product-details/" + productId;
            Product product = restTemplate.getForObject(url, Product.class);

            if (product != null) {
                product.setQuantity(product.getQuantity() - quantityToReduce);
                restTemplate.put(INVENTORY_SERVICE_URL + "/update-product/" + productId, product);
            }
        }
    }

    private void restoreProductQuantities(Map<String, Integer> productQuantities) {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            int quantityToRestore = entry.getValue();

            String url = INVENTORY_SERVICE_URL + "/product-details/" + productId;
            Product product = restTemplate.getForObject(url, Product.class);

            if (product != null) {
                product.setQuantity(product.getQuantity() + quantityToRestore);
                restTemplate.put(INVENTORY_SERVICE_URL + "/update-product/" + productId, product);
            }
        }
    }

    // Other existing methods...
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public Order updateOrder(String orderId, OrderUpdateRequest orderUpdateRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // Only allow updates if order is in PENDING status
        if (OrderStatus.valueOf(order.getOrderStatus()) != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Can only update orders in PENDING status");
        }

        validateProducts(orderUpdateRequest.getProductIds());
        
        order.setProductIds(orderUpdateRequest.getProductIds());
        order.setTotalPrice(orderUpdateRequest.getTotalPrice());
        return orderRepository.save(order);
    }
}