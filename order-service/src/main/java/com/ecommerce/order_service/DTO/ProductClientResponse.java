package com.ecommerce.order_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductClientResponse {

    private String id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private String brand;
}
