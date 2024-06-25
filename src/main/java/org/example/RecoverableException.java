package org.example;

public class RecoverableException extends RuntimeException {

    public RecoverableException() {
        super();
    }

    public RecoverableException(String message) {
        super(message);
    }
}
