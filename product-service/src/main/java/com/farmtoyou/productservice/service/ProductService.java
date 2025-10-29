package com.farmtoyou.productservice.service;

import java.util.List;

import com.farmtoyou.productservice.dto.ProductRequest;
import com.farmtoyou.productservice.dto.ProductResponse;

public interface ProductService {
	ProductResponse createProduct(ProductRequest productRequest);
	List<ProductResponse> getProductsByFarmerId(Long farmerId);
	ProductResponse getProductById(Long productId);
}
