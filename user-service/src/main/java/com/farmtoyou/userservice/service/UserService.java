package com.farmtoyou.userservice.service;

import com.farmtoyou.userserviced.dto.UserLoginRequest;
import com.farmtoyou.userserviced.dto.UserLoginResponse;
import com.farmtoyou.userserviced.dto.UserRegistrationRequest;
import com.farmtoyou.userserviced.dto.UserResponse;

public interface UserService {
	UserResponse registerUser(UserRegistrationRequest request);

	UserLoginResponse loginUser(UserLoginRequest request);

	UserResponse getUserById(Long userId);
}
