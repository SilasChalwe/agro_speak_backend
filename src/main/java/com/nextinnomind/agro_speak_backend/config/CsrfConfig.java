package com.nextinnomind.agro_speak_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * CSRF Configuration
 * Currently disabled in SecurityConfig for API endpoints using JWT tokens
 * This configuration can be used if you need CSRF protection for form-based authentication
 */
@Configuration
public class CsrfConfig {

    /**
     * CSRF Token Repository using cookies
     * This can be used if you need to enable CSRF protection
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath("/");
        // Cookie HttpOnly is already set to false by withHttpOnlyFalse()
        return repository;
    }
}