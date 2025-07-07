package com.ecommerce.testing_service.Middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtMiddleware jwtMiddleware;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtMiddleware.getUsernameFromToken(token);
                String role = jwtMiddleware.getRoleFromToken(token); // kalau sudah ada
                UserContext.setUsername(username);
                UserContext.setRole(role); // kalau kamu pakai role juga
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

    // @Override
    // public boolean preHandle(@NonNull HttpServletRequest request,
    // @NonNull HttpServletResponse response,
    // @NonNull Object handler) {
    // System.out.println("Masuk Interceptor");
    // String authHeader = request.getHeader("Authorization");
    // System.out.println("AuthHeader: " + authHeader);
    // if (authHeader != null && authHeader.startsWith("Bearer ")) {
    // String token = authHeader.substring(7);
    // try {
    // JwtMiddleware jwtMiddleware = new JwtMiddleware(); // atau Autowire
    // String username = jwtMiddleware.getUsernameFromToken(token);
    // UserContext.setUsername(username);
    // } catch (Exception e) {
    // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    // return false;
    // }
    // }

    // return true;
    // }
}
