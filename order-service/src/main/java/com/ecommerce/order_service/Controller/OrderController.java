package com.ecommerce.order_service.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order_service.DTO.OrderRequestDTO;
import com.ecommerce.order_service.DTO.OrderResponseDTO;
import com.ecommerce.order_service.DTO.ProductClientResponse;
import com.ecommerce.order_service.Mapper.OrderMapper;
import com.ecommerce.order_service.Model.Order;
import com.ecommerce.order_service.Repository.OrderRepository;
import com.ecommerce.order_service.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Autowired
    private OrderService orderService;

    @GetMapping("/test")
    public String testEndpoint() {
        return "Order Service is working";
    }

    // test webclient
    @GetMapping("/testProduct/{id}")
    public ResponseEntity<ProductClientResponse> testProductFetch(@PathVariable String id) {
        ProductClientResponse product = orderService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Create order
    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {
        OrderResponseDTO created = orderService.createOrder(dto);
        return ResponseEntity.ok(created);
    }

    // Mendapatkan semua order (berdasarkan role user)
    @GetMapping("/getAllOrders")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Mendapatkan order berdasarkan orderId
    @GetMapping("/getByOrderId/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable String id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // Mendapatkan order berdasarkan userId
    @GetMapping("/getByUserId/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(@PathVariable String userId) {
        List<OrderResponseDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Menghapus order berdasarkan ID
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<String> deleteOrderById(@PathVariable String id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok("Order dengan ID " + id + " berhasil dihapus");
    }

    // Update order (permissions berdasarkan role)
    @PutMapping("/updateOrder/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @PathVariable String id,
            @RequestBody OrderRequestDTO updatedData) {
        OrderResponseDTO updated = orderService.updateOrder(id, updatedData);
        return ResponseEntity.ok(updated);
    }

    // Update status order saja (khusus untuk transaksi-service / kasir)
    @PutMapping("/updateStatusOnly/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrderStatusOnly(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }

        logger.info("Menerima permintaan update status order {} menjadi {}", id, status);

        // Buat DTO khusus untuk update status saja
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));

        existing.setStatus(status); // Langsung set status

        Order updated = orderRepository.save(existing);
        logger.info("Status order {} berhasil diupdate menjadi {}", id, updated.getStatus());

        return ResponseEntity.ok(OrderMapper.toDTO(updated));
    }
}