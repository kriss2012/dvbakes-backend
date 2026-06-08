package com.dvbakes.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AdminDashboardDto {
    private double totalRevenue;
    private long activeOrders;
    private long totalProducts;
    private long outOfStockCount;
    private long totalOrdersToday;
    private DatabaseMetricsDto dbMetrics;
    private Map<String, Long> categoryDistribution;
    private Map<String, Double> hourlySales;
    private double avgResponseTimeMs;
    private long totalApiRequests;
}
