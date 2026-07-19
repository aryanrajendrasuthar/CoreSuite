package com.coresuite.shared.error;

/** Thrown when a call to another CoreSuite service fails or is unreachable. */
public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
