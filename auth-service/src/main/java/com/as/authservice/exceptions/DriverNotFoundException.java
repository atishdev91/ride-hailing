package com.as.authservice.exceptions;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(String email) {
        super("Driver with email " + email + " not found");
    }
}
