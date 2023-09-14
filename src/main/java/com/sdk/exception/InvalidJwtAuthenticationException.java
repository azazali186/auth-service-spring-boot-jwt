package com.sdk.exception;

import org.apache.tomcat.websocket.AuthenticationException;

public class InvalidJwtAuthenticationException extends AuthenticationException {
    public InvalidJwtAuthenticationException(String explanation, Throwable ex) {
        super(explanation);
    }
}
