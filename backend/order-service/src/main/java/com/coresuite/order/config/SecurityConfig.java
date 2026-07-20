package com.coresuite.order.config;

import com.coresuite.shared.auth.TrustedHeaderAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Every request must carry a valid trusted identity from api-gateway (see
 * TrustedHeaderAuthenticationFilter). There is no login here — this service
 * is never reached directly by a browser.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter(
            @Value("${coresuite.gateway-secret}") String gatewaySecret) {
        return new TrustedHeaderAuthenticationFilter(gatewaySecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                // Without httpBasic/formLogin, Spring Security has no
                // AuthenticationEntryPoint configured and falls back to
                // Http403ForbiddenEntryPoint for every auth failure — including
                // requests with no credentials at all. 401 is the correct code
                // for "no valid identity presented"; this API never wants a
                // browser login prompt anyway.
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(trustedHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
