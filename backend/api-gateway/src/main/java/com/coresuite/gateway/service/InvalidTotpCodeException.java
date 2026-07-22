package com.coresuite.gateway.service;

public class InvalidTotpCodeException extends RuntimeException {

    public InvalidTotpCodeException() {
        super("Invalid two-factor authentication code");
    }
}
