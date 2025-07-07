package com.ecommerce.product_service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.product_service.DTO.ProductRequestDTO;
import com.ecommerce.product_service.DTO.ProductResponseDTO;
import com.ecommerce.product_service.Mapper.ProductMapper;
import com.ecommerce.product_service.Model.Product;
import com.ecommerce.product_service.Repository.ProductRepository;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

    private boolean isCashier() {
        return getCurrentUserRole().equals("KASIR");
    }

    private boolean isUser() {
        return getCurrentUserRole().equals("USER");
    }

    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        String username = getCurrentUsername();
        Product product = ProductMapper.toEntity(dto);
        product.setCreatedBy(username);

        Product saved = productRepository.save(product);
        logger.info("Produk berhasil dibuat oleh: {}", username);

        return ProductMapper.toDTO(saved);
    }

    public List<ProductResponseDTO> getAllProducts() {
        String username = getCurrentUsername();
        String role = getCurrentUserRole();

        logger.info("User '{}' dengan role '{}' mengambil semua produk", username, role);

        List<Product> products = isUser()
                ? productRepository.findAll().stream()
                        .filter(p -> username.equals(p.getCreatedBy()))
                        .collect(Collectors.toList())
                : productRepository.findAll();

        return products.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getById(String id) {

        // Tambahkan validasi format ID
        if (!ObjectId.isValid(id)) {
            throw new RuntimeException("Format ID produk tidak valid");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan dengan ID: " + id));

        String username = getCurrentUsername();

        if (isUser() && !username.equals(product.getCreatedBy())) {
            throw new RuntimeException("Kamu tidak punya akses melihat produk ini");
        }

        logger.info("Produk ID '{}' diakses oleh '{}'", id, username);
        return ProductMapper.toDTO(product);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        String username = getCurrentUsername();

        if (isUser() && !username.equals(product.getCreatedBy())) {
            throw new RuntimeException("Kamu tidak punya akses menghapus produk ini");
        }

        if (isCashier()) {
            throw new RuntimeException("KASIR tidak diperbolehkan menghapus produk");
        }

        productRepository.deleteById(id);
        logger.info("Produk ID '{}' dihapus oleh '{}'", id, username);
    }

    public ProductResponseDTO updateProduct(String id, ProductRequestDTO updatedData) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        String username = getCurrentUsername();

        if (isUser()) {
            throw new RuntimeException("USER tidak diperbolehkan mengupdate produk");
        }

        if (isCashier()) {
            existing.setStock(updatedData.getStock());
        } else {
            existing.setName(updatedData.getName());
            existing.setDescription(updatedData.getDescription());
            existing.setPrice(updatedData.getPrice());
            existing.setStock(updatedData.getStock());
            existing.setCategory(updatedData.getCategory());
            existing.setBrand(updatedData.getBrand());
        }

        existing.setUpdatedBy(username);
        Product updated = productRepository.save(existing);

        return ProductMapper.toDTO(updated);
    }

    public List<ProductResponseDTO> searchByName(String name) {
        logger.info("Mencari produk dengan nama: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> searchByCategory(String category) {
        logger.info("Mencari produk berdasarkan kategori: {}", category);
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }

    public boolean reduceStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        if (product.getStock() < quantity) {
            logger.warn("Stok produk {} tidak mencukupi. Stok saat ini: {}, diminta: {}", productId,
                    product.getStock(), quantity);
            return false;
        }

        product.setStock(product.getStock() - quantity);
        product.setUpdatedBy(getCurrentUsername());
        productRepository.save(product);

        logger.info("Stok produk ID '{}' dikurangi sebanyak {}", productId, quantity);
        return true;
    }

    public List<ProductResponseDTO> findByCurrentUser() {
        String username = getCurrentUsername();

        List<Product> userProducts = productRepository.findByCreatedBy(username);

        logger.info("Mengambil semua produk yang dibuat oleh user '{}'. Jumlah produk: {}", username,
                userProducts.size());

        return userProducts.stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
    }
}
