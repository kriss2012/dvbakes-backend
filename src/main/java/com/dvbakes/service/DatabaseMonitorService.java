package com.dvbakes.service;

import com.dvbakes.dto.DatabaseMetricsDto;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMonitorService {

    private final DataSource dataSource;
    private final SimpMessagingTemplate messagingTemplate;

    private long totalRequestsHandled = 0;
    private long totalConnectionsCreated = 0;

    /**
     * Broadcasts live HikariCP connection pool metrics via WebSocket every 2 seconds.
     * This powers the live "DB Connection Monitor" in the admin dashboard.
     */
    @Scheduled(fixedDelay = 2000)
    public void broadcastConnectionPoolMetrics() {
        try {
            DatabaseMetricsDto metrics = collectMetrics();
            messagingTemplate.convertAndSend("/topic/db-metrics", metrics);
        } catch (Exception e) {
            log.warn("Failed to broadcast DB metrics: {}", e.getMessage());
        }
    }

    /**
     * Collects current HikariCP pool statistics.
     */
    public DatabaseMetricsDto collectMetrics() {
        if (dataSource instanceof HikariDataSource hikariDS) {
            HikariPoolMXBean pool = hikariDS.getHikariPoolMXBean();

            if (pool != null) {
                int activeConnections = pool.getActiveConnections();
                int idleConnections = pool.getIdleConnections();
                int totalConnections = pool.getTotalConnections();
                int pendingThreads = pool.getThreadsAwaitingConnection();
                int maxPoolSize = hikariDS.getMaximumPoolSize();
                int minIdle = hikariDS.getMinimumIdle();
                String poolName = hikariDS.getPoolName();

                totalRequestsHandled++;

                double utilizationPercent = totalConnections > 0
                        ? ((double) activeConnections / maxPoolSize) * 100.0
                        : 0.0;

                String connectionStatus = determineStatus(activeConnections, pendingThreads, maxPoolSize);

                return DatabaseMetricsDto.builder()
                        .timestamp(Instant.now().toString())
                        .poolName(poolName)
                        .activeConnections(activeConnections)
                        .idleConnections(idleConnections)
                        .totalConnections(totalConnections)
                        .pendingThreads(pendingThreads)
                        .maxPoolSize(maxPoolSize)
                        .minIdle(minIdle)
                        .utilizationPercent(Math.round(utilizationPercent * 10.0) / 10.0)
                        .connectionStatus(connectionStatus)
                        .databaseType("SQLite")
                        .isConnected(true)
                        .totalRequestsHandled(totalRequestsHandled)
                        .build();
            }
        }

        // Fallback when Hikari not available
        return DatabaseMetricsDto.builder()
                .timestamp(Instant.now().toString())
                .poolName("Unknown")
                .activeConnections(0)
                .idleConnections(0)
                .totalConnections(0)
                .pendingThreads(0)
                .maxPoolSize(0)
                .minIdle(0)
                .utilizationPercent(0.0)
                .connectionStatus("DISCONNECTED")
                .databaseType("SQLite")
                .isConnected(false)
                .totalRequestsHandled(0)
                .build();
    }

    private String determineStatus(int active, int pending, int max) {
        if (pending > 0) return "SATURATED";
        double pct = max > 0 ? (double) active / max * 100 : 0;
        if (pct >= 80) return "HIGH_LOAD";
        if (pct >= 50) return "MODERATE";
        if (active > 0) return "ACTIVE";
        return "IDLE";
    }

    public void incrementConnections() {
        totalConnectionsCreated++;
    }
}
