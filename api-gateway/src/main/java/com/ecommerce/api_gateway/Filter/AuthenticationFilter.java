package com.ecommerce.api_gateway.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.ecommerce.api_gateway.Util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    // @Autowired
    // private RestTemplate template;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @SuppressWarnings("null")
    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // header contains token or not
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }
                System.out.println("Melewati AuthenticationFilter");
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                System.out.println("Token dari header: " + authHeader);

                // String token;
                // if (authHeader != null && !authHeader.startsWith("Bearer ")) {
                // // authHeader = authHeader.substring(7);
                // token = authHeader.substring(7);
                // } else {
                // throw new RuntimeException("Token tidak valid atau tidak diawali dengan
                // 'Bearer '");
                // }
                String token;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                } else {
                    throw new RuntimeException("Token tidak valid atau tidak diawali dengan 'Bearer '");
                }
                try {
                    // // Rest call to AUTH Service
                    // template.getForObject("http://AUTH-SERVICE//validate?token=" + authHeader,
                    // String.class);

                    // jwtUtil.validateToken(authHeader);
                    // validasi token
                    jwtUtil.validateToken(token);

                    // ambil username dari token & simpan ke context
                    String username = jwtUtil.getUsernameFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    UserContext.setUsername(username);
                    UserContext.setRole(role);

                    exchange.mutate().request(
                            exchange.getRequest().mutate()
                                    .header("X-Username", username)
                                    .header("X-User-Role", role)
                                    .build())
                            .build();
                    System.out.println("✅ Authenticated user: " + username + " | Role: " + role);
                } catch (Exception e) {
                    System.out.println("invalid access ...!");
                    throw new RuntimeException("un authorized access to application");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }

}
