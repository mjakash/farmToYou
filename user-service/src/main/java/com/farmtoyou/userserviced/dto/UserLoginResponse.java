package com.farmtoyou.userserviced.dto;

import com.farmtoyou.userservice.entity.UserRole;

import lombok.Data;

@Data
public class UserLoginResponse {
	private Long id;
	private String name;
	private String email;
	private UserRole role;
	private String token;
	private String message;
}
