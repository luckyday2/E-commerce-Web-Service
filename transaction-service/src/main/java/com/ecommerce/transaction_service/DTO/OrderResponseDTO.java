package com.ecommerce.transaction_service.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderResponseDTO {
    private String orderId;
    private String userId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponseDTO> items;
}
