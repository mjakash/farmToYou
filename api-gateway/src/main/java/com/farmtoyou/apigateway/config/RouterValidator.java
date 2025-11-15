package com.farmtoyou.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

	// These are the endpoints that are open to the public
	public static final List<String> publicApiEndpoints = List.of("/api/users/register", "/api/users/login",
			"/v3/api-docs", "/swagger-ui"
	// We can add actuator health checks here later if needed
	// "/actuator/health"
	);

	public static final Predicate<ServerHttpRequest> isSecured = request -> publicApiEndpoints.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));
}