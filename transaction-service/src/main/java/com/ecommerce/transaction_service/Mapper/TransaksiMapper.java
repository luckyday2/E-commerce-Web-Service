package com.ecommerce.transaction_service.Mapper;

import java.time.LocalDateTime;

import com.ecommerce.transaction_service.DTO.OrderResponseDTO;
import com.ecommerce.transaction_service.DTO.TransaksiRequestDTO;
import com.ecommerce.transaction_service.DTO.TransaksiResponseDTO;
import com.ecommerce.transaction_service.Model.MetodeBayarTransaksi;
import com.ecommerce.transaction_service.Model.StatusTransaksi;
import com.ecommerce.transaction_service.Model.Transaksi;

public class TransaksiMapper {

    // Digunakan saat CREATE transaksi baru dari request DTO
    public static Transaksi toEntity(TransaksiRequestDTO dto, String createdBy, String userId) {
        Transaksi transaksi = new Transaksi();
        transaksi.setOrderId(dto.getOrderId());
        transaksi.setTotalBayar(dto.getTotalBayar()); // Ini adalah uang yang dibayarkan customer
        transaksi.setMetodeBayar(MetodeBayarTransaksi.valueOf(dto.getMetodeBayar().toUpperCase()));
        transaksi.setWaktuTransaksi(LocalDateTime.now());
        transaksi.setUserId(userId); // userId diambil dari order
        transaksi.setStatus(StatusTransaksi.PENDING); // Status awal selalu PENDING, nanti di service akan diubah
        transaksi.setCreatedBy(createdBy);
        transaksi.setUpdatedBy(createdBy);
        return transaksi;
    }

    // Digunakan saat UPDATE transaksi (misal oleh ADMIN)
    public static void updateEntity(Transaksi transaksi, TransaksiRequestDTO dto, String updatedBy) {
        // Hanya update field yang relevan dari DTO jika tidak null
        if (dto.getOrderId() != null) {
            transaksi.setOrderId(dto.getOrderId());
        }
        if (dto.getTotalBayar() != null) {
            transaksi.setTotalBayar(dto.getTotalBayar()); // Uang yang dibayarkan customer
        }
        if (dto.getMetodeBayar() != null) {
            transaksi.setMetodeBayar(MetodeBayarTransaksi.valueOf(dto.getMetodeBayar().toUpperCase()));
        }
        // userId dan status tidak diupdate dari TransaksiRequestDTO karena dikelola di service
        transaksi.setUpdatedBy(updatedBy);
    }

    // Untuk mengubah entity ke response DTO
    public static TransaksiResponseDTO toDTO(Transaksi transaksi, OrderResponseDTO orderDetails) {
        TransaksiResponseDTO dto = new TransaksiResponseDTO();
        dto.setId(transaksi.getId());
        dto.setOrderId(transaksi.getOrderId());
        dto.setTotalBayar(transaksi.getTotalBayar()); // Ini adalah uang yang dibayarkan customer
        dto.setMetodeBayar(transaksi.getMetodeBayar().toString());
        dto.setWaktuTransaksi(transaksi.getWaktuTransaksi());
        dto.setUserId(transaksi.getUserId());
        dto.setStatus(transaksi.getStatus().toString());
        dto.setCreatedBy(transaksi.getCreatedBy());
        dto.setUpdatedBy(transaksi.getUpdatedBy());
        dto.setOrderDetails(orderDetails);
        return dto;
    }
}
