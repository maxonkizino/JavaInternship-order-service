package com.javainternshiporderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class CsrfConfig {

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        // HttpOnly=true (default): token not readable by JS — preferred for XSS. SPAs that read the cookie need a different strategy.
        return new CookieCsrfTokenRepository();
    }
}
