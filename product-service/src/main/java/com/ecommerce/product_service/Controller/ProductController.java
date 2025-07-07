package com.ecommerce.product_service.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.product_service.DTO.ProductRequestDTO;
import com.ecommerce.product_service.DTO.ProductResponseDTO;
import com.ecommerce.product_service.Service.ProductService;
import com.ecommerce.product_service.Utils.ApiResponse;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // ✅ Tambah produk
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> create(@RequestBody ProductRequestDTO request) {
        ProductResponseDTO created = service.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Produk berhasil dibuat", 201, created));
    }

    // ✅ Get semua produk
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAll() {
        List<ProductResponseDTO> list = service.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil semua produk", 200, list));
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getById(@PathVariable String id) {
        ProductResponseDTO product = service.getById(id);
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil produk", 200, product));
    }

    // ✅ Update produk
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> update(@PathVariable String id,
            @RequestBody ProductRequestDTO updated) {
        ProductResponseDTO updatedProduct = service.updateProduct(id, updated);
        return ResponseEntity.ok(new ApiResponse<>("Produk berhasil diperbarui", 200, updatedProduct));
    }

    // ✅ Hapus produk
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String id) {
        service.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>("Produk berhasil dihapus", 200, id));
    }

    // ✅ Kurangi stok
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<ApiResponse<String>> reduceStock(@PathVariable String id, @RequestParam int quantity) {
        boolean success = service.reduceStock(id, quantity);
        if (success) {
            return ResponseEntity.ok(new ApiResponse<>("Stok berhasil dikurangi", 200, id));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Stok tidak mencukupi", 400, id));
        }
    }

    // ✅ Cari berdasarkan nama/kategori
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        List<ProductResponseDTO> result;

        if (name != null) {
            result = service.searchByName(name);
            return ResponseEntity.ok(new ApiResponse<>("Pencarian berdasarkan nama berhasil", 200, result));
        }

        if (category != null) {
            result = service.searchByCategory(category);
            return ResponseEntity.ok(new ApiResponse<>("Pencarian berdasarkan kategori berhasil", 200, result));
        }

        result = service.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil semua produk", 200, result));
    }

    // ✅ Ambil semua produk milik user saat ini
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getMyProducts() {
        List<ProductResponseDTO> userProducts = service.findByCurrentUser();
        return ResponseEntity.ok(new ApiResponse<>("Berhasil mengambil produk milik user", 200, userProducts));
    }
}
