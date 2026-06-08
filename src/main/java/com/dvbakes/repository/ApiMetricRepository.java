package com.dvbakes.repository;

import com.dvbakes.entity.ApiMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiMetricRepository extends JpaRepository<ApiMetric, Long> {

    @Query("SELECT a FROM ApiMetric a ORDER BY a.timestamp DESC")
    List<ApiMetric> findRecentMetrics(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT AVG(a.responseTimeMs) FROM ApiMetric a WHERE a.timestamp > ?1")
    Double getAverageResponseTime(String since);

    @Query("SELECT COUNT(a) FROM ApiMetric a WHERE a.timestamp > ?1")
    long countRequestsSince(String since);
}
