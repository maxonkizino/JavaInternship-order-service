package com.javainternshiporderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
