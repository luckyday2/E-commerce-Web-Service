package com.ecommerce.order_service.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Order")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    private String orderId;

    private String userId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;

    private List<OrderItem> items;
}
