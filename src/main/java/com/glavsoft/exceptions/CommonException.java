package com.glavsoft.exceptions;

public class CommonException extends Exception {
    public CommonException(Throwable exception) {
        super(exception);
    }

    public CommonException(String message, Throwable exception) {
        super(message, exception);
    }

    public CommonException(String message) {
        super(message);
    }
}
