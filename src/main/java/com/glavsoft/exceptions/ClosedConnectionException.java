package com.glavsoft.exceptions;

public class ClosedConnectionException extends TransportException {
    public ClosedConnectionException(Throwable exception) {
        super(exception);
    }
}
