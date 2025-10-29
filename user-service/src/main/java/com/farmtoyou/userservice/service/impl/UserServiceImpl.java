package com.farmtoyou.userservice.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.farmtoyou.userservice.UserServiceApplication;
import com.farmtoyou.userservice.config.SecurityConfig;
import com.farmtoyou.userservice.entity.User;
import com.farmtoyou.userservice.exception.EmailAlreadyExistsException;
import com.farmtoyou.userservice.repository.UserRepository;
import com.farmtoyou.userservice.service.UserService;
import com.farmtoyou.userserviced.dto.UserRegistrationRequest;
import com.farmtoyou.userserviced.dto.UserResponse;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			SecurityConfig securityConfig, UserServiceApplication userServiceApplication) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserResponse registerUser(UserRegistrationRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent())
			throw new EmailAlreadyExistsException("Email Already Registered");

		User newUser = new User();
		newUser.setName(request.getName());
		newUser.setEmail(request.getEmail());

		newUser.setPassword(passwordEncoder.encode(request.getPassword()));

		newUser.setRole(request.getRole());

		User savedUser = userRepository.save(newUser);

		UserResponse response = new UserResponse();
		response.setId(savedUser.getId());
		response.setName(savedUser.getName());
		response.setEmail(savedUser.getEmail());
		response.setRole(savedUser.getRole());

		return response;
	}

}
