package com.farmtoyou.orderservice.dto;

import java.util.List;
import lombok.Data;

@Data
public class PackageOrderRequest {
    private List<String> imageUrls;
}