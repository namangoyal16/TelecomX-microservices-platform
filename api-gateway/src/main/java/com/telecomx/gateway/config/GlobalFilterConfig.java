package com.telecomx.gateway.config;

import com.telecomx.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class GlobalFilterConfig {

    /**
     * Applies the JWT filter globally (as a GlobalFilter) so every route gets the
     * same edge-level check without having to remember to attach it per-route in YAML.
     */
    @Bean
    public GlobalFilter jwtGlobalFilter(JwtAuthenticationGatewayFilterFactory factory) {
        GatewayFilter filter = factory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        return (exchange, chain) -> filter.filter(exchange, chain);
    }
}
