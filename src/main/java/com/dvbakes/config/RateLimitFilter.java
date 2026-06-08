package com.dvbakes.config;

import com.dvbakes.entity.ApiMetric;
import com.dvbakes.repository.ApiMetricRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final ApiMetricRepository apiMetricRepository;

    @Value("${dvbakes.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${dvbakes.rate-limit.enabled:true}")
    private boolean enabled;

    // IP -> request count per minute window
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for WebSocket and static
        boolean skip = path.startsWith("/ws") || path.startsWith("/actuator");

        if (enabled && !skip) {
            long now = System.currentTimeMillis();
            long window = windowStart.computeIfAbsent(clientIp, k -> now);

            // Reset window every 60 seconds
            if (now - window > 60_000) {
                requestCounts.put(clientIp, new AtomicInteger(0));
                windowStart.put(clientIp, now);
            }

            int count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0))
                    .incrementAndGet();

            if (count > requestsPerMinute) {
                log.warn("Rate limit exceeded for IP: {} on {}", clientIp, path);
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please slow down.\",\"retryAfter\":60}");
                return;
            }

            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, requestsPerMinute - count)));
        }

        // Add security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("X-Powered-By", "DvBakes-SaaS");

        filterChain.doFilter(request, response);

        // Record API metric (async, non-blocking)
        long responseTime = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        if (!skip && path.startsWith("/api")) {
            try {
                ApiMetric metric = ApiMetric.builder()
                        .endpoint(path)
                        .method(method)
                        .statusCode(status)
                        .responseTimeMs(responseTime)
                        .timestamp(Instant.now().toString())
                        .clientIp(clientIp)
                        .build();
                apiMetricRepository.save(metric);
            } catch (Exception e) {
                log.debug("Failed to save API metric: {}", e.getMessage());
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) return realIp;
        return request.getRemoteAddr();
    }
}
