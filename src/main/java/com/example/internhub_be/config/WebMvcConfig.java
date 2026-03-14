package com.example.internhub_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // Added import for CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /assets/avatars/** to the physical uploads/avatars/ directory
        Path avatarUploadDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        registry.addResourceHandler("/assets/avatars/**")
                .addResourceLocations("file:" + avatarUploadDir.toString() + "/");

        // Also add a generic resource handler for /assets/ if needed for other static files
        // Path baseUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        // String basePath = baseUploadDir.toUri().toString();
        // registry.addResourceHandler("/assets/**")
        //         .addResourceLocations(basePath);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins("http://localhost:4200") // Allow frontend origin
                .allowedMethods("*") // Allow all HTTP methods (GET, POST, PUT, PATCH, DELETE, OPTIONS)
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow sending credentials (e.g., cookies, authorization headers)
    }
}
