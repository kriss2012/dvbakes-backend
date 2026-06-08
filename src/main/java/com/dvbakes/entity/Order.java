package com.dvbakes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String cartId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String items; // JSON string

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double tax;

    @Column(nullable = false)
    private Double shipping;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private String paymentMethod; // COD, UPI

    @Column(nullable = false)
    @Builder.Default
    private String paymentStatus = "Pending"; // Pending, Paid

    @Column(nullable = false)
    @Builder.Default
    private String orderStatus = "Pending"; // Pending, Preparing, Out for Delivery, Completed, Cancelled

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String customerAddress;

    private String timerExpiresAt; // ISO string

    @Column(nullable = false)
    private String createdAt;

    private String updatedAt;
}
