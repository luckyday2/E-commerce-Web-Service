package com.ecommerce.transaction_service.DTO;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private String orderItemId;
    private String productId;
    private String productName;
    private double productPrice;
    private int quantity;
    private double subtotal;
}

