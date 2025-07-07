package com.ecommerce.transaction_service.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.transaction_service.DTO.OrderResponseDTO;
import com.ecommerce.transaction_service.DTO.TransaksiRequestDTO;
import com.ecommerce.transaction_service.DTO.TransaksiResponseDTO;
import com.ecommerce.transaction_service.Mapper.TransaksiMapper;
import com.ecommerce.transaction_service.Model.Transaksi;
import com.ecommerce.transaction_service.Service.TransaksiService;
import com.ecommerce.transaction_service.Utils.ApiResponse;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/transaction")
public class TransaksiController {

     private static final Logger logger = LoggerFactory.getLogger(TransaksiController.class);

    private final TransaksiService transaksiService;

    public TransaksiController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    


    // Endpoint untuk membuat transaksi baru
    @PostMapping
    public ResponseEntity<ApiResponse<TransaksiResponseDTO>> createTransaksi(@RequestBody @Valid TransaksiRequestDTO requestDTO) {
        TransaksiResponseDTO response = transaksiService.createTransaksi(requestDTO);
        return ResponseEntity.ok(new ApiResponse<>("Transaksi berhasil dibuat", 200, response));
    }


    // ADMIN + KASIR - Ambil semua transaksi
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransaksiResponseDTO>>> getAll() {
        String token = getCurrentToken();
        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        logger.info("User '{}' dengan role '{}' memanggil transaksi", username, role);

        List<Transaksi> transaksiList = getCurrentUserRole().equals("USER")
                ? transaksiService.getByUsername(username)
                : transaksiService.allTransaksi();

        List<TransaksiResponseDTO> response = transaksiList.stream()
                .map(transaksi -> {
                    OrderResponseDTO order = null;
                    try {
                        order = transaksiService.getOrderFromOrderService(transaksi.getOrderId(), token);
                    } catch (Exception e) {
                        logger.warn("Gagal mengambil order {}: {}", transaksi.getOrderId(), e.getMessage());
                    }
                    return TransaksiMapper.toDTO(transaksi, order);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil semua transaksi", 200, response));
    }

    // Ambil transaksi berdasarkan ID (ADMIN saja)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransaksiResponseDTO>> getById(@PathVariable String id) {
        String token = getCurrentToken();
        Transaksi transaksi = transaksiService.getById(id);
        OrderResponseDTO order = transaksiService.getOrderFromOrderService(transaksi.getOrderId(), token);
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil transaksi", 200, TransaksiMapper.toDTO(transaksi, order)));
    }

    // Ambil transaksi berdasarkan userId (khusus USER melihat miliknya)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransaksiResponseDTO>>> getByUser(@PathVariable String userId) {
        String token = getCurrentToken();
        List<TransaksiResponseDTO> response = transaksiService.getByUserId(userId).stream().map(transaksi -> {
            OrderResponseDTO order = transaksiService.getOrderFromOrderService(transaksi.getOrderId(), token);
            return TransaksiMapper.toDTO(transaksi, order);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil transaksi user", 200, response));
    }

    // ADMIN - Update transaksi full
    @PutMapping("/{id}")
    public ResponseEntity<TransaksiResponseDTO> update(@PathVariable String id,
                                                       @RequestBody @Valid TransaksiRequestDTO dto) {
        String token = getCurrentToken();
        Transaksi existing = transaksiService.getById(id);
        TransaksiMapper.updateEntity(existing, dto, getCurrentUsername());
        Transaksi updated = transaksiService.update(id, existing);
        OrderResponseDTO order = transaksiService.getOrderFromOrderService(updated.getOrderId(), token);
        return ResponseEntity.ok(TransaksiMapper.toDTO(updated, order));
    }

    // Hapus transaksi (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTransaksiById(@PathVariable String id) {
        transaksiService.deleteTransaksiById(id);
        return ResponseEntity.ok(new ApiResponse<>("Transaksi dengan ID " + id + " telah dihapus", 200, null));
    }

    // KASIR & ADMIN - Update hanya status transaksi
    // Endpoint ini digunakan oleh kasir untuk memvalidasi pembayaran secara manual
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransaksiResponseDTO>> updateStatus(@PathVariable String id,
                                                            @RequestBody Map<String, String> body) {
        // Validasi input status
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status tidak boleh kosong.");
        }

        // Ambil token dari security context
        String token = getCurrentToken();
        if (token == null) {
            throw new RuntimeException("Token tidak ditemukan. Pastikan Anda login.");
        }

        // Update status transaksi
        Transaksi updatedTransaksi = transaksiService.updateStatus(id, status);

        // Ambil ulang data order dari order-service
        OrderResponseDTO orderDetails = transaksiService.getOrderFromOrderService(updatedTransaksi.getOrderId(), token);

        // Mapping ke DTO lengkap (transaksi + order)
        TransaksiResponseDTO responseDTO = TransaksiMapper.toDTO(updatedTransaksi, orderDetails);

        ApiResponse<TransaksiResponseDTO> response = new ApiResponse<>(
            "Status transaksi berhasil diupdate.",
            200,
            responseDTO
        );


        return ResponseEntity.ok(response);
    }


    // Ambil detail order dari transaksi
    @GetMapping("/{id}/order")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderDetailFromTransaksi(@PathVariable String id) {
        Transaksi transaksi = transaksiService.getById(id);
        String token = getCurrentToken();
        OrderResponseDTO order = transaksiService.getOrderFromOrderService(transaksi.getOrderId(), token);
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil detail order", 200, order));
    }


    // Helper method untuk mendapatkan username dari SecurityContext
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return "anonymous";
    }

    // Helper method untuk mendapatkan token dari SecurityContext
    private String getCurrentToken() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof String) {
            return (String) credentials;
        }
        return null;
    }

     // Mendapatkan role dari JWT
    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .findFirst()
                .map(role -> role.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }

    
}
