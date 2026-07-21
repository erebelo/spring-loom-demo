package com.erebelo.springloomdemo.exception.model;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }
}
