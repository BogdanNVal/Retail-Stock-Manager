package com.example.retail.service;

/**
 * Thrown when a requested domain entity does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
