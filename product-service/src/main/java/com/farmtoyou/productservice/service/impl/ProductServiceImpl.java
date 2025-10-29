package com.farmtoyou.productservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.farmtoyou.productservice.dto.ProductRequest;
import com.farmtoyou.productservice.dto.ProductResponse;
import com.farmtoyou.productservice.entity.Product;
import com.farmtoyou.productservice.repository.ProductRepository;
import com.farmtoyou.productservice.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public ProductResponse createProduct(ProductRequest productRequest) {
		Product product = new Product();
		product.setName(productRequest.getName());
		product.setDescription(productRequest.getDescription());
		product.setPrice(productRequest.getPrice());
		product.setUnit(productRequest.getUnit());
		product.setImageUrl(productRequest.getImageUrl());
		product.setFarmerId(productRequest.getFarmerId());

		Product savedProduct = productRepository.save(product);

		return mapToProductResponse(savedProduct);
	}

	@Override
	public List<ProductResponse> getProductsByFarmerId(Long farmerId) {

		List<Product> products = productRepository.findByFarmerId(farmerId);

		return products.stream().map(this::mapToProductResponse).toList();
	}

	private ProductResponse mapToProductResponse(Product product) {
		ProductResponse response = new ProductResponse();
		response.setId(product.getId());
		response.setName(product.getName());
		response.setDescription(product.getDescription());
		response.setPrice(product.getPrice());
		response.setUnit(product.getUnit());
		response.setImageUrl(product.getImageUrl());
		response.setFarmerId(product.getFarmerId());
		return response;
	}

	@Override
	public ProductResponse getProductById(Long productId) {
	    Product product = productRepository.findById(productId)
	            .orElseThrow(() -> new RuntimeException("Product not found")); // Use a real exception
	    return mapToProductResponse(product);
	}

}
