package com.github.lemerch.columbus;

public class ColumbusException extends RuntimeException {
    public ColumbusException(String message) { super(message); }
    public ColumbusException(Exception exception) { super(exception); }
}
