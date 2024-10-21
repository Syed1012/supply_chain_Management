package com.cloud_based.supply_chain.orderservice.dto;

import java.util.List;

public class OrderUpdateRequest {
    
    private List<String> productIds;
    private double totalPrice;
    private String orderStatus;

    // Getters and Setters
    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

}
