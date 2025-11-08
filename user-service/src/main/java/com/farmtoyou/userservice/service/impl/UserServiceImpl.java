package com.farmtoyou.userservice.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Import the JWT provider
import com.farmtoyou.userservice.config.JwtTokenProvider;

// We must NOT import SecurityConfig or UserServiceApplication here
// import com.farmtoyou.userservice.UserServiceApplication;
// import com.farmtoyou.userservice.config.SecurityConfig;

import com.farmtoyou.userservice.entity.User;
import com.farmtoyou.userservice.exception.EmailAlreadyExistsException;
import com.farmtoyou.userservice.exception.UserNotFoundException;
import com.farmtoyou.userservice.repository.UserRepository;
import com.farmtoyou.userservice.service.UserService;
// Import the DTOs for login
import com.farmtoyou.userserviced.dto.UserLoginRequest;
import com.farmtoyou.userserviced.dto.UserLoginResponse;
import com.farmtoyou.userserviced.dto.UserRegistrationRequest;
import com.farmtoyou.userserviced.dto.UserResponse;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	// --- THIS IS THE CORRECTED CONSTRUCTOR ---
	// It only injects the specific beans it needs, breaking the circular
	// dependency.
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public UserResponse getUserById(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
		return mapToUserResponse(user);
	}

	private UserResponse mapToUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole());
		response.setPhone(user.getPhone());
		response.setAddress(user.getAddress());
		return response;
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
		newUser.setPhone(request.getPhone());
		newUser.setAddress(request.getAddress());

		User savedUser = userRepository.save(newUser);

		UserResponse response = new UserResponse();
		response.setId(savedUser.getId());
		response.setName(savedUser.getName());
		response.setEmail(savedUser.getEmail());
		response.setRole(savedUser.getRole());
		response.setPhone(savedUser.getPhone());
		response.setAddress(savedUser.getAddress());

		return response;
	}

	// --- This is the login method you are trying to test ---
	@Override
	public UserLoginResponse loginUser(UserLoginRequest request) {
		// 1. Authenticate the user (this will now use UserDetailsServiceImpl)
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		// 2. Set security context
		SecurityContextHolder.getContext().setAuthentication(authentication);

		
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UserNotFoundException());
		
		// 3. Generate the JWT Token
		String token = jwtTokenProvider.generateToken(user);


		// 5. Return the response DTO with the token
		UserLoginResponse response = new UserLoginResponse();
		response.setId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole());
		response.setMessage("Login successful!");
		response.setToken(token);

		return response;
	}
}