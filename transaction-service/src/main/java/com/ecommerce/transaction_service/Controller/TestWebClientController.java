package com.ecommerce.transaction_service.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.transaction_service.DTO.OrderResponseDTO;
import com.ecommerce.transaction_service.Service.TransaksiService;

@RestController
@RequestMapping("/webclient-test")
public class TestWebClientController {


  private static final Logger logger = LoggerFactory.getLogger(TestWebClientController.class);

    private final TransaksiService transaksiService;

    public TestWebClientController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderResponseDTO> testGetOrderFromOrderService(@PathVariable String orderId) {
        String token = getCurrentToken();

        logger.info("Testing WebClient: memanggil order-service untuk orderId {}", orderId);
        OrderResponseDTO response = transaksiService.getOrderFromOrderService(orderId, token);

        return ResponseEntity.ok(response);
    }

    private String getCurrentToken() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof String) {
            return (String) credentials;
        }
        return null;
    }
}
