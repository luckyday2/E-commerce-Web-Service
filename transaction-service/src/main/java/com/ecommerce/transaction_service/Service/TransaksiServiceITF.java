package com.ecommerce.transaction_service.Service;

import java.util.List;

import com.ecommerce.transaction_service.DTO.TransaksiRequestDTO;
import com.ecommerce.transaction_service.DTO.TransaksiResponseDTO;
import com.ecommerce.transaction_service.Model.Transaksi;

public interface TransaksiServiceITF {
    TransaksiResponseDTO createTransaksi(TransaksiRequestDTO requestDTO); // Tambahkan ini
    List<Transaksi> allTransaksi();
    Transaksi getById(String id);
    List<Transaksi> getByUserId(String userId);
    Transaksi update(String id, Transaksi transaksiBaru);
    Transaksi updateStatus(String id, String status);
    void deleteTransaksiById(String id);
}
