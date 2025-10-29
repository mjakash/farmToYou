package com.farmtoyou.orderservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String unit; 
    private Long farmerId;
}