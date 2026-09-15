package com.deskhand.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wide-open CORS on {@code /api/**} - portfolio demo only, matching the original Python backend's
 * own {@code allow_origins=["*"]} and its explicit "portfolio demo only" comment. This lets a
 * frontend deployed on a different origin (e.g. Netlify) call this API directly; local dev doesn't
 * strictly need it since Vite's dev server proxies {@code /api} to this backend (see
 * frontend/vite.config.ts), making requests same-origin from the browser's perspective.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST");
    }
}
