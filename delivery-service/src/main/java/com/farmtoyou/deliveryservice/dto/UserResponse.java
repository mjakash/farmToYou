package com.farmtoyou.deliveryservice.dto;

import lombok.Data;

@Data
public class UserResponse {
	private Long id;
	private String name;
	private String email;
	private String phone;
	private String address;
	private String role;
}