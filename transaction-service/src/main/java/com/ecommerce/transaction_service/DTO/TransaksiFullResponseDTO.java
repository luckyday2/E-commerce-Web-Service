package com.ecommerce.transaction_service.DTO;

import com.ecommerce.transaction_service.Model.MetodeBayarTransaksi;
import com.ecommerce.transaction_service.Model.StatusTransaksi;

import lombok.Data;

@Data
public class TransaksiFullResponseDTO {
    private String transaksiId;
    private String orderId;
    private MetodeBayarTransaksi metodeBayar;
    private double totalBayar;
    private StatusTransaksi status;
    private OrderResponseDTO orderDetail;
}

