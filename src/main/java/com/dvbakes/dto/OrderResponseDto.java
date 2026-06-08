package com.dvbakes.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Frontend-friendly order response.
 * Converts the stored JSON-string `items` field into a proper List
 * so the React frontend can consume it directly without manual JSON.parse().
 */
@Data
@Builder
public class OrderResponseDto {
    private String id;
    private String cartId;
    // Parsed list of cart items (not a raw JSON string)
    private List<Map<String, Object>> items;
    private Double subtotal;
    private Double tax;
    private Double shipping;
    private Double total;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String timerExpiresAt;
    private String createdAt;
    private String updatedAt;
}
