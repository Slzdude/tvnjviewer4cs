package com.glavsoft.exceptions;

public class TransportException extends CommonException {
    public TransportException(String message, Throwable exception) {
        super(message, exception);
    }

    public TransportException(Throwable exception) {
        super(exception);
    }
}
