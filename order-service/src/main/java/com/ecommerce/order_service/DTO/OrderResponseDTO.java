package com.ecommerce.order_service.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private String orderId;
    private String userId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponseDTO> items;
}
