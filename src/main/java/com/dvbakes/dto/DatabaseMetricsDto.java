package com.dvbakes.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseMetricsDto {
    private String timestamp;
    private String poolName;
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int pendingThreads;
    private int maxPoolSize;
    private int minIdle;
    private double utilizationPercent;
    private String connectionStatus;   // IDLE, ACTIVE, MODERATE, HIGH_LOAD, SATURATED, DISCONNECTED
    private String databaseType;       // SQLite, PostgreSQL, MySQL
    private boolean isConnected;
    private long totalRequestsHandled;
}
