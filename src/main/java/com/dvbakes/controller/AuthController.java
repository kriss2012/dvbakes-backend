package com.dvbakes.controller;

import com.dvbakes.dto.AuthRequest;
import com.dvbakes.dto.AuthResponse;
import com.dvbakes.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${dvbakes.admin.username}")
    private String adminUsername;

    @Value("${dvbakes.admin.password}")
    private String adminPassword;

    @Value("${dvbakes.admin.passcode}")
    private String adminPasscode;

    @Value("${dvbakes.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Validates admin credentials and returns a JWT token.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        if (adminUsername.equals(request.getUsername()) && adminPassword.equals(request.getPassword())) {
            String token = jwtTokenProvider.generateToken(request.getUsername(), "ADMIN");
            log.info("Admin login successful for: {}", request.getUsername());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .username(request.getUsername())
                    .role("ADMIN")
                    .expiresIn(jwtExpiration)
                    .message("Login successful. Welcome to DvBakes Owner Desk.")
                    .build());
        }

        log.warn("Failed login attempt for user: {}", request.getUsername());
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials."));
    }

    /**
     * Validates a pin passcode for lightweight Owner authentication.
     * POST /api/auth/validate-pin
     */
    @PostMapping("/validate-pin")
    public ResponseEntity<?> validatePin(@RequestBody Map<String, String> body) {
        String passcode = body.get("passcode");

        if (adminPasscode.equals(passcode)) {
            String token = jwtTokenProvider.generateToken("owner_pin_user", "ADMIN");
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .username("owner")
                    .role("ADMIN")
                    .expiresIn(jwtExpiration)
                    .message("PIN validated. Welcome.")
                    .build());
        }

        return ResponseEntity.status(401).body(Map.of("error", "Incorrect PIN."));
    }

    /**
     * Validates a token and returns its info.
     * GET /api/auth/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No token provided."));
        }
        String token = authHeader.substring(7);
        if (jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", jwtTokenProvider.extractUsername(token),
                    "role", jwtTokenProvider.extractRole(token)
            ));
        }
        return ResponseEntity.status(401).body(Map.of("valid", false, "error", "Token expired or invalid."));
    }
}
