package com.ecommerce.transaction_service.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaksiRequestDTO {
    
    @NotBlank(message = "Order Id Wajib diisi")
    private String orderId;

    @NotNull(message = "Total bayar (uang dibayar) wajib diisi")
    @Min(value = 0, message = "Total bayar tidak boleh negatif")
    private Double totalBayar;

    @NotBlank(message = "Metode pembayaran wajib diisi")
    private String metodeBayar;

}
