package com.ecommerce.order_service.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {

    private String userId;
    private LocalDateTime orderDate;
    private String status; // e.g. "PENDING", "PAID", "SHIPPED", "CANCELLED"
    private Double totalAmount;

    private List<OrderItemRequestDTO> items;
}
