package com.ecommerce.product_service.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Nama produk tidak boleh kosong")
    private String name;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    private String description;

    @NotNull(message = "Harga produk wajib diisi")
    @Min(value = 0, message = "Harga tidak boleh kurang dari 0")
    private Double price;

    @NotNull(message = "Stok wajib diisi")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stock;

    @NotBlank(message = "Kategori produk tidak boleh kosong")
    private String category;

    @NotBlank(message = "Brand produk tidak boleh kosong")
    private String brand;
}