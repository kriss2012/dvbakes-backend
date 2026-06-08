package com.dvbakes.service;

import com.dvbakes.entity.Product;
import com.dvbakes.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable("products")
    public List<Product> getAllProducts() {
        return productRepository.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findByIdAndActiveTrue(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public Product createProduct(Product product) {
        // Generate unique slug ID
        String baseSlug = product.getTitle().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        String slug = baseSlug;
        int counter = 1;
        while (productRepository.existsById(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        product.setId(slug);
        product.setCreatedAt(Instant.now().toString());
        product.setActive(true);

        // Set default JSON fields if not provided
        if (product.getSpecs() == null) {
            product.setSpecs("[{\"label\":\"Servings\",\"value\":\"1 Port\"},{\"label\":\"Freshness\",\"value\":\"Daily Baked\"},{\"label\":\"Calories\",\"value\":\"220 kcal\"},{\"label\":\"Weight\",\"value\":\"100 g\"}]");
        }
        if (product.getIngredients() == null) {
            product.setIngredients("[\"Organic Flour\",\"Butter\",\"Sugar\",\"Gourmet Whipped Cream\"]");
        }
        if (product.getNutrition() == null) {
            product.setNutrition("[{\"name\":\"Carbs\",\"percentage\":60},{\"name\":\"Fats\",\"percentage\":40},{\"name\":\"Proteins\",\"percentage\":15},{\"name\":\"Sugars\",\"percentage\":30}]");
        }

        log.info("Creating product: {}", product.getTitle());
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public Product updateStock(String id, int newStock) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setStock(newStock);
        product.setUpdatedAt(Instant.now().toString());
        log.info("Updated stock for {} to {}", id, newStock);
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deductStock(String productId, int quantity) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for " + product.getTitle());
        }
        product.setStock(product.getStock() - quantity);
        product.setUpdatedAt(Instant.now().toString());
        productRepository.save(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setActive(false); // Soft delete
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockLessThanAndActiveTrue(threshold);
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }
}
