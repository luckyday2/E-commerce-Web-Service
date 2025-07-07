package com.ecommerce.product_service.Mapper;

import com.ecommerce.product_service.DTO.ProductRequestDTO;
import com.ecommerce.product_service.DTO.ProductResponseDTO;
import com.ecommerce.product_service.Model.Product;

public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setBrand(dto.getBrand());
        return product;
    }

    public static ProductResponseDTO toDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getBrand(),
                product.getCreatedBy(),
                product.getUpdatedBy());
    }
}
