package com.neu.finalproject.meskot.config;

import com.neu.finalproject.meskot.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/movies/upload/**", "/api/movies/download/**", "/api/user/**", "/api/history/**", "/api/admin/**")
            .order(0); // Ensure this runs first
    }
}
