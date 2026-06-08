package com.dvbakes.repository;

import com.dvbakes.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByOrderStatusNotIn(List<String> statuses);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderStatus != 'Cancelled'")
    Double getTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus NOT IN ('Completed', 'Cancelled')")
    long countActiveOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt LIKE ?1%")
    long countOrdersByDate(String datePrefix);
}
