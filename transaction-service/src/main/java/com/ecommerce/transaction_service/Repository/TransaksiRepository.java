package com.ecommerce.transaction_service.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ecommerce.transaction_service.Model.StatusTransaksi;
import com.ecommerce.transaction_service.Model.Transaksi;

public interface TransaksiRepository extends MongoRepository<Transaksi, String> {

    List <Transaksi> findByUserId(String userId);

    List<Transaksi> findByUserIdAndStatus(String userId, StatusTransaksi status);

    List<Transaksi> findByCreatedBy(String createdBy);

}
