package com.dvbakes.config;

import com.dvbakes.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for DvBakes SaaS Backend.
 *
 * Auth matrix:
 *  PUBLIC (no JWT needed):
 *    GET  /api/products, /api/products/**
 *    GET  /api/orders/{id}          ← customer order tracking
 *    POST /api/cart, /api/cart/**   ← cart operations
 *    PUT  /api/cart/**
 *    DELETE /api/cart/**
 *    POST /api/orders               ← place order (customer checkout)
 *    POST /api/auth/**              ← login / validate-pin / verify
 *    GET  /actuator/health
 *    OPTIONS /**                    ← CORS preflight
 *
 *  ADMIN (requires JWT with role=ADMIN):
 *    GET  /api/orders               ← all orders list
 *    PUT  /api/orders/**            ← update order status
 *    POST /api/products             ← add product
 *    PUT  /api/products/**          ← update / restock product
 *    DELETE /api/products/**        ← remove product
 *    GET  /api/admin/**             ← db-metrics, server-health, dashboard
 *    GET  /actuator/**              ← full actuator endpoints
 *
 * Method-level @PreAuthorize is also enabled for fine-grained control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize at method level
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${dvbakes.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF — stateless JWT API
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── CORS preflight — always allow OPTIONS ──────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Public Auth endpoints ──────────────────────────────
                        .requestMatchers(HttpMethod.POST,  "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,   "/api/auth/verify").permitAll()

                        // ── Public Storefront: Products ────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // ── Public Storefront: Cart (no login required) ────────
                        .requestMatchers(HttpMethod.GET,    "/api/cart").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/cart").permitAll()
                        .requestMatchers(HttpMethod.PUT,    "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/cart/clear").permitAll()

                        // ── Public Storefront: Place order + track own order ───
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/orders/{id}").permitAll()

                        // ── Public Health Check ────────────────────────────────
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // ── Admin only: full order list ────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")

                        // ── Admin only: change order/payment status ────────────
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasRole("ADMIN")

                        // ── Admin only: product management ────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // ── Admin only: admin dashboard & DB monitor ───────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── Admin only: full actuator endpoints ────────────────
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // ── Everything else requires authentication ────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parse comma-separated allowed origins (trimmed, blanks filtered)
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);   // preflight cache: 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
