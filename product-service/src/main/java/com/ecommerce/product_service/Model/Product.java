package com.ecommerce.product_service.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private String id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private String brand;
    private String createdBy;
    private String updatedBy;
}
