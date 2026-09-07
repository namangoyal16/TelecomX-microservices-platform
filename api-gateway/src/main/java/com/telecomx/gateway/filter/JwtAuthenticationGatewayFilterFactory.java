package com.telecomx.gateway.filter;

import com.telecomx.gateway.config.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Coarse-grained JWT check at the edge: rejects obviously-invalid/missing tokens
 * before they even reach a backend service, saving that service the wasted work.
 * This is intentionally NOT the only JWT check in the platform - each downstream
 * service independently re-validates the same token (see each service's
 * JwtAuthenticationFilter), so no service has to trust the gateway blindly and
 * every service stays independently testable/deployable.
 */
@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/v1/auth/", "/api/v1/plans", "/actuator", "/swagger-ui", "/v3/api-docs");

    private final JwtService jwtService;

    public JwtAuthenticationGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (PUBLIC_PREFIXES.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ") || !jwtService.isValid(authHeader.substring(7))) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
