package com.ecommerce.testing_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.testing_service.Model.TestingData;

public interface TestingRepository extends JpaRepository<TestingData, Integer> {
    List<TestingData> findByCreatedBy(String createdBy);
}
