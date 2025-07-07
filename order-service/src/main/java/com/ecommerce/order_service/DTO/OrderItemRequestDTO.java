package com.ecommerce.order_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDTO {

    private Long orderItemId; // Primary Key

    private String id; // ID produk dari Product Service

    private String productName; // redundant info (untuk display cepat)

    private Double productPrice; // harga satuan saat transaksi

    private Integer quantity;

    private Double subtotal;
}
