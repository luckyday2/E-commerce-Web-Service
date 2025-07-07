package com.ecommerce.transaction_service.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "transaksi")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaksi {
    @Id
    private String id;
    private String orderId;
    private LocalDateTime WaktuTransaksi;
    private MetodeBayarTransaksi metodeBayar;
    private double totalBayar;


    private String userId;
    private StatusTransaksi status;

    private String createdBy;
    private String updatedBy;

}
