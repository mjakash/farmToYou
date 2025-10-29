package com.farmtoyou.orderservice.entity;

public enum OrderStatus {
    PENDING_PAYMENT,        
    PENDING_FARMER_ACCEPTANCE, 
    FARMER_REJECTED,        
    FARMER_CONFIRMED,       
    PACKAGED,               
    OUT_FOR_DELIVERY,       
    DELIVERED,              
    CANCELLED               
}