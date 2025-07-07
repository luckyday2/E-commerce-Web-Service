package com.ecommerce.order_service.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "OrderItem")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    private String orderItemId;

    private String productId;
    private String productName;
    private Double productPrice;
    private Integer quantity;
    private Double subtotal;

}
