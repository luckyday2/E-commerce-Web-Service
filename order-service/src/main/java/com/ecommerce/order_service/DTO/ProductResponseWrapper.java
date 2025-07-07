package com.ecommerce.order_service.DTO;

import lombok.Data;

@Data
public class ProductResponseWrapper {

    private String message;
    private int status;
    private ProductClientResponse data;
}
