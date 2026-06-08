package com.dvbakes.controller;

import com.dvbakes.entity.Product;
import com.dvbakes.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/products  — Admin only
     * Creates a new product in the catalog.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product created = productService.createProduct(product);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/products/{id}/stock  — Admin only
     * Updates the stock level of a product.
     */
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            int stock = ((Number) body.get("stock")).intValue();
            if (stock < 0) return ResponseEntity.badRequest().body(Map.of("error", "Invalid stock value"));
            Product updated = productService.updateStock(id, stock);
            return ResponseEntity.ok(Map.of("id", id, "stock", updated.getStock(), "message", "Stock updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/products/{id}  — Admin only
     * Soft-deletes a product (sets active=false).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/products/low-stock  — Admin only
     * Returns products with stock below the given threshold.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getLowStock(@RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    /**
     * GET /api/products/out-of-stock  — Admin only
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getOutOfStock() {
        return ResponseEntity.ok(productService.getOutOfStockProducts());
    }
}
