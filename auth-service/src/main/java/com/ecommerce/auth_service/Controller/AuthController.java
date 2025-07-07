package com.ecommerce.auth_service.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.auth_service.DTO.AuthRequest;
import com.ecommerce.auth_service.DTO.RegisterRequest;
import com.ecommerce.auth_service.Service.AuthService;
import com.ecommerce.auth_service.Utils.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
        ApiResponse<String> response = authService.saveUser(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody AuthRequest authRequest) {
        ApiResponse<String> response = authService.login(authRequest.getUsername(), authRequest.getPassword());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validate(@RequestParam("token") String token) {
        authService.validateToken(token);
        ApiResponse<String> response = new ApiResponse<>("Token valid", 200, null);
        return ResponseEntity.ok(response);
    }
}
