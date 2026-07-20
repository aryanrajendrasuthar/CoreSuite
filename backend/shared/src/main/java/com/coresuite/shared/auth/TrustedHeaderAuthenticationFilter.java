package com.coresuite.shared.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Trusts identity headers set only by api-gateway, after it has validated the
 * caller's session. A request missing a valid {@link AuthHeaders#GATEWAY_SECRET}
 * is left unauthenticated — Spring Security's own authorization rules then
 * reject it. This is defense-in-depth against a service being reached
 * directly, bypassing the gateway; the real boundary is network isolation in
 * production (see SECURITY.md).
 */
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final String gatewaySecret;

    public TrustedHeaderAuthenticationFilter(String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String presentedSecret = request.getHeader(AuthHeaders.GATEWAY_SECRET);
        String userId = request.getHeader(AuthHeaders.USER_ID);

        if (gatewaySecret.equals(presentedSecret) && StringUtils.hasText(userId)) {
            List<SimpleGrantedAuthority> authorities = parseRoles(request.getHeader(AuthHeaders.USER_ROLES));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> parseRoles(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return List.of();
        }
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
