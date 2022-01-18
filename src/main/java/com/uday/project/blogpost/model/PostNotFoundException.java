package com.uday.project.blogpost.model;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(Throwable cause) {
        super(cause);
    }

}