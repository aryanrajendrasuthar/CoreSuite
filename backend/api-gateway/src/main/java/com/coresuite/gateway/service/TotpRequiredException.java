package com.coresuite.gateway.service;

public class TotpRequiredException extends RuntimeException {

    public TotpRequiredException() {
        super("Two-factor authentication code required");
    }
}
