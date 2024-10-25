package com.cloud_based.supply_chain.orderservice.dto;

import java.util.List;

public class OrderRequest {
    private List<String> productIds;
    private double totalPrice;

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
}
