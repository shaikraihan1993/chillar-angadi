package com.grocerystore.grocery_store.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

/**
 * Session-based security.
 *  - Shoppers browse and check out as guests; signing in is optional.
 *  - /api/account/** needs any signed-in user.
 *  - Admin pages (/admin/**) and every admin API need ROLE_ADMIN; the admin
 *    sign-in screen lives at /admin/login and is not linked from the store.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        new BCryptPasswordEncoder()
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        new HttpSessionSecurityContextRepository()
    }

    @Bean
    AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        def provider = new DaoAuthenticationProvider()
        provider.userDetailsService = userDetailsService
        provider.passwordEncoder = passwordEncoder
        new ProviderManager(provider)
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) {
        http
            // JSON-only API + SameSite=Lax session cookie; no form posts to protect
            .csrf { it.disable() }
            .securityContext { it.securityContextRepository(securityContextRepository) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Storefront and its assets
                    .requestMatchers('/', '/index.html', '/store.html', '/css/**', '/js/**', '/images/**', '/favicon.ico', '/error').permitAll()
                    // Admin sign-in screen
                    .requestMatchers('/admin/login', '/admin/login.html').permitAll()
                    // Sign in / sign up / session status
                    .requestMatchers('/api/auth/**').permitAll()
                    .requestMatchers('/actuator/health').permitAll()
                    // Public catalogue reads
                    .requestMatchers(HttpMethod.GET, '/api/v1/products', '/api/v1/products/**', '/api/v1/images/**', '/api/v1/enums/**').permitAll()
                    // Guest or signed-in checkout
                    .requestMatchers(HttpMethod.POST, '/api/v1/checkout').permitAll()
                    // Customer's own data
                    .requestMatchers('/api/account/**').authenticated()
                    // Everything else (admin pages, orders, product writes, uploads, dashboard, swagger) is admin-only
                    .anyRequest().hasRole('ADMIN')
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { HttpServletRequest req, HttpServletResponse res, e ->
                    SecurityConfig.deny(req, res, HttpServletResponse.SC_UNAUTHORIZED, 'Please sign in to continue')
                }
                ex.accessDeniedHandler { HttpServletRequest req, HttpServletResponse res, e ->
                    SecurityConfig.deny(req, res, HttpServletResponse.SC_FORBIDDEN, 'You do not have access to this page')
                }
            }
        http.build()
    }

    /** APIs get a JSON error; page requests are sent to the admin sign-in screen. */
    static void deny(HttpServletRequest req, HttpServletResponse res, int status, String message) {
        String path = req.requestURI - req.contextPath
        if (path.startsWith('/api/')) {
            res.status = status
            res.contentType = 'application/json'
            res.writer.write("{\"message\":\"${message}\"}")
        } else {
            String next = URLEncoder.encode(path, 'UTF-8')
            res.sendRedirect("${req.contextPath}/admin/login?next=${next}${status == HttpServletResponse.SC_FORBIDDEN ? '&denied=1' : ''}")
        }
    }
}
