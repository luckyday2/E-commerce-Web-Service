package com.ecommerce.testing_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestingCreate {
    private String name;
    private String description;
    private Boolean status;

}
