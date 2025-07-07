package com.ecommerce.order_service.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ecommerce.order_service.Model.Order;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);

    List<Order> findByStatus(String status);

    List<Order> findByUserIdAndStatus(String userId, String status);
}
