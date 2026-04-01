package com.javainternshiporderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderItemNotFoundException extends RuntimeException {
    public OrderItemNotFoundException(String message) {
        super(message);
    }
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
