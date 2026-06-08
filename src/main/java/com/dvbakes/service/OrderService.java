package com.dvbakes.service;

import com.dvbakes.dto.OrderResponseDto;
import com.dvbakes.entity.Order;
import com.dvbakes.entity.Product;
import com.dvbakes.repository.OrderRepository;
import com.dvbakes.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory cart store (for demo; production: use Redis or DB-backed sessions)
    private final Map<String, List<Map<String, Object>>> carts = new java.util.concurrent.ConcurrentHashMap<>();

    private String generateId() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private String generateOrderId() {
        String id;
        do {
            int num = 10000 + new Random().nextInt(90000);
            id = "ORDER-" + num;
        } while (orderRepository.existsById(id));
        return id;
    }

    // ─── DTO CONVERSION ──────────────────────────────────────────────

    /**
     * Converts an Order entity to the frontend-friendly DTO.
     * Safely parses the stored JSON items string back to a List so the
     * React frontend receives a proper array, not a raw JSON string.
     */
    public OrderResponseDto toDto(Order order) {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        try {
            if (order.getItems() != null && !order.getItems().isBlank()) {
                itemsList = objectMapper.readValue(order.getItems(),
                        new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse items JSON for order {}: {}", order.getId(), e.getMessage());
        }
        return OrderResponseDto.builder()
                .id(order.getId())
                .cartId(order.getCartId())
                .items(itemsList)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shipping(order.getShipping())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerAddress(order.getCustomerAddress())
                .timerExpiresAt(order.getTimerExpiresAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // ─── CART ────────────────────────────────────────────────────────

    public Map<String, Object> getCart(String cartId) {
        if (cartId == null) cartId = generateId();
        carts.putIfAbsent(cartId, new ArrayList<>());
        return Map.of("cartId", cartId, "items", carts.get(cartId));
    }

    public Map<String, Object> addToCart(String cartId, String productId, int quantity, Map<String, Boolean> toppings) {
        if (cartId == null) cartId = generateId();
        carts.putIfAbsent(cartId, new ArrayList<>());
        List<Map<String, Object>> items = carts.get(cartId);

        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStock() <= 0) {
            throw new IllegalStateException("This item is currently out of stock!");
        }

        String toppingsKey = toppings == null ? "" :
                toppings.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .sorted()
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);

        // Check if same item+toppings already in cart
        Optional<Map<String, Object>> existing = items.stream()
                .filter(i -> productId.equals(i.get("productId")) && toppingsKey.equals(i.get("toppingsKey")))
                .findFirst();

        int currentInCart = existing.map(i -> (int) i.get("quantity")).orElse(0);
        if (currentInCart + quantity > product.getStock()) {
            throw new IllegalStateException("Cannot add more. Only " + product.getStock() + " in stock.");
        }

        if (existing.isPresent()) {
            existing.get().put("quantity", currentInCart + quantity);
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("id", generateId());
            item.put("productId", productId);
            item.put("name", product.getTitle());
            item.put("price", product.getPrice());
            item.put("src", product.getSrc());
            item.put("quantity", quantity);
            item.put("toppings", toppings != null ? toppings : Map.of());
            item.put("toppingsKey", toppingsKey);
            items.add(item);
        }

        return Map.of("cartId", cartId, "items", items);
    }

    public Map<String, Object> updateCartItem(String cartId, String itemId, int quantity, Map<String, Boolean> toppings) {
        List<Map<String, Object>> items = carts.getOrDefault(cartId, new ArrayList<>());
        for (int i = 0; i < items.size(); i++) {
            if (itemId.equals(items.get(i).get("id"))) {
                if (quantity > 0) {
                    items.get(i).put("quantity", quantity);
                    if (toppings != null) items.get(i).put("toppings", toppings);
                } else {
                    items.remove(i);
                }
                break;
            }
        }
        return Map.of("cartId", cartId, "items", items);
    }

    public Map<String, Object> removeFromCart(String cartId, String itemId) {
        List<Map<String, Object>> items = carts.getOrDefault(cartId, new ArrayList<>());
        items.removeIf(i -> itemId.equals(i.get("id")));
        return Map.of("cartId", cartId, "items", items);
    }

    public Map<String, Object> clearCart(String cartId) {
        if (cartId != null) carts.put(cartId, new ArrayList<>());
        return Map.of("cartId", cartId != null ? cartId : "", "items", List.of());
    }

    // ─── ORDERS ──────────────────────────────────────────────────────

    @Transactional
    public OrderResponseDto placeOrder(String cartId, String customerName, String customerPhone,
                                       String customerAddress, String paymentMethod, String paymentStatus)
            throws Exception {

        List<Map<String, Object>> cartItems = carts.getOrDefault(cartId, new ArrayList<>());
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty.");

        // Validate stock before any deduction
        for (Map<String, Object> item : cartItems) {
            String pid = (String) item.get("productId");
            int qty = (int) item.get("quantity");
            Product p = productRepository.findByIdAndActiveTrue(pid)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + pid));
            if (p.getStock() < qty) {
                throw new IllegalStateException("Insufficient stock for " + p.getTitle() +
                        ". Available: " + p.getStock() + ", Requested: " + qty);
            }
        }

        // Deduct stock
        for (Map<String, Object> item : cartItems) {
            String pid = (String) item.get("productId");
            int qty = (int) item.get("quantity");
            Product p = productRepository.findByIdAndActiveTrue(pid).get();
            p.setStock(p.getStock() - qty);
            p.setUpdatedAt(Instant.now().toString());
            productRepository.save(p);
        }

        // Calculate totals
        double subtotal = cartItems.stream()
                .mapToDouble(i -> ((Number) i.get("price")).doubleValue() * (int) i.get("quantity"))
                .sum();
        double tax = subtotal * 0.08;
        double shipping = subtotal > 20 ? 0 : 2.99;
        double total = subtotal + tax + shipping;

        String orderId = generateOrderId();
        String timerExpiresAt = Instant.now().plusSeconds(900).toString(); // 15 min window

        Order order = Order.builder()
                .id(orderId)
                .cartId(cartId)
                .items(objectMapper.writeValueAsString(cartItems))
                .subtotal(subtotal)
                .tax(tax)
                .shipping(shipping)
                .total(total)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus != null ? paymentStatus : "Pending")
                .orderStatus("Pending")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .timerExpiresAt(timerExpiresAt)
                .createdAt(Instant.now().toString())
                .build();

        Order saved = orderRepository.save(order);
        // Clear the server-side cart after successful order
        carts.put(cartId, new ArrayList<>());

        log.info("Order placed: {} by {} | Total: ${}", orderId, customerName, String.format("%.2f", total));
        return toDto(saved);
    }

    /**
     * Returns all orders as DTOs with parsed items.
     * Used by admin panel.
     */
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single order by ID as DTO.
     * Used by public order tracker + admin.
     */
    public Optional<OrderResponseDto> getOrderById(String id) {
        return orderRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(String id, String orderStatus, String paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (orderStatus != null && !orderStatus.isBlank()) order.setOrderStatus(orderStatus);
        if (paymentStatus != null && !paymentStatus.isBlank()) order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(Instant.now().toString());

        log.info("Order {} updated — orderStatus={}, paymentStatus={}", id, orderStatus, paymentStatus);
        return toDto(orderRepository.save(order));
    }
}
