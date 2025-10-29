package com.farmtoyou.userservice.service;

import com.farmtoyou.userserviced.dto.UserRegistrationRequest;
import com.farmtoyou.userserviced.dto.UserResponse;

public interface UserService {
	UserResponse registerUser(UserRegistrationRequest request);
}
