package com.farmtoyou.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmtoyou.userservice.service.UserService;
import com.farmtoyou.userserviced.dto.UserLoginRequest;
import com.farmtoyou.userserviced.dto.UserLoginResponse;
import com.farmtoyou.userserviced.dto.UserRegistrationRequest;
import com.farmtoyou.userserviced.dto.UserResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> registerUser(@RequestBody UserRegistrationRequest request) {
		UserResponse newUser = userService.registerUser(request);
		return new ResponseEntity<>(newUser, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest request) {
		UserLoginResponse response = userService.loginUser(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
		UserResponse user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}

}