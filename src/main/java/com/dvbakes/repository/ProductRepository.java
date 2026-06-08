package com.dvbakes.repository;

import com.dvbakes.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByCategoryAndActiveTrue(String category);

    List<Product> findByStockLessThanAndActiveTrue(int threshold);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    Optional<Product> findByIdAndActiveTrue(String id);
}
