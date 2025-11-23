package com.aquarius.crypto.config.security;


import com.aquarius.crypto.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private final ReactiveUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public JwtAuthenticationWebFilter(ReactiveUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        final String jwtToken = authHeader.substring(7);

        return Mono.defer(() -> {
            String userEmail = jwtService.extractUsername(jwtToken);

            if (userEmail == null) {
                return chain.filter(exchange);
            }

            return userDetailsService.findByUsername(userEmail)
                    .filter(userDetails -> jwtService.validateToken(jwtToken, userDetails))
                    .map(userDetails -> createAuthenticationToken(userDetails, exchange))
                    .flatMap(authentication ->
                            chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    ).switchIfEmpty(chain.filter(exchange));
        });
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            UserDetails userDetails, ServerWebExchange exchange) {

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}