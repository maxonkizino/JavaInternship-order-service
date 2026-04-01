package com.javainternshiporderservice.exception;

import org.springframework.http.HttpStatus;
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
