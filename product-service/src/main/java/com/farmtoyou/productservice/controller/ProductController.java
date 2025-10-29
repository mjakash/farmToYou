package com.farmtoyou.productservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmtoyou.productservice.dto.ProductRequest;
import com.farmtoyou.productservice.dto.ProductResponse;
import com.farmtoyou.productservice.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
		ProductResponse createdProduct = productService.createProduct(productRequest);
		return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
	}

	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<List<ProductResponse>> getProductsByFarmer(@PathVariable Long farmerId) {
		List<ProductResponse> products = productService.getProductsByFarmerId(farmerId);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

}
