package com.ecommerce.transaction_service.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ecommerce.transaction_service.Utils.NotFoundException;
import com.ecommerce.transaction_service.DTO.OrderResponseDTO;
import com.ecommerce.transaction_service.DTO.TransaksiRequestDTO;
import com.ecommerce.transaction_service.DTO.TransaksiResponseDTO;
import com.ecommerce.transaction_service.Mapper.TransaksiMapper;
import com.ecommerce.transaction_service.Model.StatusTransaksi;
import com.ecommerce.transaction_service.Model.Transaksi;
import com.ecommerce.transaction_service.Repository.TransaksiRepository;

@Service
public class TransaksiService implements TransaksiServiceITF {

    private static final Logger logger = LoggerFactory.getLogger(TransaksiService.class);

    private final TransaksiRepository transaksiRepository;
    private final WebClient.Builder webClientBuilder;


    public TransaksiService(TransaksiRepository transaksiRepository, WebClient.Builder webClientBuilder) {
        this.transaksiRepository = transaksiRepository;
        this.webClientBuilder = webClientBuilder;
    }

    private boolean isValidStatus(String status) {
        return Arrays.stream(StatusTransaksi.values())
                    .anyMatch(s -> s.name().equalsIgnoreCase(status));
    }



    @Override
    public TransaksiResponseDTO createTransaksi(TransaksiRequestDTO requestDTO) {
        String token = getCurrentToken();
        String username = getCurrentUsername();

        // 1. Buat objek Transaksi dari DTO
        Transaksi transaksi = TransaksiMapper.toEntity(requestDTO, username, null); // userId diisi di service
        

        // 2. Ambil data order dari order-service
        OrderResponseDTO order;
        try {
            order = getOrderFromOrderService(transaksi.getOrderId(), token);
        } catch (WebClientResponseException.NotFound ex) {
            logger.error("Order tidak ditemukan di order-service untuk ID: {}", transaksi.getOrderId());
            throw new NotFoundException("Order tidak ditemukan: " + transaksi.getOrderId());
        } catch (Exception e) {
            logger.error("Gagal mengambil order dari order-service untuk ID {}: {}", transaksi.getOrderId(), e.getMessage());
            throw new RuntimeException("Gagal mengambil order: " + e.getMessage());
        }

        // 3. Validasi jumlah uang yang dibayar dengan total amount order
        boolean isPaymentEnough = transaksi.getTotalBayar() >= order.getTotalAmount();

        // 4. Set status transaksi berdasarkan validasi pembayaran
        if (isPaymentEnough) {
            transaksi.setStatus(StatusTransaksi.BERHASIL);
        } else {
            transaksi.setStatus(StatusTransaksi.GAGAL);
        }

        // 5. Set userId dari order dan waktu transaksi
        transaksi.setUserId(order.getUserId()); // Ambil userId dari order
        transaksi.setWaktuTransaksi(LocalDateTime.now());
        transaksi.setCreatedBy(username);
        transaksi.setUpdatedBy(username);

        // 6. Simpan transaksi
        Transaksi saved = transaksiRepository.save(transaksi);
        logger.info("Transaksi baru dibuat untuk Order ID {} dengan status: {}", saved.getOrderId(), saved.getStatus());

        // 7. Update status order di order-service
        try {
            String newOrderStatus = saved.getStatus().name(); // Status order sama dengan status transaksi
            updateOrderStatus(order.getOrderId(), newOrderStatus, token);
            logger.info("Status order {} berhasil diubah menjadi {}", order.getOrderId(), newOrderStatus);
        } catch (Exception e) {
            logger.error("Gagal mengupdate status order {} di order-service: {}", order.getOrderId(), e.getMessage());
            // Pertimbangkan penanganan error lebih lanjut, misal rollback transaksi jika update order gagal
        }

        // 8. Kembalikan response dengan detail order
        return TransaksiMapper.toDTO(saved, order); // Mengembalikan DTO dengan detail order

    }

    
    public List<Transaksi> getByUsername(String username) {
        return transaksiRepository.findByCreatedBy(username);
    }


    @Override
    public List<Transaksi> allTransaksi() {
        // Implementasi otorisasi jika diperlukan (misal: hanya ADMIN/KASIR yang bisa melihat semua)
        return transaksiRepository.findAll();
    }

    @Override
    public Transaksi getById(String id) {
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaksi dengan ID " + id + " tidak ditemukan"));

        // Otorisasi: Hanya ADMIN yang bisa melihat transaksi orang lain, USER hanya bisa melihat transaksinya sendiri
        String currentUsername = getCurrentUsername();
        String currentUserRole = getCurrentUserRole();

        if (currentUserRole.equals("USER") && !transaksi.getUserId().equals(currentUsername)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk melihat transaksi ini.");
        }

        return transaksi;
    }

    @Override
    public List<Transaksi> getByUserId(String userId) {
        // Otorisasi: Hanya ADMIN yang bisa melihat transaksi user lain, USER hanya bisa melihat transaksinya sendiri
        String currentUsername = getCurrentUsername();
        String currentUserRole = getCurrentUserRole();

        if (currentUserRole.equals("USER") && !userId.equals(currentUsername)) {
            throw new RuntimeException("Anda tidak memiliki akses untuk melihat transaksi user lain.");
        }

        return transaksiRepository.findByUserId(userId);
    }

