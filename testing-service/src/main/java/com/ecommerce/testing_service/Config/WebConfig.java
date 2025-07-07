package com.ecommerce.testing_service.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ecommerce.testing_service.Middleware.LoggingInterceptor;
import com.ecommerce.testing_service.Middleware.UserContextInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(userContextInterceptor);
    }
}
