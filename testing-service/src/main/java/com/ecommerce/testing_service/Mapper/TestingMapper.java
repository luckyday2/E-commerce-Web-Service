package com.ecommerce.testing_service.Mapper;

import com.ecommerce.testing_service.DTO.TestingCreate;
import com.ecommerce.testing_service.DTO.TestingResponse;
import com.ecommerce.testing_service.Model.TestingData;

public class TestingMapper {
    // Mapping dari entity ke DTO Response
    public static TestingResponse toResponse(TestingData data) {
        return new TestingResponse(
                data.getId(),
                data.getName(),
                data.getDescription(),
                data.getStatus(),
                data.getCreatedBy() // ✅ tambahkan ini
        );
    }

    // Mapping dari DTO Create ke entity
    public static TestingData toEntity(TestingCreate dto) {
        TestingData entity = new TestingData();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        // createdBy akan diset di service
        return entity;
    }
}
