package com.ecommerce.api_gateway.Filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/register",
            "/auth/authenticate",
            // "/testing",
            "/eureka",
            "/api/docs");
    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        System.out.println("Request Path: " + path);

        return openApiEndpoints.stream()
                .noneMatch(uri -> path.startsWith(uri));
    };

    // public Predicate<ServerHttpRequest> isSecured = request ->
    // openApiEndpoints.stream()
    // .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

    // public Object isSecured;
}
