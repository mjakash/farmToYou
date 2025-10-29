package com.farmtoyou.productservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.farmtoyou.productservice.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByFarmerId(Long farmerId);
}
