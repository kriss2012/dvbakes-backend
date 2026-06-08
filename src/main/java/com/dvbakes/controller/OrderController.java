package com.dvbakes.controller;

import com.dvbakes.dto.OrderResponseDto;
import com.dvbakes.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─── CART (Public — no auth required) ─────────────────────────────

    /**
     * GET /api/cart?cartId=xxx
     * Returns the current cart contents. Cart ID is managed client-side via localStorage.
     */
    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> getCart(@RequestParam(required = false) String cartId) {
        return ResponseEntity.ok(orderService.getCart(cartId));
    }

    /**
     * POST /api/cart
     * Adds a product to the cart (or increments quantity if already present).
     * Body: { cartId, productId, quantity, toppings }
     */
    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body) {
        try {
            String cartId = (String) body.get("cartId");
            String productId = (String) body.get("productId");
            int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
            @SuppressWarnings("unchecked")
            Map<String, Boolean> toppings = (Map<String, Boolean>) body.get("toppings");
            return ResponseEntity.ok(orderService.addToCart(cartId, productId, quantity, toppings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/cart/item
     * Updates quantity or toppings of an existing cart item.
     * Body: { cartId, itemId, quantity, toppings }
     */
    @PutMapping("/cart/item")
    public ResponseEntity<?> updateCartItem(@RequestBody Map<String, Object> body) {
        try {
            String cartId = (String) body.get("cartId");
            String itemId = (String) body.get("itemId");
            int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 0;
            @SuppressWarnings("unchecked")
            Map<String, Boolean> toppings = (Map<String, Boolean>) body.get("toppings");
            return ResponseEntity.ok(orderService.updateCartItem(cartId, itemId, quantity, toppings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/item
     * Removes a single item from the cart.
     * Body: { cartId, itemId }
     */
    @DeleteMapping("/cart/item")
    public ResponseEntity<?> removeFromCart(@RequestBody Map<String, Object> body) {
        String cartId = (String) body.get("cartId");
        String itemId = (String) body.get("itemId");
        return ResponseEntity.ok(orderService.removeFromCart(cartId, itemId));
    }

    /**
     * POST /api/cart/clear
     * Clears all items from a cart.
     * Body: { cartId }
     */
    @PostMapping("/cart/clear")
    public ResponseEntity<?> clearCart(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(orderService.clearCart((String) body.get("cartId")));
    }

    // ─── ORDERS ────────────────────────────────────────────────────────

    /**
     * POST /api/orders   (Public — customers place orders without logging in)
     * Places an order from the current cart. Deducts stock atomically.
     * Body: { cartId, customerName, customerPhone, customerAddress, paymentMethod, paymentStatus }
     */
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
        try {
            String cartId      = (String) body.get("cartId");
            String name        = (String) body.get("customerName");
            String phone       = (String) body.get("customerPhone");
            String address     = (String) body.get("customerAddress");
            String payMethod   = (String) body.get("paymentMethod");
            String payStatus   = (String) body.get("paymentStatus");

            if (cartId == null || name == null || phone == null || address == null || payMethod == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required order fields."));
            }

            OrderResponseDto order = orderService.placeOrder(cartId, name, phone, address, payMethod, payStatus);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/orders/{id}   (Public — customers can track their own order)
     * Returns order details including the parsed items list.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/orders   (Admin only — lists all orders)
     * Requires valid JWT with ADMIN role.
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * PUT /api/orders/{id}/status   (Admin only — update order/payment status)
     * Requires valid JWT with ADMIN role.
     * Body: { orderStatus?, paymentStatus? }
     */
    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id,
                                                @RequestBody Map<String, String> body) {
        try {
            String orderStatus   = body.get("orderStatus");
            String paymentStatus = body.get("paymentStatus");
            OrderResponseDto updated = orderService.updateOrderStatus(id, orderStatus, paymentStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
