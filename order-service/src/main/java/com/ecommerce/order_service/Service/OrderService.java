package com.ecommerce.order_service.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ecommerce.order_service.DTO.OrderItemRequestDTO;
import com.ecommerce.order_service.DTO.OrderRequestDTO;
import com.ecommerce.order_service.DTO.OrderResponseDTO;
import com.ecommerce.order_service.DTO.ProductClientResponse;
import com.ecommerce.order_service.DTO.ProductResponseWrapper;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Model.Order;
import com.ecommerce.order_service.Model.OrderItem;
import com.ecommerce.order_service.Repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
    }

    // Mendapatkan username dari JWT
    private String getCurrentUsername() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Mendapatkan role dari JWT
    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(role -> role.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }

    private String getCurrentToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    private boolean isCashier() {
        return getCurrentUserRole().equals("KASIR");
    }

    private boolean isUser() {
        return getCurrentUserRole().equals("USER");
    }

    // test webclient
    public ProductClientResponse getProductById(String id) {
        String token = getCurrentToken();

        WebClient client = webClientBuilder.baseUrl("http://localhost:1011").build();

        ProductResponseWrapper response = client.get()
                .uri("/product/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(ProductResponseWrapper.class)
                .block();

        if (response == null || response.getData() == null) {
            throw new RuntimeException("Produk tidak ditemukan di product-service");
        }

        return response.getData(); // ⬅ sudah bukan get(0) lagi
    }

    public OrderResponseDTO createOrder(OrderRequestDTO orderDto) {
        String username = getCurrentUsername();
        String token = getCurrentToken();

        logger.info("User {} membuat order", username);
        logger.info("TOKEN yang diambil: {}", token);

        if (orderDto == null || orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            logger.error("Order gagal: daftar item kosong atau tidak valid");
            throw new IllegalArgumentException("Item pesanan tidak boleh kosong");
        }

        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        WebClient client = webClientBuilder.baseUrl("http://localhost:1011").build();

        for (OrderItemRequestDTO itemReq : orderDto.getItems()) {
            try {
                logger.info("Mengambil produk dari product-service, id: {}", itemReq.getId());

                // Ambil response wrapper dari product-service
                ProductResponseWrapper response = client
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/product/{id}")
                                .build(itemReq.getId()))
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ProductResponseWrapper.class)
                        .block();

                // Validasi data produk
                if (response == null || response.getData() == null || response.getData().getPrice() == null) {
                    logger.error("Data produk tidak valid/null dari product-service: {}", itemReq.getId());
                    throw new IllegalArgumentException(
                            "Produk tidak ditemukan atau data tidak lengkap: " + itemReq.getId());
                }

                ProductClientResponse product = response.getData();

                // Bangun item order
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductPrice(product.getPrice());
                item.setQuantity(itemReq.getQuantity());
                item.setSubtotal(product.getPrice() * itemReq.getQuantity());

                items.add(item);
                totalAmount += item.getSubtotal();

                logger.info("Mengurangi stok untuk produk ID: {}", product.getId());

                client
                        .put()
                        .uri(uriBuilder -> uriBuilder
                                .path("/product/{id}/reduce-stock")
                                .queryParam("quantity", itemReq.getQuantity())
                                .build(product.getId()))
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();

            } catch (Exception e) {
                logger.error("Gagal memproses item order dengan ID produk {}: {}", itemReq.getId(), e.getMessage());
                throw new RuntimeException("Gagal memproses item dengan ID: " + itemReq.getId(), e);
            }
        }

        // Simpan order
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setItems(items);
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);
        logger.info("Order berhasil dibuat oleh user {} dengan total {}", username, totalAmount);

        return OrderMapper.toDTO(saved);
    }

    public OrderResponseDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan dengan ID: " + id));

        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        if (isUser() && !order.getUserId().equals(username)) {
            throw new RuntimeException("kamu tidak punya akses melihat order ini");
        }

        logger.info("order ID '{}' diakses oleh '{}'", id, username, role);

        return OrderMapper.toDTO(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        logger.info("user '{}' dengan role '{}' mengambil semua order", username, role);

        List<Order> orders = isUser()
                ? orderRepository.findAll().stream()
                        .filter(o -> username.equals((String.valueOf(o.getUserId()))))
                        .collect(Collectors.toList())
                : orderRepository.findAll();

        return orders.stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getOrdersByUserId(String userId) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        if (isUser() && !username.equals(userId)) {
            throw new RuntimeException("Kamu tidak punya akses melihat order milik user ini");
        }

        logger.info("User '{}' dengan role '{}' mengambil semua order milik userId: {}", username, role, userId);
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteOrderById(String id) {
        String username = getCurrentUsername();
        // String role = getCurrentUserRole();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan dengan ID: " + id));

        // Validasi hanya pemilik order (USER) yang boleh hapus order miliknya
        if (isUser() && !order.getUserId().equals(username)) {
            throw new RuntimeException("Kamu tidak punya akses menghapus order ini");
        }

        orderRepository.deleteById(id);
        logger.info("Order ID '{}' telah dihapus oleh user '{}'", id, username);
    }

    public OrderResponseDTO updateOrder(String id, OrderRequestDTO updatedData) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan dengan ID: " + id));

        String username = getCurrentUsername();

        if (isUser()) {
            throw new RuntimeException("USER tidak diperbolehkan mengupdate order");
        }

        if (isCashier()) {
            // Kasir hanya update status
            if (updatedData.getStatus() != null) {
                existing.setStatus(updatedData.getStatus());
            }
        } else {
            // Admin bisa update semua field jika tidak null
            if (updatedData.getUserId() != null) {
                existing.setUserId(updatedData.getUserId());
            }
            if (updatedData.getStatus() != null) {
                existing.setStatus(updatedData.getStatus());
            }
            if (updatedData.getOrderDate() != null) {
                existing.setOrderDate(updatedData.getOrderDate());
            }

            if (updatedData.getItems() != null && !updatedData.getItems().isEmpty()) {
                List<OrderItem> updatedItems = updatedData.getItems().stream().map(itemReq -> {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(UUID.randomUUID().toString());
                    item.setProductId(itemReq.getId());
                    item.setProductName(itemReq.getProductName());
                    item.setProductPrice(itemReq.getProductPrice());
                    item.setQuantity(itemReq.getQuantity());
                    item.setSubtotal(itemReq.getProductPrice() * itemReq.getQuantity());
                    return item;
                }).collect(Collectors.toList());

                double totalAmount = updatedItems.stream()
                        .mapToDouble(OrderItem::getSubtotal)
                        .sum();

                existing.setItems(updatedItems);
                existing.setTotalAmount(totalAmount);
            }
        }

        logger.info("Order ID '{}' berhasil diupdate oleh '{}'", id, username);
        Order updated = orderRepository.save(existing);

        return OrderMapper.toDTO(updated);
    }

}
