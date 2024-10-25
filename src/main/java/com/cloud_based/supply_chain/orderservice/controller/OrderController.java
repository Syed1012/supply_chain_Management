package com.cloud_based.supply_chain.orderservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_based.supply_chain.orderservice.dto.OrderRequest;
import com.cloud_based.supply_chain.orderservice.dto.OrderUpdateRequest;
import com.cloud_based.supply_chain.orderservice.exception.InsufficientInventoryException;
import com.cloud_based.supply_chain.orderservice.exception.InvalidOrderStatusException;
import com.cloud_based.supply_chain.orderservice.model.Order;
import com.cloud_based.supply_chain.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Place a new order
    @PostMapping("/create-order")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication) {
        String userId = authentication.getName(); // Extract userId from token
        Order newOrder = orderService.createOrder(userId, orderRequest.getProductIds(), orderRequest.getTotalPrice());
        return ResponseEntity.ok(newOrder);
    }

    // Get all orders for a specific user
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        String userId = authentication.getName(); // Extract userId from token
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Fetch a specific order by its ID
    @GetMapping("specific-orderId/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    // Delete an order by ID
    @DeleteMapping("/delete-order/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable String orderId) {
        boolean isDeleted = orderService.deleteOrderById(orderId);
        if (isDeleted) {
            return ResponseEntity.ok("Order deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // Update an existing order
    @PutMapping("/update/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable String orderId,
            @RequestBody OrderUpdateRequest orderUpdateRequest) {
        Order updatedOrder = orderService.updateOrder(orderId, orderUpdateRequest);
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedOrder);
    }

    // Update order status (and reduce quantities if necessary)
    @PutMapping("/update-status/{orderId}")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status.toUpperCase());
            return ResponseEntity.ok(updatedOrder);
        } catch (InvalidOrderStatusException | InsufficientInventoryException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
