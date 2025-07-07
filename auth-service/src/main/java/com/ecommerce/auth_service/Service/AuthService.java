package com.ecommerce.auth_service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.auth_service.DTO.RegisterRequest;
import com.ecommerce.auth_service.Mapper.UserMapper;
import com.ecommerce.auth_service.Midlleware.JwtMidlleware;
import com.ecommerce.auth_service.Model.UserCredential;
import com.ecommerce.auth_service.Repository.UserCredentialRepository;
import com.ecommerce.auth_service.Utils.ApiResponse;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserCredentialRepository ucr;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtMidlleware jwtMidlleware;


    public ApiResponse<String> saveUser(RegisterRequest request) {
        if (ucr.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Gagal register, username sudah digunakan: {}", request.getUsername());
            throw new RuntimeException("Username sudah digunakan, silakan pilih yang lain.");
        }

        UserCredential credential = UserMapper.toEntity(request);
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        ucr.save(credential);
        logger.info("Berhasil register user baru: {}", credential.getUsername());

        return new ApiResponse<>("User berhasil didaftarkan", 201, "true");
    }


    public ApiResponse<String> generateToken(String userName) {
        UserCredential user = ucr.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        String token = jwtMidlleware.generateToken(userName, user.getRole().name());
        logger.info("Token berhasil dibuat untuk user: {}", userName);

        return new ApiResponse<>("Token berhasil dibuat", 200, token);
    }

    public ApiResponse<String> login(String username, String rawPassword) {
        UserCredential user = ucr.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username tidak ditemukan"));

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            String token = jwtMidlleware.generateToken(user.getUsername(), user.getRole().name());
            logger.info("Login berhasil: {}", username);
            return new ApiResponse<>("Login sukses", 200, token);
        } else {
            logger.warn("Login gagal karena password salah untuk: {}", username);
            throw new RuntimeException("Password tidak valid");
        }
    }

    public void validateToken(String token) {
        jwtMidlleware.validateToken(token);
        logger.info("Token valid");
    }
}
