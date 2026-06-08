package com.dvbakes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 10;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String src;

    private String alt;

    @Column(columnDefinition = "TEXT")
    private String bg;

    private String themeColor;
    private String accentColor;
    private String textColor;

    @Column(columnDefinition = "TEXT")
    private String specs;       // JSON string

    @Column(columnDefinition = "TEXT")
    private String ingredients; // JSON string

    @Column(columnDefinition = "TEXT")
    private String nutrition;   // JSON string

    private String bgText;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private String createdAt = java.time.Instant.now().toString();

    @Column
    private String updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
