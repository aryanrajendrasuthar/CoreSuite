package com.coresuite.gateway.service;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many attempts — try again later");
    }
}
