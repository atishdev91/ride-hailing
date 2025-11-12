package com.as.authservice.exceptions;

public class RiderNotFoundException extends RuntimeException {

    public RiderNotFoundException(String email) {
        super("Rider with email " + email + " not found");
    }
}
