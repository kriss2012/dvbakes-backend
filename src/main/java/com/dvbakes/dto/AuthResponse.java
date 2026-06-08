package com.dvbakes.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private long expiresIn;
    private String message;
}
