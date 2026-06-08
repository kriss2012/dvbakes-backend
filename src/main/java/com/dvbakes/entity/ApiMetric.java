package com.dvbakes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "api_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Long responseTimeMs;

    @Column(nullable = false)
    private String timestamp;

    private String clientIp;
}
