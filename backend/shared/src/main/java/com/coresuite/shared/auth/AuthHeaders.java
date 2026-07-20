package com.coresuite.shared.auth;

/**
 * Header names used between api-gateway and the backend services to carry a
 * validated identity. Backend services never see the session cookie or
 * touch Redis — they trust these headers, which only the gateway sets.
 */
public final class AuthHeaders {

    public static final String GATEWAY_SECRET = "X-Gateway-Secret";
    public static final String USER_ID = "X-User-Id";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLES = "X-User-Roles";

    private AuthHeaders() {
    }
}
