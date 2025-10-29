package com.farmtoyou.userserviced.dto;

import com.farmtoyou.userservice.entity.UserRole;

import lombok.Data;

@Data
public class UserRegistrationRequest {
	private String name;
	private String email;
	private String password;
	private UserRole role;
}
