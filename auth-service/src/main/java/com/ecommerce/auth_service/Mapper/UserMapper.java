package com.ecommerce.auth_service.Mapper;

import com.ecommerce.auth_service.DTO.RegisterRequest;
import com.ecommerce.auth_service.Model.Role;
import com.ecommerce.auth_service.Model.UserCredential;

public class UserMapper {
    public static UserCredential toEntity(RegisterRequest dto) {
        UserCredential entity = new UserCredential();
        entity.setName(dto.getName());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setRole(Role.USER);
        return entity;
    }
}
