package com.glavsoft.exceptions;

public class AuthenticationFailedException extends ProtocolException {
    private String reason;

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }
}
