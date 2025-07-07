package com.ecommerce.transaction_service.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransaksiResponseDTO {
    private String id;
    private String orderId;
    private double totalBayar;
    private String metodeBayar;
    private LocalDateTime waktuTransaksi;

    private String userId;
    private String status;

    private String createdBy;
    private String updatedBy;

    private OrderResponseDTO orderDetails; // Menyimpan detail order

}
