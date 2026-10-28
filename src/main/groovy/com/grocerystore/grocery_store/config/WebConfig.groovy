package com.grocerystore.grocery_store.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override
    void addViewControllers(ViewControllerRegistry registry) {
        // Admin console has its own sign-in URL, not linked from the store
        registry.addViewController("/admin/login").setViewName("forward:/admin/login.html")
        registry.addRedirectViewController("/admin", "/admin/index.html")
        registry.addRedirectViewController("/admin/", "/admin/index.html")
    }

    @Override
    void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:8089")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
