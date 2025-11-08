package com.farmtoyou.apigateway.filter;

import com.farmtoyou.apigateway.config.RouterValidator;
import com.farmtoyou.apigateway.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Autowired
	private RouterValidator routerValidator;

	@Autowired
	private JwtUtil jwtUtil;

	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// Check if the endpoint is secured
			if (routerValidator.isSecured.test(request)) {
				// 1. Check if the Authorization header is present
				if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
					return this.onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
				}

				// 2. Get the token from the header
				String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
				String token = null;

				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					token = authHeader.substring(7);
				}

				// 3. Validate the token
				try {
					if (token == null)
						throw new JwtException("Invalid token format");
					jwtUtil.validateToken(token);

					String userId = jwtUtil.getUserId(token);
					String role = jwtUtil.getRole(token);

					request = exchange.getRequest().mutate().header("X-User-Id", userId).header("X-User-Role", role)
							.build();
				} catch (Exception e) {
					// This catches expired tokens, malformed tokens, etc.
					return this.onError(exchange, "Authorization failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
				}
			}
			// If the endpoint is public or the token is valid, let the request proceed
			return chain.filter(exchange.mutate().request(request).build());
		};
	}

	// Helper method to return an error response
	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		// You can write a JSON error to the response body here if you want
		return response.setComplete();
	}

	// Empty config class, required by the parent class
	public static class Config {
	}
}