    @Override
    public Transaksi update(String id, Transaksi transaksiBaru) {
        Transaksi existingTransaksi = getById(id); // Memastikan transaksi ada dan otorisasi sudah dicek

        // Otorisasi: Hanya ADMIN yang bisa melakukan update penuh
        String currentUserRole = getCurrentUserRole();
        if (!currentUserRole.equals("ADMIN")) {
            throw new RuntimeException("Anda tidak memiliki izin untuk mengupdate transaksi secara penuh.");
        }

        existingTransaksi.setOrderId(transaksiBaru.getOrderId());
        existingTransaksi.setTotalBayar(transaksiBaru.getTotalBayar()); // Uang yang dibayarkan customer
        existingTransaksi.setMetodeBayar(transaksiBaru.getMetodeBayar());
        existingTransaksi.setUserId(transaksiBaru.getUserId());
        existingTransaksi.setStatus(transaksiBaru.getStatus()); // Status bisa diupdate oleh ADMIN
        existingTransaksi.setUpdatedBy(getCurrentUsername());

        return transaksiRepository.save(existingTransaksi);
    }

    @Override
    public Transaksi updateStatus(String id, String status) {
        Transaksi transaksi = getById(id); // Memastikan transaksi ada dan otorisasi sudah dicek

        // Otorisasi: Hanya KASIR dan ADMIN yang bisa update status
        String currentUserRole = getCurrentUserRole();
        if (!currentUserRole.equals("KASIR") && !currentUserRole.equals("ADMIN")) {
            throw new RuntimeException("Anda tidak memiliki izin untuk mengupdate status transaksi.");
        }

        String token = getCurrentToken();
        OrderResponseDTO order = null;
        try {
            order = getOrderFromOrderService(transaksi.getOrderId(), token);
        } catch (WebClientResponseException.NotFound ex) {
            logger.error("Order tidak ditemukan di order-service saat update status transaksi untuk ID: {}", transaksi.getOrderId());
            throw new NotFoundException("Order terkait tidak ditemukan: " + transaksi.getOrderId());
        } catch (Exception e) {
            logger.error("Gagal mengambil order dari order-service saat update status transaksi untuk ID {}: {}", transaksi.getOrderId(), e.getMessage());
            throw new RuntimeException("Gagal mengambil order terkait: " + e.getMessage());
        }


        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Status tidak valid: hanya bisa PENDING, BERHASIL, atau GAGAL");
        }
        StatusTransaksi newStatus = StatusTransaksi.valueOf(status.toUpperCase());


        // Logika validasi uang hanya jika status diubah menjadi BERHASIL
        if (newStatus == StatusTransaksi.BERHASIL) {
            // Pastikan uang yang dibayarkan cukup untuk order ini
            if (transaksi.getTotalBayar() < order.getTotalAmount()) {
                throw new RuntimeException("Jumlah pembayaran tidak mencukupi untuk mengubah status menjadi BERHASIL.");
            }
        }

        transaksi.setStatus(newStatus);
        transaksi.setUpdatedBy(getCurrentUsername());
        transaksi.setWaktuTransaksi(LocalDateTime.now());


        // Update order status di order-service juga
        try {
            updateOrderStatus(transaksi.getOrderId(), newStatus.name(), token);
            logger.info("Status order {} berhasil diubah menjadi {}", transaksi.getOrderId(), newStatus.name());
        } catch (Exception e) {
            logger.error("Gagal mengupdate status order {} di order-service: {}", transaksi.getOrderId(), e.getMessage());
        }

        return transaksiRepository.save(transaksi);
    }

    
    public void deleteTransaksiById(String id) {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Hanya ADMIN yang boleh menghapus transaksi.");
        }

        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan dengan ID: " + id));

        transaksiRepository.deleteById(id);
        logger.info("Transaksi ID '{}' telah dihapus oleh ADMIN '{}'", id, username);
    }



    // ===============================
    //          WEBCLIENT METHODS
    // ===============================

    // Menggunakan nama service Eureka untuk load balancing
    public OrderResponseDTO getOrderFromOrderService(String orderId, String token) { 
        logger.info("Memanggil order-service untuk order ID: {}", orderId);
        return webClientBuilder.baseUrl("http://localhost:1012") // Menggunakan lb:// untuk Eureka
            .build()
            .get()
            .uri("/orders/getByOrderId/{id}", orderId) // Sesuaikan endpoint di order-service
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(OrderResponseDTO.class)
            .block();
    }

    public void updateOrderStatus(String orderId, String newStatus, String token) {
        logger.info("Mengupdate status order {} di order-service menjadi {}", orderId, newStatus);
        webClientBuilder.baseUrl("http://localhost:1012") // Menggunakan lb:// untuk Eureka
            .build()
            .put()
            .uri("/orders/updateStatusOnly/{id}", orderId) // Sesuaikan endpoint di order-service
            .header("Authorization", "Bearer " + token)
            .bodyValue(Map.of("status", newStatus))
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }

    private String getCurrentToken() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof String) {
            return (String) credentials;
        }
        logger.warn("Token tidak ditemukan di SecurityContext.");
        return null;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        logger.warn("Username tidak ditemukan di SecurityContext, menggunakan 'anonymous'.");
        return "anonymous";
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }
}
